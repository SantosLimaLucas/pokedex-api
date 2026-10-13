
package com.pokedex.infrastructure.persistence;

import com.pokedex.application.port.out.PokemonRepository;
import com.pokedex.domain.model.EvolutionPokemon;
import com.pokedex.domain.model.Pokemon;
import com.pokedex.domain.model.PokemonDetails;
import com.pokedex.domain.model.PokemonStats;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PokemonPersistenceAdapter implements PokemonRepository {

    private final PokemonJpaRepository jpaRepository;
    private final PokemonEvolutionJpaRepository evolutionRepository;

    public PokemonPersistenceAdapter(
            PokemonJpaRepository jpaRepository,
            PokemonEvolutionJpaRepository evolutionRepository
    ) {
        this.jpaRepository = jpaRepository;
        this.evolutionRepository = evolutionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pokemon> findById(Integer id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public Pokemon save(Pokemon pokemon) {
        PokemonEntity entity = jpaRepository.findById(pokemon.id())
                .orElseGet(() -> new PokemonEntity(
                        pokemon.id(),
                        pokemon.name(),
                        pokemon.spriteUrl(),
                        pokemon.weight(),
                        pokemon.height()
                ));

        entity.updateOfficialData(
                pokemon.name(),
                pokemon.spriteUrl(),
                pokemon.weight(),
                pokemon.height(),
                pokemon.types(),
                pokemon.abilities()
        );

        PokemonEntity savedEntity = jpaRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PokemonDetails> findDetailsById(Integer id) {
        return jpaRepository.findById(id)
                .filter(this::hasDetails)
                .map(entity -> {
                    List<PokemonEvolutionEntity> evolutions =
                            evolutionRepository.findByIdPokemonId(id);

                    return toDetails(entity, evolutions);
                });
    }

    @Override
    @Transactional
    public PokemonDetails saveDetails(PokemonDetails details) {
        PokemonEntity entity = jpaRepository.findById(details.id())
                .orElseGet(() -> new PokemonEntity(
                        details.id(),
                        details.name(),
                        details.imageUrl(),
                        details.weight(),
                        details.height()
                ));

        entity.updateOfficialData(
                details.name(),
                details.imageUrl(),
                details.weight(),
                details.height(),
                details.types(),
                details.abilities()
        );

        PokemonStats stats = details.stats();

        entity.updateDetails(
                details.description(),
                stats.hp(),
                stats.attack(),
                stats.defense(),
                stats.specialAttack(),
                stats.specialDefense(),
                stats.speed()
        );

        PokemonEntity savedEntity = jpaRepository.save(entity);

        // Remove the previous evolution tree before saving the new one.
        evolutionRepository.deleteByIdPokemonId(details.id());
        evolutionRepository.flush();

        List<PokemonEvolutionEntity> evolutions = new ArrayList<>();

        if (details.evolutionChain() != null) {
            collectEvolutions(
                    details.id(),
                    details.evolutionChain(),
                    null,
                    evolutions
            );
        }

        evolutionRepository.saveAll(evolutions);

        // Return official data together with the existing custom fields.
        return toDetails(savedEntity, evolutions);
    }

    private boolean hasDetails(PokemonEntity entity) {
        return entity.getHp() != null
                && entity.getAttack() != null
                && entity.getDefense() != null
                && entity.getSpecialAttack() != null
                && entity.getSpecialDefense() != null
                && entity.getSpeed() != null;
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

    private PokemonDetails toDetails(
            PokemonEntity entity,
            List<PokemonEvolutionEntity> evolutions
    ) {
        PokemonStats stats = new PokemonStats(
                entity.getHp(),
                entity.getAttack(),
                entity.getDefense(),
                entity.getSpecialAttack(),
                entity.getSpecialDefense(),
                entity.getSpeed()
        );

        EvolutionPokemon evolutionChain = buildEvolutionTree(evolutions);

        return new PokemonDetails(
                entity.getId(),
                entity.getName(),
                entity.getImageUrl(),
                List.copyOf(entity.getTypes()),
                entity.getWeight(),
                entity.getHeight(),
                List.copyOf(entity.getAbilities()),
                stats,
                entity.getDescription(),
                evolutionChain,
                entity.getCustomName(),
                entity.getRegion(),
                List.copyOf(entity.getTags())
        );
    }

    private void collectEvolutions(
            Integer pokemonId,
            EvolutionPokemon evolution,
            Integer parentId,
            List<PokemonEvolutionEntity> result
    ) {
        result.add(new PokemonEvolutionEntity(
                pokemonId,
                evolution.id(),
                parentId,
                evolution.name(),
                evolution.imageUrl()
        ));

        if (evolution.evolvesTo() != null) {
            for (EvolutionPokemon child : evolution.evolvesTo()) {
                collectEvolutions(
                        pokemonId,
                        child,
                        evolution.id(),
                        result
                );
            }
        }
    }

    private EvolutionPokemon buildEvolutionTree(
            List<PokemonEvolutionEntity> evolutions
    ) {
        if (evolutions.isEmpty()) {
            return null;
        }

        PokemonEvolutionEntity root = evolutions.stream()
                .filter(evolution ->
                        evolution.getParentEvolutionId() == null
                )
                .findFirst()
                .orElse(null);

        if (root == null) {
            return null;
        }

        return buildEvolutionNode(root, evolutions);
    }

    private EvolutionPokemon buildEvolutionNode(
            PokemonEvolutionEntity current,
            List<PokemonEvolutionEntity> evolutions
    ) {
        List<EvolutionPokemon> children = evolutions.stream()
                .filter(evolution ->
                        current.getId().evolutionId().equals(
                                evolution.getParentEvolutionId()
                        )
                )
                .map(child ->
                        buildEvolutionNode(child, evolutions)
                )
                .toList();

        return new EvolutionPokemon(
                current.getId().evolutionId(),
                current.getName(),
                current.getImageUrl(),
                children
        );
    }
}
