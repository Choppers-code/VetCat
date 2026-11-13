package com.vetcat.citas.repository;

import com.vetcat.citas.entity.Cita;
import com.vetcat.citas.entity.EstadoCita;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita, UUID> {

    @Query("select case when count(c) > 0 then true else false end "
            + "from Cita c "
            + "where c.veterinarioId = :veterinarioId "
            + "and c.estado <> com.vetcat.citas.entity.EstadoCita.CANCELADA "
            + "and c.inicio < :fin "
            + "and c.fin > :inicio")
    boolean existeEmpalmePorVeterinario(
            @Param("veterinarioId") UUID veterinarioId,
            @Param("inicio") OffsetDateTime inicio,
            @Param("fin") OffsetDateTime fin);

    @Query("select case when count(c) > 0 then true else false end "
            + "from Cita c "
            + "where c.consultorio.id = :consultorioId "
            + "and c.tipo = com.vetcat.citas.entity.TipoCita.CONSULTORIO "
            + "and c.estado <> com.vetcat.citas.entity.EstadoCita.CANCELADA "
            + "and c.inicio < :fin "
            + "and c.fin > :inicio")
    boolean existeEmpalmePorConsultorio(
            @Param("consultorioId") UUID consultorioId,
            @Param("inicio") OffsetDateTime inicio,
            @Param("fin") OffsetDateTime fin);

    @Query("select case when count(c) > 0 then true else false end "
            + "from Cita c "
            + "where c.estado <> com.vetcat.citas.entity.EstadoCita.CANCELADA "
            + "and c.inicio < :fin and c.fin > :inicio "
            + "and ((:veterinarioId is null and :consultorioId is null) "
            + "or c.veterinarioId = :veterinarioId "
            + "or (:consultorioId is not null and c.consultorio.id = :consultorioId))")
    boolean existeEmpalmeConBloqueo(
            @Param("veterinarioId") UUID veterinarioId,
            @Param("consultorioId") UUID consultorioId,
            @Param("inicio") OffsetDateTime inicio,
            @Param("fin") OffsetDateTime fin);

    @Query("select c from Cita c "
            + "where (:desde is null or c.fin > :desde) "
            + "and (:hasta is null or c.inicio < :hasta) "
            + "and (:consultorioId is null or c.consultorio.id = :consultorioId) "
            + "and (:veterinarioId is null or c.veterinarioId = :veterinarioId) "
            + "and (:estado is null or c.estado = :estado)")
    List<Cita> buscarPorFiltros(
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta,
            @Param("consultorioId") UUID consultorioId,
            @Param("veterinarioId") UUID veterinarioId,
            @Param("estado") EstadoCita estado);
}
