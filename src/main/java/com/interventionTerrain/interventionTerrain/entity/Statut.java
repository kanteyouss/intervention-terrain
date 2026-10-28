package com.interventionTerrain.interventionTerrain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.Setter;
/**
 * Représente status de intervention
 */
@Entity
@Table(name ="statut" )
@Getter
@Setter
public class Statut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String libelle ;
}
