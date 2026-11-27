package com.vetcat.mascotas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Mascota del esquema de una clínica. Sin nombre de esquema: lo fija el {@code search_path}.
 *
 * <p>El propietario se fija al crearla; {@link #editar} no lo toca.
 */
@Entity
@Table(name = "mascota")
public class Mascota {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** No se puede cambiar (PRO-008); la base de datos también lo impide. */
  @Column(name = "propietario_id", updatable = false)
  private Long propietarioId;

  private String nombre;

  @Enumerated(EnumType.STRING)
  private Especie especie;

  private String raza;

  @Enumerated(EnumType.STRING)
  private Sexo sexo;

  @Column(name = "fecha_nacimiento")
  private LocalDate fechaNacimiento;

  @Column(name = "peso_kg")
  private BigDecimal pesoKg;

  private String notas;

  private boolean activo;

  @Column(name = "actualizado_en")
  private Instant actualizadoEn;

  protected Mascota() {}

  /** Nueva mascota activa; los datos se completan con {@link #editar}. */
  public Mascota(Long propietarioId) {
    this.propietarioId = propietarioId;
    this.activo = true;
  }

  /** Reemplaza los datos generales. */
  public void editar(
      String nombre,
      Especie especie,
      String raza,
      Sexo sexo,
      LocalDate fechaNacimiento,
      BigDecimal pesoKg,
      String notas) {
    this.nombre = nombre;
    this.especie = especie;
    this.raza = raza;
    this.sexo = sexo;
    this.fechaNacimiento = fechaNacimiento;
    this.pesoKg = pesoKg;
    this.notas = notas;
    this.actualizadoEn = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public Long getPropietarioId() {
    return propietarioId;
  }

  public String getNombre() {
    return nombre;
  }

  public Especie getEspecie() {
    return especie;
  }

  public String getRaza() {
    return raza;
  }

  public Sexo getSexo() {
    return sexo;
  }

  public LocalDate getFechaNacimiento() {
    return fechaNacimiento;
  }

  public BigDecimal getPesoKg() {
    return pesoKg;
  }

  public String getNotas() {
    return notas;
  }

  public boolean isActivo() {
    return activo;
  }
}
