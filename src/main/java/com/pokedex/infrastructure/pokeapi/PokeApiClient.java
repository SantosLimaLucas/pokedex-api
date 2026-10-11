
package com.pokedex.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PokeApiClient {


    private final RestClient restClient;

    public PokeApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://pokeapi.co/api/v2")
                .build();
    }

    public PokemonListResponse listPokemon(int limit, int offset) {

        return restClient.get()
                .uri("/pokemon?limit={limit}&offset={offset}", limit, offset)
                .retrieve()
                .body(PokemonListResponse.class);
    }

    public PokemonResponse getPokemon(String url) {
        System.out.println("Consultando pokemon especifico: " + url);
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(PokemonResponse.class);
    }

    public PokemonSpeciesResponse getSpecies(String url) {
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(PokemonSpeciesResponse.class);
    }

    public record PokemonListResponse(
            int count,
            List<PokemonListItem> results
    ) {}

    public record PokemonListItem(String name, String url) {}

    public record PokemonResponse(
            int id,
            String name,
            int weight,
            Sprites sprites,
            List<TypeSlot> types,
            List<AbilitySlot> abilities,
            SpeciesReference species
    ) {}

    public record Sprites(
            @JsonProperty("front_default") String frontDefault
    ) {}

    public record TypeSlot(TypeReference type) {}

    public record TypeReference(String name) {}

    public record AbilitySlot(
            @JsonProperty("is_hidden") boolean hidden,
            AbilityReference ability
    ) {}

    public record AbilityReference(String name) {}

    public record SpeciesReference(String name, String url) {}

    public record PokemonSpeciesResponse(List<Genus> genera) {}

    public record Genus(
            String genus,
            Language language
    ) {}

    public record Language(String name) {}
}
