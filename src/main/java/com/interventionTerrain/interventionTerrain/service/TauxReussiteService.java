package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.TauxReussiteDetailDto;
import com.interventionTerrain.interventionTerrain.dto.TauxReussiteGlobalDto;
import com.interventionTerrain.interventionTerrain.entity.Agence;
import com.interventionTerrain.interventionTerrain.entity.TauxReussiteView;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.repository.AgenceRepository;
import com.interventionTerrain.interventionTerrain.repository.TauxReussiteViewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service calculant le taux de réussite des interventions par agence et par mois (Règle F19).
 * Utilise les repositories Spring Data JPA sans requête directe via EntityManager.
 */
@Service
public class TauxReussiteService {

    private final TauxReussiteViewRepository tauxReussiteViewRepository;
    private final AgenceRepository agenceRepository;

    public TauxReussiteService(TauxReussiteViewRepository tauxReussiteViewRepository,
                               AgenceRepository agenceRepository) {
        this.tauxReussiteViewRepository = tauxReussiteViewRepository;
        this.agenceRepository = agenceRepository;
    }

    public TauxReussiteGlobalDto getTauxReussite(Long agenceId, String mois) {
        Agence agence = agenceRepository.findById(agenceId)
                .orElseThrow(() -> new NotFoundException("Agence introuvable avec l'ID " + agenceId));

        List<TauxReussiteView> lignes = tauxReussiteViewRepository.findByAgenceIdAndMois(agenceId, mois);

        if (lignes.isEmpty()) {
            return TauxReussiteGlobalDto.builder()
                    .agenceId(agenceId)
                    .agenceNom(agence.getNom())
                    .mois(mois)
                    .tauxGlobal(0.0)
                    .details(List.of())
                    .build();
        }

        String agenceNom = agence.getNom();

        List<TauxReussiteDetailDto> details = lignes.stream()
                .map(v -> {
                    long terminee = v.getNbTerminee() != null ? v.getNbTerminee() : 0L;
                    long echouee = v.getNbEchouee() != null ? v.getNbEchouee() : 0L;
                    double taux = (terminee + echouee) > 0
                            ? Math.round((terminee * 100.0 / (terminee + echouee)) * 100.0) / 100.0
                            : 0.0;
                    return TauxReussiteDetailDto.builder()
                            .typeInterventionId(v.getTypeInterventionId())
                            .typeInterventionNom(v.getTypeInterventionNom())
                            .nbTerminee(terminee)
                            .nbEchouee(echouee)
                            .taux(taux)
                            .build();
                })
                .toList();

        long totalTerminee = details.stream().mapToLong(TauxReussiteDetailDto::getNbTerminee).sum();
        long totalEchouee = details.stream().mapToLong(TauxReussiteDetailDto::getNbEchouee).sum();

        double tauxGlobal = (totalTerminee + totalEchouee) > 0
                ? Math.round((totalTerminee * 100.0 / (totalTerminee + totalEchouee)) * 100.0) / 100.0
                : 0.0;

        return TauxReussiteGlobalDto.builder()
                .agenceId(agenceId)
                .agenceNom(agenceNom)
                .mois(mois)
                .tauxGlobal(tauxGlobal)
                .details(details)
                .build();
    }
}
