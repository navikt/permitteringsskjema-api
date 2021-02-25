FROM navikt/java:11
COPY --chown=apprunner:root export-vault-secrets.sh /init-scripts/
COPY --chown=apprunner:root /target/*.jar app.jar
EXPOSE 8080
