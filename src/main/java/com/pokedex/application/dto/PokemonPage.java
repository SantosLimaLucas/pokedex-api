package com.pokedex.application.dto;

import com.pokedex.domain.model.Pokemon;

import java.util.List;

public record PokemonPage(
        List<Pokemon> content,
        int page,
        int size,
        int totalElements,
        int totalPages
) {
    public PokemonPage {
        content = List.copyOf(content);
    }
}
