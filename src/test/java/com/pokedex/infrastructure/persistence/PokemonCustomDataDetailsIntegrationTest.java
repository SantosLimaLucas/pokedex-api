
package com.pokedex.infrastructure.persistence;

import com.pokedex.domain.model.PokemonCustomData;
import com.pokedex.domain.model.PokemonDetails;
import com.pokedex.domain.model.PokemonStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PokemonCustomDataDetailsIntegrationTest {

    private static final int POKEMON_ID = 999101;

    @Autowired
    private PokemonPersistenceAdapter pokemonRepository;

    @Autowired
    private PokemonCustomDataPersistenceAdapter customDataRepository;

    @BeforeEach
    void setUp() {
        pokemonRepository.saveDetails(createOfficialDetails());

        customDataRepository.save(
                new PokemonCustomData(
                        POKEMON_ID,
                        "Bulba",
                        "Kanto",
                        List.of("favorite", "starter")
                )
        );
    }

    @Test
    void shouldIncludeCustomFieldsInPokemonDetails() {
        PokemonDetails result = pokemonRepository
                .findDetailsById(POKEMON_ID)
                .orElseThrow();

        assertThat(result.id()).isEqualTo(POKEMON_ID);
        assertThat(result.name()).isEqualTo("bulbasaur");
        assertThat(result.customName()).isEqualTo("Bulba");
        assertThat(result.region()).isEqualTo("Kanto");

        assertThat(result.tags())
                .containsExactlyInAnyOrder(
                        "favorite",
                        "starter"
                );

        assertThat(result.weight()).isEqualTo(69);
        assertThat(result.types())
                .containsExactlyInAnyOrder("grass", "poison");
    }

    @Test
    void shouldPreserveCustomFieldsAfterOfficialDataSync() {
        PokemonDetails synchronizedDetails = createOfficialDetails();

        PokemonDetails saved = pokemonRepository.saveDetails(
                synchronizedDetails
        );

        assertThat(saved.customName()).isEqualTo("Bulba");
        assertThat(saved.region()).isEqualTo("Kanto");

        assertThat(saved.tags())
                .containsExactlyInAnyOrder(
                        "favorite",
                        "starter"
                );

        PokemonDetails reloaded = pokemonRepository
                .findDetailsById(POKEMON_ID)
                .orElseThrow();

        assertThat(reloaded.customName()).isEqualTo("Bulba");
        assertThat(reloaded.region()).isEqualTo("Kanto");

        assertThat(reloaded.tags())
                .containsExactlyInAnyOrder(
                        "favorite",
                        "starter"
                );
    }

    @Test
    void shouldKeepOfficialNameSeparateFromCustomName() {
        PokemonDetails result = pokemonRepository
                .findDetailsById(POKEMON_ID)
                .orElseThrow();

        assertThat(result.name()).isEqualTo("bulbasaur");
        assertThat(result.customName()).isEqualTo("Bulba");
    }

    private PokemonDetails createOfficialDetails() {
        return new PokemonDetails(
                POKEMON_ID,
                "bulbasaur",
                "https://example.com/bulbasaur.png",
                List.of("grass", "poison"),
                69,
                7,
                List.of("overgrow", "chlorophyll"),
                new PokemonStats(
                        45,
                        49,
                        49,
                        65,
                        65,
                        45
                ),
                "A strange seed was planted on its back.",
                null
        );
    }
}
