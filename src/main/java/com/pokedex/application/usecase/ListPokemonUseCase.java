
package com.pokedex.application.usecase;

import com.pokedex.application.dto.PokemonPage;
import com.pokedex.application.port.out.PokemonCatalog;

public class ListPokemonUseCase {

    private final PokemonCatalog pokemonCatalog;

    public ListPokemonUseCase(PokemonCatalog pokemonCatalog) {
        this.pokemonCatalog = pokemonCatalog;
    }

    public PokemonPage execute(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to zero"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 100"
            );
        }

        return pokemonCatalog.findAll(page, size);
    }
}
