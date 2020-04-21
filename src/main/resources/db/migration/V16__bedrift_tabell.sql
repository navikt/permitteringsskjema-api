create table bedrift
(
    id                      uuid primary key,
    permitteringsskjema_id  uuid,
    antall_berorte          integer,
    navn                    varchar
);