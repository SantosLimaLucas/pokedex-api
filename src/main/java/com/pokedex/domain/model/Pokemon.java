package com.pokedex.domain.model;

import java.util.List;

public record Pokemon(
        Integer id,
        String name,
        String spriteUrl,
        List<String> types,
        Integer weight,
        List<String> abilities
) {
    public Pokemon {
        types = types == null ? List.of() : List.copyOf(types);
        abilities = abilities == null ? List.of() : List.copyOf(abilities);
    }
}
