package com.vetcat.citas.repository;

import com.vetcat.citas.entity.HorarioConsultorio;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioConsultorioRepository extends JpaRepository<HorarioConsultorio, UUID> {

    List<HorarioConsultorio> findByConsultorio_IdOrderByDiaSemanaAscFechaExcepcionAscHoraInicioAsc(
            UUID consultorioId);

    List<HorarioConsultorio> findByConsultorio_IdAndFechaExcepcionIsNullAndDiaSemanaOrderByHoraInicio(
            UUID consultorioId,
            Integer diaSemana);

    List<HorarioConsultorio> findByConsultorio_IdAndFechaExcepcionOrderByHoraInicio(
            UUID consultorioId,
            LocalDate fechaExcepcion);
}
