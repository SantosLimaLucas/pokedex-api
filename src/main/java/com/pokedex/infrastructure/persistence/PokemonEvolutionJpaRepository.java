
package com.pokedex.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PokemonEvolutionJpaRepository
        extends JpaRepository<
                PokemonEvolutionEntity,
                PokemonEvolutionEntity.PokemonEvolutionId
        > {

    List<PokemonEvolutionEntity> findByIdPokemonId(Integer pokemonId);

    void deleteByIdPokemonId(Integer pokemonId);
}
