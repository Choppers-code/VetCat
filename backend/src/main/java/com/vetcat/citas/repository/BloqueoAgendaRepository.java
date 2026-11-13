package com.vetcat.citas.repository;

import com.vetcat.citas.entity.BloqueoAgenda;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloqueoAgendaRepository extends JpaRepository<BloqueoAgenda, UUID> {
}
