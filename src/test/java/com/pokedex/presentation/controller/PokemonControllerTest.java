package com.pokedex.presentation.controller;

import com.pokedex.application.usecase.GetPokemonDetailsUseCase;
import com.pokedex.application.usecase.ListPokemonUseCase;
import com.pokedex.application.usecase.PokemonNotFoundException;
import com.pokedex.domain.model.EvolutionPokemon;
import com.pokedex.domain.model.PokemonDetails;
import com.pokedex.domain.model.PokemonStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PokemonControllerTest {

    private GetPokemonDetailsUseCase getPokemonDetailsUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ListPokemonUseCase listPokemonUseCase =
                mock(ListPokemonUseCase.class);

        getPokemonDetailsUseCase =
                mock(GetPokemonDetailsUseCase.class);

        PokemonController controller = new PokemonController(
                listPokemonUseCase,
                getPokemonDetailsUseCase
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnPokemonDetailsWhenPokemonExists() throws Exception {
        PokemonDetails details = new PokemonDetails(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                List.of("electric"),
                new PokemonStats(35, 55, 40, 50, 50, 90),
                "Stores electricity in its cheeks.",
                new EvolutionPokemon(
                        172,
                        "pichu",
                        "https://example.com/172.png",
                        List.of()
                )
        );

        when(getPokemonDetailsUseCase.execute(25)).thenReturn(details);

        mockMvc.perform(get("/api/pokemon/25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(25))
                .andExpect(jsonPath("$.name").value("pikachu"))
                .andExpect(jsonPath("$.types[0]").value("electric"))
                .andExpect(jsonPath("$.stats.hp").value(35))
                .andExpect(jsonPath("$.stats.attack").value(55))
                .andExpect(jsonPath("$.stats.defense").value(40))
                .andExpect(jsonPath("$.stats.specialAttack").value(50))
                .andExpect(jsonPath("$.stats.specialDefense").value(50))
                .andExpect(jsonPath("$.stats.speed").value(90))
                .andExpect(jsonPath("$.description")
                        .value("Stores electricity in its cheeks."))
                .andExpect(jsonPath("$.evolutionChain.name").value("pichu"));
    }

    @Test
    void shouldReturnNotFoundWhenPokemonDoesNotExist() throws Exception {
        when(getPokemonDetailsUseCase.execute(999))
                .thenThrow(new PokemonNotFoundException(
                        "Pokemon not found: 999"
                ));

        mockMvc.perform(get("/api/pokemon/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail")
                        .value("Pokemon not found: 999"));
    }

    @Test
    void shouldReturnBadRequestWhenPokemonIdIsInvalid() throws Exception {
        when(getPokemonDetailsUseCase.execute(0))
                .thenThrow(new IllegalArgumentException(
                        "Pokemon ID must be a positive integer."
                ));

        mockMvc.perform(get("/api/pokemon/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenPokemonIdIsNotNumeric()
            throws Exception {

        mockMvc.perform(get("/api/pokemon/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadGatewayWhenExternalApiFails()
            throws Exception {

        when(getPokemonDetailsUseCase.execute(25))
                .thenThrow(new RestClientException(
                        "External API is unavailable"
                ));

        mockMvc.perform(get("/api/pokemon/25"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.detail").value(
                        "Unable to retrieve Pokemon data from the external API"
                ));
    }
}