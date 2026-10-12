package com.pokedex.domain.model;

import java.util.List;

public record EvolutionPokemon(
        int id,
        String name,
        String imageUrl,
        List<EvolutionPokemon> evolvesTo
) {
}