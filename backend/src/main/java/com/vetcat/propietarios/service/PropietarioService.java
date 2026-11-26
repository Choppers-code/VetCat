package com.vetcat.propietarios.service;

import com.vetcat.mascotas.service.MascotaFachada;
import com.vetcat.propietarios.dto.PropietarioRequest;
import com.vetcat.propietarios.dto.PropietarioResponse;
import com.vetcat.propietarios.entity.Propietario;
import com.vetcat.propietarios.repository.PropietarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro, edición y ficha de los propietarios de la clínica activa (PRO-001 y PRO-002). */
@Service
@Transactional("tenantTransactionManager")
public class PropietarioService {

  private final PropietarioRepository propietarios;
  private final MascotaFachada mascotas;

  public PropietarioService(PropietarioRepository propietarios, MascotaFachada mascotas) {
    this.propietarios = propietarios;
    this.mascotas = mascotas;
  }

  /**
   * Registra un propietario. El candado se toma también al confirmar, para que una alta sin
   * confirmar no pueda comprobar el teléfono mientras otra todavía no ha terminado de guardarlo.
   */
  public PropietarioResponse registrar(PropietarioRequest datos) {
    rechazarDireccion(datos);
    String telefono = datos.telefono().strip();
    propietarios.bloquearTelefono(telefono);
    if (!Boolean.TRUE.equals(datos.confirmarDuplicado())
        && propietarios.existeConTelefono(telefono)) {
      throw new TelefonoDuplicadoException();
    }
    Propietario nuevo = new Propietario(datos.nombre().strip(), telefono, opcional(datos.correo()));
    return aFicha(propietarios.saveAndFlush(nuevo));
  }

  @Transactional(value = "tenantTransactionManager", readOnly = true)
  public PropietarioResponse consultar(long id) {
    return aFicha(buscar(id));
  }

  public PropietarioResponse editar(long id, PropietarioRequest datos) {
    Propietario propietario = buscar(id);
    rechazarDireccion(datos);
    propietario.editar(datos.nombre().strip(), datos.telefono().strip(), opcional(datos.correo()));
    return aFicha(propietarios.saveAndFlush(propietario));
  }

  private Propietario buscar(long id) {
    return propietarios.findById(id).orElseThrow(PropietarioNoEncontradoException::new);
  }

  private PropietarioResponse aFicha(Propietario propietario) {
    return new PropietarioResponse(
        propietario.getId(),
        propietario.getNombre(),
        propietario.getTelefono(),
        propietario.getCorreo(),
        propietario.isActivo(),
        mascotas.activasDe(propietario.getId()));
  }

  private static void rechazarDireccion(PropietarioRequest datos) {
    if (opcional(datos.direccion()) != null) {
      throw new DireccionNoDisponibleException();
    }
  }

  /** Un opcional en blanco se guarda como ausente. */
  private static String opcional(String valor) {
    return valor == null || valor.isBlank() ? null : valor.strip();
  }
}
