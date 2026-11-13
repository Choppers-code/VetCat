package com.vetcat.citas.repository;

import com.vetcat.citas.entity.BloqueoAgenda;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BloqueoAgendaRepository extends JpaRepository<BloqueoAgenda, UUID> {

    @Query("select case when count(b) > 0 then true else false end "
            + "from BloqueoAgenda b "
            + "where b.inicio < :fin and b.fin > :inicio "
            + "and ((b.consultorio is null and b.veterinarioId is null) "
            + "or b.veterinarioId = :veterinarioId "
            + "or (:consultorioId is not null and b.consultorio.id = :consultorioId))")
    boolean existeConflicto(
            @Param("veterinarioId") UUID veterinarioId,
            @Param("consultorioId") UUID consultorioId,
            @Param("inicio") OffsetDateTime inicio,
            @Param("fin") OffsetDateTime fin);

    @Query("select b from BloqueoAgenda b "
            + "where b.inicio < :hasta and b.fin > :desde "
            + "and (:consultorioId is null or b.consultorio.id = :consultorioId) "
            + "and (:veterinarioId is null or b.veterinarioId = :veterinarioId) "
            + "order by b.inicio")
    List<BloqueoAgenda> buscarPorRango(
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta,
            @Param("consultorioId") UUID consultorioId,
            @Param("veterinarioId") UUID veterinarioId);
}
