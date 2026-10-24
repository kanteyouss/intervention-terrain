package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.TypeInterventionResponseDto;
import com.interventionTerrain.interventionTerrain.entity.TypeIntervention;
import org.springframework.stereotype.Service;

/**
 * Service de conversion des types d'intervention.
 * Mapper pur : aucun repository, aucune écriture — il convertit uniquement
 * l'entité {@link TypeIntervention} en {@link TypeInterventionResponseDto}.
 */
@Service
public class TypeInterventionService {

    /**
     * Convertit une entité type d'intervention en objet de réponse.
     *
     * @param typeIntervention L'entité type à convertir.
     * @return TypeInterventionResponseDto La réponse du type d'intervention.
     */
    public TypeInterventionResponseDto toDto(TypeIntervention typeIntervention) {
        return TypeInterventionResponseDto.builder()
                .id(typeIntervention.getId())
                .nom(typeIntervention.getNom())
                .description(typeIntervention.getDescription())
                .delaiHeures(typeIntervention.getDelaiHeures())
                .build();
    }
}
