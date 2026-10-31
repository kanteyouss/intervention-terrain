package com.interventionTerrain.interventionTerrain.controller;

import com.interventionTerrain.interventionTerrain.dto.AgenceRequestDto;
import com.interventionTerrain.interventionTerrain.dto.ChargeAgenceDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TauxReussiteGlobalDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienEffectifResponseDto;
import com.interventionTerrain.interventionTerrain.service.AgenceService;
import com.interventionTerrain.interventionTerrain.utils.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/agences")
@Tag(name = "Agences", description = "Endpoints pour la gestion des agences, effectifs et performances sur le terrain")
public class AgenceController {

    private final AgenceService agenceService;

    public AgenceController(AgenceService agenceService) {
        this.agenceService = agenceService;
    }

    @Operation(summary = "Lister toutes les agences", description = "Récupère la liste paginée de toutes les agences enregistrées.")
    @GetMapping
    public Response<ResponseDto> getAll(
            @Parameter(description = "Numéro de la page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int taille) {
        return new Response<>(agenceService.getAll(page, taille));
    }

    @Operation(summary = "Obtenir une agence par ID", description = "Retourne les détails d'une agence spécifique à partir de son identifiant.")
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(
            @Parameter(description = "ID unique de l'agence", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(agenceService.getById(id));
    }

    @Operation(summary = "Rechercher une agence par nom", description = "Permet de rechercher une agence en fonction de son nom.")
    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(
            @Parameter(description = "Nom ou partie du nom de l'agence", required = true) @RequestParam String nom) throws Throwable {
        return new Response<>(agenceService.getByNom(nom));
    }

    @Operation(summary = "Créer une nouvelle agence", description = "Enregistre une nouvelle agence dans le système.")
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody AgenceRequestDto agenceRequest) {
        return new Response<>(agenceService.create(agenceRequest));
    }

    @Operation(summary = "Mettre à jour une agence", description = "Modifie les informations d'une agence existante.")
    @PutMapping("/{id}")
    public Response<ResponseDto> update(
            @Parameter(description = "ID de l'agence à modifier", required = true) @PathVariable Long id,
            @Valid @RequestBody AgenceRequestDto agenceRequest) {
        return new Response<>(agenceService.update(id, agenceRequest));
    }

    @Operation(summary = "Supprimer une agence", description = "Supprime une agence à partir de son ID.")
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(
            @Parameter(description = "ID de l'agence à supprimer", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(agenceService.delete(id));
    }

    @Operation(summary = "Obtenir l'effectif des techniciens", description = "Récupère la liste et le nombre de techniciens rattachés à une agence.")
    @GetMapping("/{id}/techniciens")
    public Response<TechnicienEffectifResponseDto> getEffectif(
            @Parameter(description = "ID de l'agence", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(agenceService.getEffectif(id));
    }

    @Operation(summary = "Consulter la charge de travail", description = "Calcule la charge de travail d'une agence sur une plage de dates optionnelle.")
    @GetMapping("/{id}/charge")
    public Response<ChargeAgenceDto> getCharge(
            @Parameter(description = "ID de l'agence", required = true) @PathVariable Long id,
            @Parameter(description = "Date de début (Format: YYYY-MM-DD)", example = "2026-10-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @Parameter(description = "Date de fin (Format: YYYY-MM-DD)", example = "2026-10-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) throws Throwable {
        return new Response<>(agenceService.getCharge(id, du, au));
    }

    @Operation(summary = "Obtenir le taux de réussite global", description = "Calcule le taux de réussite des interventions d'une agence pour un mois donné.")
    @GetMapping("/{id}/taux-reussite")
    public Response<TauxReussiteGlobalDto> getTauxReussite(
            @Parameter(description = "ID de l'agence", required = true) @PathVariable Long id,
            @Parameter(description = "Mois concerné (Format: YYYY-MM)", example = "2026-10")
            @RequestParam(required = false) String mois) throws Throwable {
        String moisEffectif = (mois != null && !mois.isBlank()) ? mois : "2026-10";
        return new Response<>(agenceService.getTauxReussite(id, moisEffectif));
    }
}