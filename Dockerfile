FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY security-common ./security-common
RUN mvn -f security-common/pom.xml -B -DskipTests install

COPY appointment-service ./appointment-service
RUN mvn -f appointment-service/pom.xml -B -DskipTests package && cp /workspace/appointment-service/target/*-SNAPSHOT.jar /workspace/appointment-service/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/appointment-service/app.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]
