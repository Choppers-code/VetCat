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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "horario_consultorio")
public class HorarioConsultorio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultorio_id", nullable = false)
    private Consultorio consultorio;

    @Column(name = "dia_semana")
    private Integer diaSemana;

    @Column(name = "fecha_excepcion")
    private LocalDate fechaExcepcion;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "disponible", nullable = false)
    private boolean disponible;

    protected HorarioConsultorio() {
    }

    public HorarioConsultorio(
            Consultorio consultorio,
            Integer diaSemana,
            LocalDate fechaExcepcion,
            LocalTime horaInicio,
            LocalTime horaFin,
            boolean disponible) {
        this.consultorio = consultorio;
        this.diaSemana = diaSemana;
        this.fechaExcepcion = fechaExcepcion;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.disponible = disponible;
    }

    public UUID getId() { return id; }
    public Consultorio getConsultorio() { return consultorio; }
    public Integer getDiaSemana() { return diaSemana; }
    public LocalDate getFechaExcepcion() { return fechaExcepcion; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public boolean isDisponible() { return disponible; }
    public void setConsultorio(Consultorio consultorio) { this.consultorio = consultorio; }
    public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }
    public void setFechaExcepcion(LocalDate fechaExcepcion) { this.fechaExcepcion = fechaExcepcion; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}
