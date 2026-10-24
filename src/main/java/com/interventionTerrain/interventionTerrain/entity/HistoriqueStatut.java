package com.interventionTerrain.interventionTerrain.entity;
import com.interventionTerrain.interventionTerrain.entity.Intervention;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
/**
 * Represente l'historique des changements de statut d'une intervention
 */
@Entity
@Table(name = "historique_statut")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoriqueStatut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime dateChangement;
    @Column(length = 180)
    private String auteur;
    @ManyToOne( )
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;
    @ManyToOne()
    @JoinColumn(name = "statut_id", nullable = false)
    private Statut statut;
}
