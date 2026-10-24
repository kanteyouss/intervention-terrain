package com.interventionTerrain.interventionTerrain.exception;

/**
 * Levée lorsqu'une ressource viole une contrainte d'unicité métier.
 * Elle est traduite en réponse HTTP 409 (CONFLICT) par {@link GlobalException}.
 */
public class DoublonRessourceException extends RuntimeException {

    /**
     * Construit l'exception avec le message décrivant le doublon.
     *
     * @param message description du doublon constaté
     */
    public DoublonRessourceException(String message) {
        super(message);
    }
}
