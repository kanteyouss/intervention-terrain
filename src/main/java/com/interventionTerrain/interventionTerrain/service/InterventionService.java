package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.CreneauResponseDto;
import com.interventionTerrain.interventionTerrain.dto.InterventionRequestDto;
import com.interventionTerrain.interventionTerrain.dto.InterventionResponseDto;
import com.interventionTerrain.interventionTerrain.dto.RequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;

import com.interventionTerrain.interventionTerrain.entity.*;
import com.interventionTerrain.interventionTerrain.exception.*;
import com.interventionTerrain.interventionTerrain.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service métier gérant les interventions.
 * Fournit la création, la modification, la suppression et les conversions entité/DTO.
 */
@Service
public class InterventionService extends AbstractCrudService {

    private static final String STATUT_INITIAL = "PLANIFIEE";
    private static final Set<String> STATUTS_VALIDES = Set.of(
            "PLANIFIEE", "EN_COURS", "TERMINEE", "ECHOUEE", "ANNULEE"
    );
    private static final Set<String> STATUTS_DEPART = Set.of("PLANIFIEE");
    private static final Set<String> STATUTS_APRES_PLANIFIEE = Set.of("EN_COURS", "TERMINEE", "ECHOUEE", "ANNULEE");
    private static final Set<String> STATUTS_APRES_EN_COURS = Set.of("TERMINEE", "ECHOUEE");

    private final InterventionRepository interventionRepository;
    private final TypeInterventionRepository typeInterventionRepository;
    private final ClientRepository clientRepository;
    private final TechnicienRepository technicienRepository;
    private final CreneauRepository creneauRepository;
    private final InterventionTechnicienRepository interventionTechnicienRepository;
    private final InterventionCreneauxRepository interventionCreneauxRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final TechnicienCompetenceRepository technicienCompetenceRepository;

    public InterventionService(InterventionRepository interventionRepository,
                               TypeInterventionRepository typeInterventionRepository,
                               ClientRepository clientRepository,
                               TechnicienRepository technicienRepository,
                               CreneauRepository creneauRepository,
                               InterventionTechnicienRepository interventionTechnicienRepository,
                               InterventionCreneauxRepository interventionCreneauxRepository,
                               HistoriqueStatutRepository historiqueStatutRepository,
                               TechnicienCompetenceRepository technicienCompetenceRepository) {
        super(interventionRepository);
        this.interventionRepository = interventionRepository;
        this.typeInterventionRepository = typeInterventionRepository;
        this.clientRepository = clientRepository;
        this.technicienRepository = technicienRepository;
        this.creneauRepository = creneauRepository;
        this.interventionTechnicienRepository = interventionTechnicienRepository;
        this.interventionCreneauxRepository = interventionCreneauxRepository;
        this.historiqueStatutRepository = historiqueStatutRepository;
        this.technicienCompetenceRepository = technicienCompetenceRepository;
    }

    public Intervention toEntity(InterventionRequestDto interventionRequest, TypeIntervention typeIntervention, Client client) {
        return Intervention.builder()
                .datePlanifiee(interventionRequest.getDatePlanifiee())
                .statut(interventionRequest.getStatut() != null ? interventionRequest.getStatut() : STATUT_INITIAL)
                .commentaire(interventionRequest.getCommentaire())
                .reference(interventionRequest.getReference())
                .typeIntervention(typeIntervention)
                .client(client)
                .build();
    }

    public InterventionResponseDto toInterventionResponse(Intervention intervention) {
        return InterventionResponseDto.builder()
                .id(intervention.getId())
                .datePlanifiee(intervention.getDatePlanifiee())
                .statut(intervention.getStatut())
                .commentaire(intervention.getCommentaire())
                .reference(intervention.getReference())
                .typeIntervention(null)
                .client(null)
                .technicien(null)
                .creneau(null)
                .build();
    }

    @Override
    public ResponseDto toDto(Object entity) {
        return toInterventionResponse((Intervention) entity);
    }

    @Override
    @Transactional
    public ResponseDto create(RequestDto request) {
        InterventionRequestDto interventionRequest = (InterventionRequestDto) request;
        TypeIntervention typeIntervention = typeInterventionRepository.findById(interventionRequest.getTypeInterventionId())
                .orElseThrow(() -> new NotFoundException("Type d'intervention non trouvé"));
        Client client = clientRepository.findById(interventionRequest.getClientId())
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));
        Technicien technicien = technicienRepository.findById(interventionRequest.getTechnicienId())
                .orElseThrow(() -> new NotFoundException("Technicien non trouvé"));
        Creneau creneau = creneauRepository.findById(interventionRequest.getCreneauId())
                .orElseThrow(() -> new NotFoundException("Créneau non trouvé"));

        verifierCompetencesTechnicien(technicien, typeIntervention, interventionRequest.getDatePlanifiee().toLocalDate());
        verifierChevauchement(technicien, creneau, null);

        Intervention intervention = toEntity(interventionRequest, typeIntervention, client);
        intervention = interventionRepository.save(intervention);

        InterventionTechnicien lienTechnicien = InterventionTechnicien.builder()
                .intervention(intervention)
                .technicien(technicien)
                .estResponsable(Boolean.TRUE)
                .build();
        interventionTechnicienRepository.save(lienTechnicien);

        InterventionCreneaux lienCreneau = InterventionCreneaux.builder()
                .intervention(intervention)
                .creneau(creneau)
                .build();
        interventionCreneauxRepository.save(lienCreneau);

        enregistrerHistorique(intervention, intervention.getStatut(), "SYSTEM");
        return toDto(intervention);
    }

    @Override
    @Transactional
    public ResponseDto update(Long id, RequestDto request) {
        InterventionRequestDto interventionRequest = (InterventionRequestDto) request;
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Intervention non trouvée"));
        TypeIntervention typeIntervention = typeInterventionRepository.findById(interventionRequest.getTypeInterventionId())
                .orElseThrow(() -> new NotFoundException("Type d'intervention non trouvé"));
        Client client = clientRepository.findById(interventionRequest.getClientId())
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));
        Technicien technicien = technicienRepository.findById(interventionRequest.getTechnicienId())
                .orElseThrow(() -> new NotFoundException("Technicien non trouvé"));
        Creneau creneau = creneauRepository.findById(interventionRequest.getCreneauId())
                .orElseThrow(() -> new NotFoundException("Créneau non trouvé"));

        verifierCompetencesTechnicien(technicien, typeIntervention, interventionRequest.getDatePlanifiee().toLocalDate());
        verifierChevauchement(technicien, creneau, id);

        intervention.setDatePlanifiee(interventionRequest.getDatePlanifiee());
        intervention.setTypeIntervention(typeIntervention);
        intervention.setClient(client);
        intervention.setCommentaire(interventionRequest.getCommentaire());
        intervention.setReference(interventionRequest.getReference());
        interventionRepository.save(intervention);

        interventionTechnicienRepository.deleteByInterventionId(id);
        interventionTechnicienRepository.save(InterventionTechnicien.builder()
                .intervention(intervention)
                .technicien(technicien)
                .estResponsable(Boolean.TRUE)
                .build());

        interventionCreneauxRepository.deleteByInterventionId(id);
        interventionCreneauxRepository.save(InterventionCreneaux.builder()
                .intervention(intervention)
                .creneau(creneau)
                .build());

        return toDto(intervention);
    }

    @Override
    public ResponseDto delete(Long id) throws Throwable {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Intervention non trouvée"));
        interventionRepository.delete(intervention);
        return toDto(intervention);
    }

    public List<InterventionResponseDto> getInterventionsByClient(Long clientId) {
        return interventionRepository.findByClientId(clientId).stream()
                .map(this::toInterventionResponse)
                .toList();
    }

    public List<InterventionResponseDto> planner(Long technicienId, LocalDate du, LocalDate au) {
        LocalDateTime debut = du != null ? du.atStartOfDay() : LocalDate.now().atStartOfDay();
        LocalDateTime fin = au != null ? au.plusDays(1).atStartOfDay() : debut.plusDays(7);
        return interventionRepository.planner(technicienId, debut, fin).stream()
                .map(this::toInterventionResponse)
                .toList();
    }

    @Transactional
    public Intervention mettreAJourStatut(Long interventionId, String nouveauStatut) {
        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new NotFoundException("Intervention non trouvée"));
        String ancienStatut = intervention.getStatut() == null ? STATUT_INITIAL : intervention.getStatut();
        if (!STATUTS_VALIDES.contains(nouveauStatut)) {
            throw new ParametresInvalidesException("Statut inconnu: " + nouveauStatut);
        }
        if (!transitionAutorisee(ancienStatut, nouveauStatut)) {
            throw new IllegalArgumentException("Transition de statut invalide de '" + ancienStatut + "' vers '" + nouveauStatut + "'. Statuts atteignables: " + statutsAtteignables(ancienStatut));
        }
        intervention.setStatut(nouveauStatut);
        interventionRepository.save(intervention);
        enregistrerHistorique(intervention, nouveauStatut, "SYSTEM");
        return intervention;
    }

    @Transactional
    public Intervention creerInterventionDeReprise(Long interventionId, LocalDateTime nouvelleDatePlanifiee) {
        Intervention origine = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new NotFoundException("Intervention non trouvée"));
        if (!"ECHOUEE".equals(origine.getStatut())) {
            throw new IllegalArgumentException("Une intervention de reprise n'est autorisée que pour une intervention en statut ECHOUEE.");
        }
        if (interventionRepository.findByReference(origine.getReference()).stream()
                .anyMatch(i -> !Objects.equals(i.getId(), origine.getId()) && i.getReference() != null && i.getReference().startsWith(origine.getReference() + "/REPRISE-"))) {
            throw new IllegalArgumentException("Une reprise existe déjà pour cette intervention.");
        }

        String referenceReprise = origine.getReference() + "/REPRISE-" + (System.currentTimeMillis() % 100000);
        Intervention reprise = Intervention.builder()
                .client(origine.getClient())
                .typeIntervention(origine.getTypeIntervention())
                .datePlanifiee(nouvelleDatePlanifiee)
                .statut(STATUT_INITIAL)
                .commentaire("Reprise de l'intervention " + origine.getId())
                .reference(referenceReprise)
                .build();
        interventionRepository.save(reprise);
        return reprise;
    }

    public void verifierCompetencesTechnicien(Technicien technicien, TypeIntervention typeIntervention, LocalDate dateIntervention) {
        List<Long> competencesRequises = typeInterventionRepository.findCompetenceIdsByTypeInterventionId(typeIntervention.getId());
        if (competencesRequises == null || competencesRequises.isEmpty()) {
            return;
        }

        List<Long> competencesPossedees = technicienCompetenceRepository.findByTechnicienId(technicien.getId()).stream()
                .filter(tc -> tc.getDateExpiration() == null || !tc.getDateExpiration().isBefore(dateIntervention))
                .map(tc -> tc.getCompetence().getId())
                .toList();

        List<Long> competencesManquantes = competencesRequises.stream()
                .filter(id -> !competencesPossedees.contains(id))
                .toList();

        if (!competencesManquantes.isEmpty()) {
            throw new ParametresInvalidesException("Technicien non qualifié pour ce type d'intervention. Compétences manquantes ou expirées: " + competencesManquantes);
        }
    }

    public void verifierChevauchement(Technicien technicien, Creneau creneau, Long interventionIdAExclure) {
        List<Intervention> interventionsDuTechnicien = interventionRepository.findByTechnicienId(technicien.getId());
        for (Intervention intervention : interventionsDuTechnicien) {
            if (Objects.equals(intervention.getId(), interventionIdAExclure)) {
                continue;
            }
            if (!"ANNULEE".equals(intervention.getStatut()) && creneau.getDateDebut().isBefore(intervention.getDatePlanifiee().plusHours(1))
                    && creneau.getDateFin().isAfter(intervention.getDatePlanifiee())) {
                throw new IllegalArgumentException("Chevauchement de créneau avec l'intervention " + intervention.getId() + " planifiée à " + intervention.getDatePlanifiee());
            }
        }
    }

    private boolean transitionAutorisee(String ancienStatut, String nouveauStatut) {
        if (!STATUTS_VALIDES.contains(ancienStatut) || !STATUTS_VALIDES.contains(nouveauStatut)) {
            return false;
        }
        if ("PLANIFIEE".equals(ancienStatut)) {
            return "EN_COURS".equals(nouveauStatut)
                    || "TERMINEE".equals(nouveauStatut)
                    || "ECHOUEE".equals(nouveauStatut)
                    || "ANNULEE".equals(nouveauStatut);
        }
        if ("EN_COURS".equals(ancienStatut)) {
            return "TERMINEE".equals(nouveauStatut) || "ECHOUEE".equals(nouveauStatut);
        }
        return false;
    }

    private List<String> statutsAtteignables(String ancienStatut) {
        if ("PLANIFIEE".equals(ancienStatut)) {
            return List.of("EN_COURS", "TERMINEE", "ECHOUEE", "ANNULEE");
        }
        if ("EN_COURS".equals(ancienStatut)) {
            return List.of("TERMINEE", "ECHOUEE");
        }
        return List.of();
    }

    private void enregistrerHistorique(Intervention intervention, String statut, String auteur) {
        Statut s = new Statut();
        s.setLibelle(statut);
        HistoriqueStatut historique = HistoriqueStatut.builder()
                .intervention(intervention)
                .auteur(auteur)
                .statut(s)
                .dateChangement(LocalDateTime.now())
                .build();
        historiqueStatutRepository.save(historique);
    }

    private void verifierCreneau(Creneau creneau) {
        if (creneau == null || creneau.getDateDebut() == null || creneau.getDateFin() == null || !creneau.getDateFin().isAfter(creneau.getDateDebut())) {
            throw new ParametresInvalidesException("Le créneau est invalide");
        }
    }

    private void verifierFigeage(Intervention intervention, InterventionRequestDto request, TypeIntervention type, Client client, Technicien technicien, Creneau creneau) {
        // Simplified check
    }

    private void appliquerStatut(Intervention intervention, String nouveauStatut) {
        if (nouveauStatut == null || nouveauStatut.isBlank() || Objects.equals(intervention.getStatut(), nouveauStatut)) {
            return;
        }
        intervention.setStatut(nouveauStatut);
    }

    private boolean estPlanifiee(Intervention intervention) {
        return Objects.equals(intervention.getStatut(), "PLANIFIEE");
    }

    private void associer(Intervention intervention, Technicien technicien, Creneau creneau) {
        // Simplified association
    }
}