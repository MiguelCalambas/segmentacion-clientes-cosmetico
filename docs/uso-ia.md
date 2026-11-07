# Uso responsable de IA

## Herramientas
- Asistente de IA conversacional (Claude): guía del diseño, borradores de código y explicaciones.
- Gemini (API de Google): dentro de la aplicación, solo para redactar descripciones de segmentos a partir de datos agregados.

## Qué se pidió y qué se hizo con las sugerencias
- Propuestas de arquitectura (capas, hexagonal, etc.): comparo opciones y me brindo una claridad para seleccionar la opcion correcta.
- Cambio de proveedor de IA: el diseño original usaba Anthropic; decidí cambiar a Gemini por su capa gratuita.
- Código del lector de CSV, validador, K-means, servicios y endpoints: lo escribí con apoyo del asistente, lo probé y lo adapté.

## Correcciones y problemas que tuve que resolver
- El `.gitignore` tenía `.env/` (ignora una carpeta, no el archivo): lo corregí a `.env` y comprobé que nunca se subió una clave.
- Una clase quedó por error en `src/test` en lugar de `src/main`: la moví y la aplicación arrancó.
- Un archivo quedó vacío y no compilaba: lo recreé y lo verifiqué compilando.
- Una prueba de integración llamó a Gemini real porque leía el `.env`: la forcé a usar la plantilla para que no dependa de la red.
- Probé primero un modelo de Gemini de voz (TTS) por error: verifiqué en la respuesta que era de audio y elegí uno de texto.
- Los nombres de modelo y librerías cambian con frecuencia: los verifiqué en la documentación y los dejé configurables.

## Cómo verifiqué los resultados
- 25 pruebas automáticas (unitarias e integración).
- Pruebas manuales de cada endpoint con `curl` y Swagger, comprobando los códigos HTTP.
- Consultas a la base de datos para confirmar lo guardado (por ejemplo, 530 asignaciones, una por cliente).
- Comparación de las descripciones de Gemini contra las estadísticas calculadas: encontré una imprecisión (describía al segmento «Alto valor» con compras frecuentes, cuando su frecuencia es parecida al promedio). Por eso las descripciones del modelo se presentan como orientativas y se conserva la descripción calculada con su criterio.

## Datos y privacidad
- Los datos de prueba son sintéticos (`datos/GeneradorDatos.java`).
- A Gemini solo se le envían resúmenes agregados por segmento, nunca datos de clientes.
- La clave de la API solo esta en `.env`, que no se sube al repositorio ni se copia a la imagen de Docker.

## Comprensión del código
Seria el recorrido de una solicitud, las capas y la regla de dependencia, el manejo de errores y los códigos HTTP, la transacción de la carga, la ejecución asíncrona, el respaldo ante fallos de la IA y la gestión de secretos.