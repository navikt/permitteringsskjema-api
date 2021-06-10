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
Åpne H2-konsollen på `http://localhost:8080/permitteringsskjema-api/h2-console` og fyll inn det som står under `spring.datasource` i `application-local.yaml`.


## Grafisk fremstilling av API-ene (swagger-ui)
API-et kan sees og testes på `http://localhost:8080/permitteringsskjema-api/swagger-ui.html`

---------

# Henvendelser

## For Nav-ansatte
Dette Git-repositoriet eies av [Team Permittering og Nedbemanning](https://teamkatalog.nais.adeo.no/team/9ba027cc-1ee6-4656-9e29-ef6b36349d8e).

Slack-kanaler:
* [#permittering-og-nedbemanning-utvikling](https://nav-it.slack.com/archives/C01MCK908M6)
* [#arbeidsgiver-utvikling](https://nav-it.slack.com/archives/CD4MES6BB)
* [#arbeidsgiver-general](https://nav-it.slack.com/archives/CCM649PDH)

## For folk utenfor Nav
* Opprett gjerne en issue i Github for alle typer spørlsmål
* IT-utviklerne i Github-teamet https://github.com/orgs/navikt/teams/arbeidsgiver
* IT-avdelingen i [Arbeids- og velferdsdirektoratet](https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/arbeids-og-velferdsdirektoratet-kontorinformasjon)

