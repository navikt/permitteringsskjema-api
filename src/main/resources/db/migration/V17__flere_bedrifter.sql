create table permitteringsskjema_juridisk_enhet
(
    id                  uuid primary key,
    type                varchar,
    bedrift_nr          varchar(9),
    bedrift_navn        varchar,
    opprettet_av        varchar(11),
    opprettet_tidspunkt timestamp not null
);

alter table permitteringsskjema add column  permitteringsskjema_juridisk_enhet_id uuid;

-- Må få til en update sån at det lages en juridisk enhelt for alle eksisterende skjemaer
-- Det blir då en en til en wrapper

-- insert into permitteringsskjema_juridisk_enhet
-- select bedrift_nr from permitteringsskjema
