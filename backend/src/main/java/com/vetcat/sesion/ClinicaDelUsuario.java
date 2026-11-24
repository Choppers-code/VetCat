package com.vetcat.sesion;

import java.util.List;

/**
 * Una clínica del usuario en el selector de sesión (IDA-002).
 *
 * @param estado de la clínica: `ACTIVA`, `SUSPENDIDA` o `BAJA`; la app la deshabilita si no está
 *     activa
 * @param roles roles del usuario en esa clínica, no de la clínica activa
 */
public record ClinicaDelUsuario(Long id, String nombre, String estado, List<String> roles) {}
