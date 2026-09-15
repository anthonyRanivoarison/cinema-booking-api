ALTER TABLE reservation ADD COLUMN idempotency_key VARCHAR(64);

-- Backfill existing rows so the column can be NOT NULL
UPDATE reservation SET idempotency_key = id::text WHERE idempotency_key IS NULL;

ALTER TABLE reservation ALTER COLUMN idempotency_key SET NOT NULL;

CREATE UNIQUE INDEX uk_reservation_user_idempotency ON reservation (user_id, idempotency_key);