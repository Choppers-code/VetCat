package com.vetcat.citas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "horario_consultorio")
public class HorarioConsultorio {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "consultorio_id", nullable = false)
  private Long consultorioId;

  @Column(name = "dia_semana")
  private Short diaSemana;

  @Column(name = "fecha_excepcion")
  private LocalDate fechaExcepcion;

  private LocalTime inicio;

  private LocalTime fin;

  @Column(name = "es_cierre", nullable = false)
  private boolean esCierre;

  @Column(name = "creado_en", insertable = false, updatable = false)
  private Instant creadoEn;

  protected HorarioConsultorio() {}

  public HorarioConsultorio(
      Long consultorioId,
      Short diaSemana,
      LocalDate fechaExcepcion,
      LocalTime inicio,
      LocalTime fin,
      boolean esCierre) {
    this.consultorioId = consultorioId;
    this.diaSemana = diaSemana;
    this.fechaExcepcion = fechaExcepcion;
    this.inicio = inicio;
    this.fin = fin;
    this.esCierre = esCierre;
  }

  public Long getId() {
    return id;
  }

  public Long getConsultorioId() {
    return consultorioId;
  }

  public Short getDiaSemana() {
    return diaSemana;
  }

  public LocalDate getFechaExcepcion() {
    return fechaExcepcion;
  }

  public LocalTime getInicio() {
    return inicio;
  }

  public LocalTime getFin() {
    return fin;
  }

  public boolean isEsCierre() {
    return esCierre;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }
}
