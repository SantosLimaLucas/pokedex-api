
package com.pokedex.infrastructure.sync;

import com.pokedex.application.usecase.PokemonSyncStatus;
import org.springframework.stereotype.Service;

@Service
public class PokemonSyncService {

    private final PokemonSyncWorker worker;
    private final PokemonSyncProgress progress;

    public PokemonSyncService(
            PokemonSyncWorker worker,
            PokemonSyncProgress progress
    ) {
        this.worker = worker;
        this.progress = progress;
    }

    public boolean start() {
        if (!progress.begin()) {
            return false;
        }

        try {
            worker.synchronize();
            return true;
        } catch (RuntimeException exception) {
            progress.fail(exception.getMessage());
            throw exception;
        }
    }

    public PokemonSyncStatus getStatus() {
        return progress.snapshot();
    }
}
