
package com.pokedex.infrastructure.pokeapi;

import com.pokedex.application.port.out.PokemonDetailsCatalog;
import com.pokedex.domain.model.EvolutionPokemon;
import com.pokedex.domain.model.PokemonDetails;
import com.pokedex.domain.model.PokemonStats;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PokeApiDetailsCatalogAdapterTest {

    @Test
    void shouldReturnCompletePokemonDetails() {
        PokeApiClient client = mock(PokeApiClient.class);

        PokeApiClient.PokemonResponse pokemon =
                new PokeApiClient.PokemonResponse(
                        25,
                        "pikachu",
                        60,
                        4,
                        new PokeApiClient.Sprites(
                                "https://example.com/pikachu.png"
                        ),
                        List.of(
                                new PokeApiClient.TypeSlot(
                                        new PokeApiClient.TypeReference("electric")
                                )
                        ),
                        List.of(
                                new PokeApiClient.AbilitySlot(
                                        false,
                                        new PokeApiClient.AbilityReference("static")
                                ),
                                new PokeApiClient.AbilitySlot(
                                        true,
                                        new PokeApiClient.AbilityReference("lightning-rod")
                                )
                        ),
                        new PokeApiClient.SpeciesReference(
                                "pikachu",
                                "https://pokeapi.co/api/v2/pokemon-species/25/"
                        ),
                        List.of(
                                new PokeApiClient.StatSlot(
                                        35,
                                        new PokeApiClient.StatReference("hp")
                                ),
                                new PokeApiClient.StatSlot(
                                        55,
                                        new PokeApiClient.StatReference("attack")
                                ),
                                new PokeApiClient.StatSlot(
                                        40,
                                        new PokeApiClient.StatReference("defense")
                                ),
                                new PokeApiClient.StatSlot(
                                        50,
                                        new PokeApiClient.StatReference("special-attack")
                                ),
                                new PokeApiClient.StatSlot(
                                        50,
                                        new PokeApiClient.StatReference("special-defense")
                                ),
                                new PokeApiClient.StatSlot(
                                        90,
                                        new PokeApiClient.StatReference("speed")
                                )
                        )
                );

        PokeApiClient.PokemonSpeciesResponse species =
                new PokeApiClient.PokemonSpeciesResponse(
                        List.of(
                                new PokeApiClient.FlavorTextEntry(
                                        "Stores electricity in its cheeks.",
                                        new PokeApiClient.Language("en")
                                ),
                                new PokeApiClient.FlavorTextEntry(
                                        "Almacena electricidad en sus mejillas.",
                                        new PokeApiClient.Language("es")
                                )
                        ),
                        new PokeApiClient.EvolutionChainReference(
                                "https://pokeapi.co/api/v2/evolution-chain/10/"
                        )
                );

        PokeApiClient.EvolutionChainResponse evolutionResponse =
                new PokeApiClient.EvolutionChainResponse(
                        new PokeApiClient.ChainLink(
                                new PokeApiClient.SpeciesReference(
                                        "pichu",
                                        "https://pokeapi.co/api/v2/pokemon-species/172/"
                                ),
                                List.of(
                                        new PokeApiClient.ChainLink(
                                                new PokeApiClient.SpeciesReference(
                                                        "pikachu",
                                                        "https://pokeapi.co/api/v2/pokemon-species/25/"
                                                ),
                                                List.of(
                                                        new PokeApiClient.ChainLink(
                                                                new PokeApiClient.SpeciesReference(
                                                                        "raichu",
                                                                        "https://pokeapi.co/api/v2/pokemon-species/26/"
                                                                ),
                                                                List.of()
                                                        )
                                                )
                                        )
                                )
                        )
                );

        when(client.getPokemonById(25)).thenReturn(pokemon);
        when(client.getSpecies(pokemon.species().url())).thenReturn(species);
        when(client.getEvolutionChain(
                species.evolutionChain().url()
        )).thenReturn(evolutionResponse);

        PokemonDetailsCatalog catalog =
                new PokeApiDetailsCatalogAdapter(client);

        Optional<PokemonDetails> result = catalog.findById(25);

        assertTrue(result.isPresent());

        PokemonDetails actual = result.orElseThrow();

        assertEquals(25, actual.id());
        assertEquals("pikachu", actual.name());
        assertEquals(
                "https://example.com/pikachu.png",
                actual.imageUrl()
        );
        assertEquals(List.of("electric"), actual.types());

        assertEquals(60, actual.weight());
        assertEquals(4, actual.height());
        assertEquals(
                List.of("static", "lightning-rod"),
                actual.abilities()
        );

        assertEquals(
                new PokemonStats(35, 55, 40, 50, 50, 90),
                actual.stats()
        );

        assertEquals(
                "Stores electricity in its cheeks.",
                actual.description()
        );

        EvolutionPokemon root = actual.evolutionChain();

        assertEquals("pichu", root.name());
        assertEquals(172, root.id());
        assertEquals(1, root.evolvesTo().size());

        EvolutionPokemon pikachu = root.evolvesTo().get(0);

        assertEquals("pikachu", pikachu.name());
        assertEquals(1, pikachu.evolvesTo().size());

        EvolutionPokemon raichu = pikachu.evolvesTo().get(0);

        assertEquals("raichu", raichu.name());
        assertEquals(26, raichu.id());
        assertTrue(raichu.evolvesTo().isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenPokemonDoesNotExist() {
        PokeApiClient client = mock(PokeApiClient.class);
        when(client.getPokemonById(999)).thenReturn(null);

        PokemonDetailsCatalog catalog =
                new PokeApiDetailsCatalogAdapter(client);

        assertTrue(catalog.findById(999).isEmpty());
    }

    @Test
    void shouldReturnThreeLevelEvolutionChain() {
        PokeApiClient client = mock(PokeApiClient.class);

        PokeApiClient.PokemonResponse pokemon =
                new PokeApiClient.PokemonResponse(
                        1,
                        "bulbasaur",
                        69,
                        4,
                        new PokeApiClient.Sprites(
                                "https://example.com/bulbasaur.png"
                        ),
                        List.of(
                                new PokeApiClient.TypeSlot(
                                        new PokeApiClient.TypeReference("grass")
                                ),
                                new PokeApiClient.TypeSlot(
                                        new PokeApiClient.TypeReference("poison")
                                )
                        ),
                        List.of(),
                        new PokeApiClient.SpeciesReference(
                                "bulbasaur",
                                "https://pokeapi.co/api/v2/pokemon-species/1/"
                        ),
                        List.of()
                );

        PokeApiClient.PokemonSpeciesResponse species =
                new PokeApiClient.PokemonSpeciesResponse(
                        List.of(
                                new PokeApiClient.FlavorTextEntry(
                                        "A strange seed was planted on its back at birth.",
                                        new PokeApiClient.Language("en")
                                )
                        ),
                        new PokeApiClient.EvolutionChainReference(
                                "https://pokeapi.co/api/v2/evolution-chain/1/"
                        )
                );

        PokeApiClient.EvolutionChainResponse evolutionResponse =
                new PokeApiClient.EvolutionChainResponse(
                        new PokeApiClient.ChainLink(
                                new PokeApiClient.SpeciesReference(
                                        "bulbasaur",
                                        "https://pokeapi.co/api/v2/pokemon-species/1/"
                                ),
                                List.of(
                                        new PokeApiClient.ChainLink(
                                                new PokeApiClient.SpeciesReference(
                                                        "ivysaur",
                                                        "https://pokeapi.co/api/v2/pokemon-species/2/"
                                                ),
                                                List.of(
                                                        new PokeApiClient.ChainLink(
                                                                new PokeApiClient.SpeciesReference(
                                                                        "venusaur",
                                                                        "https://pokeapi.co/api/v2/pokemon-species/3/"
                                                                ),
                                                                List.of()
                                                        )
                                                )
                                        )
                                )
                        )
                );

        when(client.getPokemonById(1)).thenReturn(pokemon);
        when(client.getSpecies(pokemon.species().url())).thenReturn(species);
        when(client.getEvolutionChain(
                species.evolutionChain().url()
        )).thenReturn(evolutionResponse);

        PokemonDetailsCatalog catalog =
                new PokeApiDetailsCatalogAdapter(client);

        Optional<PokemonDetails> result = catalog.findById(1);

        assertTrue(result.isPresent());

        EvolutionPokemon bulbasaur =
                result.orElseThrow().evolutionChain();

        assertEquals(1, bulbasaur.id());
        assertEquals("bulbasaur", bulbasaur.name());
        assertEquals(1, bulbasaur.evolvesTo().size());

        EvolutionPokemon ivysaur = bulbasaur.evolvesTo().get(0);

        assertEquals(2, ivysaur.id());
        assertEquals("ivysaur", ivysaur.name());
        assertEquals(1, ivysaur.evolvesTo().size());

        EvolutionPokemon venusaur = ivysaur.evolvesTo().get(0);

        assertEquals(3, venusaur.id());
        assertEquals("venusaur", venusaur.name());
        assertTrue(venusaur.evolvesTo().isEmpty());
    }
}
