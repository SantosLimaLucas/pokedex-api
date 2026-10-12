package com.pokedex.domain.model;

import java.util.List;

public record PokemonDetails(
        Integer id,
        String name,
        String imageUrl,
        List<String> types,
        String description,
        List<String> evolutionChain
) {
}