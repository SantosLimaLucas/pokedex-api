package com.pokedex.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import com.pokedex.domain.model.Pokemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class PokemonPersistenceAdapterTest {

    @Mock
    private PokemonJpaRepository jpaRepository;

    @Mock
    private PokemonEvolutionJpaRepository evolutionRepository;

    private PokemonPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adapter = new PokemonPersistenceAdapter(
                jpaRepository,
                evolutionRepository
        );
    }

    @Test
    void shouldFindPokemonWhenPokemonExists() {
        // Arrange
        PokemonEntity pokemonEntity = new PokemonEntity(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                60,
                4
        );

        pokemonEntity.getTypes().add("electric");
        pokemonEntity.getAbilities().add("static");

        when(jpaRepository.findById(25))
                .thenReturn(Optional.of(pokemonEntity));

        // Act
        Optional<Pokemon> result = adapter.findById(25);

        // Assert
        assertThat(result).isPresent();

        Pokemon pokemon = result.orElseThrow();

        assertThat(pokemon.id()).isEqualTo(25);
        assertThat(pokemon.name()).isEqualTo("pikachu");
        assertThat(pokemon.spriteUrl())
                .isEqualTo("https://example.com/pikachu.png");
        assertThat(pokemon.weight()).isEqualTo(60);
        assertThat(pokemon.types()).containsExactly("electric");
        assertThat(pokemon.abilities()).containsExactly("static");
        assertThat(pokemon.height()).isEqualTo(4);

        verify(jpaRepository).findById(25);
    }

    @Test
    void shouldReturnEmptyWhenPokemonDoesNotExist() {
        // Arrange
        when(jpaRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act
        Optional<Pokemon> result = adapter.findById(999);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(999);
    }

    @Test
    void shouldSavePokemon() {
        // Arrange
        Pokemon pokemon = new Pokemon(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                List.of("electric"),
                60,
                4,
                List.of("static", "lightning-rod")
        );

        when(jpaRepository.save(any(PokemonEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pokemon savedPokemon = adapter.save(pokemon);

        // Assert
        ArgumentCaptor<PokemonEntity> captor =
                ArgumentCaptor.forClass(PokemonEntity.class);

        verify(jpaRepository).save(captor.capture());

        PokemonEntity savedEntity = captor.getValue();

        assertThat(savedEntity.getId()).isEqualTo(25);
        assertThat(savedEntity.getName()).isEqualTo("pikachu");
        assertThat(savedEntity.getImageUrl())
                .isEqualTo("https://example.com/pikachu.png");
        assertThat(savedEntity.getWeight()).isEqualTo(60);
        assertThat(savedEntity.getHeight()).isEqualTo(4);
        assertThat(savedEntity.getTypes()).containsExactly("electric");
        assertThat(savedEntity.getAbilities())
                .containsExactly("static", "lightning-rod");

        assertThat(savedPokemon).isEqualTo(pokemon);
    }
}