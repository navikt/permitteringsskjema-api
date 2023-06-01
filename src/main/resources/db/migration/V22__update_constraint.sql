ALTER TABLE journalforing
DROP CONSTRAINT journalforing_state_check;

ALTER TABLE journalforing
ADD CONSTRAINT journalforing_state_check
CHECK (state in ('NY', 'JOURNALFORT', 'FERDIG', 'NEEDS_JOURNALFORING_ONLY'));
