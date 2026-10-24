package com.interventionTerrain.interventionTerrain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Entité mappée en lecture seule sur la vue v_intervention_stats.
 * Elle porte les statistiques agrégées (techniciens et créneaux) d'une intervention.
 * La vue doit exister en base : elle est créée à la main, comme v_client_stats
 * dans billetterie (définition et procédure dans VUES.md).
 */
@Entity
@Table(name = "v_intervention_stats")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InterventionStatsView {

    @Id
    @Column(name = "intervention_id")
    private Long id;

    @Column(name = "reference")
    private String reference;

    @Column(name = "statut")
    private String statut;

    @Column(name = "date_planifiee")
    private LocalDateTime datePlanifiee;

    @Column(name = "type_intervention")
    private String typeIntervention;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "client_nom")
    private String clientNom;

    @Column(name = "client_prenom")
    private String clientPrenom;

    @Column(name = "nb_techniciens")
    private Long nbTechniciens;

    @Column(name = "nb_creneaux")
    private Long nbCreneaux;
}
