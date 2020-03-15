create table permittering
(
    id                  uuid primary key,
    opprettet_tidspunkt timestamp not null,
    org_nr              varchar(9)
)