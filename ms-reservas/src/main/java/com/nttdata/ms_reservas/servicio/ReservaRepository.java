package com.nttdata.ms_reservas.servicio;

import com.nttdata.ms_reservas.model.Reserva;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/** Almacén en memoria de las reservas. */
@Repository
public class ReservaRepository {

    private final List<Reserva> reservas = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong();

    // TODO 11 [COMPLETADO]: guarda la reserva asignándole un id correlativo (1, 2, 3...) con la secuencia
    //          y devuélvela ya con su id.
    public Reserva guardar(Reserva reserva) {
        Reserva guardada = new Reserva(
                secuencia.incrementAndGet(),
                reserva.vehiculoId(),
                reserva.placa(),
                reserva.cliente(),
                reserva.dias(),
                reserva.total(),
                reserva.estado());
        reservas.add(guardada);
        return guardada;
    }

    // TODO 12 [COMPLETADO] (RN-05): indica si el vehículo ya tiene una reserva registrada.
    public boolean tieneReservaActiva(Long vehiculoId) {
        return reservas.stream()
                .anyMatch(reserva -> Objects.equals(reserva.vehiculoId(), vehiculoId));
    }

    // TODO 13 [COMPLETADO]: devuelve la lista de reservas como vista de solo lectura.
    public List<Reserva> listar() {
        return Collections.unmodifiableList(reservas);
    }
}
