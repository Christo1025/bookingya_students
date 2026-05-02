# Guia de pruebas del taller: TDD, BDD y ATDD

Este documento resume como estan organizadas las pruebas del proyecto BookingYa y como ejecutar cada fase del taller.

El escenario funcional evaluado es la gestion de reservas dentro de una plataforma digital. Las funcionalidades principales probadas son:

- Creacion de una reserva.
- Actualizacion de una reserva existente.
- Eliminacion o cancelacion de una reserva.
- Consulta de todas las reservas.
- Obtencion de una reserva por ID.
- Consulta de reservas por usuario/huesped.
- Validacion de reglas de negocio asociadas a la capacidad de la habitacion.

## Requisitos previos

Antes de ejecutar las pruebas, se recomienda tener instalado:

- Java 17.
- Docker Desktop.
- Node.js y npm, para la fase ATDD con Playwright.
- Maven Wrapper incluido en el proyecto: `mvnw.cmd` en Windows.

Tambien debe existir un archivo `.env` en la raiz del proyecto. Ejemplo:

```env
HOST_DB=127.0.0.1
PORT_DB=5432
POSTGRES_DB=bookingya
USERNAME_DB=postgres
PASSWORD_DB=tu_password
```

Cuando se ejecuta con Docker, la aplicacion usa el servicio `db` como host interno de PostgreSQL.

## Levantar el proyecto con Docker

Desde la raiz del proyecto:

```powershell
docker compose up -d --build
```

El archivo `docker-compose.yml` levanta dos servicios:

- `app`: aplicacion Spring Boot.
- `db`: base de datos PostgreSQL 15.

Puertos publicados:

```text
Aplicacion: http://localhost:8081/api
Swagger UI: http://localhost:8081/api/swagger-ui/index.html
PostgreSQL: localhost:5433
```

Se usa `8081` como puerto externo porque en algunos equipos `8080` puede estar ocupado por otro servidor local.

Para validar el estado de los contenedores:

```powershell
docker compose ps
```

Para ver logs de la aplicacion:

```powershell
docker compose logs -f app
```

Para detener los servicios:

```powershell
docker compose down
```

Si se quiere borrar tambien el volumen de la base de datos:

```powershell
docker compose down -v
```

## Fase 1: TDD

La fase TDD esta implementada con JUnit 5, Spring Boot Test y MockMvc.

Archivo principal:

```text
src/test/java/com/project/bookingya/tdd/ReservationControllerTest.java
```

Estas pruebas se enfocan en validar el comportamiento del controlador de reservas. Se usa `@WebMvcTest` para cargar una prueba liviana de la capa web y se mockea `ReservationService` con `@MockBean`.

Casos cubiertos:

- `shouldCreateReservation`: valida la creacion de una reserva.
- `shouldGetReservationById`: valida la consulta de una reserva por ID.
- `shouldUpdateReservation`: valida la actualizacion de una reserva existente.
- `shouldDeleteReservation`: valida la eliminacion de una reserva.
- `shouldValidateReservationById`: valida que los datos retornados por ID sean correctos.

Ejecutar pruebas TDD junto con las pruebas Java del proyecto:

```powershell
.\mvnw.cmd test
```

Ejecutar solo la clase TDD:

```powershell
.\mvnw.cmd -Dtest=ReservationControllerTest test
```

Resultado esperado:

```text
BUILD SUCCESS
```

## Fase 2: BDD con Serenity

La fase BDD esta implementada con Gherkin, Cucumber, Serenity BDD y Rest Assured.

Archivos principales:

```text
src/test/resources/features/bdd/reservation.feature
src/test/java/bdd/ReservationSteps.java
src/test/java/bdd/CucumberTest.java
serenity.conf
```

El archivo `.feature` describe los escenarios en lenguaje natural. La clase `ReservationSteps` contiene los pasos en Java que conectan cada sentencia `Given`, `When` y `Then` con codigo ejecutable. La clase `CucumberTest` ejecuta Cucumber con `CucumberWithSerenity`, lo que permite generar reportes HTML de Serenity.

En esta fase los pasos no usan mocks. Las pruebas consumen la API real con Rest Assured, por lo que Docker debe estar levantado antes de ejecutarlas.

Escenarios definidos:

- Crear una reserva correctamente.
- Obtener una reserva por ID.
- Consultar todas las reservas.
- Consultar reservas por huesped.
- Actualizar una reserva existente.
- Cancelar una reserva.

URL base por defecto:

```text
http://localhost:8081/api
```

Si se requiere otra URL:

```powershell
.\mvnw.cmd -Dbookingya.api.url=http://localhost:8081/api -Dtest=CucumberTest test serenity:aggregate
```

Ejecutar pruebas BDD junto con las pruebas Java:

```powershell
docker compose up -d --build
.\mvnw.cmd test
```

Ejecutar solo el runner BDD con Serenity:

```powershell
.\mvnw.cmd -Dtest=CucumberTest test serenity:aggregate
```

Resultado esperado:

```text
BUILD SUCCESS
```

Reporte Serenity:

```text
target/site/serenity/index.html
```

Para la sustentacion, esta fase evidencia BDD porque los escenarios estan escritos en Gherkin y son ejecutados por Cucumber; tambien evidencia Serenity porque el runner usa `CucumberWithSerenity` y genera el reporte HTML.


## Fase 3: ATDD

La fase ATDD esta implementada con Playwright y TypeScript.

Carpeta principal:

```text
src/test/atdd
```

Archivos principales:

```text
src/test/atdd/package.json
src/test/atdd/playwright.config.ts
src/test/atdd/tests/reservations.acceptance.spec.ts
```

Estas pruebas se ejecutan contra la API real levantada con Docker. A diferencia de las pruebas unitarias, aqui no se mockea el servicio; se consumen endpoints HTTP reales y se valida la respuesta completa desde la perspectiva del usuario final.

URL base configurada:

```text
http://localhost:8081/api/
```

Tambien se puede cambiar usando la variable de entorno:

```powershell
$env:BOOKINGYA_API_URL="http://localhost:8081/api/"
```

### Criterios de aceptacion cubiertos

El archivo `reservations.acceptance.spec.ts` automatiza dos escenarios de aceptacion.

Escenario 1: flujo completo de reserva

- Crear una habitacion disponible.
- Crear un huesped.
- Crear una reserva valida.
- Obtener la reserva por ID.
- Consultar todas las reservas.
- Consultar reservas por huesped.
- Actualizar la reserva.
- Cancelar la reserva.
- Verificar que la reserva cancelada ya no exista.

Escenario 2: regla de negocio de capacidad

- Crear una habitacion con capacidad maxima de 1 huesped.
- Crear un huesped.
- Intentar crear una reserva para 2 huespedes.
- Verificar que el sistema responda `400`.
- Verificar el mensaje `guestsCount exceeds room capacity`.

### Instalar dependencias ATDD

Desde la carpeta de ATDD:

```powershell
cd src/test/atdd
npm install
```

Esto instala las dependencias locales de Playwright y TypeScript. La carpeta `node_modules` no se debe subir al repositorio.

### Ejecutar pruebas ATDD

Primero asegurese de que Docker este arriba:

```powershell
docker compose up -d --build
```

Luego ejecute:

```powershell
cd src/test/atdd
npm test
```

Resultado esperado:

```text
2 passed
```

### Ver resultados ATDD

Playwright genera un reporte HTML en:

```text
src/test/atdd/playwright-report/index.html
```

Para abrirlo con el comando de Playwright:

```powershell
cd src/test/atdd
npm run report
```

Tambien quedan resultados temporales en:

```text
src/test/atdd/test-results
```

Estas carpetas estan ignoradas por Git porque son salidas generadas durante la ejecucion.

## Ejecucion recomendada para entrega

Desde la raiz del proyecto:

```powershell
docker compose up -d --build
.\mvnw.cmd -Dtest=ReservationControllerTest test
.\mvnw.cmd -Dbookingya.api.url=http://localhost:8081/api -Dtest=CucumberTest test serenity:aggregate
cd src/test/atdd
npm install
npm test
```

Con esto se validan:

- Fase 1: TDD con JUnit, Spring Boot Test y MockMvc.
- Fase 2: BDD con Gherkin, Cucumber, Serenity y Rest Assured.
- Fase 3: ATDD con Playwright y TypeScript contra la API real.

## Integracion continua con GitHub Actions

El repositorio incluye el workflow:

```text
.github/workflows/ci.yml
```

Este flujo se ejecuta automaticamente en cada `push` o `pull_request` hacia las ramas `main` y `estudiantes`.

El pipeline realiza estos pasos:

- Configura Java 17.
- Ejecuta las pruebas unitarias TDD con `ReservationControllerTest`.
- Crea un archivo `.env` temporal para el entorno de CI.
- Levanta la API y la base de datos con `docker compose up -d --build`.
- Espera hasta que la API responda en `http://localhost:8081/api/v3/api-docs`.
- Ejecuta los escenarios BDD con Serenity contra la API real.
- Verifica que el resumen de Serenity no tenga fallos, errores ni pruebas comprometidas.
- Publica el reporte HTML de Serenity como artefacto llamado `serenity-report`.
- Apaga los contenedores con `docker compose down -v`.

Esto evita que los escenarios BDD fallen por `Connection refused`, porque Serenity ya no se ejecuta antes de que la aplicacion este levantada.

## Evidencias sugeridas

Para la entrega o sustentacion se pueden mostrar:

- Salida de `.\mvnw.cmd -Dtest=ReservationControllerTest test` con `BUILD SUCCESS`.
- Historial de GitHub Actions en verde.
- Reporte Serenity en `target/site/serenity/index.html`.
- Artefacto `serenity-report` generado por GitHub Actions.
- Salida de `npm test` con `2 passed`.
- Captura de Swagger en `http://localhost:8081/api/swagger-ui/index.html`.
- Captura del reporte HTML de Playwright.
- Archivos de pruebas:
  - `ReservationControllerTest.java`.
  - `reservation.feature`.
  - `ReservationSteps.java`.
  - `reservations.acceptance.spec.ts`.
