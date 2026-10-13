
package com.pokedex.domain.model;

import java.util.List;

public record PokemonCustomData(
        Integer id,
        String customName,
        String region,
        List<String> tags
) {
    public PokemonCustomData {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
