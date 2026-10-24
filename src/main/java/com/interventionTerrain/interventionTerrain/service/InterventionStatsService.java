package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.InterventionStatsResponseDto;
import com.interventionTerrain.interventionTerrain.entity.InterventionStatsView;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.repository.InterventionRepository;
import org.springframework.stereotype.Service;

/**
 * Service métier des statistiques agrégées d'une intervention (techniciens et créneaux).
 * Il est le seul à connaître la conversion de la vue v_intervention_stats.
 * La lecture de la vue est portée par {@link InterventionRepository}, exactement comme
 * la lecture de v_client_stats est portée par ClientRepository dans billetterie.
 */
@Service
public class InterventionStatsService {

    private final InterventionRepository interventionRepository;

    /**
     * Constructeur injectant le repository des interventions, qui porte la lecture de la vue.
     *
     * @param interventionRepository le repository des interventions
     */
    public InterventionStatsService(InterventionRepository interventionRepository) {
        this.interventionRepository = interventionRepository;
    }

    /**
     * Convertit une ligne de la vue en objet de réponse.
     *
     * @param stats la ligne agrégée issue de la vue
     * @return InterventionStatsResponseDto les statistiques de l'intervention
     */
    public InterventionStatsResponseDto toDto(InterventionStatsView stats) {
        return InterventionStatsResponseDto.builder()
                .interventionId(stats.getId())
                .reference(stats.getReference())
                .statut(stats.getStatut())
                .datePlanifiee(stats.getDatePlanifiee())
                .typeIntervention(stats.getTypeIntervention())
                .clientId(stats.getClientId())
                .clientNom(stats.getClientNom())
                .clientPrenom(stats.getClientPrenom())
                .nbTechniciens(stats.getNbTechniciens())
                .nbCreneaux(stats.getNbCreneaux())
                .build();
    }

    /**
     * Récupère les statistiques techniciens/créneaux d'une intervention.
     * Une intervention connue sans technicien ni créneau renvoie des compteurs à zéro ;
     * une intervention inexistante n'apparaît pas dans la vue et provoque une erreur 404.
     *
     * @param interventionId l'identifiant de l'intervention
     * @return InterventionStatsResponseDto les statistiques agrégées de l'intervention
     * @throws NotFoundException si aucune intervention ne porte cet identifiant
     */
    public InterventionStatsResponseDto getStats(long interventionId) {
        InterventionStatsView stats = interventionRepository.findInterventionStats(interventionId)
                .orElseThrow(() -> new NotFoundException(CrudService.NOT_FOUND_MESSAGE));
        return toDto(stats);
    }
}
