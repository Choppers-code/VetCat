package com.vetcat.mascotas.repository;

import com.vetcat.mascotas.entity.Mascota;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Mascotas de la clínica activa. */
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

  List<Mascota> findByPropietarioIdAndActivoTrueOrderByNombreAscIdAsc(Long propietarioId);
}
