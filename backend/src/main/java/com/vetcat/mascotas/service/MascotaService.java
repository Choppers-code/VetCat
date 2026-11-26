package com.vetcat.mascotas.service;

import com.vetcat.mascotas.dto.MascotaActualizacionRequest;
import com.vetcat.mascotas.dto.MascotaRequest;
import com.vetcat.mascotas.dto.MascotaResponse;
import com.vetcat.mascotas.entity.Mascota;
import com.vetcat.mascotas.repository.MascotaRepository;
import com.vetcat.propietarios.service.PropietarioFachada;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro, edición y ficha de las mascotas de la clínica activa (PRO-005, PRO-006, PRO-008). */
@Service
@Transactional("tenantTransactionManager")
public class MascotaService {

  private final MascotaRepository mascotas;
  private final PropietarioFachada propietarios;

  public MascotaService(MascotaRepository mascotas, PropietarioFachada propietarios) {
    this.mascotas = mascotas;
    this.propietarios = propietarios;
  }

  public MascotaResponse registrar(MascotaRequest datos) {
    propietarios.retenerActivo(datos.propietarioId());
    Mascota nueva = new Mascota(datos.propietarioId());
    nueva.editar(
        datos.nombre().strip(),
        datos.especie(),
        opcional(datos.raza()),
        datos.sexo(),
        datos.fechaNacimiento(),
        datos.pesoKg(),
        opcional(datos.notas()));
    return MascotaFachada.aRespuesta(mascotas.saveAndFlush(nueva));
  }

  @Transactional(value = "tenantTransactionManager", readOnly = true)
  public MascotaResponse consultar(long id) {
    return MascotaFachada.aRespuesta(buscar(id));
  }

  public MascotaResponse editar(long id, MascotaActualizacionRequest datos) {
    Mascota mascota = buscar(id);
    mascota.editar(
        datos.nombre().strip(),
        datos.especie(),
        opcional(datos.raza()),
        datos.sexo(),
        datos.fechaNacimiento(),
        datos.pesoKg(),
        opcional(datos.notas()));
    return MascotaFachada.aRespuesta(mascotas.saveAndFlush(mascota));
  }

  private Mascota buscar(long id) {
    return mascotas.findById(id).orElseThrow(MascotaNoEncontradaException::new);
  }

  /** Un opcional en blanco se guarda como ausente. */
  private static String opcional(String valor) {
    return valor == null || valor.isBlank() ? null : valor.strip();
  }
}
