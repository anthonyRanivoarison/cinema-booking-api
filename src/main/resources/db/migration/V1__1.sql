CREATE TABLE movie
(
    id          UUID         NOT NULL,
    title       VARCHAR(255) NOT NULL,
    gender      VARCHAR(255),
    description VARCHAR(1000),
    duration    BIGINT,
    CONSTRAINT pk_movie PRIMARY KEY (id)
);

CREATE TABLE projection
(
    id         UUID                        NOT NULL,
    datetime   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    seat_price DECIMAL,
    movie_id   UUID,
    room_id    UUID,
    CONSTRAINT pk_projection PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id            UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id       UUID,
    projection_id UUID,
    CONSTRAINT pk_reservation PRIMARY KEY (id)
);

CREATE TABLE reservation_seat
(
    reservation_id UUID NOT NULL,
    seat_id        UUID NOT NULL
);

CREATE TABLE room
(
    id       UUID         NOT NULL,
    number   VARCHAR(255) NOT NULL,
    capacity INTEGER      NOT NULL,
    CONSTRAINT pk_room PRIMARY KEY (id)
);

CREATE TABLE seat
(
    id      UUID         NOT NULL,
    number  VARCHAR(255) NOT NULL,
    room_id UUID,
    CONSTRAINT pk_seat PRIMARY KEY (id)
);

CREATE TABLE "user"
(
    id         UUID NOT NULL,
    first_name VARCHAR(200),
    last_name  VARCHAR(200),
    birth_date date,
    email      VARCHAR(255),
    password   VARCHAR(255),
    phone      VARCHAR(50),
    role       VARCHAR(255),
    CONSTRAINT pk_user PRIMARY KEY (id)
);

ALTER TABLE "user"
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE projection
    ADD CONSTRAINT FK_PROJECTION_ON_MOVIE FOREIGN KEY (movie_id) REFERENCES movie (id);

ALTER TABLE projection
    ADD CONSTRAINT FK_PROJECTION_ON_ROOM FOREIGN KEY (room_id) REFERENCES room (id);

ALTER TABLE reservation
    ADD CONSTRAINT FK_RESERVATION_ON_PROJECTION FOREIGN KEY (projection_id) REFERENCES projection (id);

ALTER TABLE reservation
    ADD CONSTRAINT FK_RESERVATION_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE seat
    ADD CONSTRAINT FK_SEAT_ON_ROOM FOREIGN KEY (room_id) REFERENCES room (id);

ALTER TABLE reservation_seat
    ADD CONSTRAINT fk_ressea_on_j_reservation FOREIGN KEY (reservation_id) REFERENCES reservation (id);

ALTER TABLE reservation_seat
    ADD CONSTRAINT fk_ressea_on_j_seat FOREIGN KEY (seat_id) REFERENCES seat (id);