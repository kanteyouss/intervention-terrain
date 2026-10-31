package com.interventionTerrain.interventionTerrain.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TechnicienCompetenceResponseDto implements ResponseDto {
    private Long id;
    private String libelle;
    private String nom;
    private String description;
    private LocalDate dateObtention;
    private LocalDate dateExpiration;
    private boolean expiree;
}