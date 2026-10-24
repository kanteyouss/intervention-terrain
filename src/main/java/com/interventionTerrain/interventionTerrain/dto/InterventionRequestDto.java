package com.interventionTerrain.interventionTerrain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Requête de création ou de modification d'une intervention.
 * Le type d'intervention et le client sont transmis via leurs identifiants.
 * Le technicien et le créneau sont optionnels (associés via tables associatives).
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterventionRequestDto implements RequestDto {
    private Long id;
    @NotNull(message = "La date planifiée est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime datePlanifiee;
    private String statut;
    private String commentaire;
    private String reference;
    @NotNull(message = "Le type d'intervention est obligatoire")
    private Long typeInterventionId;
    @NotNull(message = "Le client est obligatoire")
    private Long clientId;
    @NotNull(message = "Le technicien est obligatoire")
    private Long technicienId;
    @NotNull(message = "Le créneau est obligatoire")
    private Long creneauId;
}
