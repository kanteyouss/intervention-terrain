package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.entity.AlerteCompetence;
import com.interventionTerrain.interventionTerrain.entity.Competence;
import com.interventionTerrain.interventionTerrain.entity.TechnicienCompetence;
import com.interventionTerrain.interventionTerrain.exception.ParametresInvalidesException;
import com.interventionTerrain.interventionTerrain.repository.AlerteCompetenceRepository;
import com.interventionTerrain.interventionTerrain.repository.TechnicienCompetenceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlerteCompetenceService {

    @PersistenceContext
    private EntityManager entityManager;

    private final TechnicienCompetenceRepository technicienCompetenceRepository;
    private final AlerteCompetenceRepository alerteCompetenceRepository;

    public AlerteCompetenceService(TechnicienCompetenceRepository technicienCompetenceRepository,
                                   AlerteCompetenceRepository alerteCompetenceRepository) {
        this.technicienCompetenceRepository = technicienCompetenceRepository;
        this.alerteCompetenceRepository = alerteCompetenceRepository;
    }

    /**
     * Tâche planifiée exécutée chaque nuit à minuit.
     * Règle F16 : signale toute compétence expirant dans les 30 jours.
     * Traçabilité : une compétence déjà signalée (est_traitee = TRUE) n'est pas
     * signalée deux fois grâce à la vérification en base avant insertion.
     */
    @Scheduled(fixedDelayString = "86400000") // 24h en millisecondes
    @Transactional
    public void genererAlertesExpiration() {
        LocalDate aujourdhui = LocalDate.now();
        LocalDate finFenetre = aujourdhui.plusDays(30);

        // 1. Récupérer toutes les techno-competences ayant une dateExpiration dans la fenêtre
        List<TechnicienCompetence> technoCompetences = technicienCompetenceRepository.findAll().stream()
                .filter(tc -> tc.getDateExpiration() != null)
                .filter(tc -> !tc.getDateExpiration().isBefore(aujourdhui))
                .filter(tc -> tc.getDateExpiration().isBefore(finFenetre))
                .toList();

        // 2. Pour chacune, vérifier si une alerte non traitée existe déjà (traçabilité F16)
        for (TechnicienCompetence tc : technoCompetences) {
            // Requête JPQL : vérifie s'il existe déjà une alerte non traitée pour cette techno-competence
            Long count = entityManager.createQuery(
                            "SELECT COUNT(a) FROM AlerteCompetence a "
                            + "WHERE a.technicienCompetence.id = :tcId AND a.estTraitee = FALSE", Long.class)
                    .setParameter("tcId", tc.getId())
                    .getSingleResult();

            if (count == 0L) {
                // Pas d'alerte existante → on en crée une nouvelle
                AlerteCompetence alerte = AlerteCompetence.builder()
                        .technicienCompetence(tc)
                        .dateSignalement(LocalDateTime.now())
                        .estTraitee(false)
                        .build();
                alerteCompetenceRepository.save(alerte);
            }
        }
    }

    /**
     * Marque une alerte comme traitée (responsable a pris en compte).
     * @param alerteId l'identifiant de l'alerte
     */
    @Transactional
    public void marquerCommeTraitee(Long alerteId) {
        AlerteCompetence alerte = alerteCompetenceRepository.findById(alerteId)
                .orElseThrow(() -> new ParametresInvalidesException(
                        "Alerte non trouvée avec l'ID: " + alerteId));
        alerte.setEstTraitee(true);
        alerteCompetenceRepository.save(alerte);
    }

    /**
     * Récupère les alertes actives (non traitées) par agence.
     * @param agenceId l'identifiant de l'agence
     * @return liste des alertes actives
     */
    public List<AlerteCompetence> findAlertesNonTraiteesParAgence(Long agenceId) {
        return entityManager.createQuery(
                        "SELECT a FROM AlerteCompetence a "
                        + "JOIN a.technicienCompetence tc "
                        + "JOIN tc.technicien t "
                        + "WHERE a.estTraitee = FALSE AND t.equipe.agence.id = :agenceId", AlerteCompetence.class)
                .setParameter("agenceId", agenceId)
                .getResultList();
    }

    /**
     * Récupère les alertes actives pour une compétence donnée.
     * @param competenceId l'identifiant de la compétence
     * @return liste des alertes
     */
    public List<AlerteCompetence> findAlertesNonTraiteesParCompetence(Long competenceId) {
        return entityManager.createQuery(
                        "SELECT a FROM AlerteCompetence a "
                        + "JOIN a.technicienCompetence tc "
                        + "WHERE a.estTraitee = FALSE AND tc.competence.id = :competenceId", AlerteCompetence.class)
                .setParameter("competenceId", competenceId)
                .getResultList();
    }
}