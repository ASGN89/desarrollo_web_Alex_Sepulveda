
# Tarea 4 – Spring Boot (Java 17)

## Requisitos
- Java 17+
- Maven 3.9+
- MySQL


## Ejecutar
```
mvn spring-boot:run
```

## Técnicas usadas
- Spring Boot 3 + JPA (consultas nativas para componer el listado).
- Thymeleaf para la vista del **listado**.
- `fetch` (JS) para agregar notas (1..7) y actualizar promedio/contador sin recargar la página.

Se cumplio con todo lo solicitado, el unico detalle es que el promedio de las evaluaciones no se ve reflejado en la pagina, pero si en la base de datos, por lo que aunque se vote, en la pagina siempre se va a mostrar como si no hubiesen evaluaciones.
