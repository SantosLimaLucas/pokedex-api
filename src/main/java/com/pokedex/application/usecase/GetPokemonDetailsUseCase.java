package com.pokedex.application.usecase;

import com.pokedex.application.port.out.PokemonDetailsCatalog;
import com.pokedex.domain.model.PokemonDetails;

public class GetPokemonDetailsUseCase {

    private final PokemonDetailsCatalog catalog;

    public GetPokemonDetailsUseCase(PokemonDetailsCatalog catalog) {
        this.catalog = catalog;
    }

    public PokemonDetails execute(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Pokemon ID must be a positive integer."
            );
        }

        return catalog.findById(id)
                .orElseThrow(() ->
                        new PokemonNotFoundException(
                                "Pokemon not found: " + id
                        )
                );
    }
}