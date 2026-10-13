
package com.pokedex.infrastructure.sync;

import com.pokedex.application.port.out.PokemonDetailsCatalog;
import com.pokedex.application.port.out.PokemonRepository;
import com.pokedex.domain.model.PokemonDetails;
import com.pokedex.infrastructure.pokeapi.PokeApiClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PokemonSyncWorker {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 3;

    private final PokeApiClient client;
    private final PokemonDetailsCatalog catalog;
    private final PokemonRepository repository;
    private final PokemonSyncProgress progress;

    public PokemonSyncWorker(
            PokeApiClient client,
            PokemonDetailsCatalog catalog,
            PokemonRepository repository,
            PokemonSyncProgress progress
    ) {
        this.client = client;
        this.catalog = catalog;
        this.repository = repository;
        this.progress = progress;
    }

    @Async("pokemonSyncExecutor")
    public void synchronize() {
        try {
            synchronizeAll();
            progress.complete();
        } catch (Exception exception) {
            progress.fail(exception.getMessage());
        }
    }

    private void synchronizeAll() {
        int offset = 0;
        int total = Integer.MAX_VALUE;

        while (offset < total) {
            PokeApiClient.PokemonListResponse page =
                    fetchPage(BATCH_SIZE, offset);

            if (page == null || page.results() == null) {
                throw new IllegalStateException(
                        "PokeAPI returned an invalid Pokemon list response."
                );
            }

            total = page.count();
            progress.setTotal(total);

            List<PokeApiClient.PokemonListItem> items = page.results();

            if (items.isEmpty() && offset < total) {
                throw new IllegalStateException(
                        "PokeAPI returned an empty page before synchronization finished."
                );
            }

            for (PokeApiClient.PokemonListItem item : items) {
                synchronizeOne(item);
            }

            offset += items.size();
        }
    }

    private PokeApiClient.PokemonListResponse fetchPage(
            int limit,
            int offset
    ) {
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return client.listPokemon(limit, offset);
            } catch (RuntimeException exception) {
                lastException = exception;
            }
        }

        throw new IllegalStateException(
                "Unable to retrieve Pokemon list at offset " + offset,
                lastException
        );
    }

    private void synchronizeOne(PokeApiClient.PokemonListItem item) {
        try {
            PokemonDetails details = fetchDetails(item);

            repository.saveDetails(details);

            progress.recordSuccess();
        } catch (Exception exception) {
            progress.recordFailure(
                    item.name() + ": " + exception.getMessage()
            );
        }
    }

    private PokemonDetails fetchDetails(
            PokeApiClient.PokemonListItem item
    ) {
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                PokeApiClient.PokemonResponse pokemon =
                        client.getPokemon(item.url());

                if (pokemon == null) {
                    throw new IllegalStateException(
                            "PokeAPI returned an empty Pokemon response."
                    );
                }

                return catalog.findById(pokemon.id())
                        .orElseThrow(() -> new IllegalStateException(
                                "Pokemon details not found: " + pokemon.id()
                        ));

            } catch (RuntimeException exception) {
                lastException = exception;
            }
        }

        throw new IllegalStateException(
                "Unable to synchronize Pokemon: " + item.name(),
                lastException
        );
    }
}
