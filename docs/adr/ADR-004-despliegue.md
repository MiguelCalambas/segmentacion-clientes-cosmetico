# ADR-004: Despliegue en Render con PostgreSQL gestionado

- Contexto: la aplicación solo corría en el equipo local con Docker Compose. Para
  la sustentación hace falta una URL pública que el docente pueda abrir sin
  instalar nada. El presupuesto es cero y el proyecto ya tiene un Dockerfile.
- Decisión: desplegar en Render como servicio web con Docker (plan gratuito),
  junto con una base PostgreSQL gestionada de Render, en la misma región para
  usar su red privada. La configuración, incluidas las credenciales y la llave
  de Gemini, entra por variables de entorno y nunca se versiona. El puerto se
  toma de la variable PORT (server.port=${PORT:8080}). Cada cambio fusionado en
  main se redespliega automáticamente.
- Alternativas: Railway (crédito limitado de $5 el primer mes y luego $1
  mensual), Fly.io (sin plan gratuito para cuentas nuevas y exige tarjeta) y
  seguir solo en local, que no permite compartir una URL.
- Consecuencias: ventajas: sin costo ni tarjeta, reutiliza el Dockerfile,
  esquema creado por Flyway al arrancar y publicación automática desde main.
  Costos: el servicio gratuito se duerme tras 15 minutos sin tráfico y la
  primera visita tarda cerca de un minuto; la base gratuita expira a los 30
  días; la URL es pública y no tiene autenticación (decisión Won't del
  backlog), por lo que no deben cargarse datos reales de clientes.