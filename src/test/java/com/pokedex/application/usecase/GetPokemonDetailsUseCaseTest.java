package com.pokedex.application.usecase;

import com.pokedex.application.port.out.PokemonDetailsCatalog;
import com.pokedex.domain.model.PokemonDetails;
import com.pokedex.domain.model.PokemonStats;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetPokemonDetailsUseCaseTest {

    @Test
    void shouldReturnPokemonDetailsWhenPokemonExists() {
        PokemonDetailsCatalog catalog = mock(PokemonDetailsCatalog.class);

        PokemonDetails expected = new PokemonDetails(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                List.of("electric"),
                new PokemonStats(35, 55, 40, 50, 50, 90),
                "Pikachu stores electricity in its cheeks.",
                null
        );

        when(catalog.findById(25)).thenReturn(Optional.of(expected));

        GetPokemonDetailsUseCase useCase =
                new GetPokemonDetailsUseCase(catalog);

        PokemonDetails actual = useCase.execute(25);

        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPokemonDoesNotExist() {
        PokemonDetailsCatalog catalog = mock(PokemonDetailsCatalog.class);
        when(catalog.findById(999)).thenReturn(Optional.empty());

        GetPokemonDetailsUseCase useCase =
                new GetPokemonDetailsUseCase(catalog);

        assertThrows(
                PokemonNotFoundException.class,
                () -> useCase.execute(999)
        );
    }

    @Test
    void shouldRejectNonPositivePokemonId() {
        PokemonDetailsCatalog catalog = mock(PokemonDetailsCatalog.class);

        GetPokemonDetailsUseCase useCase =
                new GetPokemonDetailsUseCase(catalog);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(0)
        );
    }
}