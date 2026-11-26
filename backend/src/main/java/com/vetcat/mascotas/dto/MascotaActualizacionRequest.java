package com.vetcat.mascotas.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.vetcat.mascotas.entity.Especie;
import com.vetcat.mascotas.entity.Sexo;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos editables de una mascota. No trae el propietario, que no cambia (PRO-008). */
public record MascotaActualizacionRequest(
    @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
        String nombre,
    @NotNull(message = "La especie es obligatoria") Especie especie,
    @Size(max = 100, message = "La raza admite hasta 100 caracteres") String raza,
    Sexo sexo,
    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
        LocalDate fechaNacimiento,
    @Positive(message = "El peso debe ser mayor que cero")
        @Digits(integer = 4, fraction = 2, message = "El peso admite hasta 9999.99 kg")
        BigDecimal pesoKg,
    String notas) {

  /**
   * Un intento de enviar el propietario se rechaza en vez de ignorarse en silencio. Cualquier otro
   * campo desconocido se ignora, como en el resto de la API.
   */
  @JsonAnySetter
  void rechazarPropietario(String campo, Object valor) {
    if ("propietarioId".equals(campo)) {
      throw new IllegalArgumentException("El propietario de una mascota no se puede cambiar");
    }
  }
}
