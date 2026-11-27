package com.vetcat.mascotas.service;

import com.vetcat.mascotas.dto.MascotaResponse;
import com.vetcat.mascotas.entity.Mascota;
import com.vetcat.mascotas.repository.MascotaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Puerta del módulo de mascotas para los demás módulos: nadie usa su repositorio. */
@Service
@Transactional(value = "tenantTransactionManager", readOnly = true)
public class MascotaFachada {

  private final MascotaRepository mascotas;

  public MascotaFachada(MascotaRepository mascotas) {
    this.mascotas = mascotas;
  }

  /** Mascotas activas de un propietario de la clínica activa. */
  public List<MascotaResponse> activasDe(long propietarioId) {
    return mascotas.findByPropietarioIdAndActivoTrueOrderByNombreAscIdAsc(propietarioId).stream()
        .map(MascotaFachada::aRespuesta)
        .toList();
  }

  static MascotaResponse aRespuesta(Mascota mascota) {
    return new MascotaResponse(
        mascota.getId(),
        mascota.getPropietarioId(),
        mascota.getNombre(),
        mascota.getEspecie(),
        mascota.getRaza(),
        mascota.getSexo(),
        mascota.getFechaNacimiento(),
        mascota.getPesoKg(),
        mascota.getNotas(),
        mascota.isActivo());
  }
}
