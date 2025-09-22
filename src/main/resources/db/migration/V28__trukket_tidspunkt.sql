alter table permitteringsskjema_v2
    add column if not exists trukket_tidspunkt timestamp,
    add column if not exists trukket_av text;