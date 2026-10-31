package com.interventionTerrain.interventionTerrain.exception;

import com.interventionTerrain.interventionTerrain.utils.response.Response;
import com.interventionTerrain.interventionTerrain.utils.response.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.*;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Response<String>> notFoundException(NotFoundException ex) {
        Response<String> reponse = new Response<>(Collections.emptyList());
        reponse.setNombre(0L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(reponse);
    }

    @ExceptionHandler(DoublonRessourceException.class)
    public ResponseEntity<Response<String>> doublonRessourceException(DoublonRessourceException ex) {
        Response<String> reponse = new Response<>(Collections.emptyList());
        reponse.setNombre(0L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.CONFLICT.value(), "Doublon de ressource : " + ex.getMessage()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(reponse);
    }

    @ExceptionHandler(RessourceUtiliseeException.class)
    public ResponseEntity<Response<String>> ressourceUtiliseeException(RessourceUtiliseeException ex) {
        Response<String> reponse = new Response<>(Collections.emptyList());
        reponse.setNombre(0L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.CONFLICT.value(), ex.getMessage()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(reponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<String>> illegalArgumentException(IllegalArgumentException ex) {
        Response<String> reponse = new Response<>(Collections.emptyList());
        reponse.setNombre(0L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(reponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Response<String>> handleIllegalStateException(IllegalStateException ex) {
        Response<String> reponse = new Response<>(Collections.emptyList());
        reponse.setNombre(0L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.CONFLICT.value(), ex.getMessage()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(reponse);
    }

    @ExceptionHandler(ParametresInvalidesException.class)
    public ResponseEntity<Response<String>> parametresInvalidesException(ParametresInvalidesException ex) {
        Response<String> reponse = new Response<>(Collections.emptyList());
        reponse.setNombre(0L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(reponse);
    }
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<Response<String>> handleMissingServletRequestParameter(org.springframework.web.bind.MissingServletRequestParameterException ex) {
        Response<String> reponse = new Response<>(Collections.emptyList());
        reponse.setNombre(0L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.BAD_REQUEST.value(), "Le paramètre requis '" + ex.getParameterName() + "' est manquant"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(reponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleGenericException(Exception ex) {
        ex.printStackTrace();
        Response<String> reponse = new Response<>(Collections.emptyList());
        reponse.setNombre(0L);
        reponse.setHasError(true);
        reponse.setStatus(new Status(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Une erreur interne est survenue"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(reponse);
    }
    /**
     * Gère les erreurs de type de paramètre dans la requête (ex. transmettre une chaîne au lieu d'un entier).
     *
     * Code HTTP : 400 BAD_REQUEST
     *
     * @param ex L'exception de non-correspondance de type.
     * @return ResponseEntity expliquant le type attendu.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<List<String>>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String typeBrut = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "";
        String typeAtendu;

        switch (typeBrut) {
            case "int", "Long", "Integer" -> typeAtendu = "entier";
            case "boolean", "Boolean" -> typeAtendu = "booléen";
            case "String" -> typeAtendu = "chaîne de caractères";
            case "LocalDateTime" -> typeAtendu = "date et heure au format 'dd-MM-yyyy HH:mm:ss'";
            default -> typeAtendu = typeBrut.isEmpty() ? "valide" : typeBrut;
        }

        Response<List<String>> response = new Response<>(Collections.emptyList());
        response.setData(null);
        response.setStatus(new Status(
                HttpStatus.BAD_REQUEST.value(),
                "Le parametre '" + ex.getName() + "' doit etre de type " + typeAtendu
        ));
        response.setHasError(true);
        response.setNombre(0L);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les erreurs de désérialisation du corps de la requête JSON
     * (ex. syntaxe JSON invalide, caractère non guillemeté, ou type incompatible dans le body).
     *
     * @param ex L'exception de lecture du message HTTP.
     * @return ResponseEntity décrivant l'erreur de formatage JSON avec le code HTTP 400 BAD_REQUEST.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Response<String> response = new Response<>(Collections.emptyList());
        response.setNombre(0L);
        response.setHasError(true);
        response.setStatus(new Status(
                HttpStatus.BAD_REQUEST.value(),
                "Les données envoyées sont incorrectes ou mal formatées."));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}