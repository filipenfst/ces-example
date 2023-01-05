--liquibase formatted sql
--changeset filipe.silva:V1__create_subscription_tables

CREATE TABLE users
(
    id          SERIAL PRIMARY KEY,
    external_id UUID         NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL
);


CREATE TABLE merchant
(
    id          SERIAL PRIMARY KEY,
    external_id UUID         NOT NULL UNIQUE,
    mid         VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL
);

CREATE TABLE merchant_users
(
    user_id     INT REFERENCES users (id),
    merchant_id INT REFERENCES merchant (id)
);


CREATE TABLE app
(
    id             SERIAL PRIMARY KEY,
    external_id    UUID         NOT NULL UNIQUE,
    application_id VARCHAR(255) NOT NULL UNIQUE,
    name           VARCHAR(255) NOT NULL
);

CREATE TABLE merchants_subscriptions
(
    app_id      INT REFERENCES app (id),
    merchant_id INT REFERENCES merchant (id)
);
