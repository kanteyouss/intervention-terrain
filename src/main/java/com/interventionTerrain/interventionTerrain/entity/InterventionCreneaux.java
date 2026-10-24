package com.interventionTerrain.interventionTerrain.entity;

import com.interventionTerrain.interventionTerrain.entity.Creneau;
import com.interventionTerrain.interventionTerrain.entity.Intervention;
import jakarta.persistence.*;
import lombok.*;
/**
 * Représente la table associative entre créneau et intervention.
 */

@Entity
@Table(name = "intervention_creneau", indexes = {
    @Index(name = "idx_ic_intervention", columnList = "intervention_id"),
    @Index(name = "idx_ic_creneau", columnList = "creneau_id")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterventionCreneaux {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "creneau_id")
    private Creneau creneau;
    @ManyToOne
    @JoinColumn(name = "intervention_id")
    private Intervention intervention;


}
