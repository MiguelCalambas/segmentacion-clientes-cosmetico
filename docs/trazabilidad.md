# Trazabilidad: requisito, diseño, implementación y prueba

| Requisito | Diseño | Implementación | Prueba |
|---|---|---|---|
| HU-01 Cargar historial | `POST /api/v1/archivos`; entidades Archivo y Compra | `ArchivoController`, `CargaArchivoServicio`, `LectorComprasCsv` | `LectorComprasCsvTest`; `curl` con `datos/` |
| HU-02 Validar calidad | Entidad Incidencia de calidad | `ValidadorCompras` | `ValidadorComprasTest` |
| HU-05 Ejecutar segmentación | `POST /api/v1/ejecuciones`; entidades Ejecución, Segmento y Asignación | `EjecucionController`, `EjecucionServicio`, `ProcesadorEjecucion`, `KMeansSegmentador` | `KMeansSegmentadorTest`; `SegmentacionIntegracionTest` |
| HU-06 Interpretar segmentos | `GET /api/v1/ejecuciones/{id}/segmentos` | `EtiquetadorSegmentos`, `EjecucionServicio.consultar` | `EtiquetadorSegmentosTest` |
| Descripciones con IA | Entregables 6 y 7: interfaz `AsistenteDescriptivo`, respaldo y flujo de interacción | `DescripcionServicio`, `AsistenteConRespaldo`, `AsistentePlantilla`, `ClienteIAGemini` | `AsistenteConRespaldoTest`, `ClienteIAGeminiTest`, `SegmentacionIntegracionTest` |
| RNF-02 Privacidad | Cliente solo con identificador; solo datos agregados a la IA | Entidad `Cliente`; `ResumenSegmento` | Revisión del esquema y de `ClienteIAGeminiTest` |
| RNF-05 Integridad | Errores sin resultados incompletos; estado `fallida`; respaldo ante fallos | `@Transactional`, `ManejadorErrores`, `ProcesadorEjecucion.marcarFallida`, `AsistenteConRespaldo` | Pruebas de errores con `curl`; `AsistenteConRespaldoTest` |
| RNF-06 Calidad de datos | Reporte de incidencias | `ValidadorCompras`, `IncidenciaCalidad` | `ValidadorComprasTest` |
| Configuración segura | Variables de entorno sin secretos | `.env.example`, `.gitignore`, `.dockerignore`, `docker-compose.yml` |
