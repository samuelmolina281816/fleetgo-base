# ms-vehiculos — Catálogo de vehículos

Expone el catálogo de FleetGo. Es el **dueño de los datos de vehículos**: los demás servicios
los obtienen **solo a través de esta API**.

| Dato | Valor |
|---|---|
| Puerto | `8081` |
| URL base | `http://localhost:8081` |
| Clase para ejecutar | `src/main/java/com/nttdata/ms_vehiculos/MsVehiculosApplication.java` |
| Validador | `src/main/java/com/nttdata/ms_vehiculos/validacion/Validador.java` (6 puntos) |

## 1. Ejecutar
1. Abre el proyecto en IntelliJ IDEA (JDK 17) y espera a que Maven descargue las dependencias.
2. Ejecuta `MsVehiculosApplication` con el botón ▶. En la consola debe aparecer `Tomcat started on port 8081`.

## 2. Datos del catálogo
| id | placa | categoria | tarifaDia | disponible |
|---|---|---|---|---|
| 1 | ABC-123 | SEDAN | 120.0 | true |
| 2 | XYZ-789 | SUV | 200.0 | true |
| 3 | TRK-456 | CAMIONETA | 300.0 | false |

## 3. Probar en Postman
**Opción rápida:** en Postman pulsa **Import** y selecciona `ms-vehiculos.postman_collection.json` (esta carpeta).

**Opción manual:** crea cada petición con **New > HTTP**. Las peticiones GET no llevan Body.

### 3.1 Listar vehículos
| Campo | Valor |
|---|---|
| Método | `GET` |
| URL | `http://localhost:8081/api/vehiculos` |

Esperado — **200 OK**
```json
[
  { "id": 1, "placa": "ABC-123", "categoria": "SEDAN",     "tarifaDia": 120.0, "disponible": true },
  { "id": 2, "placa": "XYZ-789", "categoria": "SUV",       "tarifaDia": 200.0, "disponible": true },
  { "id": 3, "placa": "TRK-456", "categoria": "CAMIONETA", "tarifaDia": 300.0, "disponible": false }
]
```

### 3.2 Obtener un vehículo
| Campo | Valor |
|---|---|
| Método | `GET` |
| URL | `http://localhost:8081/api/vehiculos/1` |

Esperado — **200 OK**
```json
{ "id": 1, "placa": "ABC-123", "categoria": "SEDAN", "tarifaDia": 120.0, "disponible": true }
```

### 3.3 Vehículo inexistente
`GET http://localhost:8081/api/vehiculos/9` → **404 Not Found** (sin cuerpo).

## 4. Validar tu avance
Ejecuta `validacion/Validador.java` con el botón ▶ (no necesita que el servicio esté encendido).
Muestra 6 verificaciones y el puntaje de este microservicio.

## 5. Problemas frecuentes
| Problema | Solución |
|---|---|
| `Port 8081 was already in use` | Detén la ejecución anterior (■ en Run) o cambia `server.port`. |
| Postman: `Error: connect ECONNREFUSED` | El microservicio no está en ejecución. |
