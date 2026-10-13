
package com.pokedex.application.usecase;

public record PokemonSyncStatus(
        String state,
        int total,
        int processed,
        int succeeded,
        int failed,
        String lastError
) {
}
