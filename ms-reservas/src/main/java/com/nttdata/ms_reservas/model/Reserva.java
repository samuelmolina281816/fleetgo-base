package com.nttdata.ms_reservas.model;

/** Respuesta de ms-reservas. (Resuelto) */
public record Reserva(Long id, Long vehiculoId, String placa, String cliente, int dias, double total, String estado) {
}
