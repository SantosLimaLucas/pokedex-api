
package com.pokedex.config;

import com.pokedex.application.port.out.*;
import com.pokedex.application.usecase.ListPokemonUseCase;
import com.pokedex.application.usecase.UpdatePokemonCustomDataUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pokedex.application.usecase.GetPokemonDetailsUseCase;

@Configuration
public class ApplicationConfig {


    @Bean
    public ListPokemonUseCase listPokemonUseCase(
            PokemonCatalog pokemonCatalog,
            PokemonRemoteCatalog remoteCatalog,
            PokemonRepository pokemonRepository
    ) {
        return new ListPokemonUseCase(
                pokemonCatalog,
                remoteCatalog,
                pokemonRepository
        );
    }


    @Bean
    public GetPokemonDetailsUseCase getPokemonDetailsUseCase(
            PokemonDetailsCatalog catalog,
            PokemonRepository repository
    ) {
        return new GetPokemonDetailsUseCase(catalog, repository);
    }


    @Bean
    public UpdatePokemonCustomDataUseCase updatePokemonCustomDataUseCase(
            PokemonCustomDataRepository repository
    ) {
        return new UpdatePokemonCustomDataUseCase(repository);
    }

}
