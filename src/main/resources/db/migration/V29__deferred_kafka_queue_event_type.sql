alter table deferred_kafka_queue
    drop constraint if exists deferred_kafka_queue_pkey,
    add column if not exists id uuid,
    add column if not exists hendelse_type text not null default 'INNSENDT';

update deferred_kafka_queue set id = gen_random_uuid() where id is null;

alter table deferred_kafka_queue
    add constraint deferred_kafka_queue_pkey primary key (id);

create index if not exists idx_dkq_skjema_event on deferred_kafka_queue (skjema_id, hendelse_type);