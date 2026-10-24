package com.interventionTerrain.interventionTerrain.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

/**
 * Requête de création ou de modification d'une compétence.
 */
/**
 * Le type polymorphe est désactivé ici : les champs {id, nom} de cette DTO sont un
 * sous-ensemble strict de ceux d'autres RequestDto (Agence, Client, Technicien),
 * ce qui rendrait la déduction Jackson ambiguë. Le contrôleur lie directement ce
 * type concret, la déduction n'est donc pas nécessaire.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class CompetenceRequestDto implements RequestDto {
    private Long id;
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    private String description;
}
