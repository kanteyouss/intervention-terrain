package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.RequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import org.springframework.data.domain.Page;

/**
 * Contrat définissant les opérations d'écriture (création, modification),
 * la lecture paginée et les règles de transformation (mapping) des services.
 * Chaque service spécifique (ex: AgenceService, TechnicienService) doit implémenter
 * ces méthodes pour garantir une architecture uniforme.
 * Seules des signatures y figurent : tous les corps d'implémentation sont
 * portés par {@link AbstractCrudService} ou par les services concrets.
 */
public interface CrudService {

    /**
     * Message d'erreur par défaut lorsque l'entité recherchée n'est pas trouvée en base de données.
     */
    String NOT_FOUND_MESSAGE = "La ressource demandée est introuvable.";

    /**
     * Nombre d'éléments retournés par défaut lorsqu'une taille de page invalide est transmise.
     */
    int TAILLE_DEFAUT = 10;

    /**
     * Nombre maximal d'éléments qu'une requête paginée est autorisée à retourner.
     */
    int TAILLE_MAX = 100;

    /**
     * Crée une nouvelle ressource en base de données à partir des données reçues.
     *
     * @param request l'objet {@link RequestDto} contenant les informations de création
     * @return un objet {@link ResponseDto} représentant la ressource sauvegardée
     */
    ResponseDto create(RequestDto request);

    /**
     * Met à jour une ressource existante en base de données.
     *
     * @param id l'identifiant unique de la ressource à modifier
     * @param request l'objet {@link RequestDto} contenant les informations
     * @return un objet {@link ResponseDto} représentant la ressource mise à jour
     * @throws com.interventionTerrain.interventionTerrain.exception.NotFoundException si la ressource correspondante n'est pas trouvée
     */
    ResponseDto update(Long id, RequestDto request);

    /**
     * Transforme une entité issue de la base de données en un DTO de réponse.
     * Cette méthode centralise la logique d'exposition des données vers le client.
     *
     * @param entity l'entité brute récupérée depuis la base de données
     * @return un objet {@link ResponseDto} prêt à être envoyé au client HTTP
     */
    ResponseDto toDto(Object entity);

    /**
     * Récupère une page d'entités et la convertit en DTOs de réponse.
     * La page et la taille sont normalisées par {@link AbstractCrudService}.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return une page d'objets {@link ResponseDto}
     */
    Page<ResponseDto> getAll(int page, int taille);
}
