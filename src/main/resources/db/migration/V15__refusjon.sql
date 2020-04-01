create table refusjon
(
    id                  uuid primary key,
    opprettet_tidspunkt timestamp not null,
    opprettet_av        varchar   not null,
    bedrift_nr          varchar(9),
    bedrift_navn        varchar
)