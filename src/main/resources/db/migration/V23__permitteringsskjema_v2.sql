create table permitteringsskjema_v2
(
    id                  uuid primary key not null,
    type                text             not null,

    bedrift_nr          text             not null,
    bedrift_navn        text             not null,

    kontakt_navn        text             not null,
    kontakt_epost       text             not null,
    kontakt_tlf         text             not null,

    antall_berort       numeric          not null,
    arsakskode          text             not null,

    yrkeskategorier     jsonb            not null,

    start_dato          date             not null,
    slutt_dato          date,
    ukjent_slutt_dato   boolean not null,

    sendt_inn_tidspunkt timestamp        not null,
    opprettet_av        text             not null
);


