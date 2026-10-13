
package com.pokedex.domain.model;

import java.util.List;

public record PokemonDetails(
        Integer id,
        String name,
        String imageUrl,
        List<String> types,
        Integer weight,
        Integer height,
        List<String> abilities,
        PokemonStats stats,
        String description,
        EvolutionPokemon evolutionChain,
        String customName,
        String region,
        List<String> tags
) {

    public PokemonDetails {
        types = types == null ? List.of() : List.copyOf(types);
        abilities = abilities == null ? List.of() : List.copyOf(abilities);
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    // Compatibility constructor for official PokeAPI data.
    public PokemonDetails(
            Integer id,
            String name,
            String imageUrl,
            List<String> types,
            Integer weight,
            Integer height,
            List<String> abilities,
            PokemonStats stats,
            String description,
            EvolutionPokemon evolutionChain
    ) {
        this(
                id,
                name,
                imageUrl,
                types,
                weight,
                height,
                abilities,
                stats,
                description,
                evolutionChain,
                null,
                null,
                List.of()
        );
    }
}
