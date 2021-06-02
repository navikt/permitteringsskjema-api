permitteringsskjema-api
================

Appen eksponerer API-et til skjemaet for melding om massepermittering, massenedbemanning og innskrenking av arbeidstid. 
Skjemaet lagres i databasen og publiseres på Kafka, og viderebehandles i Salesforce og Gosys.

Frontend til skjemaet ligger her: https://github.com/navikt/arbeidsgiver-permittering

# Komme i gang

Koden kan kjøres som en vanlig Spring Boot-applikasjon lokalt ved å starte `LokalApplikasjon`.
Åpnes i browser: http://localhost:8080/permitteringsskjema-api/swagger-ui.html

Default spring-profil er local, og da er alle avhengigheter mocket på localhost:8081.

## Docker
Bygg image
`docker build -t permitteringsskjema-api .`

Kjør container (returnerer en ID)
`docker run -d -p 8080:8080 permitteringsskjema-api`

Stopp container
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
