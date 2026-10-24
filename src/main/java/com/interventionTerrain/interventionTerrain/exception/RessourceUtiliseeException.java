package com.interventionTerrain.interventionTerrain.exception;

/**
 * Levée lorsqu'une ressource ne peut pas être supprimée car une autre ressource
 * en dépend encore. Elle est traduite en réponse HTTP 409 (CONFLICT) par
 * {@link GlobalException}.
 */
public class RessourceUtiliseeException extends RuntimeException {

    /**
     * Construit l'exception avec le message décrivant le blocage.
     *
     * @param message description du blocage
     */
    public RessourceUtiliseeException(String message) {
        super(message);
    }

    /**
     * Construit l'exception à partir de la ressource à supprimer et de la
     * ressource qui la bloque.
     *
     * @param ressource nom de la ressource que l'on tente de supprimer
     * @param id identifiant de la ressource que l'on tente de supprimer
     * @param dependance nom de la ressource qui empêche la suppression
     */
    public RessourceUtiliseeException(String ressource, long id, String dependance) {
        super("Impossible de supprimer la ressource " + ressource + " (ID: " + id + ") : elle est "
                + "actuellement utilisée par " + dependance);
    }
}
