permitteringsskjema-api
================

Appen eksponerer API-et til skjemaet for melding om massepermittering, massenedbemanning og innskrenking av arbeidstid. 
Skjemaet lagres i databasen og publiseres på Kafka, og viderebehandles i Salesforce og Gosys.

Frontend til skjemaet ligger her: https://github.com/navikt/arbeidsgiver-permittering

# Komme i gang

Koden kan kjøres som en vanlig Spring Boot-applikasjon lokalt ved å starte `LokalApplikasjon`.

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

---------

# Henvendelser

## For Nav-ansatte
Dette Git-repositoriet eies av [Team Fager](https://teamkatalog.nav.no/team/93233539-ebfc-4b6a-805b-9fe3d2232ed5).

Slack-kanaler:
* [#team-fager](https://nav-it.slack.com/archives/C01V9FFEHEK)

## For folk utenfor Nav
* Opprett gjerne en issue i Github for alle typer spørsmål

