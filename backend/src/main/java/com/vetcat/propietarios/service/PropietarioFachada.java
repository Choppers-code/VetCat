package com.vetcat.propietarios.service;

import com.vetcat.propietarios.repository.PropietarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Puerta del módulo de propietarios para los demás módulos: nadie usa su repositorio. */
@Service
public class PropietarioFachada {

  private final PropietarioRepository propietarios;

  public PropietarioFachada(PropietarioRepository propietarios) {
    this.propietarios = propietarios;
  }

  /**
   * Exige un propietario activo de la clínica activa y lo retiene hasta el fin de la transacción de
   * quien llama, para que no se dé de baja mientras se le añade algo.
   *
   * @throws PropietarioNoEncontradoException si no existe en la clínica o está inactivo
   */
  @Transactional(value = "tenantTransactionManager", propagation = Propagation.MANDATORY)
  public void retenerActivo(long propietarioId) {
    propietarios.compartirActivo(propietarioId).orElseThrow(PropietarioNoEncontradoException::new);
  }
}
