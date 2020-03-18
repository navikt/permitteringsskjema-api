create table hendelse
(
    id        uuid primary key,
    skjema_id uuid        not null,
    tidspunkt timestamp   not null,
    type      varchar     not null,
    utført_av varchar(11) not null
)