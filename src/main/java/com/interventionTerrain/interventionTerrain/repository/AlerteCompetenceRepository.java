package com.interventionTerrain.interventionTerrain.repository;

import com.interventionTerrain.interventionTerrain.entity.AlerteCompetence;
import com.interventionTerrain.interventionTerrain.entity.TechnicienCompetence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AlerteCompetenceRepository extends JpaRepository<AlerteCompetence, Long> {

    /**
     * Récupère les alertes non traitées (pas de doublon F16).
     * @return liste des alertes non traitées
     */
    @Query("SELECT a FROM AlerteCompetence a WHERE a.estTraitee = FALSE")
    List<AlerteCompetence> findNonTraitees();

    /**
     * Compte les alertes non traitées par agence.
     * @param agenceId l'agence
     * @return le nombre d'alertes non traitées
     */
    @Query("SELECT COUNT(a) FROM AlerteCompetence a "
            + "JOIN a.technicienCompetence tc "
            + "JOIN tc.technicien t "
            + "JOIN t.equipe e "
            + "WHERE a.estTraitee = FALSE AND e.agence.id = :agenceId")
    long compterNonTraiteParAgence(@Param("agenceId") Long agenceId);

    /**
     * Récupère les alertes non traitées pour une compétence donnée.
     * @param competenceId la compétence
     * @return la liste des alertes
     */
    @Query("SELECT a FROM AlerteCompetence a "
            + "JOIN a.technicienCompetence tc "
            + "WHERE a.estTraitee = FALSE AND tc.competence.id = :competenceId")
    List<AlerteCompetence> findByCompetenceNonTraitee(@Param("competenceId") Long competenceId);
}