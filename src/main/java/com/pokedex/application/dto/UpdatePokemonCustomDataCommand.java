
package com.pokedex.application.dto;

import java.util.List;

public record UpdatePokemonCustomDataCommand(
        boolean customNameProvided,
        String customName,
        boolean regionProvided,
        String region,
        boolean tagsProvided,
        List<String> tags
) {
}
