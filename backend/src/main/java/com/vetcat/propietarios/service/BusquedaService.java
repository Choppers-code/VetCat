package com.vetcat.propietarios.service;

import com.vetcat.mascotas.dto.MascotaResponse;
import com.vetcat.mascotas.service.MascotaFachada;
import com.vetcat.propietarios.dto.BusquedaRequest;
import com.vetcat.propietarios.dto.PropietarioResponse;
import com.vetcat.propietarios.dto.ResultadoBusquedaResponse;
import com.vetcat.propietarios.entity.Propietario;
import com.vetcat.propietarios.repository.BusquedaRepository;
import com.vetcat.propietarios.repository.PropietarioRepository;
import com.vetcat.shared.PaginaResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Búsqueda en la clínica activa (PRO-009). La lista pone primero los propietarios y después las
 * mascotas, cada grupo por nombre, y se pagina como una sola.
 */
@Service
@Transactional(value = "tenantTransactionManager", readOnly = true)
public class BusquedaService {

  /** Lo que puede escribirse en un teléfono: solo entonces se compara por dígitos. */
  private static final String SOLO_TELEFONO = "[0-9 +().-]+";

  private final PropietarioRepository propietarios;
  private final BusquedaRepository busquedas;
  private final MascotaFachada mascotas;

  public BusquedaService(
      PropietarioRepository propietarios, BusquedaRepository busquedas, MascotaFachada mascotas) {
    this.propietarios = propietarios;
    this.busquedas = busquedas;
    this.mascotas = mascotas;
  }

  public PaginaResponse<ResultadoBusquedaResponse> buscar(BusquedaRequest peticion) {
    String texto = peticion.q();
    String digitos = digitosDe(texto);
    boolean inactivos = peticion.incluirInactivos();
    int tamano = peticion.size();
    long desde = (long) peticion.page() * tamano;

    long totalPropietarios = busquedas.contarCoincidencias(texto, digitos, inactivos);
    long totalMascotas = mascotas.contarCoincidencias(texto, inactivos);

    List<Propietario> hallados = busquedas.coincidencias(texto, digitos, inactivos, desde, tamano);
    int libres = tamano - hallados.size();
    List<MascotaResponse> pacientes =
        libres == 0
            ? List.of()
            : mascotas.coincidencias(
                texto, inactivos, Math.max(0, desde - totalPropietarios), libres);

    Map<Long, PropietarioResponse> fichas = fichas(hallados, pacientes);
    List<ResultadoBusquedaResponse> contenido = new ArrayList<>();
    hallados.forEach(
        p -> contenido.add(new ResultadoBusquedaResponse(fichas.get(p.getId()), null)));
    pacientes.forEach(
        m -> contenido.add(new ResultadoBusquedaResponse(fichas.get(m.propietarioId()), m)));
    return new PaginaResponse<>(
        contenido, peticion.page(), tamano, totalPropietarios + totalMascotas);
  }

  /** Fichas de los propietarios de la página, en dos consultas y no una por fila. */
  private Map<Long, PropietarioResponse> fichas(
      List<Propietario> hallados, List<MascotaResponse> pacientes) {
    Set<Long> ids = new LinkedHashSet<>();
    hallados.forEach(p -> ids.add(p.getId()));
    pacientes.forEach(m -> ids.add(m.propietarioId()));
    Map<Long, List<MascotaResponse>> activas = mascotas.activasDe(ids);
    return propietarios.findAllById(ids).stream()
        .map(p -> PropietarioService.aFicha(p, activas.getOrDefault(p.getId(), List.of())))
        .collect(Collectors.toMap(PropietarioResponse::id, Function.identity()));
  }

  private static String digitosDe(String texto) {
    if (!texto.matches(SOLO_TELEFONO)) {
      return "";
    }
    String digitos = texto.replaceAll("[^0-9]", "");
    return digitos.length() < 3 ? "" : digitos;
  }
}
