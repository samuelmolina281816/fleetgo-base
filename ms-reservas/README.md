# ms-reservas — Reservas de vehículos

Registra las reservas de FleetGo. Antes de confirmar **consulta el vehículo en ms-vehiculos por REST**
(`GET http://localhost:8081/api/vehiculos/{id}`), valida las reglas de negocio y calcula el total.

| Dato | Valor |
|---|---|
| Puerto | `8082` |
| URL base | `http://localhost:8082` |
| Depende de | `ms-vehiculos` en `http://localhost:8081` (propiedad `vehiculos.url`) |
| Clase para ejecutar | `src/main/java/com/nttdata/ms_reservas/MsReservasApplication.java` |
| Validador | `src/main/java/com/nttdata/ms_reservas/validacion/Validador.java` (14 puntos) |

## 1. Ejecutar
1. **Primero ejecuta ms-vehiculos** (puerto 8081).
2. Ejecuta `MsReservasApplication` con el botón ▶. Debe aparecer `Tomcat started on port 8082`.

## 2. Endpoints
| Método | URL | Descripción |
|---|---|---|
| `POST` | `http://localhost:8082/api/reservas` | Crea una reserva |
| `GET` | `http://localhost:8082/api/reservas` | Lista las reservas creadas |

## 3. Probar en Postman
**Opción rápida:** **Import** de `ms-reservas.postman_collection.json` (esta carpeta): trae los 8 escenarios.

**Opción manual:** crea la petición con **New > HTTP**:

| Campo | Valor |
|---|---|
| Método | `POST` |
| URL | `http://localhost:8082/api/reservas` |
| Pestaña **Body** | Selecciona **raw** y elige **JSON** (Postman agrega `Content-Type: application/json`) |

Luego cambia solo el **Body** en cada escenario:

### 3.1 Reserva confirmada
```json
{ "vehiculoId": 1, "cliente": "Ana Torres", "dias": 3 }
```
**201 Created** — 120.0 × 3 días
```json
{ "id": 1, "vehiculoId": 1, "placa": "ABC-123", "cliente": "Ana Torres", "dias": 3, "total": 360.0, "estado": "CONFIRMADA" }
```

### 3.2 Descuento por 7 días o más (RN-03)
```json
{ "vehiculoId": 2, "cliente": "Luis Paz", "dias": 7 }
```
**201 Created** — 200.0 × 7 = 1400.0, menos 10% → `"total": 1260.0`

### 3.3 Datos inválidos (RN-01)
```json
{ "vehiculoId": 1, "cliente": "Ana Torres", "dias": 0 }
```
**400 Bad Request** — `"estado": "DATOS INVALIDOS"`

### 3.4 Vehículo no disponible (RN-04)
```json
{ "vehiculoId": 3, "cliente": "Ana Torres", "dias": 2 }
```
**409 Conflict** — `"estado": "VEHICULO NO DISPONIBLE"`

### 3.5 Vehículo ya reservado (RN-05)
Repite el Body del escenario 3.1 con otro cliente.
**409 Conflict** — `"estado": "YA RESERVADO"`

### 3.6 Vehículo inexistente (RN-06)
```json
{ "vehiculoId": 9, "cliente": "Ana Torres", "dias": 3 }
```
**404 Not Found** — `"estado": "VEHICULO NO EXISTE"`

### 3.7 Catálogo no disponible (RN-07)
1. **Detén ms-vehiculos** (■ en Run).
2. Envía `{ "vehiculoId": 2, "cliente": "Ana Torres", "dias": 3 }`.

**503 Service Unavailable** — `"estado": "CATALOGO NO DISPONIBLE"`.
ms-reservas sigue funcionando aunque el catálogo esté caído: el fallo queda aislado.

### 3.8 Listar reservas
`GET http://localhost:8082/api/reservas` → **200 OK** con las reservas creadas.

## 4. Resumen de escenarios
| Body | Status | estado |
|---|---|---|
| `{"vehiculoId":1,"cliente":"Ana Torres","dias":3}` | 201 Created | CONFIRMADA (total 360.0) |
| `{"vehiculoId":2,"cliente":"Luis Paz","dias":7}` | 201 Created | CONFIRMADA (total 1260.0) |
| `{"vehiculoId":1,"cliente":"Ana Torres","dias":0}` | 400 Bad Request | DATOS INVALIDOS |
| `{"vehiculoId":3,"cliente":"Ana Torres","dias":2}` | 409 Conflict | VEHICULO NO DISPONIBLE |
| Repetir el vehículo 1 | 409 Conflict | YA RESERVADO |
| `{"vehiculoId":9,"cliente":"Ana Torres","dias":3}` | 404 Not Found | VEHICULO NO EXISTE |
| Con ms-vehiculos detenido | 503 Service Unavailable | CATALOGO NO DISPONIBLE |

## 5. Validar tu avance
Ejecuta `validacion/Validador.java` con el botón ▶. Usa un catálogo simulado, así que **no necesita
que ms-vehiculos esté en ejecución**. Muestra 14 verificaciones y el puntaje de este microservicio.

## 6. Problemas frecuentes
| Problema | Solución |
|---|---|
| `415 Unsupported Media Type` | En **Body** falta seleccionar **raw · JSON**. |
| `400 Bad Request` con datos correctos | El JSON del Body está mal escrito. |
| `503` con ms-vehiculos encendido | Revisa `vehiculos.url=http://localhost:8081` en `application.properties`. |
| `Parameter 0 of constructor ... required a bean of type 'RestClient'` | Falta el `@Bean` de `ClienteConfig` (TODO 8). |
