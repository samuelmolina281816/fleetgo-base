package com.nttdata.ms_reservas.web;

import com.nttdata.ms_reservas.cliente.CatalogoNoDisponibleException;
import com.nttdata.ms_reservas.cliente.VehiculoNoEncontradoException;
import com.nttdata.ms_reservas.model.Reserva;
import com.nttdata.ms_reservas.model.ReservaRequest;
import com.nttdata.ms_reservas.servicio.ReservaService;
import com.nttdata.ms_reservas.servicio.VehiculoNoDisponibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API REST de reservas. */
// TODO 19 [COMPLETADO]: anota la clase como @RestController con la ruta base @RequestMapping("/api/reservas")
//          e inyecta ReservaService por constructor.
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    // TODO 20 [COMPLETADO]: anota con @PostMapping y @RequestBody. Devuelve 201 con la reserva creada y,
    //          según la excepción, el estado correcto usando el método error(...):
    //            IllegalArgumentException          -> 400 "DATOS INVALIDOS"
    //            VehiculoNoDisponibleException     -> 409 con e.getMessage()
    //            VehiculoNoEncontradoException     -> 404 "VEHICULO NO EXISTE"
    //            CatalogoNoDisponibleException     -> 503 "CATALOGO NO DISPONIBLE"
    @PostMapping
    public ResponseEntity<Reserva> crear(@RequestBody ReservaRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(service.crear(req));
        } catch (IllegalArgumentException e) {
            return error(req, HttpStatus.BAD_REQUEST, "DATOS INVALIDOS");
        } catch (VehiculoNoDisponibleException e) {
            return error(req, HttpStatus.CONFLICT, e.getMessage());
        } catch (VehiculoNoEncontradoException e) {
            return error(req, HttpStatus.NOT_FOUND, "VEHICULO NO EXISTE");
        } catch (CatalogoNoDisponibleException e) {
            return error(req, HttpStatus.SERVICE_UNAVAILABLE, "CATALOGO NO DISPONIBLE");
        }
    }

    // TODO 21 [COMPLETADO]: anota con @GetMapping y devuelve la lista de reservas del servicio.
    @GetMapping
    public List<Reserva> listar() {
        return service.listar();
    }

    /** Construye la respuesta de error. (Resuelto) */
    private ResponseEntity<Reserva> error(ReservaRequest req, HttpStatus estado, String mensaje) {
        if (req == null) {
            return ResponseEntity.status(estado)
                    .body(new Reserva(null, null, null, null, 0, 0, mensaje));
        }
        return ResponseEntity.status(estado)
                .body(new Reserva(null, req.vehiculoId(), null, req.cliente(), req.dias(), 0, mensaje));
    }
}
