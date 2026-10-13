
package com.pokedex.infrastructure.pokeapi;

import com.pokedex.application.dto.PokemonPage;
import com.pokedex.application.port.out.PokemonRemoteCatalog;
import com.pokedex.domain.model.Pokemon;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PokeApiCatalogAdapter implements PokemonRemoteCatalog {

    private final PokeApiClient client;

    public PokeApiCatalogAdapter(PokeApiClient client) {
        this.client = client;
    }

    @Override
    public PokemonPage findAll(int page, int size) {
        int offset = Math.multiplyExact(page, size);

        PokeApiClient.PokemonListResponse response =
                client.listPokemon(size, offset);

        if (response == null || response.results() == null) {
            throw new IllegalStateException(
                    "PokeAPI returned an invalid Pokemon list response."
            );
        }

        List<Pokemon> pokemon = response.results()
                .stream()
                .map(item -> client.getPokemon(item.url()))
                .map(this::mapPokemon)
                .toList();

        int totalPages = (int) Math.ceil(
                (double) response.count() / size
        );

        return new PokemonPage(
                pokemon,
                page,
                size,
                response.count(),
                totalPages
        );
    }

    private Pokemon mapPokemon(
            PokeApiClient.PokemonResponse response
    ) {
        if (response == null) {
            throw new IllegalStateException(
                    "PokeAPI returned an empty Pokemon response."
            );
        }

        List<String> types = response.types() == null
                ? List.of()
                : response.types().stream()
                .filter(slot -> slot.type() != null)
                .map(slot -> slot.type().name())
                .toList();

        List<String> abilities = response.abilities() == null
                ? List.of()
                : response.abilities().stream()
                .filter(slot -> slot.ability() != null)
                .map(slot -> slot.ability().name())
                .toList();

        return new Pokemon(
                response.id(),
                response.name(),
                response.sprites() == null
                        ? null
                        : response.sprites().frontDefault(),
                types,
                response.weight(),
                response.height(),
                abilities
        );
    }
}
