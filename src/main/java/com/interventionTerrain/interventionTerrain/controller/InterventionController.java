package com.interventionTerrain.interventionTerrain.controller;

import com.interventionTerrain.interventionTerrain.dto.InterventionRequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.service.InterventionService;
import com.interventionTerrain.interventionTerrain.utils.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/interventions")
@Tag(name = "Interventions", description = "Endpoints pour la gestion, la planification et le suivi des interventions sur le terrain")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @Operation(summary = "Lister les interventions", description = "Récupère la liste paginée des interventions avec possibilité de filtrer par statut et plage de dates.")
    @GetMapping
    public Response<ResponseDto> getAll(
            @Parameter(description = "Numéro de la page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int taille,
            @Parameter(description = "Statut de l'intervention (ex: PLANIFIEE, EN_COURS, TERMINEE, ECHEC)") @RequestParam(required = false) String statut,
            @Parameter(description = "Date de début de la plage (Format: YYYY-MM-DD)", example = "2026-10-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @Parameter(description = "Date de fin de la plage (Format: YYYY-MM-DD)", example = "2026-10-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
            return new Response<>(interventionService.getInterventions(statut, du, au, page, taille));

    }

    @Operation(summary = "Obtenir une intervention par ID", description = "Retourne les détails complets d'une intervention spécifique.")
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(
            @Parameter(description = "ID unique de l'intervention", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(interventionService.getById(id));
    }

    @Operation(summary = "Créer une nouvelle intervention", description = "Enregistre et planifie une nouvelle intervention sur le terrain.")
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody InterventionRequestDto interventionRequest) {
        return new Response<>(interventionService.create(interventionRequest));
    }

    @Operation(summary = "Mettre à jour une intervention", description = "Modifie les informations d'une intervention existante.")
    @PutMapping("/{id}")
    public Response<ResponseDto> update(
            @Parameter(description = "ID de l'intervention à modifier", required = true) @PathVariable Long id,
            @Valid @RequestBody InterventionRequestDto interventionRequest) {
        return new Response<>(interventionService.update(id, interventionRequest));
    }

    @Operation(summary = "Supprimer une intervention", description = "Supprime une intervention du système.")
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(
            @Parameter(description = "ID de l'intervention à supprimer", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(interventionService.delete(id));
    }

    @Operation(summary = "Mettre à jour le statut d'une intervention", description = "Met à jour l'état d'avancement d'une intervention (ex: TERMINEE, ECHEC, EN_COURS).")
    @PostMapping("/{id}/statut")
    public Response<ResponseDto> mettreAJourStatut(
            @Parameter(description = "ID de l'intervention", required = true) @PathVariable Long id,
            @Parameter(description = "Nouveau statut à appliquer", required = true) @RequestParam String nouveauStatut) {
        return new Response<>(interventionService.toDto(interventionService.mettreAJourStatut(id, nouveauStatut)));
    }

    @Operation(summary = "Créer une intervention de reprise", description = "Planifie une nouvelle intervention suite à un échec ou un report d'intervention précédente.")
    @PostMapping("/{id}/reprise")
    public Response<ResponseDto> creerReprise(
            @Parameter(description = "ID de l'intervention d'origine", required = true) @PathVariable Long id,
            @Parameter(description = "Nouvelle date planifiée (yyyy-MM-dd)", example = "2026-11-05", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datePlanifiee) {
        return new Response<>(interventionService.toDto(interventionService.creerInterventionDeReprise(id, datePlanifiee)));
    }
}
