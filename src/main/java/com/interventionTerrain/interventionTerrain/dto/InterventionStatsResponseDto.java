package com.interventionTerrain.interventionTerrain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Réponse représentant les statistiques agrégées d'une intervention
 * (nombre de techniciens affectés et nombre de créneaux planifiés).
 * Une intervention connue sans technicien ni créneau obtient des compteurs à zéro.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterventionStatsResponseDto implements ResponseDto {
    private Long interventionId;
    private String reference;
    private String statut;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datePlanifiee;
    private String typeIntervention;
    private Long clientId;
    private String clientNom;
    private String clientPrenom;
    private Long nbTechniciens;
    private Long nbCreneaux;
}
