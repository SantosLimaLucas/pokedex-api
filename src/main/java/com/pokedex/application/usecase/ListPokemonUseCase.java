
package com.pokedex.application.usecase;

import com.pokedex.application.dto.PokemonPage;
import com.pokedex.application.port.out.PokemonCatalog;
import com.pokedex.application.port.out.PokemonRemoteCatalog;
import com.pokedex.application.port.out.PokemonRepository;
import com.pokedex.domain.model.Pokemon;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ListPokemonUseCase {

    private final PokemonCatalog pokemonCatalog;
    private final PokemonRemoteCatalog remoteCatalog;
    private final PokemonRepository repository;

    public ListPokemonUseCase(
            PokemonCatalog pokemonCatalog,
            PokemonRemoteCatalog remoteCatalog,
            PokemonRepository repository
    ) {
        this.pokemonCatalog = pokemonCatalog;
        this.remoteCatalog = remoteCatalog;
        this.repository = repository;
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

        PokemonPage localPage = pokemonCatalog.findAll(page, size);

        if (!localPage.content().isEmpty()) {
            log.info("Found locally!");
            return localPage;
        }
        log.info("Not found locally, fetching from remote catalog...");
        PokemonPage remotePage = remoteCatalog.findAll(page, size);

        List<Pokemon> pokemon = remotePage.content()
                .stream()
                .map(this::findOrSave)
                .toList();

        return new PokemonPage(
                pokemon,
                page,
                size,
                remotePage.totalElements(),
                remotePage.totalPages()
        );
    }

    private Pokemon findOrSave(Pokemon pokemon) {
        return repository.findById(pokemon.id())
                .orElseGet(() -> repository.save(pokemon));
    }
}
