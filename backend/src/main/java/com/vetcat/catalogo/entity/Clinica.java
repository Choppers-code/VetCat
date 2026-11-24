package com.vetcat.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Clínica de la plataforma (tabla global {@code catalogo.clinica}). */
@Entity
@Table(schema = "catalogo", name = "clinica")
public class Clinica {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Esquema de la clínica: lo genera la base a partir del id; el cliente nunca lo envía. */
  @Column(insertable = false, updatable = false)
  private String esquema;

  private String nombre;

  @Column(name = "correo_contacto")
  private String correoContacto;

  @Column(name = "telefono_contacto")
  private String telefonoContacto;

  @Column(name = "zona_horaria")
  private String zonaHoraria;

  private String estado;

  @Column(name = "creada_en", insertable = false, updatable = false)
  private Instant creadaEn;

  protected Clinica() {}

  public Clinica(String nombre, String zonaHoraria) {
    this.nombre = nombre;
    this.zonaHoraria = zonaHoraria;
    this.estado = "ACTIVA";
  }

  public Long getId() {
    return id;
  }

  public String getEsquema() {
    return esquema;
  }

  public String getNombre() {
    return nombre;
  }

  public String getCorreoContacto() {
    return correoContacto;
  }

  public String getTelefonoContacto() {
    return telefonoContacto;
  }

  public String getZonaHoraria() {
    return zonaHoraria;
  }

  public String getEstado() {
    return estado;
  }

  public Instant getCreadaEn() {
    return creadaEn;
  }
}
