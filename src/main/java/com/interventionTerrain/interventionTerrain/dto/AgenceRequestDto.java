package com.interventionTerrain.interventionTerrain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Requête de création ou de modification d'une agence.
 * La commune associée est transmise via son identifiant.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgenceRequestDto implements RequestDto {
    private Long id;
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    @NotBlank(message = "La ville est obligatoire")
    private String ville;
    private String responsable;
    @NotNull(message = "La commune est obligatoire")
    private Long communeId;
}
