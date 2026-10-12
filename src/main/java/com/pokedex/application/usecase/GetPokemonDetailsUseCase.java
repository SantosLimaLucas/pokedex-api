package com.pokedex.application.usecase;

import com.pokedex.application.port.out.PokemonDetailsCatalog;
import com.pokedex.domain.model.PokemonDetails;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetPokemonDetailsUseCase {

    private PokemonDetailsCatalog pokemonDetailsCatalog;
    public PokemonDetails execute(int i) {
        return null;
    }
}
