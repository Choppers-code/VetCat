package com.vetcat.citas.repository;

import com.vetcat.citas.entity.Consultorio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultorioRepository extends JpaRepository<Consultorio, Long> {
  List<Consultorio> findAllByOrderById();
}
