package com.nttdata.ms_reservas.validacion;

import com.nttdata.ms_reservas.cliente.CatalogoNoDisponibleException;
import com.nttdata.ms_reservas.cliente.VehiculoClient;
import com.nttdata.ms_reservas.cliente.VehiculoClientRest;
import com.nttdata.ms_reservas.cliente.VehiculoNoEncontradoException;
import com.nttdata.ms_reservas.config.ClienteConfig;
import com.nttdata.ms_reservas.model.Reserva;
import com.nttdata.ms_reservas.model.ReservaRequest;
import com.nttdata.ms_reservas.model.Vehiculo;
import com.nttdata.ms_reservas.servicio.ReservaRepository;
import com.nttdata.ms_reservas.servicio.ReservaService;
import com.nttdata.ms_reservas.web.ReservaController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Verificaciones automáticas de ms-reservas: 14 puntos. (Resuelto: no modificar)
 * Usa un catálogo simulado, por lo que NO necesita que ms-vehiculos esté en ejecución.
 */
public class Validador {

    private static int ok = 0, total = 0;

    /** Catálogo simulado con los mismos datos de ms-vehiculos. */
    static class CatalogoFalso implements VehiculoClient {
        boolean caido = false;
        public Vehiculo buscar(Long id) {
            if (caido) throw new CatalogoNoDisponibleException("El catálogo de vehículos no responde");
            if (id == 1L) return new Vehiculo(1L, "ABC-123", "SEDAN", 120.0, true);
            if (id == 2L) return new Vehiculo(2L, "XYZ-789", "SUV", 200.0, true);
            if (id == 3L) return new Vehiculo(3L, "TRK-456", "CAMIONETA", 300.0, false);
            throw new VehiculoNoEncontradoException(id);
        }
    }

    private static CatalogoFalso catalogo;

    private static ReservaController nuevoControlador() {
        catalogo = new CatalogoFalso();
        return new ReservaController(new ReservaService(catalogo, new ReservaRepository()));
    }

    private static ResponseEntity<Reserva> post(ReservaController c, Long vehiculo, String cliente, int dias) {
        return c.crear(new ReservaRequest(vehiculo, cliente, dias));
    }

    public static void main(String[] args) {
        System.out.println("FleetGo - ms-reservas");
        System.out.println("=====================");

        // ---------- API REST y contratos ----------
        verificar("ReservaController es @RestController con ruta /api/reservas", () -> {
            Class<?> c = ReservaController.class;
            return c.isAnnotationPresent(RestController.class)
                    && Arrays.asList(c.getAnnotation(RequestMapping.class).value()).contains("/api/reservas");
        });
        verificar("crear() tiene @PostMapping y @RequestBody", () -> {
            Method m = ReservaController.class.getMethod("crear", ReservaRequest.class);
            return m.isAnnotationPresent(PostMapping.class)
                    && m.getParameters()[0].isAnnotationPresent(RequestBody.class);
        });
        verificar("listar() tiene @GetMapping y refleja las reservas creadas", () -> {
            Method m = ReservaController.class.getMethod("listar");
            ReservaController c = nuevoControlador();
            post(c, 1L, "Ana", 3);
            post(c, 2L, "Luis", 2);
            List<Reserva> lista = c.listar();
            return m.isAnnotationPresent(GetMapping.class) && lista.size() == 2
                    && lista.get(0).id() == 1L && lista.get(1).id() == 2L;
        });

        // ---------- Comunicación y desacoplamiento ----------
        verificar("ReservaService depende de la interfaz VehiculoClient (no de RestClient)", () -> {
            Constructor<?> ctor = ReservaService.class.getConstructors()[0];
            List<Class<?>> tipos = Arrays.asList(ctor.getParameterTypes());
            boolean dependeDelDetalle = tipos.stream().anyMatch(t -> t.getSimpleName().contains("RestClient")
                    || t.equals(VehiculoClientRest.class));
            boolean guardaDependencias = Arrays.stream(ReservaService.class.getDeclaredFields())
                    .anyMatch(f -> f.getType().equals(VehiculoClient.class))
                    && Arrays.stream(ReservaService.class.getDeclaredFields())
                    .anyMatch(f -> f.getType().equals(ReservaRepository.class));
            return tipos.contains(VehiculoClient.class) && !dependeDelDetalle && guardaDependencias;
        });
        verificar("VehiculoClientRest implementa VehiculoClient y ClienteConfig expone el @Bean RestClient", () -> {
            boolean implementa = VehiculoClient.class.isAssignableFrom(VehiculoClientRest.class);
            Method bean = Arrays.stream(ClienteConfig.class.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Bean.class)).findFirst().orElse(null);
            if (bean == null) return false;
            Value v = bean.getParameters()[0].getAnnotation(Value.class);
            Properties p = new Properties();
            try (var in = new FileInputStream("src/main/resources/application.properties")) { p.load(in); }
            return implementa && v != null && v.value().equals("${vehiculos.url}")
                    && p.getProperty("vehiculos.url") != null;
        });

        // ---------- Reglas de negocio ----------
        verificar("RN-02: SEDAN por 3 días -> 201 CONFIRMADA, total 360.00", () -> {
            ResponseEntity<Reserva> r = post(nuevoControlador(), 1L, "Ana", 3);
            return r.getStatusCode().value() == 201 && r.getBody().estado().equals("CONFIRMADA")
                    && iguales(r.getBody().total(), 360.0) && r.getBody().placa().equals("ABC-123");
        });
        verificar("RN-03: SEDAN por 7 días -> descuento del 10%, total 756.00", () ->
                iguales(post(nuevoControlador(), 1L, "Ana", 7).getBody().total(), 756.0));
        verificar("RN-03: SUV por 10 días -> total 1800.00", () ->
                iguales(post(nuevoControlador(), 2L, "Luis", 10).getBody().total(), 1800.0));
        verificar("RN-01: 0 días -> 400 DATOS INVALIDOS", () -> {
            ResponseEntity<Reserva> r = post(nuevoControlador(), 1L, "Ana", 0);
            return r.getStatusCode().value() == 400 && r.getBody().estado().equals("DATOS INVALIDOS");
        });
        verificar("RN-01: cliente vacío -> 400 DATOS INVALIDOS", () -> {
            ResponseEntity<Reserva> r = post(nuevoControlador(), 1L, "   ", 3);
            return r.getStatusCode().value() == 400 && r.getBody().estado().equals("DATOS INVALIDOS");
        });
        verificar("RN-04: vehículo no disponible -> 409 VEHICULO NO DISPONIBLE", () -> {
            ResponseEntity<Reserva> r = post(nuevoControlador(), 3L, "Ana", 2);
            return r.getStatusCode().value() == 409 && r.getBody().estado().equals("VEHICULO NO DISPONIBLE");
        });

        // ---------- Errores y resiliencia ----------
        verificar("RN-05: mismo vehículo dos veces -> 409 YA RESERVADO", () -> {
            ReservaController c = nuevoControlador();
            post(c, 1L, "Ana", 3);
            ResponseEntity<Reserva> r = post(c, 1L, "Luis", 2);
            return r.getStatusCode().value() == 409 && r.getBody().estado().equals("YA RESERVADO");
        });
        verificar("RN-06: vehículo inexistente -> 404 VEHICULO NO EXISTE", () -> {
            ResponseEntity<Reserva> r = post(nuevoControlador(), 9L, "Ana", 3);
            return r.getStatusCode().value() == 404 && r.getBody().estado().equals("VEHICULO NO EXISTE");
        });
        verificar("RN-07: catálogo caído -> 503 CATALOGO NO DISPONIBLE", () -> {
            ReservaController c = nuevoControlador();
            catalogo.caido = true;
            ResponseEntity<Reserva> r = post(c, 1L, "Ana", 3);
            return r.getStatusCode().value() == 503 && r.getBody().estado().equals("CATALOGO NO DISPONIBLE");
        });

        System.out.println();
        System.out.printf("PUNTAJE ms-reservas: %d / %d%n", ok, total);
        System.out.println("(Suma el puntaje de ms-vehiculos para el total sobre 20)");
    }

    private static boolean iguales(double a, double b) {
        return Math.abs(a - b) < 0.01;
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
