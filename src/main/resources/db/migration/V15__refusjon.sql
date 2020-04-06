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
    id                  uuid primary key,
    refusjonsskjema_id  uuid,
    opprettet_av        varchar(11),
    bedrift_nr          varchar(9),
    fnr                 varchar(11),
    gradering           integer,
    periode_start       date,
    periode_slutt       date,
    innhentet_tidspunkt timestamp,
    inntekt_innhentet   numeric,
    inntekt_korrigert   numeric,
    refusjonsbeløp      numeric
);

create table arbeidsforhold_beregningsdetaljer
(
    arbeidsforhold_id uuid,
    beregningsdetaljer    varchar,
    primary key (arbeidsforhold_id, beregningsdetaljer)
);