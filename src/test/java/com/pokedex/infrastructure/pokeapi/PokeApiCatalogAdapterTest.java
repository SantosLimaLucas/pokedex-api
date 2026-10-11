
package com.pokedex.infrastructure.pokeapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PokeApiCatalogAdapterTest {

    private PokeApiClient client;
    private PokeApiCatalogAdapter adapter;

    @BeforeEach
    void setUp() {
        client = mock(PokeApiClient.class);
        adapter = new PokeApiCatalogAdapter(client);
    }

    @Test
    void shouldMapPokemonAndCalculatePagination() {
        var listResponse = new PokeApiClient.PokemonListResponse(
                1302,
                List.of(new PokeApiClient.PokemonListItem(
                        "pikachu",
                        "https://pokeapi.co/api/v2/pokemon/25/"
                ))
        );

        var pokemonResponse = new PokeApiClient.PokemonResponse(
                25,
                "pikachu",
                60,
                new PokeApiClient.Sprites(
                        "https://example.com/pikachu.png"
                ),
                List.of(new PokeApiClient.TypeSlot(
                        new PokeApiClient.TypeReference("electric")
                )),
                List.of(new PokeApiClient.AbilitySlot(
                        false,
                        new PokeApiClient.AbilityReference("static")
                )),
                new PokeApiClient.SpeciesReference(
                        "pikachu",
                        "https://pokeapi.co/api/v2/pokemon-species/25/"
                )
        );

        var speciesResponse = new PokeApiClient.PokemonSpeciesResponse(
                List.of(
                        new PokeApiClient.Genus(
                                "Mouse Pokémon",
                                new PokeApiClient.Language("en")
                        )
                )
        );

        when(client.listPokemon(20, 0)).thenReturn(listResponse);
        when(client.getPokemon(
                "https://pokeapi.co/api/v2/pokemon/25/"
        )).thenReturn(pokemonResponse);
        when(client.getSpecies(
                "https://pokeapi.co/api/v2/pokemon-species/25/"
        )).thenReturn(speciesResponse);

        var result = adapter.findAll(0, 20);

        assertEquals(1302, result.totalElements());
        assertEquals(66, result.totalPages());
        assertEquals(1, result.content().size());

        var pokemon = result.content().get(0);

        assertEquals(25, pokemon.id());
        assertEquals("pikachu", pokemon.name());
        assertEquals(List.of("electric"), pokemon.types());
        assertEquals(List.of("static"), pokemon.abilities());

        verify(client).listPokemon(20, 0);
        verify(client).getPokemon(
                "https://pokeapi.co/api/v2/pokemon/25/"
        );
        verify(client).getSpecies(
                "https://pokeapi.co/api/v2/pokemon-species/25/"
        );
    }
}
