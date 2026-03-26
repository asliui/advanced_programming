-- Base schema for Lab 6 (PostgreSQL)

CREATE TABLE IF NOT EXISTS genres (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS actors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE
);

-- A movie has exactly one genre (genre_id FK).
CREATE TABLE IF NOT EXISTS movies (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL CHECK (duration > 0),
    score NUMERIC(3,1) NOT NULL CHECK (score >= 0 AND score <= 10),
    genre_id INTEGER NOT NULL REFERENCES genres(id) ON DELETE RESTRICT
);

-- Junction table between movies and actors.
CREATE TABLE IF NOT EXISTS movie_actors (
    movie_id INTEGER NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    actor_id INTEGER NOT NULL REFERENCES actors(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, actor_id)
);

-- View for the homework report (movies + genre + actors).
CREATE OR REPLACE VIEW v_movie_report AS
SELECT
    m.id AS movie_id,
    m.title AS title,
    m.release_date AS release_date,
    m.duration AS duration,
    m.score AS score,
    g.name AS genre,
    COALESCE(string_agg(DISTINCT a.name, ', ' ORDER BY a.name), '') AS actors
FROM movies m
JOIN genres g ON g.id = m.genre_id
LEFT JOIN movie_actors ma ON ma.movie_id = m.id
LEFT JOIN actors a ON a.id = ma.actor_id
GROUP BY m.id, m.title, m.release_date, m.duration, m.score, g.name;

