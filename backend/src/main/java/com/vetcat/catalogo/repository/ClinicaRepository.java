package com.vetcat.catalogo.repository;

import com.vetcat.catalogo.entity.Clinica;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acceso a las clínicas del catálogo global (unidad de persistencia sin multi-tenant). */
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {

  /** Bloquea la fila de la clínica hasta el fin de la transacción, para serializar su personal. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Clinica c where c.id = :id")
  Optional<Clinica> bloquearPorId(@Param("id") Long id);
}
