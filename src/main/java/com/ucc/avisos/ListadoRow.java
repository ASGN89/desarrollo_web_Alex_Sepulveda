package com.ucc.avisos;

public interface ListadoRow {
  Integer getId();
  String  getFechaPublicacion(); // DATE_FORMAT
  String  getFechaEntrega();     // DATE_FORMAT
  String  getComuna();
  String  getSector();
  Integer getCantidad();
  String  getTipo();
  Integer getEdad();
  String  getUnidadMedida();
  String  getNombre();
  Long    getFotos();            // COUNT(f.id)
  Double  getNota();             // AVG(n.nota)
  Long    getVotos();            // COUNT(n.id)
}
