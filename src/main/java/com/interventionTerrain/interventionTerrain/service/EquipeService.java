package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.EquipeResponseDto;
import com.interventionTerrain.interventionTerrain.entity.Equipe;
import org.springframework.stereotype.Service;

/**
 * Service de conversion des équipes.
 * Mapper pur : aucun repository, aucune écriture — il convertit uniquement
 * l'entité {@link Equipe} en {@link EquipeResponseDto}, en déléguant la
 * conversion de l'agence imbriquée à {@link AgenceService}.
 */
@Service
public class EquipeService {

    private final AgenceService agenceService;

    /**
     * Constructeur injectant le service des agences (conversion imbriquée).
     *
     * @param agenceService le service des agences
     */
    public EquipeService(AgenceService agenceService) {
        this.agenceService = agenceService;
    }

    /**
     * Convertit une entité équipe en objet de réponse.
     *
     * @param equipe L'entité équipe à convertir.
     * @return EquipeResponseDto La réponse de l'équipe.
     */
    public EquipeResponseDto toDto(Equipe equipe) {
        return EquipeResponseDto.builder()
                .id(equipe.getId())
                .nom(equipe.getNom())
                .agence(agenceService.toAgenceResponse(equipe.getAgence()))
                .build();
    }
}
