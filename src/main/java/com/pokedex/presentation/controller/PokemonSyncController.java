
package com.pokedex.presentation.controller;

import com.pokedex.application.usecase.PokemonSyncStatus;
import com.pokedex.infrastructure.sync.PokemonSyncService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon/sync")
public class PokemonSyncController {

    private final PokemonSyncService syncService;

    public PokemonSyncController(PokemonSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping
    public ResponseEntity<PokemonSyncStatus> start() {
        if (!syncService.start()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(syncService.getStatus());
        }

        return ResponseEntity.accepted()
                .body(syncService.getStatus());
    }

    @GetMapping("/status")
    public PokemonSyncStatus status() {
        return syncService.getStatus();
    }
}
