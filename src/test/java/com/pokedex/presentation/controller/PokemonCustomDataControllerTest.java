
package com.pokedex.presentation.controller;

import com.pokedex.application.exception.PokemonNotFoundException;
import com.pokedex.application.port.out.PokemonCustomDataRepository;
import com.pokedex.domain.model.PokemonCustomData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PokemonCustomDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokemonCustomDataRepository repository;

    @Test
    void shouldUpdatePokemonCustomData() throws Exception {
        PokemonCustomData current = new PokemonCustomData(
                25,
                null,
                null,
                List.of()
        );

        when(repository.findById(25))
                .thenReturn(Optional.of(current));

        when(repository.save(any(PokemonCustomData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/pokemon/25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customName": "Sparky",
                                  "region": "Kanto",
                                  "tags": ["favorite", "electric"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(25))
                .andExpect(jsonPath("$.customName").value("Sparky"))
                .andExpect(jsonPath("$.region").value("Kanto"))
                .andExpect(jsonPath("$.tags[0]").value("favorite"))
                .andExpect(jsonPath("$.tags[1]").value("electric"));
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        when(repository.findById(999999))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/pokemon/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customName": "Missing"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectOfficialFields() throws Exception {
        mockMvc.perform(patch("/api/pokemon/25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Raichu"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectInvalidCustomName() throws Exception {
        when(repository.findById(25))
                .thenReturn(Optional.of(
                        new PokemonCustomData(
                                25,
                                null,
                                null,
                                List.of()
                        )
                ));

        mockMvc.perform(patch("/api/pokemon/25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customName": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidTags() throws Exception {
        mockMvc.perform(patch("/api/pokemon/25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tags": ["valid", 123]
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectEmptyPatch() throws Exception {
        mockMvc.perform(patch("/api/pokemon/25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }
}
