
package com.pokedex.application.usecase;

import com.pokedex.application.port.out.PokemonDetailsCatalog;
import com.pokedex.application.port.out.PokemonRepository;
import com.pokedex.domain.model.PokemonDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GetPokemonDetailsUseCaseTest {

    private PokemonDetailsCatalog catalog;
    private PokemonRepository repository;
    private GetPokemonDetailsUseCase useCase;

    @BeforeEach
    void setUp() {
        catalog = mock(PokemonDetailsCatalog.class);
        repository = mock(PokemonRepository.class);

        useCase = new GetPokemonDetailsUseCase(
                catalog,
                repository
        );
    }

    @Test
    void shouldReturnLocalPokemonWithoutCallingExternalCatalog() {
        PokemonDetails details = mock(PokemonDetails.class);

        when(repository.findDetailsById(25))
                .thenReturn(Optional.of(details));

        PokemonDetails result = useCase.execute(25);

        assertSame(details, result);

        verify(repository).findDetailsById(25);
        verifyNoInteractions(catalog);
        verify(repository, never())
                .saveDetails(any());
    }

    @Test
    void shouldFetchAndSavePokemonWhenNotFoundLocally() {
        PokemonDetails details = mock(PokemonDetails.class);

        when(repository.findDetailsById(25))
                .thenReturn(Optional.empty());

        when(catalog.findById(25))
                .thenReturn(Optional.of(details));

        when(repository.saveDetails(details))
                .thenReturn(details);

        PokemonDetails result = useCase.execute(25);

        assertSame(details, result);

        verify(repository).findDetailsById(25);
        verify(catalog).findById(25);
        verify(repository).saveDetails(details);
    }

    @Test
    void shouldThrowNotFoundWhenPokemonDoesNotExist() {
        when(repository.findDetailsById(99999))
                .thenReturn(Optional.empty());

        when(catalog.findById(99999))
                .thenReturn(Optional.empty());

        assertThrows(
                PokemonNotFoundException.class,
                () -> useCase.execute(99999)
        );

        verify(repository, never())
                .saveDetails(any());
    }

    @Test
    void shouldRejectInvalidPokemonId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(null)
        );

        verifyNoInteractions(repository, catalog);
    }
}
