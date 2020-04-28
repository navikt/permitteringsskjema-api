create table bedrift
(
    id                      uuid primary key,
    permitteringsskjema_id  uuid,
    antall                  integer,
    navn                    varchar,
    bedriftsnr              varchar
);