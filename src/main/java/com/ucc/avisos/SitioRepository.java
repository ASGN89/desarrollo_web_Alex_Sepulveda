package com.ucc.avisos;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SitioRepository extends JpaRepository<Nota, Integer> {

  // HOME: últimos 5 (como T3)
    @Query(value =
        "SELECT a.id AS id, c.nombre AS comuna, a.sector AS sector, " +
        "       a.cantidad AS cantidad, a.tipo AS tipo, a.edad AS edad, a.unidad_medida AS unidadMedida, " +
        "       DATE_FORMAT(a.fecha_ingreso, '%Y-%m-%d %H:%i') AS fechaPublicacion, " +
        "       (SELECT f.nombre_archivo FROM foto f WHERE f.aviso_id=a.id ORDER BY f.id LIMIT 1) AS foto " +
        "FROM aviso_adopcion a " +
        "JOIN comuna c ON c.id=a.comuna_id " +
        "ORDER BY a.id DESC " +
        "LIMIT 5",
        nativeQuery = true)
    List<HomeRow> ultimos5();

    // LISTADO paginado + nota/votos + #fotos
    @Query(value =
        "SELECT a.id AS id, " +
        "       DATE_FORMAT(a.fecha_ingreso,  '%Y-%m-%d %H:%i') AS fechaPublicacion, " +
        "       DATE_FORMAT(a.fecha_entrega,  '%Y-%m-%d %H:%i') AS fechaEntrega, " +
        "       c.nombre AS comuna, a.sector AS sector, a.cantidad AS cantidad, a.tipo AS tipo, " +
        "       a.edad AS edad, a.unidad_medida AS unidadMedida, a.nombre AS nombre, " +
        "       COUNT(DISTINCT f.id) AS fotos, AVG(n.nota) AS nota, COUNT(n.id) AS votos " +
        "FROM aviso_adopcion a " +
        "JOIN comuna c ON c.id=a.comuna_id " +
        "LEFT JOIN foto  f ON f.aviso_id=a.id " +
        "LEFT JOIN nota  n ON n.aviso_id=a.id " +
        "GROUP BY a.id, a.fecha_ingreso, a.fecha_entrega, c.nombre, a.sector, a.cantidad, a.tipo, a.edad, a.unidad_medida, a.nombre " +
        "ORDER BY a.fecha_ingreso DESC, a.id DESC " +
        "LIMIT :per OFFSET :off",
        nativeQuery = true)
    List<ListadoRow> listado(@Param("per") int per, @Param("off") int off);

    @Query(value = "SELECT COUNT(*) FROM aviso_adopcion", nativeQuery = true)
    long totalAvisos();

    // DETALLE
    @Query(value =
        "SELECT a.id AS id, a.cantidad AS cantidad, a.tipo AS tipo, a.edad AS edad, a.unidad_medida AS unidadMedida, " +
        "       r.nombre AS region, c.nombre AS comuna, a.sector AS sector, " +
        "       a.nombre AS nombre, a.email AS email, a.celular AS celular, a.descripcion AS descripcion, " +
        "       DATE_FORMAT(a.fecha_ingreso, '%Y-%m-%d %H:%i') AS fechaPublicacion, " +
        "       DATE_FORMAT(a.fecha_entrega, '%Y-%m-%d %H:%i') AS fechaEntrega " +
        "FROM aviso_adopcion a " +
        "JOIN comuna c ON c.id=a.comuna_id " +
        "JOIN region r ON r.id=c.region_id " +
        "WHERE a.id=:id",
        nativeQuery = true)
    DetalleAviso detalle(@Param("id") Integer id);

    @Query(value = "SELECT id AS id, ruta_archivo AS rutaArchivo, nombre_archivo AS nombreArchivo FROM foto WHERE aviso_id=:id ORDER BY id",
            nativeQuery = true)
    List<DetalleFoto> fotos(@Param("id") Integer id);

    // COMENTARIOS (para el detalle, igual a tu T3)
    @Query(value =
        "SELECT id AS id, nombre AS nombre, email AS email, texto AS texto, " +
        "       DATE_FORMAT(fecha, '%Y-%m-%d %H:%i:%s') AS fecha " +
        "FROM comentario WHERE aviso_id=:avisoId ORDER BY id DESC",
        nativeQuery = true)
        List<ComentarioRow> comentarios(@Param("avisoId") Integer avisoId);
    }
