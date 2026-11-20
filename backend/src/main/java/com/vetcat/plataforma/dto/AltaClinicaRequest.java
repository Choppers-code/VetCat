package com.vetcat.plataforma.dto;

/** Datos para dar de alta una clínica y a su administrador (CLI-001 y CLI-002). */
public record AltaClinicaRequest(
    String nombre,
    String correoContacto,
    String telefonoContacto,
    String zonaHoraria,
    String nombreAdministrador,
    String correoAdministrador) {}
