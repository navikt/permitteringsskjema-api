create table refusjonsskjema
(
    id                  uuid primary key,
    opprettet_tidspunkt timestamp not null,
    opprettet_av        varchar   not null,
    bedrift_nr          varchar(9),
    bedrift_navn        varchar,
    kontakt_navn        varchar,
    kontakt_tlf         varchar,
    kontakt_epost       varchar
);

create table arbeidsforhold
(
    id                          uuid primary key,
    refusjonssskjema_id         uuid,
    fnr                         varchar(11),
    gradering                   integer,
    inntekt_innhentet_tidspunkt timestamp,
    inntekt_fra_arbeidsgiver    integer,
    inntekt_fra_register        integer,
    periode_start               date,
    periode_slutt               date
);