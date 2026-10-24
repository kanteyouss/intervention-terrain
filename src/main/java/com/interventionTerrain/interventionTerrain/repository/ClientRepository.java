package com.interventionTerrain.interventionTerrain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.interventionTerrain.interventionTerrain.entity.Client;

/**
 * Repository JPA pour l'accès aux données des clients.
 * Fournit les opérations CRUD standard sur la table "client".
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Recherche les clients portant exactement ce nom.
     * Retourne une liste car les noms ne sont pas contraints uniques.
     *
     * @param nom Le nom des clients recherchés.
     * @return List<Client> Les clients portant ce nom.
     */
    @Query("SELECT c FROM Client c WHERE c.nom = :nom")
    List<Client> findByNom(@Param("nom") String nom);
}