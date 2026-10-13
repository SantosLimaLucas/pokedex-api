package com.pokedex.infrastructure.persistence;

import com.pokedex.domain.model.Pokemon;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PokemonPersistenceAdapterIntegrationTest {

    @Autowired
    private PokemonJpaRepository jpaRepository;

    @Autowired
    private PokemonPersistenceAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldFindPokemonWithTypesAndAbilities() {
        // Arrange
        PokemonEntity entity = new PokemonEntity(
                150,
                "mewtwo",
                "https://example.com/mewtwo.png",
                1220,
                20
        );

        entity.getTypes().add("psychic");
        entity.getAbilities().add("pressure");

        jpaRepository.saveAndFlush(entity);
        entityManager.clear();

        // Act
        Pokemon pokemon = adapter.findById(150)
                .orElseThrow();

        // Assert
        assertThat(pokemon.id()).isEqualTo(150);
        assertThat(pokemon.name()).isEqualTo("mewtwo");
        assertThat(pokemon.weight()).isEqualTo(1220);
        assertThat(pokemon.height()).isEqualTo(20);
        assertThat(pokemon.types()).containsExactly("psychic");
        assertThat(pokemon.abilities()).containsExactly("pressure");
    }

    @Test
    void shouldSavePokemonWithTypesAndAbilities() {
        // Arrange
        Pokemon pokemon = new Pokemon(
                94,
                "gengar",
                "https://example.com/gengar.png",
                List.of("ghost", "poison"),
                405,
                15,
                List.of("cursed-body")
        );

        // Act
        Pokemon savedPokemon = adapter.save(pokemon);

        entityManager.flush();
        entityManager.clear();

        // Assert
        PokemonEntity savedEntity = jpaRepository.findById(94)
                .orElseThrow();

        assertThat(savedPokemon).isEqualTo(pokemon);
        assertThat(savedEntity.getName()).isEqualTo("gengar");
        assertThat(savedEntity.getWeight()).isEqualTo(405);
        assertThat(savedEntity.getHeight()).isEqualTo(15);
        assertThat(savedEntity.getTypes())
                .containsExactlyInAnyOrder("ghost", "poison");
        assertThat(savedEntity.getAbilities())
                .containsExactly("cursed-body");
    }
}