package com.interventionTerrain.interventionTerrain.entity;
import com.interventionTerrain.interventionTerrain.entity.Departement;
import jakarta.persistence.*;
import lombok.*;
/**
 * Represente la commune
 */
@Entity
@Table(name = "commune", indexes = {
    @Index(name = "idx_commune_departement", columnList = "departement_id")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Commune {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nom;
    @Column(nullable = false, length = 10)
    private String codePostal;
    @ManyToOne()
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;
}
