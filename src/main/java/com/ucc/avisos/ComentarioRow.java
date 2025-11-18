package com.ucc.avisos;

public interface ComentarioRow {
  Integer getId();
  String  getNombre();
  String  getEmail();
  String  getTexto();
  String  getFecha(); // DATE_FORMAT
}
