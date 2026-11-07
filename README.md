# Segmentación de clientes del sector cosmético (ADCMLSC)

API REST que carga un historial de compras en CSV, lo valida y agrupa a los clientes con K-means según su comportamiento de compra (frecuencia, valor total y recencia). Las descripciones e ideas de campaña de cada grupo se redactan con una plantilla o, de forma opcional, con Gemini. El sistema apoya el análisis comercial: no ejecuta campañas ni garantiza resultados.

## Caso de uso del MVP

Cargar un archivo de compras, validarlo, segmentar a los clientes, consultar los segmentos y generar sus descripciones.

## Estado de las funcionalidades

| Funcionalidad | Estado |
|---|---|
| Cargar y validar archivo de compras (HU-01, HU-02) | Implementado |
| Ejecutar segmentación con K-means (HU-05) | Implementado |
| Consultar segmentos con etiqueta y criterio (HU-06) | Implementado |
| Descripciones e ideas de campaña (plantilla o Gemini) | Implementado |
| Documentación interactiva (Swagger UI) | Implementado |
| Contenedorización con Docker Compose | Implementado |

## Cómo ejecutar el proyecto

Requisito: Docker Desktop.

1. Copiar la configuración de ejemplo y elegir una contraseña local para la base de datos:
   ```
   Copy-Item .env.example .env
   ```
   Editar `POSTGRES_PASSWORD` en `.env` (este archivo no se sube a Git).
2. Levantar la base de datos y la aplicación:
   ```
   docker compose up --build -d
   ```
3. Abrir la documentación interactiva: http://localhost:8080/swagger-ui.html
4. Ejecutar la demostración completa (carga, segmentación y descripciones):
   ```
   powershell -ExecutionPolicy Bypass -File docs\demo.ps1
   ```

Para detener todo: `docker compose down` (con `-v` también borra los datos).

Ejecución sin Docker para la aplicación: `docker compose up -d db` y luego `.\mvnw.cmd spring-boot:run`.

## Configuración (variables de entorno)

| Variable | Para qué sirve | Valor por defecto |
|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Base de datos | Ver `.env.example` |
| `DB_URL` | Conexión a la base de datos al ejecutar fuera de Docker | `localhost` |
| `PROVEEDOR_IA` | `plantilla` (sin IA) o `gemini` | `plantilla` |
| `GEMINI_API_KEY`, `GEMINI_MODEL` | Clave y modelo de Gemini (solo si `PROVEEDOR_IA=gemini`) | vacíos |

El archivo `.env` está en `.gitignore` y `.dockerignore`: ninguna clave queda en el repositorio ni en la imagen.

## API

Documentación interactiva en `/swagger-ui.html` y especificación en `docs/openapi.json`. Guía con respuestas reales en `docs/api.md`.

| Método y ruta | Propósito | Códigos |
|---|---|---|
| `POST /api/v1/archivos` | Carga y valida un CSV de compras | 201, 400, 422 |
| `POST /api/v1/ejecuciones` | Inicia una segmentación (asíncrona) | 202, 404, 422 |
| `GET /api/v1/ejecuciones/{id}` | Consulta el estado de la ejecución | 200, 404 |
| `GET /api/v1/ejecuciones/{id}/segmentos` | Devuelve los segmentos | 200, 404, 409 |
| `POST /api/v1/ejecuciones/{id}/descripciones` | Genera descripciones e ideas de campaña | 200, 404, 409 |

Formato del CSV: columnas `id_compra`, `id_cliente`, `fecha_compra` (AAAA-MM-DD) y `valor_compra` (mayor que 0, con punto decimal). Ejemplos en `datos/`.

## Arquitectura

Arquitectura en capas con inversión de dependencias:

```
presentacion  ->  negocio  <-  infraestructura
(API REST)        (servicios,   (base de datos, K-means,
                   entidades,    lector CSV, Gemini)
                   interfaces)
```

La capa de negocio define las interfaces (`LectorCompras`, `Segmentador`, `AsistenteDescriptivo`, `LanzadorEjecuciones`) y la infraestructura las implementa, así que el negocio nunca depende de una tecnología concreta. Por eso se puede cambiar el proveedor de IA (se pasó de Anthropic a Gemini en el diseño) tocando solo una clase y la configuración.

## Tecnologías y justificación

| Tecnología | Por qué |
|---|---|
| Java 25 y Spring Boot 4 (Maven) | Lenguaje de mayor dominio del autor y estándar para APIs REST |
| PostgreSQL 17 y Flyway | Base de datos relacional que refleja el modelo de datos; Flyway versiona el esquema con migraciones |
| Spring Data JPA | Persistencia de las entidades del modelo |
| Apache Commons Math (K-means++) | Algoritmo de agrupamiento con pocas líneas de código |
| Apache Commons CSV | Lectura robusta de archivos CSV |
| springdoc-openapi | Genera Swagger UI desde el código |
| Docker y Docker Compose | Entorno reproducible con un solo comando |
| JUnit 5 | Pruebas unitarias y de integración |

## Decisiones de diseño

- **La segmentación corre dentro de la aplicación**; la IA externa solo redacta descripciones a partir de resúmenes agregados. Ningún dato individual de un cliente sale del sistema.
- **Ejecución asíncrona** con estados `en_proceso`, `completada` y `fallida`, que se consultan después.
- **Etiquetas de los segmentos con reglas explícitas**, y cada descripción incluye el criterio aplicado.
- **Recencia medida contra la fecha de la compra más reciente del archivo**, para que el resultado sea reproducible.
- **Respaldo por plantilla:** si el proveedor de IA falla o responde algo incompleto, se usa la plantilla y la respuesta lo indica (`respaldo: true`).
- **La llamada al proveedor se hace fuera de una transacción de base de datos.**
- **Clientes solo con identificador anónimo**, sin datos personales.
- Los repositorios de Spring Data viven en la capa de negocio (simplificación consciente).

## Limitaciones conocidas

- Una sola técnica de segmentación (K-means) y siempre con las tres variables.
- El número de grupos (k) va de 2 a 10, con 4 por defecto.
- Las descripciones generadas por el modelo son orientativas y pueden tener imprecisiones; por eso se conserva la descripción calculada con su criterio.
- Sin autenticación: cualquiera con acceso a la API puede usarla.
- Swagger UI está habilitado por defecto; en producción se desactivaría (`springdoc.swagger-ui.enabled=false`).
- La capa gratuita de Gemini puede usar el contenido enviado para mejorar los productos de Google; por eso solo se envían datos agregados y sintéticos.
- No hay interruptor de circuito para el proveedor de IA (solo tiempo límite, reintentos y respaldo).

## Pruebas

```
.\mvnw.cmd test
```

25 pruebas: validación de datos, lectura de CSV, K-means, etiquetado, respaldo de IA, adaptador de Gemini contra un servidor local simulado e integración con PostgreSQL. Las pruebas de Spring fuerzan el proveedor `plantilla` para no depender de la red ni consumir cuota.

## Estructura del proyecto

```
src/main/java/com/usc/segmentacion/
  presentacion/      controladores REST y DTO
  negocio/           servicios, entidades, interfaces y modelos
  infraestructura/   lector CSV, K-means, ejecución asíncrona e IA
src/main/resources/db/migration/   migraciones de Flyway
datos/               generador de datos sintéticos y archivos de ejemplo
docs/                trazabilidad, uso de IA y script de demostración
```

## Uso responsable de IA

Ver `docs/uso-ia.md`.