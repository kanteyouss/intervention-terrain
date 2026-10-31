package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.AgenceResponseDto;
import com.interventionTerrain.interventionTerrain.dto.CommuneResponseDto;
import com.interventionTerrain.interventionTerrain.dto.CompetenceRequestDto;
import com.interventionTerrain.interventionTerrain.dto.CompetenceResponseDto;
import com.interventionTerrain.interventionTerrain.dto.DisponiblesResponseDto;
import com.interventionTerrain.interventionTerrain.dto.GroupeAgenceDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienCompetenceResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienDisponibleDto;
import com.interventionTerrain.interventionTerrain.entity.Competence;
import com.interventionTerrain.interventionTerrain.entity.Creneau;
import com.interventionTerrain.interventionTerrain.entity.Equipe;
import com.interventionTerrain.interventionTerrain.entity.Intervention;
import com.interventionTerrain.interventionTerrain.entity.InterventionCreneaux;
import com.interventionTerrain.interventionTerrain.entity.TechnicienCompetence;
import com.interventionTerrain.interventionTerrain.entity.TechnicienDisponibleView;
import com.interventionTerrain.interventionTerrain.entity.TypeIntervention;
import com.interventionTerrain.interventionTerrain.repository.CompetenceRepository;
import com.interventionTerrain.interventionTerrain.repository.EquipeRepository;
import com.interventionTerrain.interventionTerrain.repository.InterventionCreneauxRepository;
import com.interventionTerrain.interventionTerrain.repository.InterventionRepository;
import com.interventionTerrain.interventionTerrain.repository.TypeInterventionRepository;
import com.interventionTerrain.interventionTerrain.dto.EquipeResponseDto;
import com.interventionTerrain.interventionTerrain.dto.RequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienRequestDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienResponseDto;
import com.interventionTerrain.interventionTerrain.exception.DoublonRessourceException;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.exception.ParametresInvalidesException;
import com.interventionTerrain.interventionTerrain.exception.RessourceUtiliseeException;
import com.interventionTerrain.interventionTerrain.repository.TechnicienCompetenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.interventionTerrain.interventionTerrain.entity.Technicien;
import com.interventionTerrain.interventionTerrain.repository.TechnicienRepository;

@Service
public class TechnicienService extends AbstractCrudService {

    private final TechnicienRepository technicienRepository;
    private final EquipeRepository equipeRepository;
    private final CompetenceRepository competenceRepository;
    private final TechnicienCompetenceRepository technicienCompetenceRepository;
    private final InterventionRepository interventionRepository;
    private final InterventionCreneauxRepository interventionCreneauxRepository;
    private final TypeInterventionRepository typeInterventionRepository;

    public TechnicienService(TechnicienRepository technicienRepository,
                             EquipeRepository equipeRepository,
                             CompetenceRepository competenceRepository,
                             TechnicienCompetenceRepository technicienCompetenceRepository,
                             InterventionRepository interventionRepository,
                             InterventionCreneauxRepository interventionCreneauxRepository,
                             TypeInterventionRepository typeInterventionRepository) {
        super(technicienRepository);
        this.technicienRepository = technicienRepository;
        this.equipeRepository = equipeRepository;
        this.competenceRepository = competenceRepository;
        this.technicienCompetenceRepository = technicienCompetenceRepository;
        this.interventionRepository = interventionRepository;
        this.interventionCreneauxRepository = interventionCreneauxRepository;
        this.typeInterventionRepository = typeInterventionRepository;
    }

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

    @Override
    public ResponseDto toDto(Object entity) {
        Technicien technicien = (Technicien) entity;
        return TechnicienResponseDto.builder()
                .id(technicien.getId())
                .nom(technicien.getNom())
                .prenom(technicien.getPrenom())
                .email(technicien.getEmail())
                .telephone(technicien.getTelephone())
                .matricule(technicien.getMatricule())
                .equipe(EquipeResponseDto.builder()
                        .id(technicien.getEquipe().getId())
                        .nom(technicien.getEquipe().getNom())
                        .agence(AgenceResponseDto.builder()
                                .id(technicien.getEquipe().getAgence().getId())
                                .nom(technicien.getEquipe().getAgence().getNom())
                                .ville(technicien.getEquipe().getAgence().getVille())
                                .responsable(technicien.getEquipe().getAgence().getResponsable())
                                .commune(CommuneResponseDto.builder()
                                        .id(technicien.getEquipe().getAgence().getCommune().getId())
                                        .nom(technicien.getEquipe().getAgence().getCommune().getNom())
                                        .codePostal(technicien.getEquipe().getAgence().getCommune().getCodePostal())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public Page<ResponseDto> getAll(int page, int taille) {
        Sort sort = Sort.by(Sort.Order.asc("nom"));
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), sort);
        return technicienRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public ResponseDto create(RequestDto requestDto) {
        TechnicienRequestDto technicienRequest = (TechnicienRequestDto) requestDto;
        technicienRequest.setNom((technicienRequest.getNom()));
        technicienRequest.setPrenom((technicienRequest.getPrenom()));
        technicienRequest.setEmail((technicienRequest.getEmail()));
        technicienRequest.setTelephone((technicienRequest.getTelephone()));
        technicienRequest.setMatricule((technicienRequest.getMatricule()));
        if (technicienRepository.existsByMatriculeIgnoreCase(technicienRequest.getMatricule())) {
            throw new DoublonRessourceException("Un technicien portant le matricule '"
                    + technicienRequest.getMatricule() + "' existe déjà");
        }
        Equipe equipe = equipeRepository.findById(technicienRequest.getEquipeId())
                .orElseThrow(() -> new NotFoundException("Équipe non trouvée"));
        return toDto(technicienRepository.save(toEntity(technicienRequest, equipe)));
    }

    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        TechnicienRequestDto technicienRequest = (TechnicienRequestDto) requestDto;
        technicienRequest.setNom((technicienRequest.getNom()));
        technicienRequest.setPrenom((technicienRequest.getPrenom()));
        technicienRequest.setEmail((technicienRequest.getEmail()));
        technicienRequest.setTelephone((technicienRequest.getTelephone()));
        technicienRequest.setMatricule((technicienRequest.getMatricule()));
        Technicien technicien = technicienRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (!Objects.equals(technicien.getMatricule(), technicienRequest.getMatricule())) {
            throw new IllegalArgumentException("Le matricule d'un technicien est figé");
        }
        Equipe equipe = equipeRepository.findById(technicienRequest.getEquipeId())
                .orElseThrow(() -> new NotFoundException("Équipe non trouvée"));
        technicien.setNom(technicienRequest.getNom());
        technicien.setPrenom(technicienRequest.getPrenom());
        technicien.setEmail(technicienRequest.getEmail());
        technicien.setTelephone(technicienRequest.getTelephone());
        technicien.setEquipe(equipe);
        return toDto(technicienRepository.save(technicien));
    }

    @Transactional
    public ResponseDto delete(Long id) throws Throwable {
        Technicien technicien = technicienRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (technicienRepository.existsInterventionByTechnicienId(id)) {
            throw new RessourceUtiliseeException("Technicien", id, "Intervention");
        }
        technicienCompetenceRepository.deleteByTechnicienId(id);
        ResponseDto response = toDto(technicien);
        technicienRepository.deleteById(id);
        return response;
    }

    public List<TechnicienResponseDto> getTechniciensByAgence(Long agenceId) {
        return technicienRepository.findByAgenceId(agenceId).stream()
                .map(this::toDto)
                .map(TechnicienResponseDto.class::cast)
                .toList();
    }

    public List<TechnicienCompetenceResponseDto> getCompetencesByTechnicien(Long technicienId) {
        technicienRepository.findById(technicienId)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        LocalDate aujourd = LocalDate.now();
        return technicienCompetenceRepository.findByTechnicienId(technicienId).stream()
                .map(link -> TechnicienCompetenceResponseDto.builder()
                        .id(link.getCompetence().getId())
                        .nom(link.getCompetence().getLibelle())
                        .libelle(link.getCompetence().getLibelle())
                        .description(link.getCompetence().getDescription())
                        .dateObtention(link.getDateObtention())
                        .dateExpiration(link.getDateExpiration())
                        .expiree(link.getDateExpiration() != null && link.getDateExpiration().isBefore(aujourd))
                        .build())
                .toList();
    }

    public TechnicienCompetenceResponseDto associerCompetence(Long technicienId, CompetenceRequestDto competenceRequest) {
        Technicien technicien = technicienRepository.findById(technicienId)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));

        if (competenceRequest == null || competenceRequest.getId() == null) {
            throw new ParametresInvalidesException("L'identifiant de la compétence est obligatoire");
        }

        Competence competence = competenceRepository.findById(competenceRequest.getId())
                .orElseThrow(() -> new NotFoundException("Compétence non trouvée"));

        LocalDate dateObtention = competenceRequest.getDateObtention() != null
                ? competenceRequest.getDateObtention() : LocalDate.now();
        LocalDate dateExpiration = competenceRequest.getDateExpiration();

        if (dateExpiration != null && !dateExpiration.isAfter(dateObtention)) {
            throw new ParametresInvalidesException("La date d'expiration doit être postérieure à la date d'obtention");
        }

        boolean doublonActif = technicienCompetenceRepository.findByTechnicienId(technicienId).stream()
                .anyMatch(link -> link.getCompetence().getId().equals(competence.getId())
                        && (link.getDateExpiration() == null || !link.getDateExpiration().isBefore(LocalDate.now())));
        if (doublonActif) {
            throw new DoublonRessourceException("Le technicien possède déjà cette compétence active");
        }

        TechnicienCompetence link = TechnicienCompetence.builder()
                .technicien(technicien)
                .competence(competence)
                .dateObtention(dateObtention)
                .dateExpiration(dateExpiration)
                .build();

        TechnicienCompetence saved = technicienCompetenceRepository.save(link);
        return TechnicienCompetenceResponseDto.builder()
                .id(saved.getCompetence().getId())
                .nom(saved.getCompetence().getLibelle())
                .libelle(saved.getCompetence().getLibelle())
                .description(saved.getCompetence().getDescription())
                .dateObtention(saved.getDateObtention())
                .dateExpiration(saved.getDateExpiration())
                .expiree(saved.getDateExpiration() != null && saved.getDateExpiration().isBefore(LocalDate.now()))
                .build();
    }

    private CompetenceResponseDto toCompetenceResponse(Competence competence) {
        return CompetenceResponseDto.builder()
                .id(competence.getId())
                .libelle(competence.getLibelle())
                .description(competence.getDescription())
                .build();
    }

    public ResponseDto getByNom(String nom) {
        Object entity = technicienRepository.findByNom(nom).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(entity);
    }

    public DisponiblesResponseDto getDisponibles(LocalDateTime debut, LocalDateTime fin, String typeNom) {
        TypeIntervention type = typeInterventionRepository.findByNomIgnoreCase(typeNom)
                .or(() -> typeInterventionRepository.findFirstByNomContainingIgnoreCase(typeNom))
                .orElseThrow(() -> new NotFoundException("Type d'intervention non trouvé : " + typeNom));

        List<Long> competencesRequises = typeInterventionRepository
                .findCompetenceIdsByTypeInterventionId(type.getId());
        LocalDate dateIntervention = debut.toLocalDate();

        List<TechnicienDisponibleView> dispo = technicienRepository.findAllDisponibles().stream()
                .filter(t -> possedeCompetences(t.getTechnicienId(), competencesRequises, dateIntervention))
                .filter(t -> estLibreSurCreneau(t.getTechnicienId(), debut, fin))
                .sorted((a, b) -> a.getNom().toLowerCase().compareTo(b.getNom().toLowerCase()))
                .toList();

        if (dispo.isEmpty()) {
            return DisponiblesResponseDto.builder()
                    .agences(List.of())
                    .raisonFiltrage("Aucun technicien compétent et disponible sur le créneau "
                            + debut + " - " + fin + " pour le type " + typeNom)
                    .build();
        }

        Map<String, List<TechnicienDisponibleView>> parAgence = new LinkedHashMap<>();
        dispo.forEach(t -> parAgence
                .computeIfAbsent(t.getAgenceNom(), k -> new ArrayList<>())
                .add(t));

        List<GroupeAgenceDto> agences = parAgence.entrySet().stream()
                .map(e -> GroupeAgenceDto.builder()
                        .agenceNom(e.getKey())
                        .techniciens(e.getValue().stream()
                                .map(t -> TechnicienDisponibleDto.builder()
                                        .id(t.getTechnicienId())
                                        .nom(t.getNom())
                                        .prenom(t.getPrenom())
                                        .matricule(t.getMatricule())
                                        .chargeJour(t.getNbInterventionsJour() != null
                                                ? t.getNbInterventionsJour() : 0)
                                        .build())
                                .toList())
                        .build())
                .toList();

        return DisponiblesResponseDto.builder()
                .agences(agences)
                .raisonFiltrage(null)
                .build();
    }

    private boolean possedeCompetences(Long technicienId, List<Long> requises, LocalDate dateIntervention) {
        if (requises == null || requises.isEmpty()) {
            return true;
        }
        List<Long> possedees = technicienCompetenceRepository.findByTechnicienId(technicienId).stream()
                .filter(tc -> tc.getDateExpiration() == null || !tc.getDateExpiration().isBefore(dateIntervention))
                .map(tc -> tc.getCompetence().getId())
                .toList();
        return possedees.containsAll(requises);
    }

    private boolean estLibreSurCreneau(Long technicienId, LocalDateTime debut, LocalDateTime fin) {
        List<Intervention> interventions = interventionRepository.findByTechnicienId(technicienId);
        for (Intervention i : interventions) {
            if ("ANNULEE".equals(i.getStatut())) {
                continue;
            }
            Creneau c = interventionCreneauxRepository.findByInterventionId(i.getId())
                    .stream().findFirst().map(InterventionCreneaux::getCreneau).orElse(null);
            if (c != null && c.getDateDebut() != null && c.getDateFin() != null) {
                LocalDateTime debutIntervention = LocalDateTime.of(i.getDatePlanifiee(), c.getDateDebut());
                LocalDateTime finIntervention = LocalDateTime.of(i.getDatePlanifiee(), c.getDateFin());
                if (debut.isBefore(finIntervention) && fin.isAfter(debutIntervention)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected String messageIntrouvable() {
        return "Technicien non trouvé";
    }
}