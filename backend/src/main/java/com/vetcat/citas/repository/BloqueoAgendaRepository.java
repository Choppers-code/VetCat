package com.vetcat.citas.repository;

import com.vetcat.citas.entity.BloqueoAgenda;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BloqueoAgendaRepository extends JpaRepository<BloqueoAgenda, Long> {

  @Query(
      """
      select b from BloqueoAgenda b
      where b.inicio < :fin and b.fin > :inicio
        and (b.consultorioId = :consultorioId or b.veterinarioId = :veterinarioId)
      order by b.id
      """)
  List<BloqueoAgenda> buscarSolapados(
      @Param("consultorioId") Long consultorioId,
      @Param("veterinarioId") Long veterinarioId,
      @Param("inicio") Instant inicio,
      @Param("fin") Instant fin);
}
