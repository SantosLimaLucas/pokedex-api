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
        EvolutionPokemon evolutionChain
) {
}