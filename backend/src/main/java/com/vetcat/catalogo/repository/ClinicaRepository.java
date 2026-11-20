package com.vetcat.catalogo.repository;

import com.vetcat.catalogo.entity.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a las clínicas del catálogo global (unidad de persistencia sin multi-tenant). */
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {}
