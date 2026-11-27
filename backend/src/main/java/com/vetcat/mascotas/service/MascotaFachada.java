package com.vetcat.mascotas.service;

import com.vetcat.mascotas.dto.MascotaResponse;
import com.vetcat.mascotas.entity.Mascota;
import com.vetcat.mascotas.repository.MascotaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

  /** Mascotas activas de varios propietarios, agrupadas por propietario. */
  public Map<Long, List<MascotaResponse>> activasDe(Collection<Long> propietarioIds) {
    if (propietarioIds.isEmpty()) {
      return Map.of();
    }
    return mascotas.findByPropietarioIdInAndActivoTrueOrderByNombreAscIdAsc(propietarioIds).stream()
        .map(MascotaFachada::aRespuesta)
        .collect(Collectors.groupingBy(MascotaResponse::propietarioId));
  }

  public long contarCoincidencias(String texto, boolean incluirInactivos) {
    return mascotas.contarCoincidencias(texto, incluirInactivos);
  }

  /** Tramo de las mascotas cuyo nombre contiene el texto, ordenadas por nombre. */
  public List<MascotaResponse> coincidencias(
      String texto, boolean incluirInactivos, long desde, int limite) {
    return mascotas.coincidencias(texto, incluirInactivos, desde, limite).stream()
        .map(MascotaFachada::aRespuesta)
        .toList();
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
