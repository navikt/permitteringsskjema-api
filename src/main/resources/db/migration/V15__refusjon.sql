create table refusjonsskjema
(
    id                  uuid primary key,
    opprettet_tidspunkt timestamp,
    opprettet_av        varchar,
    bedrift_nr          varchar(9),
    bedrift_navn        varchar,
    kontakt_navn        varchar,
    kontakt_tlf         varchar,
    kontakt_epost       varchar,
    avbrutt             boolean,
    sendt_inn_tidspunkt timestamp
);

create table arbeidsforhold
(
    id                          uuid primary key,
    refusjonsskjema_id          uuid,
    fnr                         varchar(11),
    gradering                   integer,
    inntekt_innhentet_tidspunkt timestamp,
    inntekt_fra_arbeidsgiver    integer,
    inntekt_fra_register        integer,
    periode_start               date,
    periode_slutt               date
);