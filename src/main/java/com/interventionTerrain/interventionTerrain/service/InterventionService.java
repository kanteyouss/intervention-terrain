package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.CreneauResponseDto;
import com.interventionTerrain.interventionTerrain.dto.ClientResponseDto;
import com.interventionTerrain.interventionTerrain.dto.RequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TypeInterventionResponseDto;
import com.interventionTerrain.interventionTerrain.repository.ClientRepository;
import com.interventionTerrain.interventionTerrain.repository.CreneauRepository;
import com.interventionTerrain.interventionTerrain.entity.*;
import com.interventionTerrain.interventionTerrain.dto.InterventionRequestDto;
import com.interventionTerrain.interventionTerrain.dto.InterventionResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienResponseDto;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.exception.ParametresInvalidesException;
import com.interventionTerrain.interventionTerrain.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class InterventionService extends AbstractCrudService {

    private static final String STATUT_INITIAL = "PLANIFIEE";
    private static final Set<String> STATUTS_VALIDES = Set.of("PLANIFIEE", "EN_COURS", "TERMINEE", "ECHOUEE", "ANNULEE");
    private static final long MAX_PERIOD_DAYS = 90;

    private final InterventionRepository interventionRepository;
    private final ClientRepository clientRepository;
    private final TypeInterventionRepository typeInterventionRepository;
    private final TechnicienRepository technicienRepository;
    private final CreneauRepository creneauRepository;
    private final InterventionTechnicienRepository interventionTechnicienRepository;
    private final InterventionCreneauxRepository interventionCreneauxRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final TechnicienCompetenceRepository technicienCompetenceRepository;
    private final StatutRepository statutRepository;
    private final TechnicienService technicienService;

    public InterventionService(InterventionRepository interventionRepository,
                               ClientRepository clientRepository,
                               TypeInterventionRepository typeInterventionRepository,
                               TechnicienRepository technicienRepository,
                               CreneauRepository creneauRepository,
                               InterventionTechnicienRepository interventionTechnicienRepository,
                               InterventionCreneauxRepository interventionCreneauxRepository,
                               HistoriqueStatutRepository historiqueStatutRepository,
                               TechnicienCompetenceRepository technicienCompetenceRepository,
                               StatutRepository statutRepository,
                               TechnicienService technicienService) {
        super(interventionRepository);
        this.interventionRepository = interventionRepository;
        this.clientRepository = clientRepository;
        this.typeInterventionRepository = typeInterventionRepository;
        this.technicienRepository = technicienRepository;
        this.creneauRepository = creneauRepository;
        this.interventionTechnicienRepository = interventionTechnicienRepository;
        this.interventionCreneauxRepository = interventionCreneauxRepository;
        this.historiqueStatutRepository = historiqueStatutRepository;
        this.technicienCompetenceRepository = technicienCompetenceRepository;
        this.statutRepository = statutRepository;
        this.technicienService = technicienService;
    }

    private LocalDateTime toDateTime(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time);
    }

    private void validerCreneau(LocalDate dateIntervention, Creneau creneau) {
        if (creneau == null) {
            throw new IllegalArgumentException("Le créneau est obligatoire");
        }
        if (creneau.getDateDebut() == null || creneau.getDateFin() == null) {
            throw new IllegalArgumentException("Le créneau doit contenir une heure de début et une heure de fin");
        }
        if (!creneau.getDateFin().isAfter(creneau.getDateDebut())) {
            throw new IllegalArgumentException("L'heure de fin doit être postérieure à l'heure de début");
        }

        LocalDateTime debutCreneau = toDateTime(dateIntervention, creneau.getDateDebut());
        if (debutCreneau.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Le créneau doit être dans le futur");
        }
    }

    public Intervention toEntity(InterventionRequestDto interventionRequest, TypeIntervention typeIntervention, Client client) {
        return Intervention.builder()
                .datePlanifiee(interventionRequest.getDatePlanifiee())
                .statut(STATUT_INITIAL)
                .commentaire(interventionRequest.getCommentaire())
                .reference(interventionRequest.getReference())
                .typeIntervention(typeIntervention)
                .client(client)
                .build();
    }

    @Override
    public ResponseDto toDto(Object entity) {
        Intervention intervention = (Intervention) entity;
        Technicien technicien = interventionTechnicienRepository.findByInterventionId(intervention.getId())
                .stream().findFirst().map(InterventionTechnicien::getTechnicien).orElse(null);
        Creneau creneau = interventionCreneauxRepository.findByInterventionId(intervention.getId())
                .stream().findFirst().map(InterventionCreneaux::getCreneau).orElse(null);
        return InterventionResponseDto.builder()
                .id(intervention.getId())
                .datePlanifiee(intervention.getDatePlanifiee())
                .statut(intervention.getStatut())
                .commentaire(intervention.getCommentaire())
                .reference(intervention.getReference())
                .typeIntervention(TypeInterventionResponseDto.builder()
                        .id(intervention.getTypeIntervention().getId())
                        .nom(intervention.getTypeIntervention().getNom())
                        .description(intervention.getTypeIntervention().getDescription())
                        .delaiHeures(intervention.getTypeIntervention().getDelaiHeures())
                        .build())
                .client(ClientResponseDto.builder()
                        .id(intervention.getClient().getId())
                        .nom(intervention.getClient().getNom())
                        .prenom(intervention.getClient().getPrenom())
                        .email(intervention.getClient().getEmail())
                        .telephone(intervention.getClient().getTelephone())
                        .adresse(intervention.getClient().getAdresse())
                        .commune(intervention.getClient().getCommune())
                        .build())
                .technicien(technicien == null ? null : (TechnicienResponseDto) technicienService.toDto(technicien))
                .creneau(toCreneauResponse(creneau))
                .build();
    }

    @Override
    @Transactional
    public ResponseDto create(RequestDto requestDto) {
        InterventionRequestDto interventionRequest = (InterventionRequestDto) requestDto;

        if (interventionRequest.getDatePlanifiee() == null) {
            throw new IllegalArgumentException("La date planifiée est obligatoire");
        }
        if (interventionRequest.getDatePlanifiee().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date planifiée doit être dans le futur");
        }

        TypeIntervention typeIntervention = typeInterventionRepository.findById(interventionRequest.getTypeInterventionId())
                .orElseThrow(() -> new NotFoundException("Type d'intervention non trouvé"));
        Client client = clientRepository.findById(interventionRequest.getClientId())
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));
        Technicien technicien = technicienRepository.findById(interventionRequest.getTechnicienId())
                .orElseThrow(() -> new NotFoundException("Technicien non trouvé"));
        Creneau creneau = creneauRepository.findById(interventionRequest.getCreneauId())
                .orElseThrow(() -> new NotFoundException("Créneau non trouvé"));

        validerCreneau(interventionRequest.getDatePlanifiee(), creneau);
        verifierCompetencesTechnicien(technicien, typeIntervention, interventionRequest.getDatePlanifiee());
        verifierChevauchement(technicien, creneau, null, interventionRequest.getDatePlanifiee());

        Intervention intervention = toEntity(interventionRequest, typeIntervention, client);
        intervention = interventionRepository.save(intervention);

        interventionTechnicienRepository.save(InterventionTechnicien.builder()
                .intervention(intervention)
                .technicien(technicien)
                .estResponsable(Boolean.TRUE)
                .build());

        interventionCreneauxRepository.save(InterventionCreneaux.builder()
                .intervention(intervention)
                .creneau(creneau)
                .build());

        enregistrerHistorique(intervention, intervention.getStatut(), "SYSTEM");
        return toDto(intervention);
    }

    @Override
    @Transactional
    public ResponseDto update(Long id, RequestDto requestDto) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Intervention non trouvée"));

        if (!STATUT_INITIAL.equals(intervention.getStatut())) {
            throw new IllegalStateException("Impossible de modifier une intervention qui n'est pas PLANIFIEE");
        }

        InterventionRequestDto interventionRequest = (InterventionRequestDto) requestDto;
        if (interventionRequest.getDatePlanifiee() == null) {
            throw new IllegalArgumentException("La date planifiée est obligatoire");
        }

        Technicien technicien = technicienRepository.findById(interventionRequest.getTechnicienId())
                .orElseThrow(() -> new NotFoundException("Technicien non trouvé"));
        Creneau creneau = creneauRepository.findById(interventionRequest.getCreneauId())
                .orElseThrow(() -> new NotFoundException("Créneau non trouvé"));

        validerCreneau(interventionRequest.getDatePlanifiee(), creneau);
        verifierCompetencesTechnicien(technicien, intervention.getTypeIntervention(), interventionRequest.getDatePlanifiee());
        verifierChevauchement(technicien, creneau, id, interventionRequest.getDatePlanifiee());

        intervention.setDatePlanifiee(interventionRequest.getDatePlanifiee());
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
    @Transactional
    public ResponseDto delete(Long id) throws Throwable {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Intervention non trouvée"));

        if (!STATUT_INITIAL.equals(intervention.getStatut())) {
            throw new IllegalStateException("Seules les interventions PLANIFIEE peuvent être supprimées");
        }

        ResponseDto response = toDto(intervention);
        interventionTechnicienRepository.deleteByInterventionId(id);
        interventionCreneauxRepository.deleteByInterventionId(id);
        interventionRepository.deleteById(id);
        return response;
    }

    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Sort sort = Sort.by(Sort.Order.asc("datePlanifiee"));
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), sort);
        return interventionRepository.findAll(pageable).map(this::toDto);
    }

    public Page<ResponseDto> getHistoriqueByClient(Long clientId, int page, int taille) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));
        List<ResponseDto> all = interventionRepository.findByClientId(clientId).stream()
                .map(this::toDto)
                .toList();

        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille));
        int start = (int) pageable.getOffset();
        if (start >= all.size()) {
            return new PageImpl<>(List.of(), pageable, all.size());
        }
        int end = Math.min(start + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }

    public Page<ResponseDto> getAllFiltre(String statut, LocalDate du, LocalDate au, int page, int taille) {
        if (statut != null && !STATUTS_VALIDES.contains(statut)) {
            throw new IllegalArgumentException("Statut inconnu : " + statut);
        }
        if (du != null && au != null && du.isAfter(au)) {
            throw new IllegalArgumentException("La date de début ne peut pas être postérieure à la date de fin");
        }
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille));

        List<Intervention> resultats;
        if (statut != null) {
            resultats = interventionRepository.findByStatutAndDatePlanifieeBetween(statut, du, au);
        } else {
            resultats = interventionRepository.findByDatePlanifieeBetween(du, au);
        }

        List<ResponseDto> dtos = resultats.stream().map(this::toDto).toList();
        return new PageImpl<>(dtos, pageable, dtos.size());
    }
    public Page<ResponseDto> getInterventions(String statut, LocalDate du, LocalDate au, int page, int taille) {
        if (statut != null || du != null || au != null) {
            return getAllFiltre(statut, du, au, page, taille);
        }
        return getAll(page, taille);
    }

    public List<ResponseDto> getPlanning(Long technicienId, LocalDate du, LocalDate au) {
        technicienRepository.findById(technicienId)
                .orElseThrow(() -> new NotFoundException("Technicien non trouvé"));
        LocalDate debut = du != null ? du : LocalDate.now();
        LocalDate fin = au != null ? au : debut.plusDays(7);
        if (ChronoUnit.DAYS.between(debut, fin) > MAX_PERIOD_DAYS) {
            throw new IllegalArgumentException("La période ne peut pas dépasser 3 mois");
        }
        return interventionRepository.findByTechnicienIdAndDatePlanifieeBetween(technicienId, debut, fin).stream()
                .map(this::toDto)
                .toList();
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
            throw new ParametresInvalidesException("Technicien non qualifié pour ce type d'intervention. " +
                    "Compétences manquantes ou expirées : " + competencesManquantes);
        }
    }

    public void verifierChevauchement(Technicien technicien, Creneau creneau, Long interventionIdAExclure, LocalDate dateIntervention) {
        List<Intervention> interventionsDuTechnicien = interventionRepository.findByTechnicienId(technicien.getId());

        LocalDateTime debutNouveau = toDateTime(dateIntervention, creneau.getDateDebut());
        LocalDateTime finNouveau = toDateTime(dateIntervention, creneau.getDateFin());

        for (Intervention intervention : interventionsDuTechnicien) {
            if (Objects.equals(intervention.getId(), interventionIdAExclure)) {
                continue;
            }
            if (!"ANNULEE".equals(intervention.getStatut())) {
                Creneau c = interventionCreneauxRepository.findByInterventionId(intervention.getId())
                        .stream().findFirst().map(InterventionCreneaux::getCreneau).orElse(null);
                if (c != null && c.getDateDebut() != null && c.getDateFin() != null) {
                    LocalDateTime debutExistant = toDateTime(intervention.getDatePlanifiee(), c.getDateDebut());
                    LocalDateTime finExistant = toDateTime(intervention.getDatePlanifiee(), c.getDateFin());

                    if (debutNouveau.isBefore(finExistant) && finNouveau.isAfter(debutExistant)) {
                        throw new IllegalStateException("Chevauchement de créneau avec l'intervention " + intervention.getId()
                                + " (créneau " + c.getDateDebut() + " - " + c.getDateFin() + ")");
                    }
                }
            }
        }
    }

    @Transactional
    public Intervention mettreAJourStatut(Long interventionId, String nouveauStatut) {
        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new NotFoundException("Intervention non trouvée"));
        String ancienStatut = intervention.getStatut() == null ? STATUT_INITIAL : intervention.getStatut();
        if (!STATUTS_VALIDES.contains(nouveauStatut)) {
            throw new ParametresInvalidesException("Statut inconnu : " + nouveauStatut);
        }
        if (!transitionAutorisee(ancienStatut, nouveauStatut)) {
            throw new IllegalStateException("Transition de statut invalide de '" + ancienStatut + "' vers '" + nouveauStatut
                    + "'. Statuts atteignables : " + statutsAtteignables(ancienStatut));
        }
        intervention.setStatut(nouveauStatut);
        interventionRepository.save(intervention);
        enregistrerHistorique(intervention, nouveauStatut, "SYSTEM");
        return intervention;
    }

    @Transactional
    public Intervention creerInterventionDeReprise(Long interventionId, LocalDate nouvelleDatePlanifiee) {
        Intervention origine = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new NotFoundException("Intervention non trouvée"));

        if (!"ECHOUEE".equals(origine.getStatut())) {
            throw new IllegalStateException("Une intervention de reprise n'est autorisée que pour une intervention en statut ECHOUEE");
        }

        if (interventionRepository.existsByCommentaire("Reprise de l'intervention " + origine.getId())) {
            throw new IllegalStateException("Une reprise existe déjà pour cette intervention");
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

    private boolean transitionAutorisee(String ancienStatut, String nouveauStatut) {
        if (!STATUTS_VALIDES.contains(ancienStatut) || !STATUTS_VALIDES.contains(nouveauStatut)) {
            return false;
        }
        if ("PLANIFIEE".equals(ancienStatut)) {
            return "EN_COURS".equals(nouveauStatut) || "TERMINEE".equals(nouveauStatut)
                    || "ECHOUEE".equals(nouveauStatut) || "ANNULEE".equals(nouveauStatut);
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

    private void enregistrerHistorique(Intervention intervention, String statutLibelle, String auteur) {
        Statut statut = statutRepository.findByLibelleIgnoreCase(statutLibelle)
                .orElseGet(() -> {
                    Statut s = new Statut();
                    s.setLibelle(statutLibelle);
                    return statutRepository.save(s);
                });
        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setStatut(statut);
        historique.setAuteur(auteur);
        historique.setIntervention(intervention);
        historique.setDateChangement(LocalDateTime.now());
        historiqueStatutRepository.save(historique);
    }

    private CreneauResponseDto toCreneauResponse(Creneau creneau) {
        if (creneau == null) return null;
        return CreneauResponseDto.builder()
                .id(creneau.getId())
                .dateDebut(creneau.getDateDebut())
                .dateFin(creneau.getDateFin())
                .build();
    }

    @Override
    protected String messageIntrouvable() {
        return "Intervention non trouvée";
    }
}