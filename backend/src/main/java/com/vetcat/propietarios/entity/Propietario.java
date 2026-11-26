package com.vetcat.propietarios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Propietario del esquema de una clínica. Sin nombre de esquema: lo fija el {@code search_path}.
 *
 * <p>El teléfono normalizado lo genera la base de datos y solo se usa en consultas.
 */
@Entity
@Table(name = "propietario")
public class Propietario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

  private String telefono;

  private String correo;

  private boolean activo;

  @Column(name = "actualizado_en")
  private Instant actualizadoEn;

  protected Propietario() {}

  public Propietario(String nombre, String telefono, String correo) {
    this.activo = true;
    editar(nombre, telefono, correo);
  }

  /** Reemplaza los datos de contacto. */
  public void editar(String nombre, String telefono, String correo) {
    this.nombre = nombre;
    this.telefono = telefono;
    this.correo = correo;
    this.actualizadoEn = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public String getTelefono() {
    return telefono;
  }

  public String getCorreo() {
    return correo;
  }

  public boolean isActivo() {
    return activo;
  }
}
