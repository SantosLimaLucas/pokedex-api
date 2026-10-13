
package com.pokedex.infrastructure.sync;

import com.pokedex.application.usecase.PokemonSyncStatus;
import org.springframework.stereotype.Component;

@Component
public class PokemonSyncProgress {

    private String state = "IDLE";
    private int total;
    private int processed;
    private int succeeded;
    private int failed;
    private String lastError;

    public synchronized boolean begin() {
        if ("RUNNING".equals(state)) {
            return false;
        }

        state = "RUNNING";
        total = 0;
        processed = 0;
        succeeded = 0;
        failed = 0;
        lastError = null;
        return true;
    }

    public synchronized void setTotal(int total) {
        this.total = total;
    }

    public synchronized void recordSuccess() {
        processed++;
        succeeded++;
    }

    public synchronized void recordFailure(String error) {
        processed++;
        failed++;
        lastError = error;
    }

    public synchronized void complete() {
        state = "COMPLETED";
    }

    public synchronized void fail(String error) {
        state = "FAILED";
        lastError = error;
    }

    public synchronized PokemonSyncStatus snapshot() {
        return new PokemonSyncStatus(
                state,
                total,
                processed,
                succeeded,
                failed,
                lastError
        );
    }
}
