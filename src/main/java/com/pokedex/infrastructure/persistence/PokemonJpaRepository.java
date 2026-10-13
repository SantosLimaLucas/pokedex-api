package com.pokedex.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PokemonJpaRepository
        extends JpaRepository<PokemonEntity, Integer> {
}