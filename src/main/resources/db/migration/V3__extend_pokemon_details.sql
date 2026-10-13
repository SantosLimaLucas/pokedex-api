
ALTER TABLE pokemon
    ADD COLUMN description TEXT,
    ADD COLUMN hp INTEGER,
    ADD COLUMN attack INTEGER,
    ADD COLUMN defense INTEGER,
    ADD COLUMN special_attack INTEGER,
    ADD COLUMN special_defense INTEGER,
    ADD COLUMN speed INTEGER,
    ADD COLUMN custom_name VARCHAR(100),
    ADD COLUMN region VARCHAR(100),
    ADD COLUMN last_synced_at TIMESTAMP;

CREATE TABLE pokemon_tag (
    pokemon_id INTEGER NOT NULL,
    tag_name VARCHAR(100) NOT NULL,

    CONSTRAINT pk_pokemon_tag
        PRIMARY KEY (pokemon_id, tag_name),

    CONSTRAINT fk_pokemon_tag_pokemon
        FOREIGN KEY (pokemon_id)
        REFERENCES pokemon(id)
        ON DELETE CASCADE
);

CREATE TABLE pokemon_evolution (
    pokemon_id INTEGER NOT NULL,
    evolution_id INTEGER NOT NULL,
    parent_evolution_id INTEGER,
    name VARCHAR(100) NOT NULL,
    image_url TEXT,

    CONSTRAINT pk_pokemon_evolution
        PRIMARY KEY (pokemon_id, evolution_id),

    CONSTRAINT fk_pokemon_evolution_pokemon
        FOREIGN KEY (pokemon_id)
        REFERENCES pokemon(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pokemon_evolution_parent
        FOREIGN KEY (pokemon_id, parent_evolution_id)
        REFERENCES pokemon_evolution(pokemon_id, evolution_id)
);
