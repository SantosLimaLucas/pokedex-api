package com.pokedex.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class PokemonRepositoryTest {

    @Autowired
    private PokemonJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindPokemonById() {
        // Arrange
        PokemonEntity pokemon = new PokemonEntity(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                60,
                4
        );

        // Act
        repository.saveAndFlush(pokemon);

        entityManager.clear();


        PokemonEntity savedPokemon = repository.findById(25)
                .orElseThrow();

        // Assert
        assertThat(savedPokemon.getId()).isEqualTo(25);
        assertThat(savedPokemon.getName()).isEqualTo("pikachu");
    }

    @Test
    void shouldSaveAndFindPokemonWithTypesAndAbilities() {
        // Arrange
        PokemonEntity pokemon = new PokemonEntity(
                6,
                "charizard",
                "https://example.com/charizard.png",
                905,
                17
        );

        pokemon.getTypes().add("fire");
        pokemon.getTypes().add("flying");

        pokemon.getAbilities().add("blaze");
        pokemon.getAbilities().add("solar-power");

        // Act
        repository.saveAndFlush(pokemon);
        entityManager.clear();

        PokemonEntity savedPokemon = repository.findById(6)
                .orElseThrow();

        // Assert
        assertThat(savedPokemon.getTypes())
                .containsExactlyInAnyOrder("fire", "flying");

        assertThat(savedPokemon.getAbilities())
                .containsExactlyInAnyOrder("blaze", "solar-power");
    }
}