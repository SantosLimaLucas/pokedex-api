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
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(PokemonResponse.class);
    }

    public PokemonResponse getPokemonById(int id) {
        return restClient.get()
                .uri("/pokemon/{id}", id)
                .retrieve()
                .body(PokemonResponse.class);
    }

    public PokemonSpeciesResponse getSpecies(String url) {
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(PokemonSpeciesResponse.class);
    }

    public EvolutionChainResponse getEvolutionChain(String url) {
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(EvolutionChainResponse.class);
    }

    public record PokemonListResponse(
            int count,
            List<PokemonListItem> results
    ) {
    }

    public record PokemonListItem(
            String name,
            String url
    ) {
    }

    public record PokemonResponse(
            int id,
            String name,
            int weight,
            int height,
            Sprites sprites,
            List<TypeSlot> types,
            List<AbilitySlot> abilities,
            SpeciesReference species,
            List<StatSlot> stats
    ) {
        public PokemonResponse(
                int id,
                String name,
                int weight,
                int height,
                Sprites sprites,
                List<TypeSlot> types,
                List<AbilitySlot> abilities,
                SpeciesReference species
        ) {
            this(id, name, weight, height, sprites, types, abilities, species, List.of());
        }
    }

    public record Sprites(
            @JsonProperty("front_default") String frontDefault
    ) {
    }

    public record TypeSlot(TypeReference type) {
    }

    public record TypeReference(String name) {
    }

    public record AbilitySlot(
            @JsonProperty("is_hidden") boolean hidden,
            AbilityReference ability
    ) {
    }

    public record AbilityReference(String name) {
    }

    public record SpeciesReference(
            String name,
            String url
    ) {
    }

    public record StatSlot(
            @JsonProperty("base_stat") int baseStat,
            StatReference stat
    ) {
    }

    public record StatReference(String name) {
    }

    public record PokemonSpeciesResponse(
            @JsonProperty("flavor_text_entries")
            List<FlavorTextEntry> flavorTextEntries,

            @JsonProperty("evolution_chain")
            EvolutionChainReference evolutionChain,

            List<Genus> genera
    ) {
        public PokemonSpeciesResponse(
                List<FlavorTextEntry> flavorTextEntries,
                EvolutionChainReference evolutionChain
        ) {
            this(flavorTextEntries, evolutionChain, List.of());
        }

        public PokemonSpeciesResponse(List<Genus> genera) {
            this(List.of(), null, genera);
        }
    }

    public record Genus(
            String genus,
            Language language
    ) {
    }

    public record FlavorTextEntry(
            @JsonProperty("flavor_text") String flavorText,
            Language language
    ) {
    }

    public record Language(String name) {
    }

    public record EvolutionChainReference(String url) {
    }

    public record EvolutionChainResponse(ChainLink chain) {
    }

    public record ChainLink(
            SpeciesReference species,

            @JsonProperty("evolves_to")
            List<ChainLink> evolvesTo
    ) {
    }
}