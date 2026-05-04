FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN test -f src/main/resources/db/migration/V2__add_trip_distance_km.sql \
    && test -f src/main/resources/db/migration/V3__add_app_users.sql \
    && mvn -B -q -DskipTests package \
    && jar tf target/nmichail-0.0.1-SNAPSHOT.jar | grep -q 'BOOT-INF/classes/db/migration/V2__add_trip_distance_km.sql' \
    && jar tf target/nmichail-0.0.1-SNAPSHOT.jar | grep -q 'BOOT-INF/classes/db/migration/V3__add_app_users.sql'

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring --home-dir /app spring
COPY --from=build /app/target/nmichail-0.0.1-SNAPSHOT.jar app.jar
RUN chown spring:spring app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]