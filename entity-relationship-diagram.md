Datavarehus ønsker et ER-diagram for vår datamodell. 
Følgen er basert på de tabellene som er relevant for kafka-meldingene.
Skjemaet deles med datavarehus her: https://confluence.adeo.no/pages/viewpage.action?pageId=553645854

```mermaid
erDiagram
    PERMITTERINGSSKJEMA 1--1+ YRKESKATEGORI : yrkeskategorier
    PERMITTERINGSSKJEMA {
        uuid id PK
        timestamp opprettet_tidspunkt
        varchar bedrift_nr
        varchar type
        varchar kontakt_navn
        varchar kontakt_tlf
        date varslet_ansatt_dato
        date varslet_nav_dato
        date start_dato
        date slutt_dato
        boolean ukjent_slutt_dato
        varchar fritekst
        varchar bedrift_navn
        varchar kontakt_epost
        timestamp sendt_inn_tidspunkt
        varchar opprettet_av
        boolean avbrutt
        integer antall_beroert
        varchar aarsakskode
        varchar aarsakstekst
    }
    YRKESKATEGORI {
        uuid id PK
        uuid permitteringsskjema_id FK
        integer konsept_id
        varchar styrk08
        varchar label
        integer antall
}
```