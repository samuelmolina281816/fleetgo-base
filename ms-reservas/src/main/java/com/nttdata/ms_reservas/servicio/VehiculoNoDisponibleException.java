package com.nttdata.ms_reservas.servicio;

/** El vehículo no puede reservarse: no está disponible o ya tiene una reserva. (Resuelto) */
public class VehiculoNoDisponibleException extends RuntimeException {

    public VehiculoNoDisponibleException(String estado) {
        super(estado);
    }
}
