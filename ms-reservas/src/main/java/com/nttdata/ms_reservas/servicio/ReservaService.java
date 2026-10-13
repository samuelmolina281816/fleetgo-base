package com.nttdata.ms_reservas.servicio;

import com.nttdata.ms_reservas.cliente.VehiculoClient;
import com.nttdata.ms_reservas.model.Reserva;
import com.nttdata.ms_reservas.model.ReservaRequest;
import com.nttdata.ms_reservas.model.Vehiculo;
import org.springframework.stereotype.Service;

import java.util.List;

/** Reglas de negocio de las reservas. */
@Service
public class ReservaService {

    private static final int DIAS_MINIMOS_DESCUENTO = 7;
    private static final double TASA_DESCUENTO = 0.10;

    private final VehiculoClient vehiculoClient;
    private final ReservaRepository repository;

    // TODO 14 [COMPLETADO]: declara las dependencias e inyéctalas por constructor.
    //          IMPORTANTE: depende de la interfaz VehiculoClient, nunca de RestClient
    //          ni de VehiculoClientRest (desacoplamiento).
    public ReservaService(VehiculoClient vehiculoClient, ReservaRepository repository) {
        this.vehiculoClient = vehiculoClient;
        this.repository = repository;
    }

    // TODO 15 [COMPLETADO]: implementa el flujo de creación de la reserva:
    //   1. valida los datos (RN-01)
    //   2. busca el vehículo con vehiculoClient.buscar(...)
    //   3. si no está disponible -> VehiculoNoDisponibleException("VEHICULO NO DISPONIBLE")  (RN-04)
    //   4. si ya tiene una reserva -> VehiculoNoDisponibleException("YA RESERVADO")          (RN-05)
    //   5. calcula el total y guarda la reserva con estado "CONFIRMADA"
    public Reserva crear(ReservaRequest req) {
        validar(req);

        Vehiculo vehiculo = vehiculoClient.buscar(req.vehiculoId());
        if (!vehiculo.disponible()) {
            throw new VehiculoNoDisponibleException("VEHICULO NO DISPONIBLE");
        }
        if (repository.tieneReservaActiva(req.vehiculoId())) {
            throw new VehiculoNoDisponibleException("YA RESERVADO");
        }

        double total = calcularTotal(vehiculo.tarifaDia(), req.dias());
        Reserva reserva = new Reserva(
                null,
                req.vehiculoId(),
                vehiculo.placa(),
                req.cliente(),
                req.dias(),
                total,
                "CONFIRMADA");
        return repository.guardar(reserva);
    }

    // TODO 16 [COMPLETADO]: devuelve todas las reservas registradas.
    public List<Reserva> listar() {
        return repository.listar();
    }

    // TODO 17 [COMPLETADO] (RN-02 y RN-03): total = tarifa diaria x días;
    //          si son 7 días o más se aplica un 10% de descuento. Usa constantes.
    private double calcularTotal(double tarifaDia, int dias) {
        double total = tarifaDia * dias;
        if (dias >= DIAS_MINIMOS_DESCUENTO) {
            total *= 1 - TASA_DESCUENTO;
        }
        return total;
    }

    // TODO 18 [COMPLETADO] (RN-01): cliente obligatorio y días entre 1 y 30; si no, IllegalArgumentException.
    private void validar(ReservaRequest req) {
        if (req == null || req.cliente() == null || req.cliente().isBlank()
                || req.dias() < 1 || req.dias() > 30) {
            throw new IllegalArgumentException("Datos de reserva inválidos");
        }
    }
}
