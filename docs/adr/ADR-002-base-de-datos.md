# ADR-002: PostgreSQL y Docker Compose

- Contexto: los datos (archivos, clientes, compras, ejecuciones y segmentos)
  son relacionales y el entorno debe ser reproducible en cualquier equipo.
- Decisión: PostgreSQL 17 con migraciones Flyway, junto con la aplicación en
  Docker Compose.
- Alternativas: MariaDB, y H2 en memoria.
- Consecuencias: ventajas: integridad relacional, esquema versionado y arranque
  con un solo comando. Costos: requiere Docker y mantener un volumen para no
  perder los datos.