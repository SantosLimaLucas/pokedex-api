
package com.pokedex.application.exception;

public class PokemonNotFoundException extends RuntimeException {

    public PokemonNotFoundException(Integer id) {
        super("Pokemon not found with id: " + id);
    }
}
