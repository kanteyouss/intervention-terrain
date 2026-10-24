package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.CreneauResponseDto;
import com.interventionTerrain.interventionTerrain.entity.Creneau;
import org.springframework.stereotype.Service;

/**
 * Service de conversion des créneaux.
 * Mapper pur : aucun repository, aucune écriture — il convertit uniquement
 * l'entité {@link Creneau} en {@link CreneauResponseDto}.
 */
@Service
public class CreneauService {

    /**
     * Convertit une entité créneau en objet de réponse.
     * Un créneau absent (null) retourne null : une intervention peut
     * n'avoir aucune association de créneau.
     *
     * @param creneau L'entité créneau à convertir (peut être null).
     * @return CreneauResponseDto La réponse du créneau, ou null.
     */
    public CreneauResponseDto toDto(Creneau creneau) {
        if (creneau == null) {
            return null;
        }
        return CreneauResponseDto.builder()
                .id(creneau.getId())
                .dateDebut(creneau.getDateDebut())
                .dateFin(creneau.getDateFin())
                .build();
    }
}
