package com.interventionTerrain.interventionTerrain.entity;
import jakarta.persistence.*;
import lombok.*;
/**
 * Represente la competence que peut avoir un  technicien
 */
@Entity
@Table(name = "competence", uniqueConstraints = {
    @UniqueConstraint(name = "uk_competence_nom", columnNames = "nom")
}, indexes = {
    @Index(name = "idx_competence_nom", columnList = "nom")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Competence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nom;
    @Column(length = 500)
    private String description;
}
