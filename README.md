permitteringsskjema-api
================

Appen eksponerer API-et til skjemaet for melding om massepermittering, massenedbemanning og innskrenking av arbeidstid. 
Skjemaet lagres i databasen og publiseres på Kafka, og viderebehandles i Salesforce og Gosys.

Frontend til skjemaet ligger her: https://github.com/navikt/arbeidsgiver-permittering

# Komme i gang

Koden kan kjøres som en vanlig Spring Boot-applikasjon lokalt ved å starte `LokalApplikasjon`.
Åpnes i browser: http://localhost:8080/permitteringsskjema-api/swagger-ui.html

Hvis du vil kjøre applikasjonen med Kafka, se neste punkt.

## Kafka
Appen kan kjøres med lokal Kafka satt opp i `docker-compose.yaml`. Du må i så fall ha Docker og Kafka installert lokalt.

 - Start kafka: `docker-compose up`

 - Skriv meldinger fra topic ut i konsollen: 
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic permittering-og-nedbemanning.aapen-permittering-arbeidsgiver --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true
```


## Docker
 - Bygg image:
`docker build -t permitteringsskjema-api .`

 - Kjør container (returnerer en ID):
`docker run -d -p 8080:8080 permitteringsskjema-api`

 - Stopp container:
`docker stop <ID>`

## Koble til H2-database lokalt
TODO

## Grafisk fremstilling av API-ene (swagger-ui)
API-et kan sees og testes på `http://localhost:8080/permitteringsskjema-api/swagger-ui.html`

---------

# Henvendelser

## For Nav-ansatte
TODO

## For folk utenfor Nav
TODO
