package com.pokedex.presentation.controller;

import com.pokedex.application.dto.PokemonPage;
import com.pokedex.application.usecase.GetPokemonDetailsUseCase;
import com.pokedex.application.usecase.ListPokemonUseCase;
import com.pokedex.domain.model.PokemonDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final ListPokemonUseCase listPokemonUseCase;
    private final GetPokemonDetailsUseCase getPokemonDetailsUseCase;

    public PokemonController(
            ListPokemonUseCase listPokemonUseCase,
            GetPokemonDetailsUseCase getPokemonDetailsUseCase
    ) {
        this.listPokemonUseCase = listPokemonUseCase;
        this.getPokemonDetailsUseCase = getPokemonDetailsUseCase;
    }

    @GetMapping
    public PokemonPage listPokemon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return listPokemonUseCase.execute(page, size);
    }

    @GetMapping("/{id}")
    public PokemonDetails getPokemonDetails(@PathVariable Integer id) {
        return getPokemonDetailsUseCase.execute(id);
    }
}