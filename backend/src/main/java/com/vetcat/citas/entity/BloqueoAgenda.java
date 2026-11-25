package com.vetcat.citas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "bloqueo_agenda")
public class BloqueoAgenda {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "veterinario_id")
  private Long veterinarioId;

  @Column(name = "consultorio_id")
  private Long consultorioId;

  @Column(nullable = false)
  private Instant inicio;

  @Column(nullable = false)
  private Instant fin;

  @Column(nullable = false, columnDefinition = "text")
  private String motivo;

  @Column(name = "creado_en", insertable = false, updatable = false)
  private Instant creadoEn;

  protected BloqueoAgenda() {}

  public BloqueoAgenda(
      Long veterinarioId, Long consultorioId, Instant inicio, Instant fin, String motivo) {
    this.veterinarioId = veterinarioId;
    this.consultorioId = consultorioId;
    this.inicio = inicio;
    this.fin = fin;
    this.motivo = motivo;
  }

  public Long getId() {
    return id;
  }

  public Long getVeterinarioId() {
    return veterinarioId;
  }

  public Long getConsultorioId() {
    return consultorioId;
  }

  public Instant getInicio() {
    return inicio;
  }

  public Instant getFin() {
    return fin;
  }

  public String getMotivo() {
    return motivo;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }
}
