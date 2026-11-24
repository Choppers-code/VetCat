package com.vetcat.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Pertenencia de un usuario a una clínica, una fila por rol (ADR-0006).
 *
 * <p>Es la única fuente de verdad de los roles efectivos del token de tenant.
 */
@Entity
@Table(schema = "catalogo", name = "usuario_clinica")
public class UsuarioClinica {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "keycloak_id")
  private UUID keycloakId;

  @Column(name = "clinica_id")
  private Long clinicaId;

  private String rol;

  private String estado;

  @Column(name = "creado_en", insertable = false, updatable = false)
  private Instant creadoEn;

  protected UsuarioClinica() {}

  public UsuarioClinica(UUID keycloakId, Long clinicaId, String rol) {
    this.keycloakId = keycloakId;
    this.clinicaId = clinicaId;
    this.rol = rol;
    this.estado = "ACTIVO";
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public Long getId() {
    return id;
  }

  public UUID getKeycloakId() {
    return keycloakId;
  }

  public Long getClinicaId() {
    return clinicaId;
  }

  public String getRol() {
    return rol;
  }

  public String getEstado() {
    return estado;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }
}
