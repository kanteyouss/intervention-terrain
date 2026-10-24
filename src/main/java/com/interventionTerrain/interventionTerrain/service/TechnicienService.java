package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.CompetenceResponseDto;
import com.interventionTerrain.interventionTerrain.entity.Competence;
import com.interventionTerrain.interventionTerrain.entity.Equipe;
import com.interventionTerrain.interventionTerrain.exception.DoublonRessourceException;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.exception.RessourceUtiliseeException;
import com.interventionTerrain.interventionTerrain.repository.EquipeRepository;
import com.interventionTerrain.interventionTerrain.dto.RequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienRequestDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienCompetenceResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienResponseDto;
import com.interventionTerrain.interventionTerrain.repository.TechnicienCompetenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import com.interventionTerrain.interventionTerrain.entity.Technicien;
import com.interventionTerrain.interventionTerrain.repository.TechnicienRepository;

/**
 * Service métier gérant les techniciens.
 * Fournit la création, la modification et les conversions entité/DTO.
 * La lecture et la suppression sont héritées de {@link AbstractCrudService}.
 * Les conversions imbriquées (équipe/agence/commune) sont
 * déléguées à {@link EquipeService}.
 */
@Service
public class TechnicienService extends AbstractCrudService {

    private final TechnicienRepository technicienRepository;
    private final EquipeRepository equipeRepository;
    private final TechnicienCompetenceRepository technicienCompetenceRepository;
    private final EquipeService equipeService;

    /**
     * Constructeur injectant les repositories du technicien et les services de conversion.
     *
     * @param technicienRepository le repository des techniciens
     * @param equipeRepository le repository des équipes (résolution de la FK)
     * @param technicienCompetenceRepository le repository de l'association technicien/compétence
     * @param equipeService le service de conversion des équipes
     */
    public TechnicienService(TechnicienRepository technicienRepository,
                             EquipeRepository equipeRepository,
                             TechnicienCompetenceRepository technicienCompetenceRepository,
                             EquipeService equipeService) {
        super(technicienRepository);
        this.technicienRepository = technicienRepository;
        this.equipeRepository = equipeRepository;
        this.technicienCompetenceRepository = technicienCompetenceRepository;
        this.equipeService = equipeService;
    }

    /**
     * Convertit les données d'une requête en entité technicien.
     *
     * @param technicienRequest Les données de la requête pour créer le technicien.
     * @param equipe L'équipe associée au technicien.
     * @return Technicien L'entité technicien créée.
     */
    public Technicien toEntity(TechnicienRequestDto technicienRequest, Equipe equipe) {
        return Technicien.builder()
                .nom(technicienRequest.getNom())
                .prenom(technicienRequest.getPrenom())
                .email(technicienRequest.getEmail())
                .telephone(technicienRequest.getTelephone())
                .matricule(technicienRequest.getMatricule())
                .equipe(equipe)
                .build();
    }

    /**
     * Convertit une entité technicien en objet de réponse typé.
     * Seul point de construction de {@link TechnicienResponseDto}.
     *
     * @param technicien L'entité technicien à convertir.
     * @return TechnicienResponseDto Les détails du technicien converti.
     */
    public TechnicienResponseDto toTechnicienResponse(Technicien technicien) {
        return TechnicienResponseDto.builder()
                .id(technicien.getId())
                .nom(technicien.getNom())
                .prenom(technicien.getPrenom())
                .email(technicien.getEmail())
                .telephone(technicien.getTelephone())
                .matricule(technicien.getMatricule())
                .equipe(equipeService.toDto(technicien.getEquipe()))
                .build();
    }

    /**
     * Convertit une entité technicien en objet de réponse.
     *
     * @param entity L'entité technicien à convertir.
     * @return ResponseDto Les détails du technicien converti.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toTechnicienResponse((Technicien) entity);
    }

    /**
     * Crée un nouveau technicien.
     *
     * @param requestDto les informations du technicien à créer
     * @return le technicien créé
     * @throws NotFoundException si l'équipe associée n'existe pas
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        TechnicienRequestDto technicienRequest = (TechnicienRequestDto) requestDto;
        if (technicienRepository.existsByMatriculeIgnoreCase(technicienRequest.getMatricule())) {
            throw new DoublonRessourceException("Un technicien portant le matricule '"
                    + technicienRequest.getMatricule() + "' existe déjà");
        }
        Equipe equipe = equipeRepository.findById(technicienRequest.getEquipeId())
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(technicienRepository.save(toEntity(technicienRequest, equipe)));
    }

    /**
     * Modifie un technicien existant.
     *
     * @param id L'identifiant du technicien à modifier.
     * @param requestDto Les données de la requête pour modifier le technicien.
     * @return ResponseDto Les détails du technicien modifié.
     * @throws NotFoundException si le technicien ou l'équipe n'existent pas
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        TechnicienRequestDto technicienRequest = (TechnicienRequestDto) requestDto;
        Technicien technicien = technicienRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        Equipe equipe = equipeRepository.findById(technicienRequest.getEquipeId())
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (!java.util.Objects.equals(technicien.getMatricule(), technicienRequest.getMatricule())) {
            throw new IllegalArgumentException("Le matricule d'un technicien est figé");
        }
        technicien.setNom(technicienRequest.getNom());
        technicien.setPrenom(technicienRequest.getPrenom());
        technicien.setEmail(technicienRequest.getEmail());
        technicien.setTelephone(technicienRequest.getTelephone());
        technicien.setEquipe(equipe);
        return toDto(technicienRepository.save(technicien));
    }

    /**
     * Récupère les compétences d'un technicien (via la table associative).
     *
     * @param technicienId L'identifiant du technicien.
     * @return List<TechnicienCompetenceResponseDto> Les compétences du technicien,
     *         avec les dates d'obtention et d'expiration et le drapeau des
     *         compétences expirées (signalées, jamais masquées).
     * @throws NotFoundException si le technicien n'existe pas
     */
    public List<TechnicienCompetenceResponseDto> getCompetencesByTechnicien(Long technicienId) {
        technicienRepository.findById(technicienId)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        LocalDate aujourd = LocalDate.now();
        return technicienCompetenceRepository.findByTechnicienId(technicienId).stream()
                .map(link -> TechnicienCompetenceResponseDto.builder()
                        .id(link.getCompetence().getId())
                        .nom(link.getCompetence().getNom())
                        .description(link.getCompetence().getDescription())
                        .dateObtention(link.getDateObtention())
                        .dateExpiration(link.getDateExpiration())
                        .expiree(link.getDateExpiration() != null
                                && link.getDateExpiration().isBefore(aujourd))
                        .build())
                .toList();
    }

    /**
     * Recherche un technicien par son nom exact (premier résultat en cas d'homonymie).
     *
     * @param nom Le nom du technicien recherché.
     * @return ResponseDto Les détails du technicien trouvé.
     * @throws NotFoundException si aucun technicien ne porte ce nom
     */
    public ResponseDto getByNom(String nom) {
        Technicien technicien = technicienRepository.findByNom(nom).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(technicien);
    }
    /**
     * Récupère une page de techniciens triée par nom.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return une page de ressources converties en DTO
     */
    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Sort sort = Sort.by(Sort.Order.asc("nom"));
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), sort);
        return technicienRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * Supprime un technicien. La suppression est refusée si le technicien est
     * affecté à au moins une intervention.
     *
     * @param id L'identifiant du technicien à supprimer.
     * @return ResponseDto Les détails du technicien supprimé.
     * @throws NotFoundException si le technicien n'existe pas
     * @throws RessourceUtiliseeException si le technicien a des interventions
     */
    @Override
    public ResponseDto delete(Long id) throws Throwable {
        Technicien technicien = technicienRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (technicienRepository.existsInterventionByTechnicienId(id)) {
            throw new RessourceUtiliseeException("Technicien", id, "Intervention");
        }
        ResponseDto response = toDto(technicien);
        technicienRepository.deleteById(id);
        return response;
    }

    /**
     * Nomme la ressource dans le message d'erreur d'un identifiant inconnu.
     *
     * @return le message d'introuvable propre à la ressource
     */
    @Override
    protected String messageIntrouvable() {
        return "Technicien non trouvé";
    }
}
