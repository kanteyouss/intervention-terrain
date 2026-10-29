package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.*;
import com.interventionTerrain.interventionTerrain.exception.DoublonRessourceException;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.exception.RessourceUtiliseeException;
import com.interventionTerrain.interventionTerrain.repository.CommuneRepository;
import com.interventionTerrain.interventionTerrain.repository.InterventionCreneauxRepository;
import com.interventionTerrain.interventionTerrain.repository.InterventionRepository;
import com.interventionTerrain.interventionTerrain.repository.TechnicienRepository;
import com.interventionTerrain.interventionTerrain.repository.TechnicienCompetenceRepository;
import com.interventionTerrain.interventionTerrain.entity.Commune;
import com.interventionTerrain.interventionTerrain.entity.Creneau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.interventionTerrain.interventionTerrain.entity.Agence;
import com.interventionTerrain.interventionTerrain.repository.AgenceRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgenceService extends AbstractCrudService {

    private final AgenceRepository agenceRepository;
    private final CommuneRepository communeRepository;
    private final TechnicienRepository technicienRepository;
    private final TechnicienCompetenceRepository technicienCompetenceRepository;
    private final InterventionRepository interventionRepository;
    private final InterventionCreneauxRepository interventionCreneauxRepository;
    private final TauxReussiteService tauxReussiteService;

    public AgenceService(AgenceRepository agenceRepository,
                         CommuneRepository communeRepository,
                         TechnicienRepository technicienRepository,
                         TechnicienCompetenceRepository technicienCompetenceRepository,
                         InterventionRepository interventionRepository,
                         InterventionCreneauxRepository interventionCreneauxRepository,
                         TauxReussiteService tauxReussiteService) {
        super(agenceRepository);
        this.agenceRepository = agenceRepository;
        this.communeRepository = communeRepository;
        this.technicienRepository = technicienRepository;
        this.technicienCompetenceRepository = technicienCompetenceRepository;
        this.interventionRepository = interventionRepository;
        this.interventionCreneauxRepository = interventionCreneauxRepository;
        this.tauxReussiteService = tauxReussiteService;
    }

    public Agence toEntity(AgenceRequestDto agenceRequest, Commune commune) {
        return Agence.builder()
                .nom(agenceRequest.getNom())
                .ville(agenceRequest.getVille())
                .responsable(agenceRequest.getResponsable())
                .commune(commune)
                .build();
    }

    @Override
    public ResponseDto toDto(Object entity) {
        Agence agence = (Agence) entity;
        return AgenceResponseDto.builder()
                .id(agence.getId())
                .nom(agence.getNom())
                .ville(agence.getVille())
                .responsable(agence.getResponsable())
                .commune(CommuneResponseDto.builder()
                        .id(agence.getCommune().getId())
                        .nom(agence.getCommune().getNom())
                        .codePostal(agence.getCommune().getCodePostal())
                        .departementId(agence.getCommune().getDepartement().getId())
                        .build())
                .build();
    }

    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Sort sort = Sort.by(Sort.Order.asc("ville"), Sort.Order.asc("nom"));
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), sort);
        return agenceRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public ResponseDto create(RequestDto requestDto) {
        AgenceRequestDto agenceRequest = (AgenceRequestDto) requestDto;
        agenceRequest.setNom((agenceRequest.getNom()));
        agenceRequest.setVille((agenceRequest.getVille()));
        if (agenceRepository.existsByNomIgnoreCase(agenceRequest.getNom())) {
            throw new DoublonRessourceException("Une agence nommée '" + agenceRequest.getNom() + "' existe déjà");
        }
        Commune commune = communeRepository.findById(agenceRequest.getCommuneId())
                .orElseThrow(() -> new NotFoundException("Commune non trouvée"));
        return toDto(agenceRepository.save(toEntity(agenceRequest, commune)));
    }

    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        AgenceRequestDto agenceRequest = (AgenceRequestDto) requestDto;
        agenceRequest.setNom((agenceRequest.getNom()));
        agenceRequest.setVille((agenceRequest.getVille()));
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (agenceRepository.existsByNomIgnoreCaseAndIdNot(agenceRequest.getNom(), id)) {
            throw new DoublonRessourceException("Une agence nommée '" + agenceRequest.getNom() + "' existe déjà");
        }
        boolean villeModifiee = !java.util.Objects.equals(agence.getVille(), agenceRequest.getVille());
        if (villeModifiee && technicienRepository.findByAgenceId(id).size() > 0) {
            throw new IllegalArgumentException("Impossible de supprimer cette agence car des techniciens y sont rattachés.");
        }
        Commune commune = communeRepository.findById(agenceRequest.getCommuneId())
                .orElseThrow(() -> new NotFoundException("Commune non trouvée"));
        agence.setNom(agenceRequest.getNom());
        agence.setVille(agenceRequest.getVille());
        agence.setResponsable(agenceRequest.getResponsable());
        agence.setCommune(commune);
        return toDto(agenceRepository.save(agence));
    }

    @Override
    public ResponseDto delete(Long id) throws Throwable {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (technicienRepository.findByAgenceId(id).size() > 0) {
            throw new RessourceUtiliseeException("Agence", id, "Technicien");
        }
        ResponseDto response = toDto(agence);
        agenceRepository.deleteById(id);
        return response;
    }

    public Page<TechnicienEffectifResponseDto> getEffectif(Long agenceId, int page, int taille) {
        agenceRepository.findById(agenceId)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        Map<Long, Long> validesParTechnicien = new HashMap<>();
        for (Object[] ligne : technicienCompetenceRepository.compterValidesParAgence(agenceId, LocalDate.now())) {
            validesParTechnicien.put((Long) ligne[0], (Long) ligne[1]);
        }
        List<TechnicienEffectifResponseDto> all = technicienRepository.findByAgenceId(agenceId).stream()
                .sorted(java.util.Comparator.comparing(t -> t.getNom().toLowerCase()))
                .map(t -> TechnicienEffectifResponseDto.builder()
                        .id(t.getId())
                        .nom(t.getNom())
                        .prenom(t.getPrenom())
                        .matricule(t.getMatricule())
                        .nbCompetencesValides(validesParTechnicien.getOrDefault(t.getId(), 0L))
                        .build())
                .toList();

        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille));
        int start = (int) pageable.getOffset();
        if (start >= all.size()) {
            return new PageImpl<>(List.of(), pageable, all.size());
        }
        int end = Math.min(start + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }

    public ChargeAgenceDto getCharge(Long agenceId, LocalDate du, LocalDate au) {
        Agence agence = agenceRepository.findById(agenceId)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        LocalDate debut = du != null ? du : LocalDate.now().minusDays(30);
        LocalDate fin = au != null ? au : LocalDate.now().plusDays(30);

        List<ChargeTechDto> techs = new ArrayList<>();
        long totalInterventions = 0;
        double totalHeures = 0;

        for (com.interventionTerrain.interventionTerrain.entity.Technicien t : technicienRepository.findByAgenceId(agenceId)) {
            List<com.interventionTerrain.interventionTerrain.entity.Intervention> interventions = interventionRepository
                    .findByTechnicienIdAndDatePlanifieeBetweenNonAnnulee(t.getId(), debut, fin);

            double heuresTech = 0;
            for (com.interventionTerrain.interventionTerrain.entity.Intervention i : interventions) {
                heuresTech += interventionCreneauxRepository.findByInterventionId(i.getId()).stream()
                        .mapToDouble(ic -> {
                            Creneau c = ic.getCreneau();
                            if (c != null && c.getDateDebut() != null && c.getDateFin() != null) {
                                return Duration.between(c.getDateDebut(), c.getDateFin()).toMinutes() / 60.0;
                            }
                            return 0;
                        }).sum();
            }

            heuresTech = Math.round(heuresTech * 100.0) / 100.0;
            totalInterventions += interventions.size();
            totalHeures += heuresTech;

            techs.add(ChargeTechDto.builder()
                    .id(t.getId())
                    .nom(t.getNom())
                    .prenom(t.getPrenom())
                    .nbInterventions(interventions.size())
                    .totalHeures(heuresTech)
                    .build());
        }

        totalHeures = Math.round(totalHeures * 100.0) / 100.0;

        return ChargeAgenceDto.builder()
                .agenceId(agenceId)
                .agenceNom(agence.getNom())
                .techniciens(techs)
                .totalInterventions(totalInterventions)
                .totalHeures(totalHeures)
                .build();
    }

    public TauxReussiteGlobalDto getTauxReussite(Long agenceId, String mois) {
        agenceRepository.findById(agenceId)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return tauxReussiteService.getTauxReussite(agenceId, mois);
    }

    public ResponseDto getByNom(String nom) {
        Agence agence = agenceRepository.findByNom(nom)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(agence);
    }

    @Override
    protected String messageIntrouvable() {
        return "Agence non trouvée";
    }
}