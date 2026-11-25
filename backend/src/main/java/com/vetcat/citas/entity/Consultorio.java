package com.vetcat.citas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "consultorio")
public class Consultorio {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false)
  private boolean activo;

  @Column(name = "creado_en", insertable = false, updatable = false)
  private Instant creadoEn;

  @Column(name = "actualizado_en", insertable = false, updatable = false)
  private Instant actualizadoEn;

  protected Consultorio() {}

  public Consultorio(String nombre) {
    this.nombre = nombre;
    this.activo = true;
  }

  public Long getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public boolean isActivo() {
    return activo;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }

  public Instant getActualizadoEn() {
    return actualizadoEn;
  }
}
