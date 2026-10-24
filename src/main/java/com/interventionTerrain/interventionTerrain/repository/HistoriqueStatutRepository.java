package com.interventionTerrain.interventionTerrain.repository;

import com.interventionTerrain.interventionTerrain.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository JPA pour l'accès à l'historique des changements de statut des
 * interventions. Chaque changement de statut y laisse une trace.
 */
@Repository
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Long> {
}
