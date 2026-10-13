
package com.pokedex.application.usecase;

import com.pokedex.application.dto.PokemonPage;
import com.pokedex.application.port.out.PokemonCatalog;
import com.pokedex.domain.model.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListPokemonUseCaseTest {

    private PokemonCatalog catalog;
    private ListPokemonUseCase useCase;

    @BeforeEach
    void setUp() {
        catalog = mock(PokemonCatalog.class);
        useCase = new ListPokemonUseCase(catalog);
    }

    @Test
    void shouldReturnRequestedPage() {
        Pokemon pikachu = new Pokemon(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                List.of("electric"),
                60,
                4,
                List.of("static")
        );

        PokemonPage expected = new PokemonPage(
                List.of(pikachu),
                1,
                10,
                100,
                10
        );

        when(catalog.findAll(1, 10)).thenReturn(expected);

        PokemonPage result = useCase.execute(1, 10);

        assertEquals(expected, result);
        verify(catalog).findAll(1, 10);
    }

    @Test
    void shouldRejectNegativePage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(-1, 20)
        );

        verifyNoInteractions(catalog);
    }

    @Test
    void shouldRejectZeroPageSize() {
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(0, 0)
        );

        verifyNoInteractions(catalog);
    }

    @Test
    void shouldRejectPageSizeAbove100() {
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(0, 101)
        );

        verifyNoInteractions(catalog);
    }
}
