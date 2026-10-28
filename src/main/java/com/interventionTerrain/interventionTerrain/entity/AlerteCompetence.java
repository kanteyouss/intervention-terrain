package com.interventionTerrain.interventionTerrain.entity;

import com.interventionTerrain.interventionTerrain.entity.TechnicienCompetence;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Table unique pour le signalement des compétences proches expiration (Règle F16).
 * La traçabilité "déjà signalée" est assurée par la colonne est_traitee.
 * Toutes les infos techno/compétence sont récupérées par JOINs sur l'existant.
 */
@Entity
@Table(name = "alerte_competence")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlerteCompetence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "technicien_competence_id", nullable = false)
    private TechnicienCompetence technicienCompetence;

    @Column(name = "est_traitee", nullable = false)
    private boolean estTraitee = false; // Drapeau traçabilité F16 : empêche le doublon

    @Column(name = "date_signalement", nullable = false)
    private LocalDateTime dateSignalement;
}