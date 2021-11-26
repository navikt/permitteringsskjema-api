FROM navikt/java:17
COPY --chown=apprunner:root /target/*.jar app.jar
EXPOSE 8080
