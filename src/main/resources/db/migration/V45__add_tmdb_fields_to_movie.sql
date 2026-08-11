ALTER TABLE movie
    ADD COLUMN poster_url VARCHAR(512),
    ADD COLUMN trailer_youtube_key VARCHAR(128),
    ADD COLUMN tmdb_id BIGINT;
