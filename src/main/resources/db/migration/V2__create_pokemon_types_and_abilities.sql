CREATE TABLE pokemon_type (
    pokemon_id INTEGER NOT NULL,
    type_name VARCHAR(100) NOT NULL,

    CONSTRAINT pk_pokemon_type
        PRIMARY KEY (pokemon_id, type_name),

    CONSTRAINT fk_pokemon_type_pokemon
        FOREIGN KEY (pokemon_id)
        REFERENCES pokemon(id)
        ON DELETE CASCADE
);

CREATE TABLE pokemon_ability (
    pokemon_id INTEGER NOT NULL,
    ability_name VARCHAR(100) NOT NULL,

    CONSTRAINT pk_pokemon_ability
        PRIMARY KEY (pokemon_id, ability_name),

    CONSTRAINT fk_pokemon_ability_pokemon
        FOREIGN KEY (pokemon_id)
        REFERENCES pokemon(id)
        ON DELETE CASCADE
);