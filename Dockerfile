FROM navikt/java:11
COPY export-vault-secrets.sh /init-scripts/
COPY /target/*.jar app.jar
EXPOSE 8080
