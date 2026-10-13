
package com.pokedex.application.usecase;

import com.pokedex.application.dto.UpdatePokemonCustomDataCommand;
import com.pokedex.application.exception.InvalidPokemonCustomDataException;
import com.pokedex.application.exception.PokemonNotFoundException;
import com.pokedex.application.port.out.PokemonCustomDataRepository;
import com.pokedex.domain.model.PokemonCustomData;

import java.util.List;

public class UpdatePokemonCustomDataUseCase {

    private final PokemonCustomDataRepository repository;

    public UpdatePokemonCustomDataUseCase(
            PokemonCustomDataRepository repository
    ) {
        this.repository = repository;
    }

    public PokemonCustomData execute(
            Integer id,
            UpdatePokemonCustomDataCommand command
    ) {
        if (id == null || id <= 0) {
            throw new InvalidPokemonCustomDataException(
                    "Pokemon id must be greater than zero"
            );
        }

        if (command == null) {
            throw new InvalidPokemonCustomDataException(
                    "Request body is required"
            );
        }

        if (!command.customNameProvided()
                && !command.regionProvided()
                && !command.tagsProvided()) {
            throw new InvalidPokemonCustomDataException(
                    "At least one custom field must be provided"
            );
        }

        PokemonCustomData current = repository.findById(id)
                .orElseThrow(() -> new PokemonNotFoundException(id));

        String customName = command.customNameProvided()
                ? validateText(command.customName(), "customName", 100)
                : current.customName();

        String region = command.regionProvided()
                ? validateText(command.region(), "region", 100)
                : current.region();

        List<String> tags = command.tagsProvided()
                ? validateTags(command.tags())
                : current.tags();

        PokemonCustomData updated = new PokemonCustomData(
                id,
                customName,
                region,
                tags
        );

        return repository.save(updated);
    }

    private String validateText(
            String value,
            String field,
            int maxLength
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new InvalidPokemonCustomDataException(
                    field + " must not be blank"
            );
        }

        if (normalized.length() > maxLength) {
            throw new InvalidPokemonCustomDataException(
                    field + " must not exceed " + maxLength + " characters"
            );
        }

        return normalized;
    }

    private List<String> validateTags(List<String> tags) {
        if (tags == null) {
            throw new InvalidPokemonCustomDataException(
                    "tags must be an array"
            );
        }

        if (tags.size() > 20) {
            throw new InvalidPokemonCustomDataException(
                    "tags must not contain more than 20 items"
            );
        }

        List<String> normalized = tags.stream()
                .map(tag -> {
                    if (tag == null || tag.isBlank()) {
                        throw new InvalidPokemonCustomDataException(
                                "tags must not contain blank values"
                        );
                    }

                    String value = tag.trim();

                    if (value.length() > 50) {
                        throw new InvalidPokemonCustomDataException(
                                "Each tag must not exceed 50 characters"
                        );
                    }

                    return value;
                })
                .distinct()
                .toList();

        return normalized;
    }
}
