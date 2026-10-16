# BarberBook — Backend

API REST de BarberBook, la plataforma de reservas para barberías (curso *Análisis y Diseño de Sistemas*, UdeA).
Atiende al front React: registro e inicio de sesión, catálogo de servicios, estilistas, consulta de
disponibilidad, reservas, pantalla de inicio, notificaciones y administración.

**Stack:** Java 17 · Spring Boot 4.1 · Spring Security (JWT RS256, sin sesión) · Spring Data JPA / Hibernate ·
PostgreSQL · Maven · Lombok.

## Historias de usuario cubiertas

| HU | Qué hace |
|---|---|
| HU-01 · HU-02 · HU-03 | Registro, inicio de sesión (con bloqueo por intentos) y cierre de sesión con revocación del token |
| HU-09 | El administrador crea estilistas con su horario semanal |
| HU-07 | El administrador gestiona el catálogo de servicios |
| HU-12 | Disponibilidad de horarios por estilista (o "cualquiera") y fecha |
| HU-13 | Confirmación de la reserva, con protección contra reservas simultáneas |
| HU-06 | Datos de la pantalla de inicio: populares, recomendados, estilistas, perfil, notificaciones y búsqueda |
| HS-01 | Privilegio mínimo: denegación por defecto, rol verificado en la base y bitácora de auditoría |

La referencia de cada endpoint (rutas, ejemplos de petición y respuesta, errores) está en la
[Guía de Integración Frontend](https://claude.ai/code/artifact/133bf25f-f7f9-4a7a-8d33-d0014380bbc8).

## Requisitos

- JDK 17 o superior
- PostgreSQL (probado con 18) y una base de datos vacía, por ejemplo `barberbook_db`
- OpenSSL (para generar las llaves del JWT)

Maven no hace falta instalarlo: el proyecto trae `mvnw`.

## Puesta en marcha local

1. **Base de datos.** Crear la base `barberbook_db`. Las tablas las crea Hibernate al arrancar (`ddl-auto=update`).

2. **Llaves JWT** (no se versionan; cada entorno genera las suyas):

   ```bash
   mkdir -p src/main/resources/keys
   openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out src/main/resources/keys/private_key.pem
   openssl rsa -pubout -in src/main/resources/keys/private_key.pem -out src/main/resources/keys/public_key.pem
   ```

3. **Variables de entorno** (ver la tabla de abajo). Como mínimo la clave de la base de datos:

   ```bash
   # PowerShell
   $env:DB_PASSWORD = "tu_clave"
   # Bash
   export DB_PASSWORD=tu_clave
   ```

4. **Arrancar:**

   ```bash
   ./mvnw spring-boot:run
   ```

   La API queda en `http://localhost:8080/api/v1`.

5. **Primer administrador.** Si se definen `SEED_ADMIN_EMAIL` y `SEED_ADMIN_PASSWORD`, al arrancar se crea un
   usuario `ADMIN` (solo si ese correo aún no existe). Con él se inicia sesión y se crean servicios y estilistas.

## Variables de entorno

| Variable | Obligatoria | Por defecto | Para qué sirve |
|---|---|---|---|
| `DB_PASSWORD` | Sí, en local | — | Clave de PostgreSQL |
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/barberbook_db` | URL JDBC |
| `DB_USERNAME` | No | `postgres` | Usuario de PostgreSQL |
| `DATABASE_URL` | En Render | — | Base de datos en formato `postgres://usuario:clave@servidor:puerto/base`. Si existe, reemplaza a `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` |
| `PORT` | No | `8080` | Puerto HTTP (Render lo inyecta) |
| `FRONTEND_ORIGINS` | En producción | `http://localhost:8443,http://localhost:5173` | Orígenes del front permitidos por CORS, separados por coma. Debe incluir la URL exacta del front desplegado |
| `JWT_PRIVATE_KEY` / `JWT_PUBLIC_KEY` | En Render | — | Las llaves como texto: el `.pem` completo, o el `.pem` codificado en base64 (una sola línea). Tienen prioridad sobre las rutas |
| `JWT_PRIVATE_KEY_PATH` / `JWT_PUBLIC_KEY_PATH` | No | `classpath:keys/...` | Ruta a los archivos de las llaves, por ejemplo `file:/etc/secrets/private_key.pem` |
| `SEED_ADMIN_EMAIL` / `SEED_ADMIN_PASSWORD` / `SEED_ADMIN_NAME` | No | vacío | Crea el primer administrador |
| `BUSINESS_ZONE` | No | `America/Bogota` | Zona horaria del negocio (define "hoy" y la anticipación mínima) |
| `BUSINESS_CLOSE_TIME` | No | `19:00` | Hora de cierre: ningún servicio puede terminar después |
| `EMAIL_PROVIDER` | No | `log` | `log` solo escribe los correos en el registro del servidor. `brevo` o `resend` envían correos reales por la API HTTP del proveedor (Render gratis bloquea SMTP) |
| `EMAIL_API_KEY` / `EMAIL_FROM` | Con proveedor real | — | Clave de la API y correo remitente, que el proveedor debe tener verificado |
| `EMAIL_FROM_NAME` | No | `BarberBook` | Nombre que ve quien recibe el correo |
| `BUSINESS_ADDRESS` | No | texto provisional | Dirección del local que lleva el correo de confirmación |
| `RECOMMENDATION_MIN_BOOKINGS` | No | `10` | Reservas necesarias para ver "Recomendado para ti" |
| `FORWARD_HEADERS_STRATEGY` | En Render | `none` | Poner `framework` detrás del proxy de Render para registrar la IP real en la bitácora |
| `JPA_SHOW_SQL` | No | `false` | `true` imprime el SQL en el log (solo para depurar) |
| `GOOGLE_CLIENT_ID` | No | vacío | Habilita `POST /auth/google` (sin esto responde 503) |

## Roles y seguridad

- Roles: `CLIENTE`, `ESTILISTA`, `ADMIN`. La API no guarda sesión: cada petición lleva `Authorization: Bearer <token>`.
- El rol se lee de la base de datos en cada petición, no del token; una cuenta degradada o deshabilitada pierde
  acceso de inmediato.
- Denegación por defecto: toda ruta que no esté listada en `SecurityConfig` se rechaza.
- `401` = sin sesión válida. `403` = hay sesión pero el rol no alcanza; ese intento queda en la tabla `audit_logs`
  (consultable por el admin en `GET /api/v1/admin/audit-logs`).

## Reglas de negocio a tener presentes

- Los turnos se ofrecen cada **15 minutos**, con **30 minutos** de anticipación mínima el mismo día.
- Un servicio debe terminar antes del cierre (`BUSINESS_CLOSE_TIME`) y dentro del turno del estilista.
- Un estilista **sin servicios asignados no aparece** en las reservas: asignarlos con
  `PUT /api/v1/admin/stylists/{id}/services`.
- Las reservas simultáneas se serializan bloqueando la fila del estilista en la base de datos; la segunda recibe
  `409` con `code: "SLOT_TAKEN"`.
- Los precios son pesos colombianos enteros y la cita guarda el precio vigente al reservar.
- Los correos (confirmación de cita y credenciales del estilista) se envían de forma asíncrona: un fallo del
  proveedor nunca retrasa la API ni deshace una reserva. Sin `EMAIL_PROVIDER` quedan solo en el log.
  Nota: con Resend gratuito solo se entrega al correo de la cuenta, salvo que se verifique un dominio propio.

## Estructura

```
com.udea.barberbook.barberbook_backend
├── auth/          registro, login, logout, Google
├── user/          perfil (/users/me)
├── stylist/       estilistas, horarios, servicios que ofrecen
├── catalog/       servicios del catálogo
├── availability/  cálculo de horas libres
├── booking/       citas y confirmación de reservas
├── home/          populares y recomendados
├── notification/  campana de notificaciones
├── audit/         bitácora de auditoría
├── security/      JWT, filtro, manejo de 401 y 403
├── config/        seguridad, CORS, reloj, administrador inicial
└── common/        excepciones y correo
```

Cada módulo sigue `controller → service → repository` con sus DTO y entidades.

## Docker

```bash
docker build -t barberbook-backend .
docker run -p 8080:8080 -e DB_URL=... -e DB_PASSWORD=... -e JWT_PRIVATE_KEY_PATH=... -e JWT_PUBLIC_KEY_PATH=... barberbook-backend
```

La imagen no incluye las llaves JWT (están en `.dockerignore`); hay que montarlas como archivos.

## Despliegue en Render

El archivo `render.yaml` (Blueprint) describe la base de datos y el servicio web, así que se crea todo de una vez:

1. En Render: **New → Blueprint**, elegir este repositorio y la rama.
2. Render pide cinco valores: `FRONTEND_ORIGINS` (URL exacta del front, sin barra final), `SEED_ADMIN_EMAIL`,
   `SEED_ADMIN_PASSWORD`, `JWT_PRIVATE_KEY` y `JWT_PUBLIC_KEY` (las llaves en base64, de un par generado solo
   para producción; ver *Puesta en marcha*).
3. Render construye la imagen Docker, conecta la base por `DATABASE_URL` y comprueba `GET /api/v1/health`.

Notas del plan gratuito: el servicio se duerme tras 15 minutos sin tráfico (la primera petición tarda cerca de
1 minuto) y la base de datos gratuita expira a los 30 días de creada.
