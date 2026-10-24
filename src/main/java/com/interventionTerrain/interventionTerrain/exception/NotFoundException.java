package com.interventionTerrain.interventionTerrain.exception;

/**
 * Exception levée lorsqu'une ressource demandée n'est pas trouvée en base de données.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Constructeur avec message d'erreur.
     *
     * @param message le message décrivant la ressource introuvable
     */
    public NotFoundException(String message) {
        super(message);
    }
}
