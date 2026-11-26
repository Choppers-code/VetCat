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
import java.time.LocalDate;

/**
 * Mascota del esquema de una clínica. Sin nombre de esquema: lo fija el {@code search_path}.
 *
 * <p>Por ahora solo se lee, para la ficha del propietario; el registro y la edición llegan con
 * #169.
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

  protected Mascota() {}

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
