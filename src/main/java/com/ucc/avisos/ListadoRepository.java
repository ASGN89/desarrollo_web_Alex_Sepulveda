package com.ucc.avisos;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ListadoRepository extends JpaRepository<Nota, Integer> {

  @Query(
    value =
      "SELECT a.id as id, " +
      "       DATE_FORMAT(a.fecha_ingreso, '%Y-%m-%d') as fechaPublicacion, " +
      "       COALESCE(a.sector, '') as sector, " +
      "       a.cantidad as cantidad, " +
      "       a.tipo as tipo, " +
      "       CONCAT(a.edad, ' ', IF(a.unidad_medida='m','meses','años')) as edad, " +
      "       c.nombre as comuna, " +
      "       AVG(n.nota) as nota, " +
      "       COUNT(n.id) as votos " +
      "FROM aviso_adopcion a " +
      "JOIN comuna c ON c.id = a.comuna_id " +
      "LEFT JOIN nota n ON n.aviso_id = a.id " +
      "GROUP BY a.id, a.fecha_ingreso, a.sector, a.cantidad, a.tipo, a.edad, a.unidad_medida, c.nombre " +
      "ORDER BY a.fecha_ingreso DESC, a.id DESC " +
      "LIMIT :limite",
    nativeQuery = true
  )
  List<AvisoRow> listado(@Param("limite") int limite);

  @Query(value = "SELECT AVG(nota) FROM nota WHERE aviso_id = :avisoId", nativeQuery = true)
  Double avgNota(@Param("avisoId") Integer avisoId);

  @Query(value = "SELECT COUNT(*) FROM nota WHERE aviso_id = :avisoId", nativeQuery = true)
  Long cntNota(@Param("avisoId") Integer avisoId);
}
