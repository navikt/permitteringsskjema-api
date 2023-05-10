create table journalforing
(
    skjema_id uuid primary key,
    state text not null check (state in ('NY', 'JOURNALFORT', 'FERDIG')),
    row_inserted_at text not null,

    journalpost_id text,
    journalfort_at text,
    kommunenummer text,
    behandlende_enhet text,
    oppgave_id text,
    oppgave_opprettet_at text
);