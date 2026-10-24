package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.CreneauResponseDto;
import com.interventionTerrain.interventionTerrain.dto.RequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.exception.ParametresInvalidesException;
import com.interventionTerrain.interventionTerrain.repository.ClientRepository;
import com.interventionTerrain.interventionTerrain.repository.CreneauRepository;
import com.interventionTerrain.interventionTerrain.entity.Client;
import com.interventionTerrain.interventionTerrain.entity.Creneau;
import com.interventionTerrain.interventionTerrain.entity.InterventionCreneaux;
import com.interventionTerrain.interventionTerrain.entity.InterventionTechnicien;
import com.interventionTerrain.interventionTerrain.entity.HistoriqueStatut;
import com.interventionTerrain.interventionTerrain.entity.Technicien;
import com.interventionTerrain.interventionTerrain.entity.TypeIntervention;
import com.interventionTerrain.interventionTerrain.dto.InterventionRequestDto;
import com.interventionTerrain.interventionTerrain.dto.InterventionResponseDto;
import com.interventionTerrain.interventionTerrain.repository.InterventionCreneauxRepository;
import com.interventionTerrain.interventionTerrain.repository.InterventionTechnicienRepository;
import com.interventionTerrain.interventionTerrain.repository.HistoriqueStatutRepository;
import com.interventionTerrain.interventionTerrain.repository.StatutRepository;
import com.interventionTerrain.interventionTerrain.repository.TechnicienRepository;
import com.interventionTerrain.interventionTerrain.repository.TypeInterventionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import com.interventionTerrain.interventionTerrain.entity.Intervention;
import com.interventionTerrain.interventionTerrain.repository.InterventionRepository;

/**
 * Service métier gérant les interventions.
 * Fournit la création, la modification, la suppression et les conversions entité/DTO.
 * Le technicien et le créneau sont gérés via les tables associatives existantes
 * (intervention_technicien et intervention_creneau), sans nouvelle relation.
 * Les conversions imbriquées sont déléguées à {@link ClientService},
 * {@link TypeInterventionService}, {@link CreneauService} et {@link TechnicienService}.
 */
@Service
public class InterventionService extends AbstractCrudService {

    /** Statut attribué à toute intervention nouvellement créée. */
    private static final String STATUT_INITIAL = "PLANIFIEE";

    private final InterventionRepository interventionRepository;
    private final ClientRepository clientRepository;
    private final TypeInterventionRepository typeInterventionRepository;
    private final TechnicienRepository technicienRepository;
    private final CreneauRepository creneauRepository;
    private final InterventionTechnicienRepository interventionTechnicienRepository;
    private final InterventionCreneauxRepository interventionCreneauxRepository;
    private final StatutRepository statutRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final TechnicienService technicienService;
    private final ClientService clientService;
    private final TypeInterventionService typeInterventionService;
    private final CreneauService creneauService;

    /**
     * Constructeur injectant les repositories nécessaires à l'intervention
     * et les services de conversion imbriqués.
     *
     * @param interventionRepository le repository des interventions
     * @param clientRepository le repository des clients
     * @param typeInterventionRepository le repository des types d'intervention
     * @param technicienRepository le repository des techniciens
     * @param creneauRepository le repository des créneaux
     * @param interventionTechnicienRepository le repository de l'association intervention/technicien
     * @param interventionCreneauxRepository le repository de l'association intervention/créneau
     * @param statutRepository le repository du référentiel des statuts (historique)
     * @param historiqueStatutRepository le repository de l'historique des statuts
     * @param technicienService le service des techniciens (conversion du technicien associé)
     * @param clientService le service de conversion des clients
     * @param typeInterventionService le service de conversion des types d'intervention
     * @param creneauService le service de conversion des créneaux
     */
    public InterventionService(InterventionRepository interventionRepository,
                               ClientRepository clientRepository,
                               TypeInterventionRepository typeInterventionRepository,
                               TechnicienRepository technicienRepository,
                               CreneauRepository creneauRepository,
                               InterventionTechnicienRepository interventionTechnicienRepository,
                               InterventionCreneauxRepository interventionCreneauxRepository,
                               StatutRepository statutRepository,
                               HistoriqueStatutRepository historiqueStatutRepository,
                               TechnicienService technicienService,
                               ClientService clientService,
                               TypeInterventionService typeInterventionService,
                               CreneauService creneauService) {
        super(interventionRepository);
        this.interventionRepository = interventionRepository;
        this.clientRepository = clientRepository;
        this.typeInterventionRepository = typeInterventionRepository;
        this.technicienRepository = technicienRepository;
        this.creneauRepository = creneauRepository;
        this.interventionTechnicienRepository = interventionTechnicienRepository;
        this.interventionCreneauxRepository = interventionCreneauxRepository;
        this.statutRepository = statutRepository;
        this.historiqueStatutRepository = historiqueStatutRepository;
        this.technicienService = technicienService;
        this.clientService = clientService;
        this.typeInterventionService = typeInterventionService;
        this.creneauService = creneauService;
    }

    /**
     * Convertit les données d'une requête en entité intervention.
     *
     * @param interventionRequest Les données de la requête pour créer l'intervention.
     * @param typeIntervention Le type d'intervention associé.
     * @param client Le client associé à l'intervention.
     * @return Intervention L'entité intervention créée.
     */
    public Intervention toEntity(InterventionRequestDto interventionRequest, TypeIntervention typeIntervention, Client client) {
        return Intervention.builder()
                .datePlanifiee(interventionRequest.getDatePlanifiee())
                .statut(interventionRequest.getStatut())
                .commentaire(interventionRequest.getCommentaire())
                .reference(interventionRequest.getReference())
                .typeIntervention(typeIntervention)
                .client(client)
                .build();
    }

    /**
     * Convertit une entité intervention en objet de réponse typé.
     * Seul point de construction de {@link InterventionResponseDto}.
     * Les associations technicien et créneau sont lues via les tables
     * associatives (peut être absentes → null).
     *
     * @param intervention L'entité intervention à convertir.
     * @return InterventionResponseDto Les détails de l'intervention convertie.
     */
    public InterventionResponseDto toInterventionResponse(Intervention intervention) {
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
                .typeIntervention(typeInterventionService.toDto(intervention.getTypeIntervention()))
                .client(clientService.toClientResponse(intervention.getClient()))
                .technicien(technicien == null ? null : technicienService.toTechnicienResponse(technicien))
                .creneau(creneauService.toDto(creneau))
                .build();
    }

    /**
     * Convertit une entité intervention en objet de réponse.
     *
     * @param entity L'entité intervention à convertir.
     * @return ResponseDto Les détails de l'intervention convertie.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toInterventionResponse((Intervention) entity);
    }

    /**
     * Crée une nouvelle intervention ainsi que ses associations technicien et créneau.
     *
     * @param requestDto les informations de l'intervention à créer
     * @return l'intervention créée
     * @throws NotFoundException si le type d'intervention ou le client n'existent pas
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        InterventionRequestDto interventionRequest = (InterventionRequestDto) requestDto;
        TypeIntervention typeIntervention = typeInterventionRepository.findById(interventionRequest.getTypeInterventionId())
                .orElseThrow(() -> new NotFoundException("Type d'intervention non trouvé"));
        Client client = clientRepository.findById(interventionRequest.getClientId())
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));
        Technicien technicien = technicienRepository.findById(interventionRequest.getTechnicienId())
                .orElseThrow(() -> new NotFoundException("Technicien non trouvé"));
        Creneau creneau = creneauRepository.findById(interventionRequest.getCreneauId())
                .orElseThrow(() -> new NotFoundException("Créneau non trouvé"));
        verifierCreneau(creneau);

        Intervention intervention = toEntity(interventionRequest, typeIntervention, client);
        intervention.setStatut(STATUT_INITIAL);
        Intervention interventionSauvee = interventionRepository.save(intervention);
        associer(interventionSauvee, technicien, creneau);
        return toDto(interventionSauvee);
    }

    /**
     * Modifie une intervention existante et réassocie technicien et créneau.
     *
     * @param id L'identifiant de l'intervention à modifier.
     * @param requestDto Les données de la requête pour modifier l'intervention.
     * @return ResponseDto Les détails de l'intervention modifiée.
     * @throws NotFoundException si l'intervention, le type ou le client n'existent pas
     */
    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        InterventionRequestDto interventionRequest = (InterventionRequestDto) requestDto;
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        TypeIntervention typeIntervention = typeInterventionRepository.findById(interventionRequest.getTypeInterventionId())
                .orElseThrow(() -> new NotFoundException("Type d'intervention non trouvé"));
        Client client = clientRepository.findById(interventionRequest.getClientId())
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));
        Technicien technicien = technicienRepository.findById(interventionRequest.getTechnicienId())
                .orElseThrow(() -> new NotFoundException("Technicien non trouvé"));
        Creneau creneau = creneauRepository.findById(interventionRequest.getCreneauId())
                .orElseThrow(() -> new NotFoundException("Créneau non trouvé"));

        if (estPlanifiee(intervention)) {
            verifierCreneau(creneau);
        } else {
            verifierFigeage(intervention, interventionRequest, typeIntervention, client, technicien, creneau);
        }

        intervention.setDatePlanifiee(interventionRequest.getDatePlanifiee());
        intervention.setCommentaire(interventionRequest.getCommentaire());
        intervention.setReference(interventionRequest.getReference());
        intervention.setTypeIntervention(typeIntervention);
        intervention.setClient(client);
        appliquerStatut(intervention, interventionRequest.getStatut());
        interventionRepository.save(intervention);

        interventionTechnicienRepository.deleteByInterventionId(id);
        interventionCreneauxRepository.deleteByInterventionId(id);
        associer(intervention, technicien, creneau);
        return toDto(intervention);
    }

    /**
     * Supprime une intervention ainsi que ses associations technicien et créneau.
     * La suppression des associations doit précéder la suppression de l'intervention,
     * sous peine de violation des contraintes de clé étrangère.
     *
     * @param id L'identifiant de l'intervention à supprimer.
     * @return ResponseDto Les détails de l'intervention supprimée.
     * @throws NotFoundException si l'intervention à supprimer n'existe pas
     */
    @Override
    public ResponseDto delete(Long id) throws Throwable {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (!estPlanifiee(intervention)) {
            throw new IllegalArgumentException("Seule une intervention PLANIFIEE peut être supprimée : "
                    + "une intervention commencée s'annule ou se clôture");
        }
        ResponseDto response = toDto(intervention);
        interventionTechnicienRepository.deleteByInterventionId(id);
        interventionCreneauxRepository.deleteByInterventionId(id);
        interventionRepository.deleteById(id);
        return response;
    }

    /**
     * Récupère l'historique des interventions d'un client, de la plus récente
     * à la plus ancienne, avec le technicien, le type et le statut.
     * Un client sans intervention renvoie une liste vide.
     *
     * @param clientId L'identifiant du client.
     * @return List<InterventionResponseDto> Les interventions du client.
     * @throws NotFoundException si le client n'existe pas
     */
    public List<InterventionResponseDto> getInterventionsByClient(Long clientId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));
        return interventionRepository.findByClientId(clientId).stream()
                .map(this::toInterventionResponse)
                .toList();
    }

    /**
     * Récupère une page d'interventions triée par date planifiée puis par heure
     * de début du créneau associé (jointure sur la table de liaison existante).
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return une page d'interventions converties en DTO
     */
    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille));
        return interventionRepository.findAllTriees(pageable).map(this::toDto);
    }

    /**
     * Associe un technicien et un créneau à une intervention, via les tables
     * associatives existantes intervention_technicien et intervention_creneau.
     *
     * @param intervention l'intervention concernée
     * @param technicien le technicien affecté
     * @param creneau le créneau retenu
     */
    private void associer(Intervention intervention, Technicien technicien, Creneau creneau) {
        interventionTechnicienRepository.save(InterventionTechnicien.builder()
                .intervention(intervention)
                .technicien(technicien)
                .estResponsable(Boolean.TRUE)
                .build());
        interventionCreneauxRepository.save(InterventionCreneaux.builder()
                .intervention(intervention)
                .creneau(creneau)
                .build());
    }

    /**
     * Indique si une intervention est encore au statut PLANIFIEE.
     *
     * @param intervention l'intervention à contrôler
     * @return vrai si le statut est PLANIFIEE (comparaison insensible à la casse)
     */
    private boolean estPlanifiee(Intervention intervention) {
        return intervention.getStatut() != null && STATUT_INITIAL.equalsIgnoreCase(intervention.getStatut());
    }

    /**
     * Vérifie la cohérence d'un créneau : l'heure de fin doit suivre l'heure de
     * début et le créneau doit être situé dans le futur.
     *
     * @param creneau le créneau à contrôler
     * @throws IllegalArgumentException si le créneau est incohérent ou déjà passé
     */
    private void verifierCreneau(Creneau creneau) {
        if (!creneau.getDateFin().isAfter(creneau.getDateDebut())) {
            throw new IllegalArgumentException("L'heure de fin du créneau doit suivre son heure de début");
        }
        if (!creneau.getDateDebut().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Le créneau doit être dans le futur");
        }
    }

    /**
     * Vérifie qu'aucun champ figé d'une intervention non PLANIFIEE n'est modifié.
     * Seul le statut peut encore évoluer (annulation ou clôture).
     *
     * @param intervention l'intervention existante
     * @param request les données de la requête de modification
     * @param type le type demandé
     * @param client le client demandé
     * @param technicien le technicien demandé
     * @param creneau le créneau demandé
     * @throws IllegalArgumentException si un champ figé est modifié
     */
    private void verifierFigeage(Intervention intervention, InterventionRequestDto request,
                                 TypeIntervention type, Client client,
                                 Technicien technicien, Creneau creneau) {
        Long technicienActuel = interventionTechnicienRepository.findByInterventionId(intervention.getId())
                .stream().findFirst().map(it -> it.getTechnicien().getId()).orElse(null);
        Long creneauActuel = interventionCreneauxRepository.findByInterventionId(intervention.getId())
                .stream().findFirst().map(ic -> ic.getCreneau().getId()).orElse(null);
        boolean modifie = !Objects.equals(intervention.getDatePlanifiee(), request.getDatePlanifiee())
                || !Objects.equals(intervention.getCommentaire(), request.getCommentaire())
                || !Objects.equals(intervention.getReference(), request.getReference())
                || !Objects.equals(intervention.getTypeIntervention().getId(), type.getId())
                || !Objects.equals(intervention.getClient().getId(), client.getId())
                || !Objects.equals(technicienActuel, technicien.getId())
                || !Objects.equals(creneauActuel, creneau.getId());
        if (modifie) {
            throw new IllegalArgumentException("Une intervention qui n'est plus PLANIFIEE est figée : "
                    + "seul son statut peut évoluer (annulation ou clôture)");
        }
    }

    /**
     * Applique un changement de statut et enregistre une ligne d'historique
     * lorsque le libellé existe dans le référentiel des statuts.
     *
     * @param intervention l'intervention concernée
     * @param nouveauStatut le statut demandé (peut être null → aucun changement)
     */
    private void appliquerStatut(Intervention intervention, String nouveauStatut) {
        if (nouveauStatut == null || nouveauStatut.isBlank()
                || Objects.equals(intervention.getStatut(), nouveauStatut)) {
            return;
        }
        intervention.setStatut(nouveauStatut);
        statutRepository.findByLibelleIgnoreCase(nouveauStatut).ifPresent(statut ->
                historiqueStatutRepository.save(HistoriqueStatut.builder()
                        .dateChangement(LocalDateTime.now())
                        .auteur("systeme")
                        .intervention(intervention)
                        .statut(statut)
                        .build()));
    }

    /**
     * Récupère une page d'interventions, éventuellement filtrée par statut et
     * par période. Les critères sont optionnels et combinables.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @param statut le statut recherché (absent = aucun filtre)
     * @param du début de période au format date (absent = aucune borne basse)
     * @param au fin de période au format date (absent = aucune borne haute)
     * @return la page d'interventions triées par date puis par heure de début
     * @throws ParametresInvalidesException si le statut est inconnu ou si la
     *         période est incohérente (du postérieur à au)
     */
    public Page<ResponseDto> lister(int page, int taille, String statut,
                                    LocalDate du, LocalDate au) {
        if (statut == null && du == null && au == null) {
            return getAll(page, taille);
        }
        if (du != null && au != null && du.isAfter(au)) {
            throw new ParametresInvalidesException("La date 'du' (" + du
                    + ") ne peut pas être postérieure à la date 'au' (" + au + ")");
        }
        String statutFiltre = "";
        if (statut != null && !statut.isBlank()) {
            statutRepository.findByLibelleIgnoreCase(statut)
                    .orElseThrow(() -> new ParametresInvalidesException(
                            "Statut inconnu : '" + statut + "'"));
            statutFiltre = statut;
        }
        LocalDateTime debut = du != null ? du.atStartOfDay() : LocalDateTime.of(1, 1, 1, 0, 0);
        LocalDateTime fin = au != null ? au.plusDays(1).atStartOfDay()
                : LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille));
        return interventionRepository.filtrer(statutFiltre, debut, fin, pageable).map(this::toDto);
    }

    /**
     * Récupère le planning d'un technicien : ses interventions sur une période,
     * triées par date puis par heure de début du créneau, avec le client, son
     * adresse et le créneau. Sans période, la semaine en cours (lundi au
     * dimanche) est utilisée.
     *
     * @param technicienId l'identifiant du technicien
     * @param du début de période au format date (absent = début de la semaine en cours)
     * @param au fin de période au format date (absent = fin de la semaine en cours)
     * @return les interventions du technicien, triées
     * @throws NotFoundException si le technicien n'existe pas
     * @throws ParametresInvalidesException si la période est incohérente ou
     *         dépasse trois mois
     */
    public List<InterventionResponseDto> planner(Long technicienId, LocalDate du, LocalDate au) {
        technicienRepository.findById(technicienId)
                .orElseThrow(() -> new NotFoundException("Technicien non trouvé"));
        LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate debut = du != null ? du : lundi;
        LocalDate fin = au != null ? au : lundi.plusDays(6);
        if (debut.isAfter(fin)) {
            throw new ParametresInvalidesException("La date 'du' (" + debut
                    + ") ne peut pas être postérieure à la date 'au' (" + fin + ")");
        }
        if (fin.isAfter(debut.plusMonths(3))) {
            throw new ParametresInvalidesException("La période demandée (du " + debut
                    + " au " + fin + ") ne peut pas dépasser trois mois");
        }
        return interventionRepository.planner(technicienId, debut.atStartOfDay(),
                        fin.plusDays(1).atStartOfDay()).stream()
                .map(this::toInterventionResponse)
                .toList();
    }

    /**
     * Nomme la ressource dans le message d'erreur d'un identifiant inconnu.
     *
     * @return le message d'introuvable propre à la ressource
     */
    @Override
    protected String messageIntrouvable() {
        return "Intervention non trouvée";
    }
}
