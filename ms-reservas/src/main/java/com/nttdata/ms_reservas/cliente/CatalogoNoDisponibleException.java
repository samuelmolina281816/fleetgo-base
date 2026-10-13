package com.nttdata.ms_reservas.cliente;

/** El catálogo no responde: problema de red o servicio caído. (Resuelto) */
public class CatalogoNoDisponibleException extends RuntimeException {

    public CatalogoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
