package com.interventionTerrain.interventionTerrain.dto;

import lombok.*;

/**
 * Réponse contenant les données d'un client.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientResponseDto implements ResponseDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String commune;
}