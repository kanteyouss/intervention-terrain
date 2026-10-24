package com.interventionTerrain.interventionTerrain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Requête de création ou de modification d'un technicien.
 * L'équipe associée est transmise via son identifiant.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TechnicienRequestDto implements RequestDto {
    private Long id;
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    private String email;
    private String telephone;
    @NotBlank(message = "Le matricule est obligatoire")
    @NotBlank(message = "Le matricule est obligatoire")
    private String matricule;
    @NotNull(message = "L'équipe est obligatoire")
    private Long equipeId;
}
