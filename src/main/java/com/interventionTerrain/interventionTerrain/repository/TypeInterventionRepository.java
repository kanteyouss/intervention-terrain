package com.interventionTerrain.interventionTerrain.repository;

import com.interventionTerrain.interventionTerrain.entity.TypeIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'accès aux données des types d'intervention.
 * Nécessaire à la résolution des relations de l'entité Intervention.
 */
@Repository
public interface TypeInterventionRepository extends JpaRepository<TypeIntervention, Long> {

    /**
     * Récupère les identifiants des compétences requises pour un type d'intervention.
     * La table de jointure est consultée directement par requête SQL, sans création
     * d'entité JPA de relation supplémentaire.
     */
    @Query(value = "SELECT DISTINCT tic.competence_id FROM type_intervention_competence tic "
            + "WHERE tic.type_intervention_id = :typeInterventionId",
            nativeQuery = true)
    List<Long> findCompetenceIdsByTypeInterventionId(@Param("typeInterventionId") Long typeInterventionId);
}