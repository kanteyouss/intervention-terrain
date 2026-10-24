package com.interventionTerrain.interventionTerrain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Réponse représentant une compétence suivie par un technicien.
 * La compétence expirée est signalée par le drapeau {@code expiree}
 * plutôt que masquée, conformément au cahier des charges.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TechnicienCompetenceResponseDto implements ResponseDto {
    private Long id;
    private String nom;
    private String description;
    private LocalDate dateObtention;
    private LocalDate dateExpiration;
    /**
     * Indique si la date d'expiration est renseignée et déjà dépassée.
     */
    private boolean expiree;
}
