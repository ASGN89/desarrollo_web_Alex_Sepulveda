package com.ucc.avisos;

import jakarta.persistence.*;

@Entity
@Table(name = "nota")
public class Nota {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="aviso_id", nullable=false)
    private Integer avisoId;

    @Column(nullable=false)
    private Integer nota;

    public Nota() {}
    public Nota(Integer avisoId, Integer nota){ this.avisoId = avisoId; this.nota = nota; }

    public Integer getId() { return id; }
    public Integer getAvisoId() { return avisoId; }
    public Integer getNota() { return nota; }
    public void setAvisoId(Integer avisoId) { this.avisoId = avisoId; }
    public void setNota(Integer nota) { this.nota = nota; }
}
