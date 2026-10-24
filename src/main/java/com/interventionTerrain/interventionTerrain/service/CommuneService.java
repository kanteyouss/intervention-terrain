package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.CommuneResponseDto;
import com.interventionTerrain.interventionTerrain.entity.Commune;
import org.springframework.stereotype.Service;

/**
 * Service de conversion des communes.
 * Mapper pur : aucun repository, aucune écriture — il convertit uniquement
 * l'entité {@link Commune} en {@link CommuneResponseDto}, comme les services
 * mappers purs de flottevehicule (ContratService, PrestataireService).
 */
@Service
public class CommuneService {

    /**
     * Convertit une entité commune en objet de réponse.
     *
     * @param commune L'entité commune à convertir.
     * @return CommuneResponseDto La réponse de la commune.
     */
    public CommuneResponseDto toDto(Commune commune) {
        return CommuneResponseDto.builder()
                .id(commune.getId())
                .nom(commune.getNom())
                .codePostal(commune.getCodePostal())
                .departementId(commune.getDepartement().getId())
                .build();
    }
}
