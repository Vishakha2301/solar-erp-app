CREATE TYPE user_role AS ENUM ('ADMIN', 'MANAGER', 'SALES', 'VIEWER');

CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        user_role    NOT NULL DEFAULT 'VIEWER',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO users (username, email, password, role)
VALUES (
    'admin',
    'admin@solarerp.com',
    '$2a$12$NOUI1Ix4t/fEuwqw5R4F4uUtIqcPlB28cO3XG4Xw/H4CxfsnU7nSy',
    'ADMIN'
);