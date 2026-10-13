package com.nttdata.ms_vehiculos;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST del catálogo de vehículos.
 */
// TODO 1 [COMPLETADO]: anota la clase como @RestController y con la ruta base @RequestMapping("/api/vehiculos").
@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    // TODO 2 [COMPLETADO]: crea la lista de los 3 vehículos del enunciado (sección 5.2), en ese orden.
    private final List<Vehiculo> vehiculos = List.of(
            new Vehiculo(1L, "ABC-123", "SEDAN", 120.0, true),
            new Vehiculo(2L, "XYZ-789", "SUV", 200.0, true),
            new Vehiculo(3L, "TRK-456", "CAMIONETA", 300.0, false)
    );

    // TODO 3 [COMPLETADO]: anota el método con @GetMapping y devuelve la lista completa.
    @GetMapping
    public List<Vehiculo> listar() {
        return vehiculos;
    }

    // TODO 4 [COMPLETADO]: anota con @GetMapping("/{id}") y recibe @PathVariable("id").
    //         Devuelve 200 con el vehículo, o 404 si no existe.
    @GetMapping("/{id}")
    public ResponseEntity<Vehiculo> obtener(@PathVariable("id") Long id) {
        return vehiculos.stream()
                .filter(vehiculo -> vehiculo.id().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
