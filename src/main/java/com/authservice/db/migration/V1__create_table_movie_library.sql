CREATE TABLE movies (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    release_year INT,
    genre VARCHAR(100),
    rating SMALLINT CHECK (rating >= 1 AND rating <= 5),
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT current_timestamp
);