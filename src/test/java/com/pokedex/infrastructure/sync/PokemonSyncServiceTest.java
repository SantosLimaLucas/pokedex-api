
package com.pokedex.infrastructure.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PokemonSyncServiceTest {

    private PokemonSyncWorker worker;
    private PokemonSyncProgress progress;
    private PokemonSyncService service;

    @BeforeEach
    void setUp() {
        worker = mock(PokemonSyncWorker.class);
        progress = new PokemonSyncProgress();
        service = new PokemonSyncService(worker, progress);
    }

    @Test
    void shouldStartSynchronization() {
        boolean started = service.start();

        assertThat(started).isTrue();
        assertThat(service.getStatus().state())
                .isEqualTo("RUNNING");

        verify(worker).synchronize();
    }

    @Test
    void shouldRejectConcurrentSynchronization() {
        service.start();

        boolean secondAttempt = service.start();

        assertThat(secondAttempt).isFalse();
        verify(worker, times(1)).synchronize();
    }

    @Test
    void shouldTrackSynchronizationProgress() {
        progress.begin();
        progress.setTotal(3);

        progress.recordSuccess();
        progress.recordSuccess();
        progress.recordFailure("Pokemon not found");

        assertThat(service.getStatus().total()).isEqualTo(3);
        assertThat(service.getStatus().processed()).isEqualTo(3);
        assertThat(service.getStatus().succeeded()).isEqualTo(2);
        assertThat(service.getStatus().failed()).isEqualTo(1);

        progress.complete();

        assertThat(service.getStatus().state())
                .isEqualTo("COMPLETED");
    }

    @Test
    void shouldAllowAnotherSynchronizationAfterCompletion() {
        service.start();
        progress.complete();

        boolean restarted = service.start();

        assertThat(restarted).isTrue();
        verify(worker, times(2)).synchronize();
    }

    @Test
    void shouldReportSchedulingFailure() {
        doThrow(new IllegalStateException("Executor unavailable"))
                .when(worker).synchronize();

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.start()
        );

        assertThat(service.getStatus().state())
                .isEqualTo("FAILED");
    }
}
