import os
from datetime import datetime, timedelta
from flask import Flask, render_template, request, redirect, url_for, send_from_directory, flash, jsonify
from werkzeug.utils import secure_filename
from sqlalchemy import func, extract
# Modelos
from models import db, Region, Comuna, AvisoAdopcion, Foto, ContactarPor, Comentario
# Config DB (usa tu config.py existente)
try:
    from config import get_database_uri
    DATABASE_URI = get_database_uri()
except Exception:
    # fallback por si solo existe la clase Config con el atributo
    from config import Config
    DATABASE_URI = getattr(Config, "SQLALCHEMY_DATABASE_URI")

# ----------------------------------
# App & configuración
# ----------------------------------
ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"}
def allowed_file(filename: str) -> bool:
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_EXTENSIONS

app = Flask(__name__)
app.config["SQLALCHEMY_DATABASE_URI"] = DATABASE_URI
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
app.config["SECRET_KEY"] = os.getenv("SECRET_KEY", "dev")
app.config["UPLOAD_FOLDER"] = os.getenv("UPLOAD_FOLDER", "uploads")
os.makedirs(app.config["UPLOAD_FOLDER"], exist_ok=True)

db.init_app(app)

# ----------------------------------
# Rutas
# ----------------------------------

@app.route("/")
def home():
    """Portada: últimos 5 avisos."""
    avisos = AvisoAdopcion.query.order_by(AvisoAdopcion.id.desc()).limit(5).all()
    return render_template("home.html", avisos=avisos)


@app.get("/uploads/<path:fn>")
def uploaded_file(fn: str):
    """Servir archivos subidos."""
    return send_from_directory(app.config["UPLOAD_FOLDER"], fn)


@app.get("/api/comunas")
def api_comunas():
    """Lista comunas por región (para el combo dependiente)."""
    region_id = request.args.get("region_id", type=int)
    if not region_id:
        return jsonify([])
    comunas = Comuna.query.filter_by(region_id=region_id).order_by(Comuna.nombre.asc()).all()
    return jsonify([{"id": c.id, "nombre": c.nombre} for c in comunas])


@app.route("/agregar", methods=["GET", "POST"])
def agregar():
    """Formulario para agregar aviso de adopción."""
    if request.method == "GET":
        regiones = Region.query.order_by(Region.nombre.asc()).all()
        comunas = []
        if regiones:
            comunas = Comuna.query.filter_by(region_id=regiones[0].id).order_by(Comuna.nombre.asc()).all()
        return render_template("agregar.html", regiones=regiones, comunas=comunas, data={}, errors={})

    # ------ POST ------
    form = request.form
    files = request.files.getlist("fotos")
    errors = {}
    data = {k: form.get(k) for k in form}

    # Helpers
    def must(field, label):
        if not (form.get(field) and str(form.get(field)).strip()):
            errors[field] = f"{label} es obligatorio."

    # Requeridos
    must("region_id", "Región")
    must("comuna_id", "Comuna")
    must("nombre", "Nombre")
    must("email", "Email")
    must("tipo", "Tipo")
    must("cantidad", "Cantidad")
    must("edad", "Edad")
    must("unidad_medida", "Unidad de medida")
    must("fecha_entrega", "Fecha de entrega")

    # Longitudes básicas
    if form.get("sector") and len(form.get("sector")) > 100:
        errors["sector"] = "El sector no puede superar 100 caracteres."
    if form.get("nombre") and not (3 <= len(form.get("nombre")) <= 200):
        errors["nombre"] = "El nombre debe tener entre 3 y 200 caracteres."
    if form.get("email") and len(form.get("email")) > 100:
        errors["email"] = "Email demasiado largo (máx 100)."

    # Email muy básico
    if form.get("email") and ("@" not in form.get("email") or "." not in form.get("email")):
        errors["email"] = "Email inválido."

    # Enteros estrictos (sin decimales, ni negativos)
    for fld in ("cantidad", "edad"):
        v = (form.get(fld) or "").strip()
        if not v.isdigit():
            errors[fld] = "Debe ser número entero (sin comas ni puntos)."
        else:
            if int(v) < 1:
                errors[fld] = "Debe ser un entero ≥ 1."

    # Tipo y unidad
    if form.get("tipo") not in ("perro", "gato"):
        errors["tipo"] = "Tipo inválido (perro/gato)."
    if form.get("unidad_medida") not in ("a", "m"):
        errors["unidad_medida"] = "Unidad inválida (a=años, m=meses)."

    # Parse región y comuna, y comprobar coherencia región–comuna
    region_id = int(form.get("region_id")) if form.get("region_id") and str(form.get("region_id")).isdigit() else None
    comuna_id = int(form.get("comuna_id")) if form.get("comuna_id") and str(form.get("comuna_id")).isdigit() else None

    comuna = Comuna.query.get(comuna_id) if comuna_id else None
    if comuna is None:
        errors["comuna_id"] = "Comuna inválida."
    elif region_id and comuna.region_id != region_id:
        errors["comuna_id"] = "La comuna no pertenece a la región seleccionada."

    # Fecha futura
    fecha_entrega = None
    if form.get("fecha_entrega"):
        try:
            # input datetime-local → YYYY-MM-DDTHH:MM
            fecha_entrega = datetime.fromisoformat(form.get("fecha_entrega"))
            if fecha_entrega < datetime.now() + timedelta(minutes=1):
                errors["fecha_entrega"] = "La fecha de entrega no puede ser pasada."
        except Exception:
            errors["fecha_entrega"] = "Fecha de entrega inválida."

    # Fotos: al menos 1
    selected_files = [f for f in files if f and f.filename]
    if not selected_files:
        errors["fotos"] = "Debes subir al menos 1 foto."
    else:
        # máximo opcional (ej: 5)
        if len(selected_files) > 5:
            errors["fotos"] = "Máximo 5 fotos."
        for f in selected_files:
            if not allowed_file(f.filename):
                errors["fotos"] = "Formato de imagen no permitido."
                break

    # Si hay errores, re-render con mensajes
    if errors:
        regiones = Region.query.order_by(Region.nombre.asc()).all()
        comunas = Comuna.query.filter_by(region_id=region_id).order_by(Comuna.nombre.asc()).all() if region_id else []
        return render_template("agregar.html", regiones=regiones, comunas=comunas, data=data, errors=errors), 400

    # Crear aviso
    aviso = AvisoAdopcion(
        fecha_ingreso=datetime.now(),
        comuna_id=comuna.id,
        sector=(form.get("sector") or "").strip() or None,
        nombre=form.get("nombre").strip(),
        email=form.get("email").strip(),
        celular=(form.get("celular") or "").strip() or None,
        tipo=form.get("tipo"),
        cantidad=int(form.get("cantidad")),
        edad=int(form.get("edad")),
        unidad_medida=form.get("unidad_medida"),
        fecha_entrega=fecha_entrega,
        descripcion=(form.get("descripcion") or "").strip() or None,
    )
    db.session.add(aviso)
    db.session.commit()

    # Guardar fotos
    for f in selected_files:
        fname = f"{aviso.id}_{secure_filename(f.filename)}"
        dest = os.path.join(app.config["UPLOAD_FOLDER"], fname)
        f.save(dest)
        db.session.add(Foto(ruta_archivo=fname, nombre_archivo=fname, aviso_id=aviso.id))

    db.session.commit()
    flash("Aviso creado correctamente", "success")
    return redirect(url_for("avisos"))


@app.route("/avisos", endpoint="avisos")
def listado():
    """Listado paginado (5 por página)."""
    page = request.args.get("page", 1, type=int)
    per_page = 5
    pagination = AvisoAdopcion.query.order_by(AvisoAdopcion.id.desc()).paginate(page=page, per_page=per_page, error_out=False)
    return render_template("listado.html", avisos=pagination.items, pagination=pagination)


@app.get("/detalle/<int:aviso_id>")
def detalle(aviso_id):
    aviso = AvisoAdopcion.query.get_or_404(aviso_id)
    comentarios = (Comentario.query
                   .filter_by(aviso_id=aviso_id)
                   .order_by(Comentario.fecha.desc())
                   .all())
    return render_template("detalle.html", aviso=aviso, comentarios=comentarios)


@app.get("/estadisticas")
def estadisticas():
    return render_template("estadisticas.html")
@app.get("/api/comentarios")
def api_comentarios():
    aviso_id = request.args.get("aviso_id", type=int)
    q = Comentario.query.filter_by(aviso_id=aviso_id).order_by(Comentario.fecha.desc())
    return jsonify([{
        "id": c.id, "nombre": c.nombre, "email": c.email or "",
        "texto": c.texto, "fecha": c.fecha.isoformat(timespec="minutes")
    } for c in q.all()])

@app.post("/api/comentarios")
def crear_comentario():
    data = request.get_json(force=True)
    nombre = (data.get("nombre") or "").strip()
    email = (data.get("email") or "").strip()
    texto  = (data.get("texto")  or "").strip()
    aviso_id = int(data.get("aviso_id", 0))

    # Validación servidor
    errs = {}
    if len(nombre) < 3 or len(nombre) > 80: errs["nombre"] = "Entre 3 y 80 caracteres"
    if len(texto)  < 5 or len(texto)  > 500: errs["texto"]  = "Entre 5 y 500 caracteres"
    if not email or len(email) > 100 or "@" not in email or "." not in email: errs["email"] = "Email inválido"
    if not aviso_id or not AvisoAdopcion.query.get(aviso_id): errs["aviso_id"] = "Aviso inválido"
    if errs: return jsonify({"ok": False, "errors": errs}), 400

    c = Comentario(aviso_id=aviso_id, nombre=nombre, email=email, texto=texto)
    db.session.add(c); db.session.commit()
    return jsonify({"ok": True, "comentario": {
        "id": c.id, "nombre": c.nombre, "email": c.email or "",
        "texto": c.texto, "fecha": c.fecha.isoformat(timespec="minutes")
    }}), 201
@app.get("/api/estad/avisos-por-dia")
def estad_avisos_por_dia():
    rows = (db.session.query(func.date(AvisoAdopcion.fecha_ingreso), func.count())
            .group_by(func.date(AvisoAdopcion.fecha_ingreso))
            .order_by(func.date(AvisoAdopcion.fecha_ingreso)).all())
    return jsonify([{"dia": d.isoformat(), "total": n} for d, n in rows])

@app.get("/api/estad/por-tipo")
def estad_por_tipo():
    rows = (db.session.query(AvisoAdopcion.tipo, func.count())
            .group_by(AvisoAdopcion.tipo).all())
    return jsonify([{"tipo": t, "total": n} for t, n in rows])

@app.get("/api/estad/por-mes")
def estad_por_mes():
    rows = (db.session.query(extract('year', AvisoAdopcion.fecha_ingreso).label('y'),
                             extract('month', AvisoAdopcion.fecha_ingreso).label('m'),
                             AvisoAdopcion.tipo, func.count().label('n'))
            .group_by('y', 'm', AvisoAdopcion.tipo)
            .order_by('y', 'm').all())
    # compactar: { "YYYY-MM": {"gato": n, "perro": n} }
    acc = {}
    for y, m, t, n in rows:
        key = f"{int(y):04d}-{int(m):02d}"
        acc.setdefault(key, {"gato": 0, "perro": 0})
        acc[key][t] = n
    data = [{"mes": k, "gato": v["gato"], "perro": v["perro"]} for k, v in acc.items()]
    return jsonify(data)

# Permite ejecutar con `python app.py` si quieres
if __name__ == "__main__":
    with app.app_context():
        # db.create_all()  # si lo necesitas en local
        pass
    app.run(debug=False)
