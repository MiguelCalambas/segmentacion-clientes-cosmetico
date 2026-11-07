# ---------- Etapa 1: compilar la aplicación ----------
FROM eclipse-temurin:25-jdk AS compilacion
WORKDIR /app

# Primero solo lo necesario para descargar dependencias (se guarda en caché)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B -q dependency:go-offline

# Después el código fuente
COPY src/ src/
RUN ./mvnw -B -q -DskipTests package

# ---------- Etapa 2: ejecutar la aplicación ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Usuario sin privilegios: la aplicación no corre como administrador
RUN useradd --system --no-create-home --shell /usr/sbin/nologin appuser

COPY --from=compilacion /app/target/*.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]