package com.interventionTerrain.interventionTerrain.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.interventionTerrain.interventionTerrain.service.AbstractClean;
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
public class ClientRequestDto extends AbstractClean implements RequestDto {
    private Long id;
    @NotBlank(message = "le nom est obligatoire")
    private String nom;
    @NotBlank(message = "le prenom est obligatoire")
    private String prenom;
    private String email;
    @NotBlank(message = "le telephone est obligatoire")
    private String telephone;
    @NotBlank(message = "l'adresse est obligatoire")
    private String adresse;
    @NotBlank(message = "la commune est obligatoire")
    private String commune;

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
    public void setAdresse(String adresse) {
        this.adresse = cleanText(adresse);
    }

    @JsonSetter
    public void setCommune(String commune) {
        this.commune = cleanText(commune);
    }
}