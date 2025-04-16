# Etapa de construcción
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Copiar el código fuente
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
COPY system.properties .

# Dar permisos de ejecución al script de Maven Wrapper
RUN chmod +x ./mvnw

# Compilar el proyecto
RUN ./mvnw install -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]