package com.interventionTerrain.interventionTerrain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Requête de création ou de modification d'un client.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientRequestDto implements RequestDto {
    private Long id;
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    private String email;
    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;
    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;
    @NotBlank(message = "La commune est obligatoire")
    private String commune;
}
