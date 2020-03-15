create table permittering
(
    id                  uuid primary key,
    opprettet_tidspunkt timestamp not null,
    bedrift_nr          varchar(9)
)