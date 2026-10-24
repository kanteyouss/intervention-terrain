package com.interventionTerrain.interventionTerrain.entity;
import com.interventionTerrain.interventionTerrain.entity.Commune;
import jakarta.persistence.*;
import lombok.*;
/**
 * Represente agence auquelle appartient un agent. Une agence est localisée dans une commune.
 */
@Entity
@Table(name = "agence", uniqueConstraints = {
    @UniqueConstraint(name = "uk_agence_nom", columnNames = "nom")
}, indexes = {
    @Index(name = "idx_agence_commune", columnList = "commune_id"),
    @Index(name = "idx_agence_nom", columnList = "nom")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Agence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nom;
    @Column(length = 100)
    private String ville;
    @Column(length = 120)
    private String responsable;
    @ManyToOne()
    @JoinColumn(name = "commune_id", nullable = false)
    private Commune commune;
}
