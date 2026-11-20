package com.vetcat.tenancy.prueba;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Entidad de tenant solo para pruebas: sin esquema, vive en el de la clínica activa. */
@Entity
@Table(name = "mascota")
public class MascotaPrueba {

  @Id private Long id;

  private String nombre;

  protected MascotaPrueba() {}

  public MascotaPrueba(Long id, String nombre) {
    this.id = id;
    this.nombre = nombre;
  }

  public Long getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }
}
