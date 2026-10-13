Voy a revisar la estructura, el código de reservas y el `Validador.java` sin modificarlo; con eso documentaré el flujo real y cada TODO pendiente en un `.md`.

La revisión confirma 14 TODO pendientes (del 8 al 21) y propiedades 6–7 ya completadas. Estoy contrastando los contratos de excepciones y el arranque para que el documento distinga claramente la arquitectura prevista del comportamiento que todavía no está implementado.

No pude crear el archivo directamente porque en esta sesión solo tengo herramientas de lectura, no de escritura. Dejo abajo el contenido propuesto para guardar como `CONTEXTO_PROYECTO.md` en la raíz de `ms-reservas`. El análisis se basa en este microservicio y sus contratos; no inspeccioné el código de `ms-vehiculos`.

# FleetGo — Contexto del microservicio `ms-reservas`

## Propósito y contexto

FleetGo está compuesto por dos microservicios independientes que se comunican por REST:

| Microservicio | Responsabilidad | Puerto |
|---|---|---:|
| `ms-vehiculos` | Catálogo y consulta de vehículos | 8081 |
| `ms-reservas` | Validación y registro de reservas | 8082 |

Este proyecto corresponde a `ms-reservas`. Su responsabilidad es validar una solicitud, consultar el vehículo en el catálogo, aplicar las reglas de reserva y responder al cliente con el resultado. La URL del catálogo se configura con `vehiculos.url` en `src/main/resources/application.properties`; actualmente apunta a `http://localhost:8081`.

La intención del diseño es mantener las reglas de negocio dentro de `ReservaService`, aislar el detalle HTTP detrás de `VehiculoClient` y traducir errores del catálogo a respuestas comprensibles para el consumidor de la API.

## Componentes y responsabilidades

| Componente | Responsabilidad |
|---|---|
| `web/ReservaController` | Publicar los endpoints REST y convertir resultados o excepciones en respuestas HTTP. |
| `servicio/ReservaService` | Ejecutar el flujo de reserva y aplicar las reglas del negocio. |
| `servicio/ReservaRepository` | Mantener y consultar las reservas; actualmente está planteado como almacenamiento en memoria. |
| `cliente/VehiculoClient` | Contrato que desacopla la lógica de negocio del mecanismo de consulta de vehículos. |
| `cliente/VehiculoClientRest` | Implementación REST del contrato; debe consultar el catálogo y traducir sus errores. |
| `config/ClienteConfig` | Crear el `RestClient` con la URL base configurable. |
| `model/ReservaRequest` | Entrada del `POST`: `vehiculoId`, `cliente` y `dias`. |
| `model/Reserva` | Respuesta de la reserva: identificador, vehículo, placa, cliente, días, total y estado. |
| `model/Vehiculo` | Copia del contrato necesario para interpretar la respuesta del catálogo. |
| `validacion/Validador` | Verificaciones automáticas de `ms-reservas`; usa un catálogo simulado y no requiere ejecutar `ms-vehiculos`. |

## Flujo previsto de una reserva

1. El consumidor envía `POST /api/reservas` con `vehiculoId`, `cliente` y `dias`.
2. El controlador delega la operación a `ReservaService`.
3. El servicio valida que el cliente sea obligatorio y que los días estén entre 1 y 30 (RN-01).
4. `VehiculoClient` consulta `GET /api/vehiculos/{id}` en `ms-vehiculos`.
5. El servicio rechaza vehículos no disponibles (RN-04) y vehículos que ya tengan una reserva registrada (RN-05).
6. Calcula el total: tarifa diaria multiplicada por días (RN-02), con 10 % de descuento para reservas de 7 días o más (RN-03).
7. Guarda la reserva con un identificador correlativo y estado `CONFIRMADA`.
8. El controlador devuelve el resultado HTTP y el cuerpo correspondiente.

El contrato REST previsto para `VehiculoClientRest` distingue el vehículo inexistente (404 del catálogo, RN-06) de la falta de conexión o disponibilidad del catálogo (RN-07). El servicio no debe depender de `RestClient` ni de `VehiculoClientRest` directamente.

## API esperada

| Método | Ruta | Resultado esperado |
|---|---|---|
| `POST` | `/api/reservas` | Crea una reserva; 201 si se confirma. |
| `GET` | `/api/reservas` | Lista las reservas registradas; 200. |

Respuestas de error previstas para el `POST`:

| Situación | HTTP esperado | Estado indicado |
|---|---:|---|
| Solicitud inválida (RN-01) | 400 | `DATOS INVALIDOS` |
| Vehículo no disponible (RN-04) | 409 | `VEHICULO NO DISPONIBLE` |
| Vehículo ya reservado (RN-05) | 409 | `YA RESERVADO` |
| Vehículo inexistente (RN-06) | 404 | `VEHICULO NO EXISTE` |
| Catálogo no disponible (RN-07) | 503 | `CATALOGO NO DISPONIBLE` |

## Reglas de negocio

| Regla | Comportamiento esperado |
|---|---|
| RN-01 | Cliente obligatorio; días entre 1 y 30. Si no se cumple, responder 400. |
| RN-02 | Total base = tarifa diaria × días. |
| RN-03 | Aplicar un descuento del 10 % cuando la reserva sea de 7 días o más. |
| RN-04 | No confirmar una reserva si el vehículo no está disponible. |
| RN-05 | No permitir una segunda reserva del mismo vehículo. |
| RN-06 | Convertir un vehículo inexistente en una respuesta 404 clara. |
| RN-07 | Convertir la falta de disponibilidad del catálogo en una respuesta 503 clara. |

## Estado actual y pendientes

Los métodos marcados con TODO y que lanzan `UnsupportedOperationException` están pendientes. Los TODO 6 y 7 aparecen como completados en `application.properties`. La siguiente tabla cubre todos los TODO pendientes encontrados en el código fuente:

| TODO | Archivo / método | Pendiente | Relación con el flujo |
|---:|---|---|---|
| 8 | `config/ClienteConfig.java` — `vehiculosClient` | Exponer un `@Bean RestClient` y configurar su `baseUrl` usando `@Value("${vehiculos.url}")`. | Habilita la comunicación configurable con el catálogo. |
| 9 | `cliente/VehiculoClientRest.java` — constructor | Recibir e inyectar `RestClient` por constructor. | Conecta la implementación HTTP con el cliente configurado. |
| 10 | `cliente/VehiculoClientRest.java` — `buscar` | Ejecutar `GET /api/vehiculos/{id}`, devolver `Vehiculo` y traducir 404 y errores de conexión. | Implementa la consulta al catálogo y RN-06/RN-07. |
| 11 | `servicio/ReservaRepository.java` — `guardar` | Asignar un identificador correlativo, guardar y devolver la reserva con su ID. | Persiste la reserva en el almacenamiento en memoria. |
| 12 | `servicio/ReservaRepository.java` — `tieneReservaActiva` | Detectar si ya existe una reserva para el vehículo. | Permite hacer cumplir RN-05. |
| 13 | `servicio/ReservaRepository.java` — `listar` | Devolver las reservas como vista de solo lectura. | Soporta la consulta de reservas y el endpoint GET. |
| 14 | `servicio/ReservaService.java` — constructor y dependencias | Guardar e inyectar `VehiculoClient` y `ReservaRepository`; depender de la interfaz, no del cliente HTTP concreto. | Mantiene el desacoplamiento entre reglas de negocio y transporte. |
| 15 | `servicio/ReservaService.java` — `crear` | Implementar validación, consulta del vehículo, verificaciones de disponibilidad y duplicidad, cálculo y guardado. | Orquesta el flujo de creación y aplica RN-01, RN-04 y RN-05. |
| 16 | `servicio/ReservaService.java` — `listar` | Devolver las reservas del repositorio. | Proporciona los datos del endpoint GET. |
| 17 | `servicio/ReservaService.java` — `calcularTotal` | Calcular tarifa × días y aplicar el 10 % de descuento desde 7 días; usar constantes. | Implementa RN-02 y RN-03. |
| 18 | `servicio/ReservaService.java` — `validar` | Exigir cliente y días entre 1 y 30; lanzar `IllegalArgumentException` si los datos no son válidos. | Implementa RN-01 antes de consultar el catálogo. |
| 19 | `web/ReservaController.java` — clase y constructor | Anotar como `@RestController`, establecer `/api/reservas` e inyectar `ReservaService`. | Publica la ruta base y conecta la API con la lógica de negocio. |
| 20 | `web/ReservaController.java` — `crear` | Añadir `@PostMapping` y `@RequestBody`; devolver 201 o traducir las excepciones a los estados especificados. | Implementa el contrato HTTP para crear reservas y los errores de RN-01/RN-04–RN-07. |
| 21 | `web/ReservaController.java` — `listar` | Añadir `@GetMapping` y devolver la lista del servicio. | Implementa el endpoint de consulta. |

### Impacto de los pendientes

En el estado actual, la API no tiene los mapeos REST previstos porque `ReservaController` todavía no tiene las anotaciones del TODO 19. Además, `VehiculoClientRest` es un componente cuya dependencia `RestClient` no puede resolverse mientras no exista el bean del TODO 8; esto puede impedir que Spring inicie la aplicación. Los métodos pendientes con `UnsupportedOperationException` tampoco ejecutan el flujo previsto.

El repositorio está diseñado para guardar datos en memoria; por tanto, no ofrece persistencia duradera entre reinicios.

## Validador y verificación

`src/main/java/com/nttdata/ms_reservas/validacion/Validador.java` contiene 14 verificaciones para este microservicio y **no debe modificarse**. Utiliza `CatalogoFalso`, que devuelve vehículos simulados —incluidos casos de vehículo no disponible, inexistente y catálogo caído—, por lo que puede validar `ms-reservas` sin que `ms-vehiculos` esté ejecutándose.

Cuando un método verificado todavía lanza `UnsupportedOperationException`, el Validador lo informa como `[X]` y muestra el TODO correspondiente. El puntaje global indicado por el proyecto es `ms-vehiculos (6) + ms-reservas (14) = 20`.

## Ejemplos de cálculo

| Vehículo | Tarifa diaria | Días | Cálculo | Total esperado |
|---|---:|---:|---|---:|
| SEDAN | 120 | 3 | 120 × 3 | 360 |
| SEDAN | 120 | 7 | 120 × 7 × 0.90 | 756 |
| SUV | 200 | 10 | 200 × 10 × 0.90 | 1800 |

Las tarifas y los resultados anteriores corresponden a los escenarios descritos por el README y el Validador del proyecto.