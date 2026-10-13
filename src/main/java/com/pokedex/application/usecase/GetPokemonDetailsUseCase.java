
package com.pokedex.application.usecase;

import com.pokedex.application.port.out.PokemonDetailsCatalog;
import com.pokedex.application.port.out.PokemonRepository;
import com.pokedex.domain.model.PokemonDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetPokemonDetailsUseCase {

    private final PokemonDetailsCatalog catalog;
    private final PokemonRepository repository;

    public GetPokemonDetailsUseCase(
            PokemonDetailsCatalog catalog,
            PokemonRepository repository
    ) {
        this.catalog = catalog;
        this.repository = repository;
    }

    public PokemonDetails execute(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Pokemon ID must be a positive integer."
            );
        }
        log.info("Searching locally");
        return repository.findDetailsById(id)
                .orElseGet(() -> fetchAndSave(id));
    }

    private PokemonDetails fetchAndSave(Integer id) {
        log.info("Not found locally, fetching from remote catalog...");
        PokemonDetails details = catalog.findById(id)
                .orElseThrow(() ->
                        new PokemonNotFoundException(
                                "Pokemon not found: " + id
                        )
                );

        return repository.saveDetails(details);
    }
}
