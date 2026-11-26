package com.vetcat.citas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "cita")
public class Cita {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "mascota_id", nullable = false)
  private Long mascotaId;

  @Column(name = "veterinario_id", nullable = false)
  private Long veterinarioId;

  @Column(name = "consultorio_id")
  private Long consultorioId;

  @Column(nullable = false, length = 20)
  private String tipo;

  @Column(length = 300)
  private String direccion;

  @Column(nullable = false)
  private Instant inicio;

  @Column(nullable = false)
  private Instant fin;

  @Column(columnDefinition = "text")
  private String motivo;

  @Column(nullable = false, length = 20)
  private String estado = "PENDIENTE";

  @Column(nullable = false, length = 20)
  private String origen = "PERSONAL";

  @Column(name = "cancelada_por")
  private Long canceladaPor;

  @Column(name = "motivo_cancelacion", columnDefinition = "text")
  private String motivoCancelacion;

  @Column(name = "reprogramada_de")
  private Long reprogramadaDe;

  @Column(name = "creado_en", insertable = false, updatable = false)
  private Instant creadoEn;

  @Column(name = "actualizado_en", insertable = false, updatable = false)
  private Instant actualizadoEn;

  protected Cita() {}

  public Cita(
      Long mascotaId,
      Long veterinarioId,
      Long consultorioId,
      String tipo,
      String direccion,
      Instant inicio,
      Instant fin,
      String motivo,
      String origen) {
    this.mascotaId = mascotaId;
    this.veterinarioId = veterinarioId;
    this.consultorioId = consultorioId;
    this.tipo = tipo;
    this.direccion = direccion;
    this.inicio = inicio;
    this.fin = fin;
    this.motivo = motivo;
    this.origen = origen;
  }

  public Long getId() {
    return id;
  }

  public Long getMascotaId() {
    return mascotaId;
  }

  public Long getVeterinarioId() {
    return veterinarioId;
  }

  public Long getConsultorioId() {
    return consultorioId;
  }

  public String getTipo() {
    return tipo;
  }

  public String getDireccion() {
    return direccion;
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

  public String getEstado() {
    return estado;
  }

  public String getOrigen() {
    return origen;
  }

  public Long getCanceladaPor() {
    return canceladaPor;
  }

  public String getMotivoCancelacion() {
    return motivoCancelacion;
  }

  public Long getReprogramadaDe() {
    return reprogramadaDe;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }

  public Instant getActualizadoEn() {
    return actualizadoEn;
  }
}
