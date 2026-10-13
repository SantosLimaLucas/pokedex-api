
package com.pokedex.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serializable;

@Entity
@Table(name = "pokemon_evolution")
@Getter
public class PokemonEvolutionEntity {

    @EmbeddedId
    private PokemonEvolutionId id;

    @Column(name = "parent_evolution_id")
    private Integer parentEvolutionId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    protected PokemonEvolutionEntity() {
    }

    public PokemonEvolutionEntity(
            Integer pokemonId,
            Integer evolutionId,
            Integer parentEvolutionId,
            String name,
            String imageUrl
    ) {
        this.id = new PokemonEvolutionId(pokemonId, evolutionId);
        this.parentEvolutionId = parentEvolutionId;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    @Embeddable
    public record PokemonEvolutionId(
            @Column(name = "pokemon_id") Integer pokemonId,
            @Column(name = "evolution_id") Integer evolutionId
    ) implements Serializable {
    }
}
