
package com.pokedex.presentation.dto;

import java.util.List;

public record UpdatePokemonCustomDataRequest(
        boolean customNameProvided,
        String customName,
        boolean regionProvided,
        String region,
        boolean tagsProvided,
        List<String> tags
) {
}
