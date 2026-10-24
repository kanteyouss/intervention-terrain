package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Service abstrait de base (classe mère) pour toutes les entités de l'application.
 * Il implémente {@link CrudService} et centralise la logique métier répétitive
 * de lecture, de suppression et de pagination en utilisant un {@link JpaRepository}
 * global (type brut). Les classes enfants (ex: AgenceService, TechnicienService)
 * héritent de ces méthodes et se concentrent uniquement sur la création,
 * la modification et le mapping (DTO/Entité).
 */
public abstract class AbstractCrudService implements CrudService {

    /**
     * Le repository global utilisé pour les opérations génériques (find, delete).
     * Il est injecté par la classe enfant via le mot-clé "super".
     */
    private final JpaRepository repository;

    /**
     * Constructeur protégé appelé par les services enfants lors de leur instanciation.
     *
     * @param repository le repository spécifique à l'entité (ex: AgenceRepository)
     *                  traité ici de manière globale
     */
    public AbstractCrudService(JpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Récupère une page d'entités présentes dans la base de données
     * et les convertit en DTOs de réponse.
     * La page et la taille transmises sont normalisées par
     * {@link #pageAutorisee(int)} et {@link #tailleAutorisee(int)}.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return une page d'objets {@link ResponseDto}
     */
    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille));

        return repository.findAll(pageable)
                .map(this::toDto);
    }

    /**
     * Recherche une entité spécifique par son identifiant et la convertit en DTO.
     *
     * @param id l'identifiant de l'entité recherchée
     * @return un objet {@link ResponseDto} représentant l'entité trouvée
     * @throws NotFoundException si aucune entité ne correspond à cet identifiant
     */
    public ResponseDto getById(Long id) throws Throwable {
        Object entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(entity);
    }

    /**
     * Supprime une entité de la base de données après avoir vérifié son existence,
     * puis retourne les informations de l'entité supprimée.
     *
     * @param id l'identifiant de l'entité à supprimer
     * @return un objet {@link ResponseDto} contenant les données de l'entité tout juste supprimée
     * @throws NotFoundException si l'entité à supprimer n'existe pas
     */
    public ResponseDto delete(Long id) throws Throwable {
        Object entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        repository.deleteById(id);
        return toDto(entity);
    }

    /**
     * Borne la taille de page demandée entre 1 et {@link #TAILLE_MAX}.
     * Une valeur inférieure à 1 retombe sur {@link #TAILLE_DEFAUT}.
     *
     * @param taille la taille de page transmise par le client
     * @return la taille de page autorisée
     */
    /**
     * Message renvoyé lorsqu'une ressource est absente.
     * Les services spécialisés remplacent cette méthode pour nommer la
     * ressource concernée dans le message d'erreur.
     *
     * @return le message d'introuvable par défaut
     */
    protected String messageIntrouvable() {
        return NOT_FOUND_MESSAGE;
    }

    static int tailleAutorisee(int taille) {
        if (taille < 1) {
            return TAILLE_DEFAUT;
        }
        return Math.min(taille, TAILLE_MAX);
    }

    /**
     * Normalise l'index de page en ramenant toute valeur négative à 0.
     *
     * @param page l'index de page transmis par le client
     * @return l'index de page autorisé
     */
    static int pageAutorisee(int page) {
        return Math.max(page, 0);
    }
}
