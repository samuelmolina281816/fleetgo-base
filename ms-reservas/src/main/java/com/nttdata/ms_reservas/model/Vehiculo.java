package com.nttdata.ms_reservas.model;

/** Copia del contrato de ms-vehiculos: los microservicios comparten el contrato, no el código. (Resuelto) */
public record Vehiculo(Long id, String placa, String categoria, double tarifaDia, boolean disponible) {
}
