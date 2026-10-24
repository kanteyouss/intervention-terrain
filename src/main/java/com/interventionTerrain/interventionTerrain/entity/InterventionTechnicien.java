package com.interventionTerrain.interventionTerrain.entity;
import com.interventionTerrain.interventionTerrain.entity.Technicien;
import com.interventionTerrain.interventionTerrain.entity.Intervention;
import jakarta.persistence.*;
import lombok.*;
/**
 * Représente la table associative entre technicien et intervention.
 */
@Entity
@Table(name = "intervention_technicien", indexes = {
    @Index(name = "idx_it_intervention", columnList = "intervention_id"),
    @Index(name = "idx_it_technicien", columnList = "technicien_id")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterventionTechnicien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Boolean estResponsable = Boolean.FALSE;
    @ManyToOne
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;
    @ManyToOne
    @JoinColumn(name = "technicien_id", nullable = false)
    private Technicien technicien;
}
