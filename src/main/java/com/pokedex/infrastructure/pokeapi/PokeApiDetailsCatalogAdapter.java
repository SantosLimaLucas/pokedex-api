package com.pokedex.infrastructure.pokeapi;

import com.pokedex.application.port.out.PokemonDetailsCatalog;
import com.pokedex.domain.model.EvolutionPokemon;
import com.pokedex.domain.model.PokemonDetails;
import com.pokedex.domain.model.PokemonStats;
import com.pokedex.infrastructure.pokeapi.PokeApiClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PokeApiDetailsCatalogAdapter implements PokemonDetailsCatalog {

    private static final String SPRITE_URL_TEMPLATE =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%d.png";

    private final PokeApiClient client;

    public PokeApiDetailsCatalogAdapter(PokeApiClient client) {
        this.client = client;
    }

    @Override
    public Optional<PokemonDetails> findById(Integer id) {
        PokeApiClient.PokemonResponse pokemon;

        try {
            pokemon = client.getPokemonById(id);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw exception;
        }

        if (pokemon == null) {
            return Optional.empty();
        }

        PokeApiClient.PokemonSpeciesResponse species =
                client.getSpecies(pokemon.species().url());

        PokemonStats stats = mapStats(pokemon.stats());
        List<String> types = mapTypes(pokemon.types());
        List<String> abilities = mapAbilities(pokemon.abilities());
        String description = mapEnglishDescription(species);

        EvolutionPokemon evolutionChain = mapEvolutionChain(species);

        String imageUrl = pokemon.sprites() == null
                ? null
                : pokemon.sprites().frontDefault();

        PokemonDetails details = new PokemonDetails(
                pokemon.id(),
                pokemon.name(),
                imageUrl,
                types,
                pokemon.weight(),
                pokemon.height(),
                abilities,
                stats,
                description,
                evolutionChain
        );

        return Optional.of(details);
    }

    private PokemonStats mapStats(List<PokeApiClient.StatSlot> statSlots) {
        Map<String, Integer> stats = statSlots == null
                ? Map.of()
                : statSlots.stream()
                        .filter(slot -> slot.stat() != null)
                        .collect(Collectors.toMap(
                                slot -> slot.stat().name(),
                                PokeApiClient.StatSlot::baseStat,
                                (first, ignored) -> first
                        ));

        return new PokemonStats(
                stats.getOrDefault("hp", 0),
                stats.getOrDefault("attack", 0),
                stats.getOrDefault("defense", 0),
                stats.getOrDefault("special-attack", 0),
                stats.getOrDefault("special-defense", 0),
                stats.getOrDefault("speed", 0)
        );
    }

    private List<String> mapTypes(
            List<PokeApiClient.TypeSlot> typeSlots
    ) {
        if (typeSlots == null) {
            return List.of();
        }

        return typeSlots.stream()
                .filter(slot -> slot.type() != null)
                .map(slot -> slot.type().name())
                .toList();
    }

    private List<String> mapAbilities(
            List<PokeApiClient.AbilitySlot> abilitySlots
    ) {
        if (abilitySlots == null) {
            return List.of();
        }

        return abilitySlots.stream()
                .filter(slot -> slot.ability() != null)
                .map(slot -> slot.ability().name())
                .toList();
    }

    private String mapEnglishDescription(
            PokeApiClient.PokemonSpeciesResponse species
    ) {
        if (species.flavorTextEntries() == null) {
            return "";
        }

        return species.flavorTextEntries().stream()
                .filter(entry -> entry.language() != null)
                .filter(entry -> "en".equals(entry.language().name()))
                .map(PokeApiClient.FlavorTextEntry::flavorText)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .map(this::normalizeDescription)
                .orElse("");
    }

    private String normalizeDescription(String description) {
        return description
                .replace('\n', ' ')
                .replace('\f', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private EvolutionPokemon mapEvolutionChain(
            PokeApiClient.PokemonSpeciesResponse species
    ) {
        if (species.evolutionChain() == null
                || species.evolutionChain().url() == null) {
            return null;
        }

        PokeApiClient.EvolutionChainResponse response =
                client.getEvolutionChain(species.evolutionChain().url());

        if (response == null || response.chain() == null) {
            return null;
        }

        return mapEvolutionNode(response.chain());
    }

    private EvolutionPokemon mapEvolutionNode(
            PokeApiClient.ChainLink link
    ) {
        int id = extractSpeciesId(link.species().url());

        List<EvolutionPokemon> nextEvolutions =
                link.evolvesTo() == null
                        ? List.of()
                        : link.evolvesTo().stream()
                                .map(this::mapEvolutionNode)
                                .toList();

        return new EvolutionPokemon(
                id,
                link.species().name(),
                SPRITE_URL_TEMPLATE.formatted(id),
                nextEvolutions
        );
    }

    private int extractSpeciesId(String url) {
        String normalizedUrl = url.endsWith("/")
                ? url.substring(0, url.length() - 1)
                : url;

        String idPart = normalizedUrl.substring(
                normalizedUrl.lastIndexOf('/') + 1
        );

        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Could not extract Pokemon species ID from URL: " + url,
                    exception
            );
        }
    }
}