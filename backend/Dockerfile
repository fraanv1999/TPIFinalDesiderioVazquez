# Imagen del backend. Se construye desde la raíz del repo porque el jar incluye database/*.sql:
#   docker build -f backend/Dockerfile .
# Render no tiene runtime Java nativo: el servicio se despliega con esta imagen (ver render.yaml).

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml backend/
# Las dependencias quedan en una capa propia: si solo cambia el código, no se vuelven a bajar.
RUN mvn -f backend/pom.xml -B -q dependency:go-offline
COPY database database
COPY backend/src backend/src
# Las pruebas corren aparte (./mvnw test): necesitan los Excel de ejemplo y un PostgreSQL embebido.
RUN mvn -f backend/pom.xml -B -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
