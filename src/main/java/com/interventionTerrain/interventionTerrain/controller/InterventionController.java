package com.interventionTerrain.interventionTerrain.controller;

import com.interventionTerrain.interventionTerrain.dto.InterventionRequestDto;
import com.interventionTerrain.interventionTerrain.dto.InterventionResponseDto;
import com.interventionTerrain.interventionTerrain.dto.InterventionStatsResponseDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.service.InterventionService;
import com.interventionTerrain.interventionTerrain.service.InterventionStatsService;
import com.interventionTerrain.interventionTerrain.utils.response.Response;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Contrôleur REST pour la gestion des interventions.
 * Outre le CRUD standard, il expose les interventions filtrées
 * par client, par technicien et par statut.
 */
@RestController
@RequestMapping("/interventions")
public class InterventionController {

    private final InterventionService interventionService;
    private final InterventionStatsService interventionStatsService;

    /**
     * Constructeur injectant le service des interventions et le service de statistiques.
     *
     * @param interventionService le service des interventions
     * @param interventionStatsService le service des statistiques d'intervention
     */
    public InterventionController(InterventionService interventionService,
                                  InterventionStatsService interventionStatsService) {
        this.interventionService = interventionService;
        this.interventionStatsService = interventionStatsService;
    }

    /**
     * Récupère une page d'interventions.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return Response<ResponseDto> La page des interventions encapsulée.
     */
    @GetMapping
    public Response<ResponseDto> getAll(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int taille,
                                        @RequestParam(required = false) String statut,
                                        @RequestParam(required = false) LocalDate du,
                                        @RequestParam(required = false) LocalDate au) {
        return new Response<>(interventionService.lister(page, taille, statut, du, au).getContent());
    }

    /**
     * Récupère une intervention à partir de son identifiant.
     *
     * @param id l'identifiant de l'intervention
     * @return Response<ResponseDto> L'intervention trouvée encapsulée.
     * @throws Throwable si l'intervention n'est pas trouvée
     */
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(@PathVariable Long id) throws Throwable {
        return new Response<>(interventionService.getById(id));
    }

    /**
     * Crée une nouvelle intervention.
     *
     * @param interventionRequest les données de la requête
     * @return Response<ResponseDto> L'intervention créée encapsulée.
     */
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody InterventionRequestDto interventionRequest) {
        return new Response<>(interventionService.create(interventionRequest));
    }

    /**
     * Modifie une intervention existante.
     *
     * @param id l'identifiant de l'intervention à modifier
     * @param interventionRequest les données de la requête
     * @return Response<ResponseDto> L'intervention modifiée encapsulée.
     */
    @PutMapping("/{id}")
    public Response<ResponseDto> update(@PathVariable Long id,
                                        @Valid @RequestBody InterventionRequestDto interventionRequest) {
        return new Response<>(interventionService.update(id, interventionRequest));
    }

    /**
     * Supprime une intervention à partir de son identifiant.
     *
     * @param id l'identifiant de l'intervention
     * @return Response<ResponseDto> L'intervention supprimée encapsulée.
     * @throws Throwable si l'intervention n'est pas trouvée
     */
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(@PathVariable Long id) throws Throwable {
        return new Response<>(interventionService.delete(id));
    }

    /**
     * Récupère les statistiques agrégées d'une intervention
     * (nombre de techniciens affectés et nombre de créneaux planifiés).
     *
     * @param id L'identifiant de l'intervention.
     * @return Response<InterventionStatsResponseDto> Les statistiques de l'intervention encapsulées.
     * @throws Throwable si l'intervention n'est pas trouvée
     */
    @GetMapping("/{id}/stats")
    public Response<InterventionStatsResponseDto> getStats(@PathVariable long id) throws Throwable {
        return new Response<>(interventionStatsService.getStats(id));
    }
}
