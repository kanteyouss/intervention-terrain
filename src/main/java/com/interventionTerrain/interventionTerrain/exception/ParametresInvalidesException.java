package com.interventionTerrain.interventionTerrain.exception;

/**
 * Levée lorsqu'un paramètre de requête est valide syntactiquement mais refuse
 * par une règle métier d'entrée (statut inconnu, période incohérente,
 * période trop longue...). Elle est traduite en réponse HTTP 400 (BAD REQUEST)
 * par {@link GlobalException}.
 */
public class ParametresInvalidesException extends RuntimeException {

    /**
     * Construit l'exception avec le message décrivant le paramètre refusé.
     *
     * @param message description du paramètre refusé
     */
    public ParametresInvalidesException(String message) {
        super(message);
    }
}
