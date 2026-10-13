
ALTER TABLE pokemon_evolution
    DROP CONSTRAINT fk_pokemon_evolution_parent;

ALTER TABLE pokemon_evolution
    ADD CONSTRAINT fk_pokemon_evolution_parent
    FOREIGN KEY (pokemon_id, parent_evolution_id)
    REFERENCES pokemon_evolution(pokemon_id, evolution_id)
    DEFERRABLE INITIALLY DEFERRED;
