package com.interventionTerrain.interventionTerrain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.interventionTerrain.interventionTerrain.service.AbstractClean;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompetenceRequestDto extends AbstractClean implements RequestDto {

    private Long id;

    @NotBlank(message = "Le libelle est obligatoire")
    private String libelle;

    private String description;

    private LocalDate dateObtention;

    private LocalDate dateExpiration;

    @JsonSetter
    public void setLibelle(String libelle) {
        this.libelle = cleanText(libelle);
    }

    @JsonSetter
    public void setDescription(String description) {
        this.description = cleanText(description);
    }
}