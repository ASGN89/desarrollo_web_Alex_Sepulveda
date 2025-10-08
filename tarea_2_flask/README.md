# Tarea 2 – Adopciones (Flask + MySQL)

## Requisitos
- Python 3.10+ 
- MySQL 8+
- pip

## Cómo correr
1. Crear y activar venv:
   - Windows:
     py -m venv .venv
     .\.venv\Scripts\Activate.ps1
2. Instalar dependencias:
     pip install -r requirements.txt
3. MySQL:
     - Crear esquema y tablas:
         DROP SCHEMA IF EXISTS tarea2;
         CREATE SCHEMA tarea2 DEFAULT CHARACTER SET utf8mb4;
         USE tarea2;
         SOURCE tarea2.sql;
         SOURCE region-comuna.sql;
4. Configurar variables:
     - Copiar .env.example a .env y completar credenciales locales.
5. Ejecutar:
     flask --app app run
     # http://127.0.0.1:5000

## Rutas
- `/`              → Portada (últimos 5 avisos)
- `/agregar`       → Formulario (validación servidor + subida de fotos)
- `/avisos`        → Listado paginado
- `/avisos/<id>`   → Detalle del aviso
- `/estadisticas`  → Estadísticas simples
- `/api/comunas?region_id=<id>` → JSON para filtrar comunas por región

## Notas
- Validaciones servidor:
  * enteros estrictos (sin comas),
  * fecha de entrega futura,
  * al menos 1 foto,
  * email válido,
  * región y comuna consistentes (comuna pertenece a región).
- Conexión: credenciales tomadas desde `.env`.
- Carpeta `uploads/` debe existir; puede llegar vacía.
