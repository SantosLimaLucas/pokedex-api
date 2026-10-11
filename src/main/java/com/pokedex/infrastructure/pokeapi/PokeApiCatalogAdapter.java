
package com.pokedex.infrastructure.pokeapi;

import com.pokedex.application.dto.PokemonPage;
import com.pokedex.application.port.out.PokemonCatalog;
import com.pokedex.domain.model.Pokemon;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PokeApiCatalogAdapter implements PokemonCatalog {

    private final PokeApiClient client;

    public PokeApiCatalogAdapter(PokeApiClient client) {
        this.client = client;
    }

    @Override
    public PokemonPage findAll(int page, int size) {
        int offset = Math.multiplyExact(page, size);

        var response = client.listPokemon(size, offset);

        List<Pokemon> pokemon = response.results()
                .stream()
                .map(item -> mapPokemon(client.getPokemon(item.url())))
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

    private Pokemon mapPokemon(PokeApiClient.PokemonResponse response) {
        var species = client.getSpecies(response.species().url());

        String category = species.genera() == null
                ? null
                : species.genera().stream()
                    .filter(genus -> "en".equals(genus.language().name()))
                    .map(PokeApiClient.Genus::genus)
                    .findFirst()
                    .orElse(null);

        List<String> types = response.types().stream()
                .map(slot -> slot.type().name())
                .toList();

        List<String> abilities = response.abilities().stream()
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
                abilities
        );
    }
}
