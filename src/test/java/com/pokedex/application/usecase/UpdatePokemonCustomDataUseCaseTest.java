
package com.pokedex.application.usecase;

import com.pokedex.application.dto.UpdatePokemonCustomDataCommand;
import com.pokedex.application.exception.InvalidPokemonCustomDataException;
import com.pokedex.application.exception.PokemonNotFoundException;
import com.pokedex.application.port.out.PokemonCustomDataRepository;
import com.pokedex.domain.model.PokemonCustomData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdatePokemonCustomDataUseCaseTest {

    private PokemonCustomDataRepository repository;
    private UpdatePokemonCustomDataUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(PokemonCustomDataRepository.class);
        useCase = new UpdatePokemonCustomDataUseCase(repository);
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        PokemonCustomData current = new PokemonCustomData(
                25,
                "My Pikachu",
                "Kanto",
                List.of("favorite")
        );

        when(repository.findById(25))
                .thenReturn(Optional.of(current));

        when(repository.save(any(PokemonCustomData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePokemonCustomDataCommand command =
                new UpdatePokemonCustomDataCommand(
                        true,
                        "Sparky",
                        false,
                        null,
                        false,
                        null
                );

        PokemonCustomData result = useCase.execute(25, command);

        assertThat(result.customName()).isEqualTo("Sparky");
        assertThat(result.region()).isEqualTo("Kanto");
        assertThat(result.tags()).containsExactly("favorite");
    }

    @Test
    void shouldClearNullableFieldsAndTags() {
        PokemonCustomData current = new PokemonCustomData(
                25,
                "Sparky",
                "Kanto",
                List.of("favorite")
        );

        when(repository.findById(25))
                .thenReturn(Optional.of(current));

        when(repository.save(any(PokemonCustomData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePokemonCustomDataCommand command =
                new UpdatePokemonCustomDataCommand(
                        true,
                        null,
                        true,
                        null,
                        true,
                        List.of()
                );

        PokemonCustomData result = useCase.execute(25, command);

        assertThat(result.customName()).isNull();
        assertThat(result.region()).isNull();
        assertThat(result.tags()).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenPokemonDoesNotExist() {
        when(repository.findById(999))
                .thenReturn(Optional.empty());

        UpdatePokemonCustomDataCommand command =
                new UpdatePokemonCustomDataCommand(
                        true,
                        "Missing",
                        false,
                        null,
                        false,
                        null
                );

        assertThatThrownBy(() -> useCase.execute(999, command))
                .isInstanceOf(PokemonNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectEmptyPatch() {
        UpdatePokemonCustomDataCommand command =
                new UpdatePokemonCustomDataCommand(
                        false, null,
                        false, null,
                        false, null
                );

        assertThatThrownBy(() -> useCase.execute(25, command))
                .isInstanceOf(InvalidPokemonCustomDataException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectBlankCustomName() {
        when(repository.findById(25))
                .thenReturn(Optional.of(
                        new PokemonCustomData(
                                25, null, null, List.of()
                        )
                ));

        UpdatePokemonCustomDataCommand command =
                new UpdatePokemonCustomDataCommand(
                        true, "   ",
                        false, null,
                        false, null
                );

        assertThatThrownBy(() -> useCase.execute(25, command))
                .isInstanceOf(InvalidPokemonCustomDataException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidTags() {
        when(repository.findById(25))
                .thenReturn(Optional.of(
                        new PokemonCustomData(
                                25, null, null, List.of()
                        )
                ));

        UpdatePokemonCustomDataCommand command =
                new UpdatePokemonCustomDataCommand(
                        false, null,
                        false, null,
                        true, List.of("valid", " ")
                );

        assertThatThrownBy(() -> useCase.execute(25, command))
                .isInstanceOf(InvalidPokemonCustomDataException.class);

        verify(repository, never()).save(any());
    }
}
