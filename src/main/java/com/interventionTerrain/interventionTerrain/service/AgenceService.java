package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.AgenceRequestDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienEffectifResponseDto;
import com.interventionTerrain.interventionTerrain.dto.AgenceResponseDto;
import com.interventionTerrain.interventionTerrain.dto.RequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.exception.DoublonRessourceException;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.exception.RessourceUtiliseeException;
import com.interventionTerrain.interventionTerrain.repository.CommuneRepository;
import com.interventionTerrain.interventionTerrain.repository.TechnicienCompetenceRepository;
import com.interventionTerrain.interventionTerrain.repository.TechnicienRepository;
import com.interventionTerrain.interventionTerrain.entity.Commune;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.interventionTerrain.interventionTerrain.entity.Agence;
import com.interventionTerrain.interventionTerrain.repository.AgenceRepository;

/**
 * Service métier gérant les agences.
 * Fournit la création, la modification et les conversions entité/DTO.
 * La lecture et la suppression sont héritées de {@link AbstractCrudService}.
 * La conversion de la commune imbriquée est déléguée à {@link CommuneService}.
 */
@Service
public class AgenceService extends AbstractCrudService {

    private final AgenceRepository agenceRepository;
    private final CommuneRepository communeRepository;
    private final TechnicienRepository technicienRepository;
    private final TechnicienCompetenceRepository technicienCompetenceRepository;
    private final CommuneService communeService;

    /**
     * Constructeur injectant le repository des agences, des communes
     * et le service de conversion des communes.
     *
     * @param agenceRepository le repository des agences
     * @param communeRepository le repository des communes (résolution de la FK)
     * @param technicienRepository le repository des techniciens (figement et suppression)
     * @param technicienCompetenceRepository le repository de l'association technicien/compétence (effectif)
     * @param communeService le service de conversion des communes
     */
    public AgenceService(AgenceRepository agenceRepository, CommuneRepository communeRepository,
                         TechnicienRepository technicienRepository,
                         TechnicienCompetenceRepository technicienCompetenceRepository,
                         CommuneService communeService) {
        super(agenceRepository);
        this.agenceRepository = agenceRepository;
        this.communeRepository = communeRepository;
        this.technicienRepository = technicienRepository;
        this.technicienCompetenceRepository = technicienCompetenceRepository;
        this.communeService = communeService;
    }

    /**
     * Convertit les données d'une requête en entité agence.
     *
     * @param agenceRequest Les données de la requête pour créer l'agence.
     * @param commune La commune associée à l'agence.
     * @return Agence L'entité agence créée.
     */
    public Agence toEntity(AgenceRequestDto agenceRequest, Commune commune) {
        return Agence.builder()
                .nom(agenceRequest.getNom())
                .ville(agenceRequest.getVille())
                .responsable(agenceRequest.getResponsable())
                .commune(commune)
                .build();
    }

    /**
     * Convertit une entité agence en objet de réponse typé.
     * Seul point de construction de {@link AgenceResponseDto}.
     *
     * @param agence L'entité agence à convertir.
     * @return AgenceResponseDto Les détails de l'agence convertie.
     */
    public AgenceResponseDto toAgenceResponse(Agence agence) {
        return AgenceResponseDto.builder()
                .id(agence.getId())
                .nom(agence.getNom())
                .ville(agence.getVille())
                .responsable(agence.getResponsable())
                .commune(communeService.toDto(agence.getCommune()))
                .build();
    }

    /**
     * Convertit une entité agence en objet de réponse.
     *
     * @param entity L'entité agence à convertir.
     * @return ResponseDto Les détails de l'agence convertie.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toAgenceResponse((Agence) entity);
    }

    /**
     * Crée une nouvelle agence.
     *
     * @param requestDto les informations de l'agence à créer
     * @return l'agence créée
     * @throws NotFoundException si la commune associée n'existe pas
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        AgenceRequestDto agenceRequest = (AgenceRequestDto) requestDto;
        if (agenceRepository.existsByNomIgnoreCase(agenceRequest.getNom())) {
            throw new DoublonRessourceException("Une agence nommée '" + agenceRequest.getNom()
                    + "' existe déjà");
        }
        Commune commune = communeRepository.findById(agenceRequest.getCommuneId())
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(agenceRepository.save(toEntity(agenceRequest, commune)));
    }

    /**
     * Modifie une agence existante.
     *
     * @param id L'identifiant de l'agence à modifier.
     * @param requestDto Les données de la requête pour modifier l'agence.
     * @return ResponseDto Les détails de l'agence modifiée.
     * @throws NotFoundException si l'agence ou la commune n'existent pas
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        AgenceRequestDto agenceRequest = (AgenceRequestDto) requestDto;
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        Commune commune = communeRepository.findById(agenceRequest.getCommuneId())
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (agenceRepository.existsByNomIgnoreCaseAndIdNot(agenceRequest.getNom(), id)) {
            throw new DoublonRessourceException("Une agence nommée '" + agenceRequest.getNom()
                    + "' existe déjà");
        }
        boolean villeModifiee = !java.util.Objects.equals(agence.getVille(), agenceRequest.getVille());
        if (villeModifiee && technicienRepository.existsByAgenceId(id)) {
            throw new IllegalArgumentException("La ville d'une agence est figée dès que des "
                    + "techniciens y sont rattachés");
        }
        agence.setNom(agenceRequest.getNom());
        agence.setVille(agenceRequest.getVille());
        agence.setResponsable(agenceRequest.getResponsable());
        agence.setCommune(commune);
        return toDto(agenceRepository.save(agence));
    }

    /**
     * Recherche une agence par son nom exact (premier résultat en cas d'homonymie).
     *
     * @param nom Le nom de l'agence recherchée.
     * @return ResponseDto Les détails de l'agence trouvée.
     * @throws NotFoundException si aucune agence ne porte ce nom
     */
    public ResponseDto getByNom(String nom) {
        Agence agence = agenceRepository.findByNom(nom).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(agence);
    }
    /**
     * Récupère une page d'agences triée par ville puis par nom.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return une page de ressources converties en DTO
     */
    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Sort sort = Sort.by(Sort.Order.asc("ville"), Sort.Order.asc("nom"));
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), sort);
        return agenceRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * Supprime une agence. La suppression est refusée si des techniciens y sont rattachés.
     *
     * @param id L'identifiant de l'agence à supprimer.
     * @return ResponseDto Les détails de l'agence supprimée.
     * @throws NotFoundException si l'agence n'existe pas
     * @throws RessourceUtiliseeException si des techniciens sont rattachés à l'agence
     */
    @Override
    public ResponseDto delete(Long id) throws Throwable {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (technicienRepository.existsByAgenceId(id)) {
            throw new RessourceUtiliseeException("Agence", id, "Technicien");
        }
        ResponseDto response = toDto(agence);
        agenceRepository.deleteById(id);
        return response;
    }

    /**
     * Listé l'effectif d'une agence : ses techniciens triés par nom, avec le
     * nombre de compétences encore valides de chacun. Une agence sans
     * technicien renvoie une liste vide.
     *
     * @param agenceId l'identifiant de l'agence
     * @return la liste des techniciens de l'agence, triée par nom
     * @throws NotFoundException si l'agence n'existe pas
     */
    public List<TechnicienEffectifResponseDto> getEffectif(Long agenceId) {
        agenceRepository.findById(agenceId)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        Map<Long, Long> validesParTechnicien = new HashMap<>();
        for (Object[] ligne : technicienCompetenceRepository.compterValidesParAgence(agenceId, LocalDate.now())) {
            validesParTechnicien.put((Long) ligne[0], (Long) ligne[1]);
        }
        return technicienRepository.findByAgenceId(agenceId).stream()
                .sorted(java.util.Comparator.comparing(technicien -> technicien.getNom().toLowerCase()))
                .map(technicien -> TechnicienEffectifResponseDto.builder()
                        .id(technicien.getId())
                        .nom(technicien.getNom())
                        .prenom(technicien.getPrenom())
                        .matricule(technicien.getMatricule())
                        .nbCompetencesValides(validesParTechnicien.getOrDefault(technicien.getId(), 0L))
                        .build())
                .toList();
    }

    /**
     * Nomme la ressource dans le message d'erreur d'un identifiant inconnu.
     *
     * @return le message d'introuvable propre à la ressource
     */
    @Override
    protected String messageIntrouvable() {
        return "Agence non trouvée";
    }
}
