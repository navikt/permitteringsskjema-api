create table yrkeskategori
(
    id                     uuid primary key,
    permitteringsskjema_id uuid,
    konsept_id             integer,
    styrk08                varchar,
    label                  varchar,
    antall                 integer
)