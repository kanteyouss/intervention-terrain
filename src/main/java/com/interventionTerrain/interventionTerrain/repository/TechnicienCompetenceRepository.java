package com.interventionTerrain.interventionTerrain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.interventionTerrain.interventionTerrain.entity.TechnicienCompetence;

import java.time.LocalDate;
import com.interventionTerrain.interventionTerrain.entity.TypeIntervention;

/**
 * Repository JPA pour l'accès aux données de la table associative
 * entre les techniciens et leurs compétences.
 */
@Repository
public interface TechnicienCompetenceRepository extends JpaRepository<TechnicienCompetence, Long> {

    /**
     * Récupère les compétences associées à un technicien.
     *
     * @param technicienId L'identifiant du technicien.
     * @return List<TechnicienCompetence> Les lignes associatives.
     */
    List<TechnicienCompetence> findByTechnicienId(Long technicienId);

    /**
     * Liste les types d'intervention dans lesquels une compétence est mobilisée.
     * Le chemin emprunté est : compétence → techniciens qui la possèdent →
     * interventions auxquelles ils sont affectés → type de ces interventions.
     * Aucune table ni relation nouvelle n'est créée : uniquement des jointures
     * sur les tables existantes.
     *
     * @param competenceId l'identifiant de la compétence
     * @return la liste des types d'intervention où la compétence est mobilisée
     */
    @Query("SELECT DISTINCT i.typeIntervention FROM Intervention i WHERE i.id IN ("
            + "SELECT it.intervention.id FROM InterventionTechnicien it WHERE it.technicien.id IN ("
            + "SELECT tc.technicien.id FROM TechnicienCompetence tc WHERE tc.competence.id = :competenceId))")
    List<TypeIntervention> findTypesInterventionParCompetence(@Param("competenceId") Long competenceId);

    /**
     * Compte les compétences encore valides de chaque technicien d'une agence.
     * Une compétence est valide si elle n'a pas de date d'expiration ou si son
     * expiration n'est pas encore dépassée.
     *
     * @param agenceId l'identifiant de l'agence
     * @param jour la date du jour servant de référence
     * @return une ligne par technicien : {identifiant du technicien, nombre de compétences valides}
     */
    @Query("SELECT tc.technicien.id, COUNT(tc) FROM TechnicienCompetence tc "
            + "JOIN tc.technicien t WHERE t.equipe.agence.id = :agenceId "
            + "AND (tc.dateExpiration IS NULL OR tc.dateExpiration >= :jour) "
            + "GROUP BY tc.technicien.id")
    List<Object[]> compterValidesParAgence(@Param("agenceId") Long agenceId, @Param("jour") LocalDate jour);
}
