
package com.pokedex.presentation.controller;

import tools.jackson.databind.JsonNode;
import com.pokedex.application.dto.UpdatePokemonCustomDataCommand;
import com.pokedex.application.exception.InvalidPokemonCustomDataException;
import com.pokedex.application.usecase.UpdatePokemonCustomDataUseCase;
import com.pokedex.domain.model.PokemonCustomData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonCustomDataController {

    private final UpdatePokemonCustomDataUseCase useCase;

    public PokemonCustomDataController(
            UpdatePokemonCustomDataUseCase useCase
    ) {
        this.useCase = useCase;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PokemonCustomData> update(
            @PathVariable Integer id,
            @RequestBody JsonNode body
    ) {
        if (body == null || !body.isObject()) {
            throw new InvalidPokemonCustomDataException(
                    "Request body must be a JSON object"
            );
        }

        rejectUnknownFields(body);

        UpdatePokemonCustomDataCommand command =
                new UpdatePokemonCustomDataCommand(
                        body.has("customName"),
                        readNullableString(body, "customName"),
                        body.has("region"),
                        readNullableString(body, "region"),
                        body.has("tags"),
                        readTags(body)
                );

        PokemonCustomData updated = useCase.execute(id, command);

        return ResponseEntity.ok(updated);
    }

    private String readNullableString(
            JsonNode body,
            String field
    ) {
        if (!body.has(field)) {
            return null;
        }

        JsonNode value = body.get(field);

        if (value.isNull()) {
            return null;
        }

        if (!value.isTextual()) {
            throw new InvalidPokemonCustomDataException(
                    field + " must be a string or null"
            );
        }

        return value.asText();
    }

    private List<String> readTags(JsonNode body) {
        if (!body.has("tags")) {
            return null;
        }

        JsonNode value = body.get("tags");

        if (!value.isArray()) {
            throw new InvalidPokemonCustomDataException(
                    "tags must be an array"
            );
        }

        List<String> tags = new ArrayList<>();

        for (JsonNode item : value) {
            if (!item.isTextual()) {
                throw new InvalidPokemonCustomDataException(
                        "Every tag must be a string"
                );
            }

            tags.add(item.asText());
        }

        return tags;
    }



    private void rejectUnknownFields(JsonNode body) {
        for (var property : body.properties()) {
            String field = property.getKey();

            if (!field.equals("customName")
                    && !field.equals("region")
                    && !field.equals("tags")) {
                throw new InvalidPokemonCustomDataException(
                        "Field cannot be updated: " + field
                );
            }
        }
    }


}
