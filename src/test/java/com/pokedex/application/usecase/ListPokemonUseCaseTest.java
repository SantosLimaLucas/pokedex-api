
package com.pokedex.application.usecase;

import com.pokedex.application.dto.PokemonPage;
import com.pokedex.application.port.out.PokemonCatalog;
import com.pokedex.application.port.out.PokemonRemoteCatalog;
import com.pokedex.application.port.out.PokemonRepository;
import com.pokedex.domain.model.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ListPokemonUseCaseTest {

    private PokemonCatalog localCatalog;
    private PokemonRemoteCatalog remoteCatalog;
    private PokemonRepository repository;
    private ListPokemonUseCase useCase;

    @BeforeEach
    void setUp() {
        localCatalog = mock(PokemonCatalog.class);
        remoteCatalog = mock(PokemonRemoteCatalog.class);
        repository = mock(PokemonRepository.class);

        useCase = new ListPokemonUseCase(
                localCatalog,
                remoteCatalog,
                repository
        );
    }

    @Test
    void shouldReturnLocalPageWithoutCallingPokeApi() {
        Pokemon pikachu = createPokemon(25, "pikachu");

        PokemonPage localPage = new PokemonPage(
                List.of(pikachu),
                0,
                1,
                1,
                1
        );

        when(localCatalog.findAll(0, 1))
                .thenReturn(localPage);

        PokemonPage result = useCase.execute(0, 1);

        assertThat(result).isEqualTo(localPage);

        verify(localCatalog).findAll(0, 1);
        verifyNoInteractions(remoteCatalog, repository);
    }

    @Test
    void shouldReturnIncompleteLocalPageWithoutFallback() {
        Pokemon bulbasaur = createPokemon(1, "bulbasaur");
        Pokemon ivysaur = createPokemon(2, "ivysaur");

        PokemonPage localPage = new PokemonPage(
                List.of(bulbasaur, ivysaur),
                0,
                20,
                2,
                1
        );

        when(localCatalog.findAll(0, 20))
                .thenReturn(localPage);

        PokemonPage result = useCase.execute(0, 20);

        assertThat(result.content())
                .containsExactly(bulbasaur, ivysaur);

        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(2);

        verifyNoInteractions(remoteCatalog, repository);
    }

    @Test
    void shouldFallbackToPokeApiWhenLocalPageIsEmpty() {
        Pokemon bulbasaur = createPokemon(1, "bulbasaur");
        Pokemon ivysaur = createPokemon(2, "ivysaur");

        when(localCatalog.findAll(0, 2)).thenReturn(
                new PokemonPage(List.of(), 0, 2, 0, 0)
        );

        when(remoteCatalog.findAll(0, 2)).thenReturn(
                new PokemonPage(
                        List.of(bulbasaur, ivysaur),
                        0,
                        2,
                        2,
                        1
                )
        );

        when(repository.findById(1))
                .thenReturn(Optional.empty());

        when(repository.findById(2))
                .thenReturn(Optional.empty());

        when(repository.save(bulbasaur))
                .thenReturn(bulbasaur);

        when(repository.save(ivysaur))
                .thenReturn(ivysaur);

        PokemonPage result = useCase.execute(0, 2);

        assertThat(result.content())
                .containsExactly(bulbasaur, ivysaur);

        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(1);

        verify(repository).save(bulbasaur);
        verify(repository).save(ivysaur);
    }

    @Test
    void shouldNotOverwriteExistingPokemonDuringFallback() {
        Pokemon localPokemon = createPokemon(1, "bulbasaur");
        Pokemon remotePokemon = createPokemon(1, "bulbasaur");

        when(localCatalog.findAll(0, 2)).thenReturn(
                new PokemonPage(List.of(), 0, 2, 0, 0)
        );

        when(remoteCatalog.findAll(0, 2)).thenReturn(
                new PokemonPage(
                        List.of(remotePokemon),
                        0,
                        2,
                        1,
                        1
                )
        );

        when(repository.findById(1))
                .thenReturn(Optional.of(localPokemon));

        PokemonPage result = useCase.execute(0, 2);

        assertThat(result.content())
                .containsExactly(localPokemon);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectNegativePage() {
        assertThatThrownBy(() -> useCase.execute(-1, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Page must be greater than or equal to zero"
                );

        verifyNoInteractions(
                localCatalog,
                remoteCatalog,
                repository
        );
    }

    @Test
    void shouldRejectInvalidPageSize() {
        assertThatThrownBy(() -> useCase.execute(0, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> useCase.execute(0, 101))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(
                localCatalog,
                remoteCatalog,
                repository
        );
    }

    private Pokemon createPokemon(int id, String name) {
        return new Pokemon(
                id,
                name,
                "https://example.com/" + name + ".png",
                List.of("normal"),
                100,
                10,
                List.of("run-away")
        );
    }
}
