
package com.pokedex.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pokemon")
@Getter
public class PokemonEntity {

    @Id
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "height")
    private Integer height;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "pokemon_type",
            joinColumns = @JoinColumn(name = "pokemon_id")
    )
    @Column(name = "type_name", nullable = false)
    private List<String> types = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "pokemon_ability",
            joinColumns = @JoinColumn(name = "pokemon_id")
    )
    @Column(name = "ability_name", nullable = false)
    private List<String> abilities = new ArrayList<>();

    @Column(name = "description")
    private String description;

    @Column(name = "hp")
    private Integer hp;

    @Column(name = "attack")
    private Integer attack;

    @Column(name = "defense")
    private Integer defense;

    @Column(name = "special_attack")
    private Integer specialAttack;

    @Column(name = "special_defense")
    private Integer specialDefense;

    @Column(name = "speed")
    private Integer speed;

    @Column(name = "custom_name", length = 100)
    private String customName;

    @Column(name = "region", length = 100)
    private String region;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "pokemon_tag",
            joinColumns = @JoinColumn(name = "pokemon_id")
    )
    @Column(name = "tag_name", nullable = false)
    private List<String> tags = new ArrayList<>();

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PokemonEntity() {
        // Required by JPA
    }

    public PokemonEntity(
            Integer id,
            String name,
            String imageUrl,
            Integer weight,
            Integer height
    ) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.weight = weight;
        this.height = height;

        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void updateOfficialData(
            String name,
            String imageUrl,
            Integer weight,
            Integer height,
            List<String> types,
            List<String> abilities
    ) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.weight = weight;
        this.height = height;

        this.types.clear();
        this.types.addAll(types);

        this.abilities.clear();
        this.abilities.addAll(abilities);

        this.updatedAt = LocalDateTime.now();
        this.lastSyncedAt = this.updatedAt;
    }

    public void updateDetails(
            String description,
            Integer hp,
            Integer attack,
            Integer defense,
            Integer specialAttack,
            Integer specialDefense,
            Integer speed
    ) {
        this.description = description;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;

        this.updatedAt = LocalDateTime.now();
    }

    public void updateCustomData(
            String customName,
            String region,
            List<String> tags
    ) {
        this.customName = customName;
        this.region = region;

        this.tags.clear();
        this.tags.addAll(tags);

        this.updatedAt = LocalDateTime.now();
    }

}
