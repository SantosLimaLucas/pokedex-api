
package com.pokedex.presentation.exception;

import com.pokedex.application.exception.InvalidPokemonCustomDataException;
import com.pokedex.application.exception.PokemonNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class PokemonCustomDataExceptionHandler {

    @ExceptionHandler(PokemonNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            PokemonNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidPokemonCustomDataException.class)
    public ResponseEntity<Map<String, String>> handleInvalidData(
            InvalidPokemonCustomDataException exception
    ) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", exception.getMessage()));
    }
}
