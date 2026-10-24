package com.interventionTerrain.interventionTerrain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.interventionTerrain.interventionTerrain.entity.Agence;

/**
 * Repository JPA pour l'accès aux données des agences.
 * Fournit les opérations CRUD standard sur la table "agence".
 */
@Repository
public interface AgenceRepository extends JpaRepository<Agence, Long> {

    /**
     * Recherche les agences portant exactement ce nom.
     * Retourne une liste car les noms ne sont pas contraints uniques
     * (plusieurs agences peuvent partager le même nom).
     *
     * @param nom Le nom des agences recherchées.
     * @return List<Agence> Les agences portant ce nom.
     */
    @Query("SELECT a FROM Agence a WHERE a.nom = :nom")
    List<Agence> findByNom(@Param("nom") String nom);

    /**
     * Vérifie l'existence d'une agence portant ce nom, sans tenir compte de la casse.
     *
     * @param nom le nom recherché
     * @return vrai si une agence porte déjà ce nom
     */
    boolean existsByNomIgnoreCase(String nom);

    /**
     * Vérifie l'existence d'une agence portant ce nom en excluant une agence donnée.
     * Utilisé en modification pour autoriser la conservation du nom courant.
     *
     * @param nom le nom recherché
     * @param id l'identifiant de l'agence à exclure du contrôle
     * @return vrai si une autre agence porte déjà ce nom
     */
    boolean existsByNomIgnoreCaseAndIdNot(String nom, Long id);
}
