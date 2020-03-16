create table person
(
    id                     uuid primary key,
    permitteringsskjema_id uuid,
    fnr                    varchar(11),
    grad                   integer,
    kommentar              varchar
)