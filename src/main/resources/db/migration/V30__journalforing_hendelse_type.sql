alter table journalforing
drop constraint journalforing_pkey,
  add column id uuid,
  add column hendelse_type text not null default 'INNSENDT';

update journalforing set id = gen_random_uuid() where id is null;

alter table journalforing
    alter column id set not null,
  add constraint journalforing_pkey primary key (id);

create index if not exists idx_journalforing_skjema on journalforing (skjema_id);
-- Sørg for idempotent opprettelse av journalførings-arbeid per (skjema_id, hendelse_type)
create unique index if not exists ux_journalforing_skjema_event on journalforing (skjema_id, hendelse_type);

create unique index if not exists ux_journalforing_skjema_jp_event
    on journalforing (skjema_id, journalpost_id, hendelse_type);
