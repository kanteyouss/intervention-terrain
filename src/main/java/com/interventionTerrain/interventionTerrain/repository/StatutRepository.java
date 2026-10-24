package com.interventionTerrain.interventionTerrain.repository;

import com.interventionTerrain.interventionTerrain.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA pour l'accès au référentiel des statuts.
 * Il sert uniquement à retrouver l'identifiant d'un libellé de statut lors de
 * l'écriture d'une ligne d'historique.
 */
@Repository
public interface StatutRepository extends JpaRepository<Statut, Long> {

    /**
     * Recherche un statut par son libellé, sans tenir compte de la casse.
     *
     * @param libelle le libellé recherché (par exemple PLANIFIEE)
     * @return le statut correspondant, le cas échéant
     */
    @Query("SELECT s FROM Statut s WHERE LOWER(s.libelle) = LOWER(:libelle)")
    Optional<Statut> findByLibelleIgnoreCase(@Param("libelle") String libelle);
}
