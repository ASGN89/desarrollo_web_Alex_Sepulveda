
- **Comentarios en avisos (nuevo)**
  - **Qué:** Se creó la tabla `comentario` (FK a `aviso_adopcion`), el endpoint `GET/POST /api/comentarios` y la UI en `detalle.html` con envío/lectura vía `fetch`.
  - **Por qué:** Para cumplir el requerimiento de interacción asincrónica (AJAX) y permitir feedback de usuarios sin recargar la página.

- **Validaciones reforzadas**
  - **Qué:** Reglas del lado servidor para `comentarios` (nombre 3–80, texto 5–500, email básico, aviso válido) y ajustes en el form de avisos (email, enteros estrictos, fecha futura, coherencia región–comuna, al menos 1 foto).
  - **Por qué:** Aumentar robustez y evitar datos inválidos al integrarse con endpoints públicos.

- **Endpoints JSON para estadísticas (nuevo)**
  - **Qué:** `/api/estad/avisos-por-dia`, `/api/estad/por-tipo`, `/api/estad/por-mes` y vista `estadisticas.html` con Highcharts que consume esos endpoints.
  - **Por qué:** Separar datos (API) de presentación (frontend) y cumplir el requisito de visualización dinámica.

- **Plantillas actualizadas**
  - **Qué:** `detalle.html` ahora renderiza la lista de comentarios y el formulario con JS; se añadieron hooks y clases mínimas para pintar resultados sin recarga.
  - **Por qué:** Integrar la nueva API de comentarios manteniendo una UX fluida.

- **Configuración y estructura**
  - **Qué:** Se documentó `.env`/variables de entorno, se aseguró la carpeta `uploads/`, y se añadieron scripts SQL (`tabla-comentario.sql`).
  - **Por qué:** Facilitar despliegue local y que cualquier corrector pueda reproducir la app sin pasos manuales ambiguos.

En resumen, Tarea 3 agrega **capas de API + cliente (fetch)**, **persistencia de comentarios** y **gráficas dinámicas**.
