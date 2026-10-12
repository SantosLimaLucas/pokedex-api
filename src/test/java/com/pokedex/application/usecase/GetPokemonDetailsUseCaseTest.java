package com.pokedex.application.usecase;

import com.pokedex.application.port.out.PokemonDetailsCatalog;
import com.pokedex.domain.model.PokemonDetails;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetPokemonDetailsUseCaseTest {

    @Test
    void shouldReturnPokemonDetailsWhenPokemonExists() {
        // Arrange
        PokemonDetailsCatalog catalog =
                mock(PokemonDetailsCatalog.class);

        PokemonDetails expected = new PokemonDetails(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                List.of("electric"),
                "Pikachu stores electricity in its cheeks.",
                List.of("pichu", "pikachu", "raichu")
        );

        when(catalog.findById(25))
                .thenReturn(Optional.of(expected));

        GetPokemonDetailsUseCase useCase =
                new GetPokemonDetailsUseCase(catalog);

        // Act
        PokemonDetails actual = useCase.execute(25);

        // Assert
        assertEquals(expected, actual);
    }
}