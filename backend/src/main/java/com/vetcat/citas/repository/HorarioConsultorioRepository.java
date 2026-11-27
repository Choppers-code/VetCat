package com.vetcat.citas.repository;

import com.vetcat.citas.entity.HorarioConsultorio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioConsultorioRepository extends JpaRepository<HorarioConsultorio, Long> {
  List<HorarioConsultorio> findAllByOrderById();

  List<HorarioConsultorio> findByConsultorioIdOrderById(Long consultorioId);
}
