
package com.pokedex.infrastructure.persistence;

import com.pokedex.application.exception.PokemonNotFoundException;
import com.pokedex.domain.model.Pokemon;
import com.pokedex.domain.model.PokemonCustomData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class PokemonCustomDataPersistenceAdapterIntegrationTest {

    @Autowired
    private PokemonCustomDataPersistenceAdapter adapter;

    @Autowired
    private PokemonJpaRepository jpaRepository;

    @Autowired
    private PokemonPersistenceAdapter pokemonPersistenceAdapter;

    private static final int POKEMON_ID = 999001;

    @BeforeEach
    void setUp() {
        Pokemon pokemon = new Pokemon(
                POKEMON_ID,
                "pikachu",
                "https://example.com/pikachu.png",
                List.of("electric"),
                60,
                4,
                List.of("static", "lightning-rod")
        );

        pokemonPersistenceAdapter.save(pokemon);

        jpaRepository.flush();
    }

    @Test
    void shouldUpdateCustomFields() {
        PokemonCustomData updated = adapter.save(
                new PokemonCustomData(
                        POKEMON_ID,
                        "Sparky",
                        "Kanto",
                        List.of("favorite", "electric")
                )
        );

        assertThat(updated.customName()).isEqualTo("Sparky");
        assertThat(updated.region()).isEqualTo("Kanto");
        assertThat(updated.tags())
                .containsExactlyInAnyOrder("favorite", "electric");

        PokemonCustomData reloaded = adapter.findById(POKEMON_ID)
                .orElseThrow();

        assertThat(reloaded).isEqualTo(updated);
    }

    @Test
    void shouldPreserveOfficialPokemonData() {
        adapter.save(
                new PokemonCustomData(
                        POKEMON_ID,
                        "Sparky",
                        "Kanto",
                        List.of("favorite")
                )
        );

        PokemonEntity entity = jpaRepository.findById(POKEMON_ID)
                .orElseThrow();

        assertThat(entity.getName()).isEqualTo("pikachu");
        assertThat(entity.getImageUrl())
                .isEqualTo("https://example.com/pikachu.png");
        assertThat(entity.getWeight()).isEqualTo(60);
        assertThat(entity.getHeight()).isEqualTo(4);
        assertThat(entity.getTypes()).containsExactly("electric");
        assertThat(entity.getAbilities())
                .containsExactlyInAnyOrder("static", "lightning-rod");
    }

    @Test
    void shouldClearCustomFields() {
        adapter.save(
                new PokemonCustomData(
                        POKEMON_ID,
                        "Sparky",
                        "Kanto",
                        List.of("favorite")
                )
        );

        PokemonCustomData cleared = adapter.save(
                new PokemonCustomData(
                        POKEMON_ID,
                        null,
                        null,
                        List.of()
                )
        );

        assertThat(cleared.customName()).isNull();
        assertThat(cleared.region()).isNull();
        assertThat(cleared.tags()).isEmpty();
    }

    @Test
    void shouldThrowNotFoundForMissingPokemon() {
        assertThatThrownBy(() -> adapter.save(
                new PokemonCustomData(
                        999999,
                        "Missing",
                        "Kanto",
                        List.of()
                )
        )).isInstanceOf(PokemonNotFoundException.class);
    }
}
