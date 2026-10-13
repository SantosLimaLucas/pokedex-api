
package com.pokedex.infrastructure.persistence;

import com.pokedex.domain.model.Pokemon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PokemonPersistenceAdapterLazyLoadingTest {

    @Autowired
    private PokemonJpaRepository jpaRepository;

    @Autowired
    private PokemonPersistenceAdapter adapter;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void shouldFindPokemonWithLazyCollectionsWithoutExternalTransaction() {
        // Arrange
        transactionTemplate.executeWithoutResult(status -> {
            PokemonEntity entity = new PokemonEntity(
                    151,
                    "mew",
                    "https://example.com/mew.png",
                    40,
                    4
            );

            entity.getTypes().add("psychic");
            entity.getAbilities().add("synchronize");

            jpaRepository.saveAndFlush(entity);
        });

        try {
            // Act
            Pokemon pokemon = adapter.findById(151)
                    .orElseThrow();

            // Assert
            assertThat(pokemon.id()).isEqualTo(151);
            assertThat(pokemon.name()).isEqualTo("mew");
            assertThat(pokemon.height()).isEqualTo(4);
            assertThat(pokemon.types()).containsExactly("psychic");
            assertThat(pokemon.abilities()).containsExactly("synchronize");
        } finally {
            // Cleanup
            transactionTemplate.executeWithoutResult(status ->
                    jpaRepository.deleteById(151)
            );
        }
    }
}
