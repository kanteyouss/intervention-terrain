package com.interventionTerrain.interventionTerrain.exception;

import com.interventionTerrain.interventionTerrain.utils.response.Response;
import com.interventionTerrain.interventionTerrain.utils.response.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire global des exceptions de l'application.
 * Il centralise le format des réponses d'erreur afin que tous les endpoints
 * renvoient la même enveloppe {@link Response} :
 * <ul>
 *     <li>erreur de validation des champs : 400 ;</li>
 *     <li>paramètre de requête du mauvais type : 400 ;</li>
 *     <li>ressource introuvable : 404 ;</li>
 *     <li>doublon, ressource utilisée ou règle métier violée : 409.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalException {

    /**
     * Gère les erreurs de validation des requêtes annotées {@code @Valid}.
     *
     * @param ex l'exception de validation levée par Spring
     * @return la liste des erreurs par champ, dans l'enveloppe standard, avec le code 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        List<Map<String, String>> listeDerreurs = new ArrayList<>();
        for (FieldError error : ex.getFieldErrors()) {
            Map<String, String> detailErreur = new HashMap<>();
            detailErreur.put("champ", error.getField());
            detailErreur.put("message", error.getDefaultMessage());
            listeDerreurs.add(detailErreur);
        }
        Response<Map<String, String>> response = new Response<>();
        response.setData(listeDerreurs);
        response.setStatus(new Status(HttpStatus.BAD_REQUEST.value(), "Erreur de validation"));
        response.setHasError(true);
        response.setNombre((long) listeDerreurs.size());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les erreurs de ressource introuvable.
     *
     * @param ex l'exception de ressource introuvable
     * @return le message d'erreur encapsulé, avec le code 404
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Response<String>> notFoundException(NotFoundException ex) {
        Response<String> reponse = new Response<>(ex.getMessage());
        reponse.setNombre(1L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.NOT_FOUND.value(), "l'operation a echouée"));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(reponse);
    }

    /**
     * Gère les tentatives de création ou de modification créant un doublon.
     *
     * @param ex l'exception de doublon
     * @return le message d'erreur encapsulé, avec le code 409
     */
    @ExceptionHandler(DoublonRessourceException.class)
    public ResponseEntity<Response<String>> doublonRessourceException(DoublonRessourceException ex) {
        Response<String> reponse = new Response<>(ex.getMessage());
        reponse.setNombre(1L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.CONFLICT.value(), "Doublon de ressource"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(reponse);
    }

    /**
     * Gère les suppressions refusées car une autre ressource dépend de celle visée.
     *
     * @param ex l'exception de ressource utilisée
     * @return le message d'erreur encapsulé, avec le code 409
     */
    @ExceptionHandler(RessourceUtiliseeException.class)
    public ResponseEntity<Response<String>> ressourceUtiliseeException(RessourceUtiliseeException ex) {
        Response<String> reponse = new Response<>(ex.getMessage());
        reponse.setNombre(1L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.CONFLICT.value(), "Suppression impossible"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(reponse);
    }

    /**
     * Gère les règles métier violées (dates incohérentes, champ figé, statut
     * incompatible, etc.).
     *
     * @param ex l'exception métier
     * @return le message d'erreur encapsulé, avec le code 409
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<String>> illegalArgumentException(IllegalArgumentException ex) {
        Response<String> reponse = new Response<>(ex.getMessage());
        reponse.setNombre(1L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.CONFLICT.value(), "Règle métier non respectée"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(reponse);
    }

    /**
     * Gère les paramètres de requête refusés par une règle d'entrée
     * (statut inconnu, période incohérente ou trop longue...).
     *
     * @param ex l'exception de paramètre refusé
     * @return le message d'erreur encapsulé, avec le code 400
     */
    @ExceptionHandler(ParametresInvalidesException.class)
    public ResponseEntity<Response<String>> parametresInvalidesException(ParametresInvalidesException ex) {
        Response<String> reponse = new Response<>(ex.getMessage());
        reponse.setNombre(1L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.BAD_REQUEST.value(), "Paramètre de requête invalide"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(reponse);
    }

    /**
     * Gère les paramètres de requête qui ne peuvent pas être convertis dans le
     * type attendu (par exemple un texte à la place d'un identifiant numérique).
     *
     * @param ex l'exception de conversion de paramètre
     * @return le message d'erreur encapsulé, avec le code 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<String>> methodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        String message = "Le paramètre '" + ex.getName() + "' n'a pas le type attendu";
        Response<String> reponse = new Response<>(message);
        reponse.setNombre(1L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.BAD_REQUEST.value(), "Paramètre invalide"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(reponse);
    }
}
