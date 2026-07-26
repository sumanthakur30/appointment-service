# Rebuild common-libs after shared-lib changes:
#   docker build -f docker/Dockerfile.common-libs -t sugamflow-common-libs:local .
FROM sugamflow-common-libs:local AS build
WORKDIR /workspace

COPY docker/maven-docker-settings.xml /root/.m2/settings.xml
COPY docker/mvn-package-retry.sh /usr/local/bin/mvn-package-retry.sh
COPY ipd-service ./ipd-service
RUN sed -i 's/\r$//' /usr/local/bin/mvn-package-retry.sh \
    && chmod +x /usr/local/bin/mvn-package-retry.sh \
    && sh /usr/local/bin/mvn-package-retry.sh ipd-service/pom.xml \
    && cp /workspace/ipd-service/target/*-SNAPSHOT.jar /workspace/ipd-service/app.jar

FROM sugamflow-jre:local
WORKDIR /app
COPY --from=build /workspace/ipd-service/app.jar app.jar
EXPOSE 8100
ENTRYPOINT ["java", "-jar", "app.jar"]
