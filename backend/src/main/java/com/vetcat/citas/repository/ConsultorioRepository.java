package com.vetcat.citas.repository;

import com.vetcat.citas.entity.Consultorio;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultorioRepository extends JpaRepository<Consultorio, UUID> {
}
