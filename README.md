# Segmentación de clientes del sector cosmético (ADCMLSC)

MVP: API REST que carga un historial de compras, lo valida y segmenta a los clientes con Machine Learning (K-means) para apoyar decisiones comerciales.

## Estado de las funcionalidades

| Funcionalidad | Estado |
|---|---|
| Cargar y validar archivo de compras (HU-01, HU-02) | Implementado |
| Ejecutar segmentación (HU-05) | Implementado |
| Consultar segmentos (HU-06) | Implementado |
| Descripciones con IA | Pendiente |

## Limitaciones conocidas del MVP

- Una sola técnica de segmentación (K-means) y siempre con las tres variables (frecuencia, valor total y recencia).
- El número de grupos (k) va de 2 a 10, con 4 por defecto.
- Las etiquetas de los segmentos se asignan con reglas fijas y explícitas, no con IA.
- Sin autenticación (RNF-01): futuro.