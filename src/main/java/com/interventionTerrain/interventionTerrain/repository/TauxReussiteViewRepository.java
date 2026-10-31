package com.interventionTerrain.interventionTerrain.repository;

import com.interventionTerrain.interventionTerrain.entity.TauxReussiteView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Spring Data JPA pour la vue SQL v_taux_reussite_agence (Règle F19).
 * Permet de récupérer les données agrégées de réussite sans faire appel directement à EntityManager.
 */
@Repository
public interface TauxReussiteViewRepository extends JpaRepository<TauxReussiteView, Long> {

    @Query("SELECT v FROM TauxReussiteView v WHERE v.agenceId = :agenceId AND v.mois = :mois")
    List<TauxReussiteView> findByAgenceIdAndMois(@Param("agenceId") Long agenceId, @Param("mois") String mois);
}
