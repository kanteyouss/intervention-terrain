package com.interventionTerrain.interventionTerrain.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.interventionTerrain.interventionTerrain.service.AbstractClean;
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
public class TechnicienRequestDto extends AbstractClean implements RequestDto {
    private Long id;
    @NotBlank(message = "le nom est obligatoire")
    private String nom;
    @NotBlank(message = "le prenom est obligatoire")
    private String prenom;
    private String email;
    private String telephone;

    @NotBlank(message = "le nom est obligatoire")
    private String matricule;
    @NotNull(message = "L'identifiant de l'équipe est obligatoire")
    private Long equipeId;

    @JsonSetter
    public void setNom(String nom) {
        this.nom = cleanText(nom);
    }

    @JsonSetter
    public void setPrenom(String prenom) {
        this.prenom = cleanText(prenom);
    }

    @JsonSetter
    public void setEmail(String email) {
        this.email = cleanText(email);
    }

    @JsonSetter
    public void setTelephone(String telephone) {
        this.telephone = cleanText(telephone);
    }

    @JsonSetter
    public void setMatricule(String matricule) {
        this.matricule = cleanText(matricule);
    }
}