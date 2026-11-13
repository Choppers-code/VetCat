package com.vetcat.citas.repository;

import com.vetcat.citas.entity.Cita;
import java.time.OffsetDateTime;
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
}
