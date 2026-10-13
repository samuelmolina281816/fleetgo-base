# Análisis del proyecto FleetGo — `ms-vehiculos`

## 1. Contexto y objetivo

FleetGo se plantea como un sistema compuesto por dos microservicios independientes:

- **`ms-vehiculos`**: catálogo y fuente de verdad de los datos de vehículos.
- **`ms-reservas`**: gestiona las reservas y consulta el catálogo mediante REST.

El objetivo general es que ambos servicios apliquen sus reglas de negocio, se comuniquen por HTTP y respondan adecuadamente cuando el otro servicio no está disponible. Los objetivos del sistema incluyen exponer APIs REST con códigos HTTP correctos, consumir el otro servicio con `RestClient` y una URL configurable, aislar el detalle HTTP tras una interfaz y traducir fallos de comunicación en respuestas claras.

Este proyecto corresponde al microservicio de catálogo. En el estado actual, el código encontrado solo incluye la aplicación Spring Boot, el modelo `Vehiculo` y un controlador incompleto. No contiene la implementación de `ms-reservas`.

## 2. Tecnologías y configuración observadas

| Elemento | Detalle |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Dependencia web | `spring-boot-starter-web` |
| Servidor | Spring Boot con Tomcat embebido |
| Nombre de aplicación | `ms-vehiculos` |
| Puerto previsto | `8081` — pendiente de configurar |
| Persistencia | No se observa; el catálogo previsto es una lista en memoria |
| Punto de entrada | `MsVehiculosApplication` |

La configuración actual de `application.properties` declara el nombre de la aplicación, pero no `server.port`. Por ello, el puerto 8081 documentado en el README y requerido por el validador todavía no queda establecido por esa configuración.

## 3. Estructura relevante

| Archivo | Responsabilidad |
|---|---|
| `src/main/java/com/nttdata/ms_vehiculos/MsVehiculosApplication.java` | Inicia la aplicación Spring Boot. |
| `src/main/java/com/nttdata/ms_vehiculos/Vehiculo.java` | Record que define el contrato JSON de un vehículo. |
| `src/main/java/com/nttdata/ms_vehiculos/VehiculoController.java` | Punto previsto para los endpoints REST del catálogo; actualmente incompleto. |
| `src/main/resources/application.properties` | Configuración de la aplicación; falta fijar el puerto. |
| `src/main/java/com/nttdata/ms_vehiculos/validacion/Validador.java` | Verificaciones automáticas del microservicio, valor total de 6 puntos. No modificar. |
| `README.md` | Documenta datos previstos, endpoints y ejecución. |
| `ms-vehiculos.postman_collection.json` | Colección de peticiones para probar los endpoints documentados. |

## 4. Modelo de dominio

`Vehiculo` es un `record` con los siguientes campos:

| Campo | Tipo | Significado |
|---|---|---|
| `id` | `Long` | Identificador del vehículo. |
| `placa` | `String` | Placa del vehículo. |
| `categoria` | `String` | Categoría, por ejemplo `SEDAN` o `SUV`. |
| `tarifaDia` | `double` | Tarifa diaria. |
| `disponible` | `boolean` | Indica si está disponible. |

El README define el catálogo inicial esperado:

| id | placa | categoria | tarifaDia | disponible |
|---:|---|---|---:|---|
| 1 | `ABC-123` | `SEDAN` | 120.0 | `true` |
| 2 | `XYZ-789` | `SUV` | 200.0 | `true` |
| 3 | `TRK-456` | `CAMIONETA` | 300.0 | `false` |

## 5. API prevista y flujo

### Endpoints

| Método | Ruta | Resultado esperado |
|---|---|---|
| `GET` | `/api/vehiculos` | `200 OK` con los tres vehículos, en el orden documentado. |
| `GET` | `/api/vehiculos/{id}` | `200 OK` con el vehículo si existe; `404 Not Found` si no existe. |

El README especifica que la respuesta del caso inexistente no lleva cuerpo.

### Flujo del microservicio de vehículos

1. Un cliente —por ejemplo `ms-reservas`— envía una petición HTTP al catálogo.
2. Spring enruta la petición al controlador bajo `/api/vehiculos`.
3. El controlador consulta la lista en memoria.
4. Spring serializa el `Vehiculo` o la lista como JSON.
5. Para un identificador no encontrado, el endpoint individual responde `404`.

En el estado observado, el controlador aún no tiene las anotaciones REST requeridas y sus métodos lanzan `UnsupportedOperationException`; por tanto, este flujo describe el comportamiento objetivo, no uno ya operativo.

### Interacción prevista con reservas

Como parte del sistema, `ms-reservas` debe consultar el catálogo por REST y manejar fallos del servicio remoto. La consigna indica que el validador de reservas usa un catálogo simulado, por lo que sus verificaciones pueden ejecutarse sin iniciar este microservicio. No se encontraron en este proyecto clases de cliente REST, interfaz de acceso remoto ni configuración de URL para reservas; son responsabilidades de la integración entre servicios y no deben atribuirse a la implementación actual de `ms-vehiculos`.

## 6. TODO pendientes en `ms-vehiculos`

La búsqueda del texto `TODO` en el proyecto encontró cinco pendientes:

| TODO | Ubicación | Trabajo pendiente | Verificación relacionada |
|---:|---|---|---|
| 1 | `VehiculoController.java`, declaración de clase | Anotar la clase con `@RestController` y `@RequestMapping("/api/vehiculos")`. | Validador 1: controlador REST y ruta base. |
| 2 | `VehiculoController.java`, campo `vehiculos` | Inicializar la lista con los tres vehículos documentados, en el orden indicado. Actualmente usa `List.of()` sin elementos. | Validador 2 verifica que se devuelvan tres elementos y comprueba datos del catálogo. |
| 3 | `VehiculoController.java`, método `listar()` | Añadir `@GetMapping` y devolver la lista completa. Actualmente lanza `UnsupportedOperationException("TODO 3")`. | Validador 2: método de listado y contenido. |
| 4 | `VehiculoController.java`, método `obtener(Long id)` | Mapear `GET /{id}`, enlazar el parámetro con `@PathVariable("id")` y responder `200` con el vehículo o `404` si no existe. Actualmente lanza `UnsupportedOperationException("TODO 4")`. | Validador 3: ruta y parámetro; validadores 4 y 5: respuestas para vehículo existente e inexistente. |
| 5 | `application.properties` | Configurar `server.port=8081`. Actualmente solo se declara `spring.application.name=ms-vehiculos`. | Validador 6: puerto 8081. |

## 7. Estado esperado del validador

`Validador.java` comprueba seis condiciones y asigna hasta 6 puntos a este microservicio. Según el código inspeccionado, las seis condiciones están pendientes en la implementación actual:

| N.º | Verificación del validador | Estado observado |
|---:|---|---|
| 1 | Controlador REST y ruta `/api/vehiculos`. | `[X]` Anotaciones ausentes. |
| 2 | `listar()` anotado y con tres vehículos. | `[X]` La lista está vacía y el método lanza `UnsupportedOperationException("TODO 3")`. |
| 3 | Ruta `/{id}` y `@PathVariable("id")`. | `[X]` Anotaciones ausentes. |
| 4 | Consulta del vehículo 1 devuelve `200` y `ABC-123`. | `[X]` `obtener()` lanza `UnsupportedOperationException("TODO 4")`. |
| 5 | Consulta del vehículo 9 devuelve `404`. | `[X]` `obtener()` lanza `UnsupportedOperationException("TODO 4")`. |
| 6 | Puerto configurado en 8081. | `[X]` Falta `server.port` en `application.properties`. |

Esta tabla es una evaluación estática a partir de los archivos leídos; no representa una ejecución del validador.

## 8. Alcance de puntuación del sistema

| Microservicio | Puntaje máximo |
|---|---:|
| `ms-vehiculos` | 6 |
| `ms-reservas` | 14 |
| **Total FleetGo** | **20** |

El alcance de este análisis y de los TODO enumerados es el proyecto `ms-vehiculos`. `Validador.java` se considera de solo lectura y no debe modificarse.