# ADR-001: Java y Spring Boot

- Contexto: necesitaba una API REST con validación, acceso a base de datos y
  documentación, y desarrollada por una sola persona que domina Java.
- Decisión: Java 25 con Spring Boot, Spring Data JPA y springdoc (Swagger).
- Alternativas: Python con FastAPI, y Node.js con Express.
- Consecuencias: ventajas: dominio del lenguaje, validación, inyección de
  dependencias y documentación automática. Costos: más código repetitivo y
  arranque más lento que en FastAPI.