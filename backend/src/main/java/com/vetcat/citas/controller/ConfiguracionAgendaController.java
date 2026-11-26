package com.vetcat.citas.controller;

import com.vetcat.citas.dto.ConsultorioRequest;
import com.vetcat.citas.dto.ConsultorioResponse;
import com.vetcat.citas.dto.HorarioRequest;
import com.vetcat.citas.dto.HorarioResponse;
import com.vetcat.citas.service.ConfiguracionAgendaService;
import com.vetcat.shared.Sobre;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ConfiguracionAgendaController {

  private final ConfiguracionAgendaService configuracion;

  public ConfiguracionAgendaController(ConfiguracionAgendaService configuracion) {
    this.configuracion = configuracion;
  }

  @GetMapping("/consultorios")
  public Sobre<List<ConsultorioResponse>> consultorios() {
    return Sobre.ok(configuracion.listarConsultorios());
  }

  @PostMapping("/consultorios")
  @ResponseStatus(HttpStatus.CREATED)
  public Sobre<ConsultorioResponse> crearConsultorio(@RequestBody ConsultorioRequest request) {
    return Sobre.ok(configuracion.crearConsultorio(request));
  }

  @PutMapping("/consultorios/{id}")
  public Sobre<ConsultorioResponse> editarConsultorio(
      @PathVariable long id, @RequestBody ConsultorioRequest request) {
    return Sobre.ok(configuracion.editarConsultorio(id, request));
  }

  @GetMapping("/horarios")
  public Sobre<List<HorarioResponse>> horarios(
      @RequestParam(required = false) Long consultorioId,
      @RequestParam(required = false) LocalDate fecha) {
    return Sobre.ok(configuracion.listarHorarios(consultorioId, fecha));
  }

  @PostMapping("/horarios")
  @ResponseStatus(HttpStatus.CREATED)
  public Sobre<HorarioResponse> crearHorario(@RequestBody HorarioRequest request) {
    return Sobre.ok(configuracion.crearHorario(request));
  }

  @PutMapping("/horarios/{id}")
  public Sobre<HorarioResponse> editarHorario(
      @PathVariable long id, @RequestBody HorarioRequest request) {
    return Sobre.ok(configuracion.editarHorario(id, request));
  }
}
