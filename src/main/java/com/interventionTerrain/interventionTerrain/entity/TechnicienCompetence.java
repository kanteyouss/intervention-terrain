package com.interventionTerrain.interventionTerrain.entity;
import com.interventionTerrain.interventionTerrain.entity.Competence;
import com.interventionTerrain.interventionTerrain.entity.Technicien;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
/**
 * Represente la table associative entre les techniciens et leurs competences, avec les dates d'obtention et d'expiration de chaque competence.
 */
@Entity
@Table(name = "technicien_competence", indexes = {
    @Index(name = "idx_tec_technicien", columnList = "technicien_id"),
    @Index(name = "idx_tec_competence", columnList = "competence_id")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TechnicienCompetence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "technicien_id", nullable = false)
    private Technicien technicien;
    @ManyToOne
    @JoinColumn(name = "competence_id", nullable = false)
    private Competence competence;
    @Column(nullable = false)
    private LocalDate dateObtention;
    private LocalDate dateExpiration;
}
