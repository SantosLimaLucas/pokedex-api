
package com.pokedex.infrastructure.persistence;

import com.pokedex.domain.model.EvolutionPokemon;
import com.pokedex.domain.model.Pokemon;
import com.pokedex.domain.model.PokemonDetails;
import com.pokedex.domain.model.PokemonStats;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        Pokemon pokemon = adapter.findById(150)
                .orElseThrow();

        assertThat(pokemon.id()).isEqualTo(150);
        assertThat(pokemon.name()).isEqualTo("mewtwo");
        assertThat(pokemon.weight()).isEqualTo(1220);
        assertThat(pokemon.height()).isEqualTo(20);
        assertThat(pokemon.types()).containsExactly("psychic");
        assertThat(pokemon.abilities()).containsExactly("pressure");
    }

    @Test
    void shouldSavePokemonWithTypesAndAbilities() {
        Pokemon pokemon = new Pokemon(
                94,
                "gengar",
                "https://example.com/gengar.png",
                List.of("ghost", "poison"),
                405,
                15,
                List.of("cursed-body")
        );

        Pokemon savedPokemon = adapter.save(pokemon);

        entityManager.flush();
        entityManager.clear();

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

    @Test
    void shouldSaveAndRetrievePokemonDetailsWithEvolutionChain() {
        PokemonDetails details = createPikachuDetails();

        adapter.saveDetails(details);

        entityManager.flush();
        entityManager.clear();

        PokemonDetails savedDetails = adapter.findDetailsById(25)
                .orElseThrow();

        assertThat(savedDetails.id()).isEqualTo(25);
        assertThat(savedDetails.name()).isEqualTo("pikachu");
        assertThat(savedDetails.imageUrl())
                .isEqualTo("https://example.com/pikachu.png");
        assertThat(savedDetails.types()).containsExactly("electric");
        assertThat(savedDetails.abilities())
                .containsExactly("static", "lightning-rod");
        assertThat(savedDetails.weight()).isEqualTo(60);
        assertThat(savedDetails.height()).isEqualTo(4);
        assertThat(savedDetails.description())
                .isEqualTo("Stores electricity in its cheeks.");

        assertThat(savedDetails.stats().hp()).isEqualTo(35);
        assertThat(savedDetails.stats().attack()).isEqualTo(55);
        assertThat(savedDetails.stats().defense()).isEqualTo(40);
        assertThat(savedDetails.stats().specialAttack()).isEqualTo(50);
        assertThat(savedDetails.stats().specialDefense()).isEqualTo(50);
        assertThat(savedDetails.stats().speed()).isEqualTo(90);

        EvolutionPokemon pichu = savedDetails.evolutionChain();

        assertThat(pichu.id()).isEqualTo(172);
        assertThat(pichu.name()).isEqualTo("pichu");
        assertThat(pichu.evolvesTo()).hasSize(1);

        EvolutionPokemon pikachu = pichu.evolvesTo().get(0);

        assertThat(pikachu.id()).isEqualTo(25);
        assertThat(pikachu.name()).isEqualTo("pikachu");
        assertThat(pikachu.evolvesTo()).hasSize(1);

        EvolutionPokemon raichu = pikachu.evolvesTo().get(0);

        assertThat(raichu.id()).isEqualTo(26);
        assertThat(raichu.name()).isEqualTo("raichu");
        assertThat(raichu.evolvesTo()).isEmpty();
    }

    @Test
    void shouldSaveAndRetrieveBranchedEvolutionChain() {
        PokemonDetails details = createEeveeDetails();

        adapter.saveDetails(details);

        entityManager.flush();
        entityManager.clear();

        PokemonDetails savedDetails = adapter.findDetailsById(133)
                .orElseThrow();

        EvolutionPokemon eevee = savedDetails.evolutionChain();

        assertThat(eevee.name()).isEqualTo("eevee");
        assertThat(eevee.evolvesTo())
                .extracting(EvolutionPokemon::name)
                .containsExactlyInAnyOrder(
                        "vaporeon",
                        "jolteon",
                        "flareon"
                );
    }

    @Test
    void shouldReplaceExistingEvolutionChain() {
        adapter.saveDetails(createPikachuDetails());

        entityManager.flush();
        entityManager.clear();

        PokemonDetails updatedDetails = new PokemonDetails(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                List.of("electric"),
                60,
                4,
                List.of("static", "lightning-rod"),
                new PokemonStats(35, 55, 40, 50, 50, 90),
                "Updated description.",
                new EvolutionPokemon(
                        172,
                        "pichu",
                        "https://example.com/pichu.png",
                        List.of(
                                new EvolutionPokemon(
                                        25,
                                        "pikachu",
                                        "https://example.com/pikachu.png",
                                        List.of()
                                )
                        )
                )
        );

        adapter.saveDetails(updatedDetails);

        entityManager.flush();
        entityManager.clear();

        PokemonDetails savedDetails = adapter.findDetailsById(25)
                .orElseThrow();

        assertThat(savedDetails.description())
                .isEqualTo("Updated description.");

        assertThat(savedDetails.evolutionChain().evolvesTo())
                .hasSize(1);

        assertThat(savedDetails.evolutionChain()
                .evolvesTo().get(0).evolvesTo())
                .isEmpty();
    }

    @Test
    void shouldPreserveCustomFieldsWhenOfficialDataIsUpdated() {
        PokemonEntity entity = new PokemonEntity(
                25,
                "pikachu",
                "https://example.com/old-pikachu.png",
                60,
                4
        );

        entity.updateCustomData(
                "Sparky",
                "Kanto",
                List.of("favorite", "starter")
        );

        jpaRepository.saveAndFlush(entity);
        entityManager.clear();

        Pokemon updatedPokemon = new Pokemon(
                25,
                "pikachu",
                "https://example.com/new-pikachu.png",
                List.of("electric"),
                65,
                4,
                List.of("static")
        );

        adapter.save(updatedPokemon);

        entityManager.flush();
        entityManager.clear();

        PokemonEntity savedEntity = jpaRepository.findById(25)
                .orElseThrow();

        assertThat(savedEntity.getImageUrl())
                .isEqualTo("https://example.com/new-pikachu.png");

        assertThat(savedEntity.getWeight()).isEqualTo(65);

        assertThat(savedEntity.getCustomName()).isEqualTo("Sparky");
        assertThat(savedEntity.getRegion()).isEqualTo("Kanto");
        assertThat(savedEntity.getTags())
                .containsExactlyInAnyOrder("favorite", "starter");
    }

    @Test
    void shouldPreserveCustomFieldsWhenDetailsAreUpdated() {
        PokemonEntity entity = new PokemonEntity(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                60,
                4
        );

        entity.updateCustomData(
                "Sparky",
                "Kanto",
                List.of("favorite")
        );

        jpaRepository.saveAndFlush(entity);
        entityManager.clear();

        adapter.saveDetails(createPikachuDetails());

        entityManager.flush();
        entityManager.clear();

        PokemonEntity savedEntity = jpaRepository.findById(25)
                .orElseThrow();

        assertThat(savedEntity.getCustomName()).isEqualTo("Sparky");
        assertThat(savedEntity.getRegion()).isEqualTo("Kanto");
        assertThat(savedEntity.getTags()).containsExactly("favorite");
        assertThat(savedEntity.getHp()).isEqualTo(35);
    }

    @Test
    void shouldNotReturnDetailsWhenPokemonHasOnlyBasicData() {
        PokemonEntity entity = new PokemonEntity(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                60,
                4
        );

        jpaRepository.saveAndFlush(entity);
        entityManager.clear();

        assertThat(adapter.findDetailsById(25)).isEmpty();
    }

    private PokemonDetails createPikachuDetails() {
        EvolutionPokemon raichu = new EvolutionPokemon(
                26,
                "raichu",
                "https://example.com/raichu.png",
                List.of()
        );

        EvolutionPokemon pikachu = new EvolutionPokemon(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                List.of(raichu)
        );

        EvolutionPokemon pichu = new EvolutionPokemon(
                172,
                "pichu",
                "https://example.com/pichu.png",
                List.of(pikachu)
        );

        return new PokemonDetails(
                25,
                "pikachu",
                "https://example.com/pikachu.png",
                List.of("electric"),
                60,
                4,
                List.of("static", "lightning-rod"),
                new PokemonStats(35, 55, 40, 50, 50, 90),
                "Stores electricity in its cheeks.",
                pichu
        );
    }

    private PokemonDetails createEeveeDetails() {
        EvolutionPokemon eevee = new EvolutionPokemon(
                133,
                "eevee",
                "https://example.com/eevee.png",
                List.of(
                        new EvolutionPokemon(
                                134,
                                "vaporeon",
                                "https://example.com/vaporeon.png",
                                List.of()
                        ),
                        new EvolutionPokemon(
                                135,
                                "jolteon",
                                "https://example.com/jolteon.png",
                                List.of()
                        ),
                        new EvolutionPokemon(
                                136,
                                "flareon",
                                "https://example.com/flareon.png",
                                List.of()
                        )
                )
        );

        return new PokemonDetails(
                133,
                "eevee",
                "https://example.com/eevee.png",
                List.of("normal"),
                65,
                3,
                List.of("run-away", "adaptability"),
                new PokemonStats(55, 55, 50, 45, 65, 55),
                "A Pokemon with an unstable genetic code.",
                eevee
        );
    }
}
