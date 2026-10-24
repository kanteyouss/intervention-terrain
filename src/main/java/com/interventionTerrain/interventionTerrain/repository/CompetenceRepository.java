package com.interventionTerrain.interventionTerrain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.interventionTerrain.interventionTerrain.entity.Competence;

/**
 * Repository JPA pour l'accès aux données des compétences.
 * Fournit les opérations CRUD standard sur la table "competence".
 */
@Repository
public interface CompetenceRepository extends JpaRepository<Competence, Long> {

    /**
     * Recherche les compétences portant exactement ce nom.
     * Retourne une liste car les noms ne sont pas contraints uniques.
     *
     * @param nom Le nom des compétences recherchées.
     * @return List<Competence> Les compétences portant ce nom.
     */
    @Query("SELECT c FROM Competence c WHERE c.nom = :nom")
    List<Competence> findByNom(@Param("nom") String nom);

    /**
     * Vérifie l'existence d'une compétence portant ce nom, sans tenir compte de la casse.
     *
     * @param nom le nom recherché
     * @return vrai si une compétence porte déjà ce nom
     */
    boolean existsByNomIgnoreCase(String nom);

    /**
     * Vérifie l'existence d'une compétence portant ce nom en excluant une
     * compétence donnée. Utilisé en modification.
     *
     * @param nom le nom recherché
     * @param id l'identifiant de la compétence à exclure du contrôle
     * @return vrai si une autre compétence porte déjà ce nom
     */
    boolean existsByNomIgnoreCaseAndIdNot(String nom, Long id);

    /**
     * Vérifie qu'une compétence est associée à au moins un technicien.
     * Utilisé pour refuser sa suppression.
     *
     * @param competenceId l'identifiant de la compétence
     * @return vrai si un technicien possède cette compétence
     */
    @Query("SELECT COUNT(tc) > 0 FROM TechnicienCompetence tc WHERE tc.competence.id = :competenceId")
    boolean existsTechnicienByCompetenceId(@Param("competenceId") Long competenceId);
}
