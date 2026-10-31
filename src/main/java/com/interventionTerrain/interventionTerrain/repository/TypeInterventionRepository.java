package com.interventionTerrain.interventionTerrain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.interventionTerrain.interventionTerrain.entity.TypeIntervention;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeInterventionRepository extends JpaRepository<TypeIntervention, Long> {

    @Query(value = "SELECT DISTINCT tic.competence_id FROM type_intervention_competence tic "
            + "WHERE tic.type_intervention_id = :typeInterventionId", nativeQuery = true)
    List<Long> findCompetenceIdsByTypeInterventionId(@Param("typeInterventionId") Long typeInterventionId);

    Optional<TypeIntervention> findByNomIgnoreCase(String nom);

    Optional<TypeIntervention> findFirstByNomContainingIgnoreCase(String nom);
}