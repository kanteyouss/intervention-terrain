package com.interventionTerrain.interventionTerrain.entity;
import jakarta.persistence.*;
import lombok.*;
/**
 * Représente le type d'intervention que nous pouvons effectuer sur le terrain.
 */
@Entity
@Table(name = "types_intervention", indexes = {
    @Index(name = "idx_types_intervention_nom", columnList = "nom")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TypeIntervention {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nom;
    @Column(length = 500)
    private String description;
    @Column(nullable = false)
    private Integer delaiHeures;
}
