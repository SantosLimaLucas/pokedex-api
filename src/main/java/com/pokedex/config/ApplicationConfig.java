
package com.pokedex.config;

import com.pokedex.application.port.out.PokemonCatalog;
import com.pokedex.application.usecase.ListPokemonUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    ListPokemonUseCase listPokemonUseCase(PokemonCatalog pokemonCatalog) {
        return new ListPokemonUseCase(pokemonCatalog);
    }
}
