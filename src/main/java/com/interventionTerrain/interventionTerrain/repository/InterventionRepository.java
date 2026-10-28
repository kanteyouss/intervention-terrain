package com.interventionTerrain.interventionTerrain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.interventionTerrain.interventionTerrain.entity.Intervention;
import com.interventionTerrain.interventionTerrain.entity.InterventionStatsView;
import com.interventionTerrain.interventionTerrain.entity.InterventionTechnicien;

/**
 * Repository JPA pour l'accès aux données des interventions.
 * Fournit les opérations CRUD standard sur la table "intervention"
 * ainsi que des requêtes personnalisées avec jointures.
 */
@Repository
public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    /**
     * Récupère les interventions d'un client.
     *
     * @param clientId L'identifiant du client.
     * @return List<Intervention> Les interventions du client, de la plus
     *         récente à la plus ancienne.
     */
    @Query("SELECT i FROM Intervention i WHERE i.client.id = :clientId ORDER BY i.datePlanifiee DESC")
    List<Intervention> findByClientId(@Param("clientId") Long clientId);

    /**
     * Récupère les interventions par statut.
     *
     * @param statut Le statut recherché.
     * @return List<Intervention> Les interventions correspondantes.
     */
    @Query("SELECT i FROM Intervention i WHERE i.statut = :statut")
    List<Intervention> findByStatut(@Param("statut") String statut);

    /**
     * Récupère les interventions affectées à un technicien.
     *
     * @param technicienId L'identifiant du technicien.
     * @return List<Intervention> Les interventions du technicien.
     */
    @Query("SELECT it.intervention FROM InterventionTechnicien it WHERE it.technicien.id = :technicienId")
    List<Intervention> findByTechnicienId(@Param("technicienId") Long technicienId);

    /**
     * Récupère la ligne de statistiques agrégées d'une intervention depuis la vue v_intervention_stats.
     *
     * @param id L'identifiant de l'intervention.
     * @return Optional<InterventionStatsView> La ligne de statistiques, le cas échéant.
     */
    @Query("SELECT v FROM InterventionStatsView v WHERE v.id = :id")
    Optional<InterventionStatsView> findInterventionStats(@Param("id") Long id);

    /**
     * Vérifie qu'un client possède au moins une intervention.
     * Utilisé pour refuser la suppression d'un client.
     *
     * @param clientId l'identifiant du client
     * @return vrai si le client a au moins une intervention
     */
    boolean existsByClientId(Long clientId);

    /**
     * Récupère une page d'interventions triée par date planifiée puis par heure
     * de début du créneau associé. Le tri secondaire passe par la table de
     * liaison intervention_creneau, sans créer de table ni de relation nouvelle.
     * La sous-requête scalaire évite toute duplication de lignes dans la page.
     *
     * @param pageable la pagination demandée (sans tri, le tri est dans la requête)
     * @return une page d'interventions triées
     */
    @Query(value = "SELECT i.* FROM intervention i ORDER BY i.date_planifiee, "
            + "(SELECT MIN(c.date_debut) FROM intervention_creneau ic "
            + "JOIN creneau c ON c.id = ic.creneau_id WHERE ic.intervention_id = i.id)",
            countQuery = "SELECT COUNT(*) FROM intervention", nativeQuery = true)
    Page<Intervention> findAllTriees(Pageable pageable);

    /**
     * Filtre les interventions par statut et par période, puis les trie par date
     * planifiée puis par heure de début du créneau (même tri que
     * {@link #findAllTriees}). Les critères sont optionnels : un statut vide
     * signifie « aucun filtre sur le statut », et les dates sentinelle couvrent
     * l'absence de borne. Aucune table ni relation nouvelle : uniquement des
     * clauses WHERE sur l'existant.
     *
     * @param statut le statut recherché (chaîne vide = aucun filtre)
     * @param du borne basse de période (datetime, inclusive)
     * @param auFin borne haute de période (datetime, exclusive)
     * @param pageable la pagination demandée (sans tri, le tri est dans la requête)
     * @return la page d'interventions correspondante
     */
    @Query(value = "SELECT i.* FROM intervention i "
            + "WHERE (:statut = '' OR LOWER(i.statut) = LOWER(:statut)) "
            + "AND i.date_planifiee >= :du AND i.date_planifiee < :auFin "
            + "ORDER BY i.date_planifiee, "
            + "(SELECT MIN(c.date_debut) FROM intervention_creneau ic "
            + "JOIN creneau c ON c.id = ic.creneau_id WHERE ic.intervention_id = i.id)",
            countQuery = "SELECT COUNT(*) FROM intervention i "
                    + "WHERE (:statut = '' OR LOWER(i.statut) = LOWER(:statut)) "
                    + "AND i.date_planifiee >= :du AND i.date_planifiee < :auFin",
            nativeQuery = true)
    Page<Intervention> filtrer(@Param("statut") String statut,
                               @Param("du") LocalDateTime du,
                               @Param("auFin") LocalDateTime auFin,
                               Pageable pageable);

    /**
     * Récupère les interventions d'un technicien sur une période : le planning
     * du technicien, trié par date puis par heure de début du créneau.
     *
     * @param technicienId l'identifiant du technicien
     * @param du début de période (datetime, inclusive)
     * @param auFin fin de période (datetime, exclusive)
     * @return les interventions du technicien, triées
     */
    @Query(value = "SELECT i.* FROM intervention i "
            + "JOIN intervention_technicien it ON it.intervention_id = i.id "
            + "WHERE it.technicien_id = :technicienId "
            + "AND i.date_planifiee >= :du AND i.date_planifiee < :auFin "
            + "ORDER BY i.date_planifiee, "
            + "(SELECT MIN(c.date_debut) FROM intervention_creneau ic "
            + "JOIN creneau c ON c.id = ic.creneau_id WHERE ic.intervention_id = i.id)",
            nativeQuery = true)
    List<Intervention> planner(@Param("technicienId") Long technicienId,
                               @Param("du") LocalDateTime du,
                               @Param("auFin") LocalDateTime auFin);

    /**
     * Récupère les interventions par référence.
     *
     * @param reference La référence de l'intervention.
     * @return List<Intervention> Les interventions correspondantes.
     */
    @Query("SELECT i FROM Intervention i WHERE i.reference = :reference")
    List<Intervention> findByReference(@Param("reference") String reference);
}
