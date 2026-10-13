
package com.pokedex.infrastructure.persistence;

import com.pokedex.application.dto.PokemonPage;
import com.pokedex.application.port.out.PokemonCatalog;
import com.pokedex.domain.model.Pokemon;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class PokemonDatabaseCatalogAdapter implements PokemonCatalog {

    private final PokemonJpaRepository jpaRepository;

    public PokemonDatabaseCatalogAdapter(
            PokemonJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PokemonPage findAll(int page, int size) {
        var pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "id")
        );

        var result = jpaRepository.findAll(pageable);

        List<Pokemon> pokemon = result.getContent()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PokemonPage(
                pokemon,
                page,
                size,
                Math.toIntExact(result.getTotalElements()),
                result.getTotalPages()
        );
    }

    private Pokemon toDomain(PokemonEntity entity) {
        return new Pokemon(
                entity.getId(),
                entity.getName(),
                entity.getImageUrl(),
                List.copyOf(entity.getTypes()),
                entity.getWeight(),
                entity.getHeight(),
                List.copyOf(entity.getAbilities())
        );
    }
}
