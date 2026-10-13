package com.nttdata.ms_reservas.cliente;

/** El catálogo respondió 404: el vehículo no existe. (Resuelto) */
public class VehiculoNoEncontradoException extends RuntimeException {

    public VehiculoNoEncontradoException(Long id) {
        super("No existe el vehículo " + id);
    }
}
