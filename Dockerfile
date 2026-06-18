FROM sugamflow-common-libs:local AS build
WORKDIR /workspace

COPY docker/maven-docker-settings.xml /root/.m2/settings.xml
COPY docker/mvn-package-retry.sh /usr/local/bin/mvn-package-retry.sh
COPY appointment-service ./appointment-service
RUN sed -i 's/\r$//' /usr/local/bin/mvn-package-retry.sh \
    && chmod +x /usr/local/bin/mvn-package-retry.sh \
    && sh /usr/local/bin/mvn-package-retry.sh appointment-service/pom.xml \
    && cp /workspace/appointment-service/target/*-SNAPSHOT.jar /workspace/appointment-service/app.jar

FROM sugamflow-jre:local
WORKDIR /app
COPY --from=build /workspace/appointment-service/app.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]