create table deferred_kafka_queue
(
    skjema_id uuid primary key,
    queue_position bigint not null generated always as identity
);
