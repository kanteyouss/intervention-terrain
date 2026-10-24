package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.CompetenceRequestDto;
import com.interventionTerrain.interventionTerrain.dto.CompetenceResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TypeInterventionResponseDto;
import com.interventionTerrain.interventionTerrain.dto.RequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.exception.DoublonRessourceException;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.exception.RessourceUtiliseeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import com.interventionTerrain.interventionTerrain.entity.Competence;
import com.interventionTerrain.interventionTerrain.repository.CompetenceRepository;
import com.interventionTerrain.interventionTerrain.repository.TechnicienCompetenceRepository;

/**
 * Service métier gérant les compétences.
 * Fournit la création, la modification et les conversions entité/DTO.
 * La lecture et la suppression sont héritées de {@link AbstractCrudService}.
 */
@Service
public class CompetenceService extends AbstractCrudService {

    private final CompetenceRepository competenceRepository;
    private final TechnicienCompetenceRepository technicienCompetenceRepository;
    private final TypeInterventionService typeInterventionService;

    /**
     * Constructeur injectant le repository des compétences, le repository de
     * l'association technicien/compétence et le service de conversion des types
     * d'intervention.
     *
     * @param competenceRepository le repository des compétences
     * @param technicienCompetenceRepository le repository de l'association technicien/compétence
     * @param typeInterventionService le service de conversion des types d'intervention
     */
    public CompetenceService(CompetenceRepository competenceRepository,
                             TechnicienCompetenceRepository technicienCompetenceRepository,
                             TypeInterventionService typeInterventionService) {
        super(competenceRepository);
        this.competenceRepository = competenceRepository;
        this.technicienCompetenceRepository = technicienCompetenceRepository;
        this.typeInterventionService = typeInterventionService;
    }

    /**
     * Convertit les données d'une requête en entité compétence.
     *
     * @param competenceRequest Les données de la requête pour créer la compétence.
     * @return Competence L'entité compétence créée.
     */
    public Competence toEntity(CompetenceRequestDto competenceRequest) {
        return Competence.builder()
                .nom(competenceRequest.getNom())
                .description(competenceRequest.getDescription())
                .build();
    }

    /**
     * Convertit une entité compétence en objet de réponse typé.
     * Seul point de construction de {@link CompetenceResponseDto}.
     *
     * @param competence L'entité compétence à convertir.
     * @return CompetenceResponseDto Les détails de la compétence convertie.
     */
    public CompetenceResponseDto toCompetenceResponse(Competence competence) {
        return CompetenceResponseDto.builder()
                .id(competence.getId())
                .nom(competence.getNom())
                .description(competence.getDescription())
                .build();
    }

    /**
     * Convertit une entité compétence en objet de réponse.
     *
     * @param entity L'entité compétence à convertir.
     * @return ResponseDto Les détails de la compétence convertie.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toCompetenceResponse((Competence) entity);
    }

    /**
     * Crée une nouvelle compétence.
     *
     * @param requestDto les informations de la compétence à créer
     * @return la compétence créée
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        CompetenceRequestDto competenceRequest = (CompetenceRequestDto) requestDto;
        if (competenceRepository.existsByNomIgnoreCase(competenceRequest.getNom())) {
            throw new DoublonRessourceException("Une compétence nommée '"
                    + competenceRequest.getNom() + "' existe déjà");
        }
        return toDto(competenceRepository.save(toEntity(competenceRequest)));
    }

    /**
     * Modifie une compétence existante.
     *
     * @param id L'identifiant de la compétence à modifier.
     * @param requestDto Les données de la requête pour modifier la compétence.
     * @return ResponseDto Les détails de la compétence modifiée.
     * @throws NotFoundException si la compétence n'existe pas
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        CompetenceRequestDto competenceRequest = (CompetenceRequestDto) requestDto;
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (competenceRepository.existsByNomIgnoreCaseAndIdNot(competenceRequest.getNom(), id)) {
            throw new DoublonRessourceException("Une compétence nommée '"
                    + competenceRequest.getNom() + "' existe déjà");
        }
        competence.setNom(competenceRequest.getNom());
        competence.setDescription(competenceRequest.getDescription());
        return toDto(competenceRepository.save(competence));
    }

    /**
     * Recherche une compétence par son nom exact (premier résultat en cas d'homonymie).
     *
     * @param nom Le nom de la compétence recherchée.
     * @return ResponseDto Les détails de la compétence trouvée.
     * @throws NotFoundException si aucune compétence ne porte ce nom
     */
    public ResponseDto getByNom(String nom) {
        Competence competence = competenceRepository.findByNom(nom).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(competence);
    }
    /**
     * Récupère une page de compétences triée par libellé.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return une page de ressources converties en DTO
     */
    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Sort sort = Sort.by(Sort.Order.asc("nom"));
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), sort);
        return competenceRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * Supprime une compétence. La suppression est refusée si la compétence est
     * associée à au moins un technicien (et donc potentiellement mobilisée sur
     * des interventions).
     *
     * @param id L'identifiant de la compétence à supprimer.
     * @return ResponseDto Les détails de la compétence supprimée.
     * @throws NotFoundException si la compétence n'existe pas
     * @throws RessourceUtiliseeException si la compétence est associée à un technicien
     */
    @Override
    public ResponseDto delete(Long id) throws Throwable {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (competenceRepository.existsTechnicienByCompetenceId(id)) {
            throw new RessourceUtiliseeException("Competence", id, "Technicien");
        }
        ResponseDto response = toDto(competence);
        competenceRepository.deleteById(id);
        return response;
    }

    /**
     * Liste les types d'intervention dans lesquels une compétence est mobilisée,
     * en s'appuyant uniquement sur les jointures des tables existantes.
     *
     * @param competenceId l'identifiant de la compétence
     * @return la liste des types d'intervention concernés
     * @throws NotFoundException si la compétence n'existe pas
     */
    public List<TypeInterventionResponseDto> getTypesIntervention(Long competenceId) {
        competenceRepository.findById(competenceId)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return technicienCompetenceRepository.findTypesInterventionParCompetence(competenceId).stream()
                .map(typeInterventionService::toDto)
                .toList();
    }

    /**
     * Nomme la ressource dans le message d'erreur d'un identifiant inconnu.
     *
     * @return le message d'introuvable propre à la ressource
     */
    @Override
    protected String messageIntrouvable() {
        return "Compétence non trouvée";
    }
}
