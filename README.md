# FleetGo — Taller de Microservicios (proyecto base)

Proyecto base del **Taller 3: Conceptos de Microservicios**. Completa los `// TODO` siguiendo el
documento del taller.

| Microservicio | Puerto | Responsabilidad |
|---|---|---|
| `ms-vehiculos` | 8081 | Catálogo de vehículos |
| `ms-reservas` | 8082 | Reservas; consulta el catálogo por REST |

## Requisitos
- JDK 17 · IntelliJ IDEA · Postman
- Conexión a internet la primera vez (descarga de dependencias de Maven)

## Abrir en IntelliJ IDEA
Cada microservicio es un **proyecto independiente**. Ábrelos en ventanas separadas:
**File > Open…** sobre la carpeta `ms-vehiculos`, y otra vez sobre `ms-reservas` (**New Window**).

## Qué está resuelto y qué debes completar
| Archivo | Estado |
|---|---|
| `ms-vehiculos/.../Vehiculo.java` | Resuelto (contrato) |
| `ms-vehiculos/.../VehiculoController.java` | TODO 1 a 4 |
| `ms-vehiculos/.../application.properties` | TODO 5 |
| `ms-reservas/.../model/*` | Resuelto (contrato) |
| `ms-reservas/.../application.properties` | TODO 6 y 7 |
| `ms-reservas/.../config/ClienteConfig.java` | TODO 8 |
| `ms-reservas/.../cliente/VehiculoClient.java` | Resuelto (interfaz) |
| `ms-reservas/.../cliente/VehiculoClientRest.java` | TODO 9 y 10 |
| `ms-reservas/.../servicio/ReservaRepository.java` | TODO 11 a 13 |
| `ms-reservas/.../servicio/ReservaService.java` | TODO 14 a 18 |
| `ms-reservas/.../web/ReservaController.java` | TODO 19 a 21 |
| `validacion/Validador.java` (ambos) | Resuelto · **no modificar** |

## Cómo validar tu avance
Ejecuta el `Validador` de cada microservicio con el botón ▶:

| Validador | Puntos |
|---|---|
| `ms-vehiculos/.../validacion/Validador.java` | 6 |
| `ms-reservas/.../validacion/Validador.java` | 14 |
| **Total** | **20** |

Al inicio ambos dan 0: el puntaje sube a medida que completas los TODO.
El Validador de reservas usa un catálogo simulado, así que funciona sin ms-vehiculos encendido.

Cuando ambos estén al 100%, ejecuta los dos microservicios y prueba los escenarios en Postman
(ver el README de cada carpeta).
