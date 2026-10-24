package com.interventionTerrain.interventionTerrain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.interventionTerrain.interventionTerrain.entity.Technicien;

/**
 * Repository JPA pour l'accès aux données des techniciens.
 * Fournit les opérations CRUD standard sur la table "technicien"
 * ainsi qu'une requête personnalisée avec jointure vers l'agence.
 */
@Repository
public interface TechnicienRepository extends JpaRepository<Technicien, Long> {

    /**
     * Récupère les techniciens rattachés à une agence (via leur équipe).
     *
     * @param agenceId L'identifiant de l'agence.
     * @return List<Technicien> Les techniciens de l'agence.
     */
    @Query("SELECT t FROM Technicien t WHERE t.equipe.agence.id = :agenceId")
    List<Technicien> findByAgenceId(@Param("agenceId") Long agenceId);

    /**
     * Recherche les techniciens portant exactement ce nom.
     * Retourne une liste car les noms ne sont pas contraints uniques.
     *
     * @param nom Le nom des techniciens recherchés.
     * @return List<Technicien> Les techniciens portant ce nom.
     */
    @Query("SELECT t FROM Technicien t WHERE t.nom = :nom")
    List<Technicien> findByNom(@Param("nom") String nom);

    /**
     * Vérifie l'existence d'un technicien portant ce matricule, sans tenir compte
     * de la casse.
     *
     * @param matricule le matricule recherché
     * @return vrai si un technicien porte déjà ce matricule
     */
    boolean existsByMatriculeIgnoreCase(String matricule);

    /**
     * Vérifie l'existence d'un technicien portant ce matricule en excluant un
     * technicien donné. Utilisé en modification pour autoriser la conservation
     * du matricule courant.
     *
     * @param matricule le matricule recherché
     * @param id l'identifiant du technicien à exclure du contrôle
     * @return vrai si un autre technicien porte déjà ce matricule
     */
    boolean existsByMatriculeIgnoreCaseAndIdNot(String matricule, Long id);

    /**
     * Vérifie qu'une agence possède au moins un technicien, via les équipes.
     * Utilisé pour figer la ville d'une agence et pour refuser sa suppression.
     *
     * @param agenceId l'identifiant de l'agence
     * @return vrai si au moins un technicien est rattaché à cette agence
     */
    @Query("SELECT COUNT(t) > 0 FROM Technicien t WHERE t.equipe.agence.id = :agenceId")
    boolean existsByAgenceId(@Param("agenceId") Long agenceId);

    /**
     * Vérifie qu'un technicien possède au moins une intervention, via la table
     * associative intervention_technicien. Utilisé pour refuser sa suppression.
     *
     * @param technicienId l'identifiant du technicien
     * @return vrai si le technicien est affecté à au moins une intervention
     */
    @Query("SELECT COUNT(it) > 0 FROM InterventionTechnicien it WHERE it.technicien.id = :technicienId")
    boolean existsInterventionByTechnicienId(@Param("technicienId") Long technicienId);
}
