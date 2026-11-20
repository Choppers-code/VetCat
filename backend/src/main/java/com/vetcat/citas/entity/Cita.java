package com.vetcat.citas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cita")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "mascota_id", nullable = false)
    private UUID mascotaId;

    @Column(name = "veterinario_id", nullable = false)
    private UUID veterinarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoCita tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultorio_id")
    private Consultorio consultorio;

    @Column(name = "direccion", length = 500)
    private String direccion;

    @Column(name = "inicio", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime inicio;

    @Column(name = "fin", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime fin;

    @Column(name = "motivo", columnDefinition = "text")
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCita estado;

    @Column(name = "cancelada_por", length = 255)
    private String canceladaPor;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reprogramada_de")
    private Cita reprogramadaDe;

    protected Cita() {
    }

    public Cita(
            UUID mascotaId,
            UUID veterinarioId,
            TipoCita tipo,
            Consultorio consultorio,
            String direccion,
            OffsetDateTime inicio,
            OffsetDateTime fin,
            String motivo,
            EstadoCita estado,
            String canceladaPor,
            String motivoCancelacion,
            Cita reprogramadaDe) {
        this.mascotaId = mascotaId;
        this.veterinarioId = veterinarioId;
        this.tipo = tipo;
        this.consultorio = consultorio;
        this.direccion = direccion;
        this.inicio = inicio;
        this.fin = fin;
        this.motivo = motivo;
        this.estado = estado;
        this.canceladaPor = canceladaPor;
        this.motivoCancelacion = motivoCancelacion;
        this.reprogramadaDe = reprogramadaDe;
    }

    public UUID getId() { return id; }
    public UUID getMascotaId() { return mascotaId; }
    public UUID getVeterinarioId() { return veterinarioId; }
    public TipoCita getTipo() { return tipo; }
    public Consultorio getConsultorio() { return consultorio; }
    public String getDireccion() { return direccion; }
    public OffsetDateTime getInicio() { return inicio; }
    public OffsetDateTime getFin() { return fin; }
    public String getMotivo() { return motivo; }
    public EstadoCita getEstado() { return estado; }
    public String getCanceladaPor() { return canceladaPor; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public Cita getReprogramadaDe() { return reprogramadaDe; }
    public void setMascotaId(UUID mascotaId) { this.mascotaId = mascotaId; }
    public void setVeterinarioId(UUID veterinarioId) { this.veterinarioId = veterinarioId; }
    public void setTipo(TipoCita tipo) { this.tipo = tipo; }
    public void setConsultorio(Consultorio consultorio) { this.consultorio = consultorio; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setInicio(OffsetDateTime inicio) { this.inicio = inicio; }
    public void setFin(OffsetDateTime fin) { this.fin = fin; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
    public void setCanceladaPor(String canceladaPor) { this.canceladaPor = canceladaPor; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public void setReprogramadaDe(Cita reprogramadaDe) { this.reprogramadaDe = reprogramadaDe; }
}
