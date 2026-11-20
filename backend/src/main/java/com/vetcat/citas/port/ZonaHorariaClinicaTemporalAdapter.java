package com.vetcat.citas.port;

import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class ZonaHorariaClinicaTemporalAdapter implements ClinicaContextPort {
    private static final ZoneId ZONA_TEMPORAL = ZoneId.of("America/Mexico_City");

    @Override
    public ZoneId getZonaHorariaActual() {
        return ZONA_TEMPORAL;
    }
}
