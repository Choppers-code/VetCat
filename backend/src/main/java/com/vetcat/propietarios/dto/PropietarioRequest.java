package com.vetcat.propietarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrar o editar un propietario.
 *
 * @param confirmarDuplicado solo cuenta al registrar: crea la ficha aunque el teléfono ya exista
 */
public record PropietarioRequest(
    @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre admite hasta 150 caracteres")
        String nombre,
    @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 30, message = "El teléfono admite hasta 30 caracteres")
        @Pattern(regexp = ".*[0-9].*", message = "El teléfono debe tener al menos un número")
        String telefono,
    @Email(message = "El correo no es válido")
        @Size(max = 254, message = "El correo admite hasta 254 caracteres")
        String correo,
    String direccion,
    // Envoltorio y no primitivo: el contrato lo hace opcional.
    Boolean confirmarDuplicado) {}
