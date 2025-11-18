
package com.ucc.avisos;

public interface AvisoRow {
  Integer getId();
  String getFechaPublicacion();
  String getSector();
  Integer getCantidad();
  String getTipo();
  String getEdad(); // texto "2 meses" o "3 años"
  String getComuna();
  Double getNota(); // promedio (puede ser null)
  Long getVotos();  // cantidad de notas (puede ser 0)
}
