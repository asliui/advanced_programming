-- Movie lists for Advanced: unrelated movies in each list

CREATE TABLE IF NOT EXISTS movie_lists (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS movie_list_movies (
    list_id INTEGER NOT NULL REFERENCES movie_lists(id) ON DELETE CASCADE,
    movie_id INTEGER NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    PRIMARY KEY (list_id, movie_id)
);

