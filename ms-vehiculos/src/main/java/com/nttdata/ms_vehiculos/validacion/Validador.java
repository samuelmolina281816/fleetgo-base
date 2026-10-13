package com.nttdata.ms_vehiculos.validacion;

import com.nttdata.ms_vehiculos.Vehiculo;
import com.nttdata.ms_vehiculos.VehiculoController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Verificaciones automáticas de ms-vehiculos: 6 puntos. (Resuelto: no modificar) */
public class Validador {

    private static int ok = 0, total = 0;

    public static void main(String[] args) {
        System.out.println("FleetGo - ms-vehiculos");
        System.out.println("======================");

        verificar("VehiculoController es @RestController con ruta /api/vehiculos", () -> {
            Class<?> c = VehiculoController.class;
            return c.isAnnotationPresent(RestController.class)
                    && c.isAnnotationPresent(RequestMapping.class)
                    && Arrays.asList(c.getAnnotation(RequestMapping.class).value()).contains("/api/vehiculos");
        });

        verificar("listar() tiene @GetMapping y devuelve los 3 vehículos", () -> {
            Method m = VehiculoController.class.getMethod("listar");
            List<Vehiculo> lista = new VehiculoController().listar();
            return m.isAnnotationPresent(GetMapping.class) && lista.size() == 3
                    && lista.get(0).placa().equals("ABC-123") && lista.get(2).categoria().equals("CAMIONETA");
        });

        verificar("obtener() usa @GetMapping(\"/{id}\") y @PathVariable(\"id\")", () -> {
            Method m = VehiculoController.class.getMethod("obtener", Long.class);
            if (!m.isAnnotationPresent(GetMapping.class)) return false;
            String ruta = m.getAnnotation(GetMapping.class).value()[0];
            PathVariable pv = m.getParameters()[0].getAnnotation(PathVariable.class);
            return pv != null && ruta.equals("/{" + pv.value() + "}");
        });

        verificar("GET /api/vehiculos/1 devuelve 200 con el SEDAN ABC-123", () -> {
            ResponseEntity<Vehiculo> r = new VehiculoController().obtener(1L);
            return r.getStatusCode().value() == 200 && r.getBody().placa().equals("ABC-123")
                    && r.getBody().tarifaDia() == 120.0 && r.getBody().disponible();
        });

        verificar("GET /api/vehiculos/9 devuelve 404", () ->
                new VehiculoController().obtener(9L).getStatusCode().value() == 404);

        verificar("El microservicio escucha en el puerto 8081", () -> {
            Properties p = new Properties();
            try (var in = new FileInputStream("src/main/resources/application.properties")) { p.load(in); }
            return "8081".equals(p.getProperty("server.port"));
        });

        System.out.println();
        System.out.printf("PUNTAJE ms-vehiculos: %d / %d%n", ok, total);
    }

    interface Prueba { boolean ejecutar() throws Exception; }

    private static void verificar(String descripcion, Prueba prueba) {
        total++;
        boolean r;
        String detalle = "";
        try {
            r = prueba.ejecutar();
        } catch (UnsupportedOperationException e) {
            r = false; detalle = "  (pendiente: " + e.getMessage() + ")";
        } catch (Throwable e) {
            r = false; detalle = "  (" + e.getClass().getSimpleName() + ")";
        }
        if (r) ok++;
        System.out.printf("%s %2d. %s%s%n", r ? "[OK]" : "[X] ", total, descripcion, detalle);
    }
}
