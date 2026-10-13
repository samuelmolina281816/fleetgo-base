package com.nttdata.ms_reservas.cliente;

import com.nttdata.ms_reservas.model.Vehiculo;

/**
 * Contrato para obtener vehículos del catálogo. (Resuelto)
 * El servicio depende de esta interfaz y no del detalle HTTP: así se puede cambiar
 * la forma de comunicación (REST, mensajería, caché) sin tocar las reglas de negocio.
 */
public interface VehiculoClient {

    /**
     * @throws VehiculoNoEncontradoException si el catálogo responde 404
     * @throws CatalogoNoDisponibleException si el catálogo no responde
     */
    Vehiculo buscar(Long id);
}
