FROM navikt/java:11
COPY export-vault-secrets.sh /init-scripts/
RUN chmod +x /init-scripts/*
COPY /target/permitteringsskjema-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
