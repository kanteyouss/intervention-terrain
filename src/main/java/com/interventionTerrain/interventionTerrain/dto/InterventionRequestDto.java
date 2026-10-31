package com.interventionTerrain.interventionTerrain.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.interventionTerrain.interventionTerrain.service.AbstractClean;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * Requête de création ou de modification d'une intervention.
 * Le type d'intervention et le client sont transmis via leurs identifiants.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterventionRequestDto extends AbstractClean implements RequestDto {
    private Long id;

    @NotNull(message = "La date planifiée est obligatoire")
    private LocalDate datePlanifiee;

    private String statut;

    @NotBlank(message = "Le commentaire est obligatoire")
    private String commentaire;

    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    @NotNull(message = "Le type d'intervention est obligatoire")
    private Long typeInterventionId;

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    @NotNull(message = "Le technicien est obligatoire")
    private Long technicienId;

    @NotNull(message = "Le créneau est obligatoire")
    private Long creneauId;

    @JsonSetter
    public void setReference(String reference) {
        this.reference = cleanText(reference);
    }

    @JsonSetter
    public void setCommentaire(String commentaire) {
        this.commentaire = cleanText(commentaire);
    }

    @JsonSetter
    public void setStatut(String statut) {
        this.statut = cleanText(statut);
    }
}