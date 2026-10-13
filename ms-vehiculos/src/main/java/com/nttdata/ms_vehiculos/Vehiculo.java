package com.nttdata.ms_vehiculos;

/** Contrato JSON del vehículo que expone el catálogo. (Resuelto) */
public record Vehiculo(Long id, String placa, String categoria, double tarifaDia, boolean disponible) {
}
