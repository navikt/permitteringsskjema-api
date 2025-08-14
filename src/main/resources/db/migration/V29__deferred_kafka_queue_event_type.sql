alter table deferred_kafka_queue
drop constraint deferred_kafka_queue_pkey,
    add column event_type text not null default 'INNSENDT',
    add column id uuid default gen_random_uuid() primary key;

create index if not exists idx_dkq_skjema_event on deferred_kafka_queue (skjema_id, event_type);