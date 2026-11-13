package com.vetcat.citas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "bloqueo_agenda")
public class BloqueoAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultorio_id")
    private Consultorio consultorio;

    @Column(name = "veterinario_id")
    private UUID veterinarioId;

    @Column(name = "inicio", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime inicio;

    @Column(name = "fin", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime fin;

    @Column(name = "motivo", nullable = false, length = 300)
    private String motivo;

    @Column(name = "creado_por", nullable = false, length = 255)
    private String creadoPor;

    protected BloqueoAgenda() {
    }

    public BloqueoAgenda(
            Consultorio consultorio,
            UUID veterinarioId,
            OffsetDateTime inicio,
            OffsetDateTime fin,
            String motivo,
            String creadoPor) {
        this.consultorio = consultorio;
        this.veterinarioId = veterinarioId;
        this.inicio = inicio;
        this.fin = fin;
        this.motivo = motivo;
        this.creadoPor = creadoPor;
    }

    public UUID getId() { return id; }
    public Consultorio getConsultorio() { return consultorio; }
    public UUID getVeterinarioId() { return veterinarioId; }
    public OffsetDateTime getInicio() { return inicio; }
    public OffsetDateTime getFin() { return fin; }
    public String getMotivo() { return motivo; }
    public String getCreadoPor() { return creadoPor; }
    public void setConsultorio(Consultorio consultorio) { this.consultorio = consultorio; }
    public void setVeterinarioId(UUID veterinarioId) { this.veterinarioId = veterinarioId; }
    public void setInicio(OffsetDateTime inicio) { this.inicio = inicio; }
    public void setFin(OffsetDateTime fin) { this.fin = fin; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
}
