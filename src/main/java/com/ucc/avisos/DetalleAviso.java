package com.ucc.avisos;

public interface DetalleAviso {
  Integer getId();
  Integer getCantidad();
  String  getTipo();
  Integer getEdad();
  String  getUnidadMedida();
  String  getRegion();
  String  getComuna();
  String  getSector();
  String  getNombre();
  String  getEmail();
  String  getCelular();
  String  getDescripcion();
  String  getFechaPublicacion(); // DATE_FORMAT
  String  getFechaEntrega();     // DATE_FORMAT
}
