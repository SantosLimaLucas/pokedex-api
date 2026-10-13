
package com.pokedex.infrastructure.persistence;

import com.pokedex.application.exception.PokemonNotFoundException;
import com.pokedex.application.port.out.PokemonCustomDataRepository;
import com.pokedex.domain.model.PokemonCustomData;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PokemonCustomDataPersistenceAdapter
        implements PokemonCustomDataRepository {

    private final PokemonJpaRepository jpaRepository;

    public PokemonCustomDataPersistenceAdapter(
            PokemonJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PokemonCustomData> findById(Integer id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public PokemonCustomData save(PokemonCustomData customData) {
        PokemonEntity entity = jpaRepository
                .findById(customData.id())
                .orElseThrow(
                        () -> new PokemonNotFoundException(customData.id())
                );

        entity.updateCustomData(
                customData.customName(),
                customData.region(),
                new ArrayList<>(customData.tags())
        );

        PokemonEntity saved = jpaRepository.save(entity);

        return toDomain(saved);
    }

    private PokemonCustomData toDomain(PokemonEntity entity) {
        return new PokemonCustomData(
                entity.getId(),
                entity.getCustomName(),
                entity.getRegion(),
                List.copyOf(entity.getTags())
        );
    }
}
