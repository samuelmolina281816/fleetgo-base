package com.nttdata.ms_reservas.model;

/** Cuerpo de la petición POST /api/reservas. (Resuelto) */
public record ReservaRequest(Long vehiculoId, String cliente, int dias) {
}
