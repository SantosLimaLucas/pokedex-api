
package com.pokedex.config;

import com.pokedex.application.port.out.PokemonCatalog;
import com.pokedex.application.port.out.PokemonRepository;
import com.pokedex.application.usecase.ListPokemonUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pokedex.application.port.out.PokemonDetailsCatalog;
import com.pokedex.application.usecase.GetPokemonDetailsUseCase;

@Configuration
public class ApplicationConfig {

    @Bean
    ListPokemonUseCase listPokemonUseCase(PokemonCatalog pokemonCatalog) {
        return new ListPokemonUseCase(pokemonCatalog);
    }

    @Bean
    public GetPokemonDetailsUseCase getPokemonDetailsUseCase(
            PokemonDetailsCatalog catalog,
            PokemonRepository repository
    ) {
        return new GetPokemonDetailsUseCase(catalog, repository);
    }
}
