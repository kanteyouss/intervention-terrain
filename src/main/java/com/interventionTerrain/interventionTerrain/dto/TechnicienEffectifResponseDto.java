package com.interventionTerrain.interventionTerrain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Réponse représentant un technicien de l'effectif d'une agence,
 * avec le nombre de ses compétences actuellement valides
 * (date d'expiration renseignée et non dépassée, ou sans expiration).
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TechnicienEffectifResponseDto implements ResponseDto {
    private Long id;
    private String nom;
    private String prenom;
    private String matricule;
    private Long nbCompetencesValides;
}
