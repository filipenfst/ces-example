CREATE TABLE users
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);


CREATE TABLE merchant
(
    id   UUID PRIMARY KEY,
    mid  VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE merchant_users
(
    user_id     UUID REFERENCES users (id),
    merchant_id UUID REFERENCES merchant (id)
);


CREATE TABLE app
(
    id            UUID PRIMARY KEY,
    application_id VARCHAR(255) NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL
);

CREATE TABLE merchants_subscriptions
(
    app_id      UUID REFERENCES app (id),
    merchant_id UUID REFERENCES merchant (id)
);
