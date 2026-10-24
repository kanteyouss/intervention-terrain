package com.interventionTerrain.interventionTerrain.utils.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente le statut d'une réponse HTTP (code et message).
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Status {
    private int code;
    private String message;
}
