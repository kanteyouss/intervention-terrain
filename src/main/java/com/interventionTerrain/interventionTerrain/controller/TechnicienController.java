package com.interventionTerrain.interventionTerrain.controller;

import com.interventionTerrain.interventionTerrain.dto.CompetenceRequestDto;
import com.interventionTerrain.interventionTerrain.dto.DisponiblesResponseDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienCompetenceResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienRequestDto;
import com.interventionTerrain.interventionTerrain.service.InterventionService;
import com.interventionTerrain.interventionTerrain.service.TechnicienService;
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
@RequestMapping("/techniciens")
@Tag(name = "Techniciens",
        description = "Endpoints pour la gestion des techniciens, de leurs compétences, plannings et disponibilités")
public class TechnicienController {

    private final TechnicienService technicienService;
    private final InterventionService interventionService;

    public TechnicienController(TechnicienService technicienService,
                                InterventionService interventionService) {
        this.technicienService = technicienService;
        this.interventionService = interventionService;
    }

    @Operation(summary = "Lister tous les techniciens",
            description = "Récupère la liste paginée de tous les techniciens enregistrés.")
    @GetMapping
    public Response<ResponseDto> getAll(
            @Parameter(description = "Numéro de la page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int taille) {
        return new Response<>(technicienService.getAll(page, taille));
    }

    @Operation(summary = "Obtenir un technicien par ID",
            description = "Retourne les détails d'un technicien spécifique à partir de son identifiant.")
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(
            @Parameter(description = "ID unique du technicien",
                    required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(technicienService.getById(id));
    }

    @Operation(summary = "Rechercher un technicien par nom",
            description = "Permet de rechercher un technicien en fonction de son nom ou prénom.")
    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(
            @Parameter(description = "Nom ou partie du nom du technicien",
                    required = true) @RequestParam String nom) throws Throwable {
        return new Response<>(technicienService.getByNom(nom));
    }

    @Operation(summary = "Créer un nouveau technicien",
            description = "Enregistre un nouveau technicien dans le système.")
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody TechnicienRequestDto technicienRequest) {
        return new Response<>(technicienService.create(technicienRequest));
    }

    @Operation(summary = "Mettre à jour un technicien",
            description = "Modifie les informations d'un technicien existant.")
    @PutMapping("/{id}")
    public Response<ResponseDto> update(
            @Parameter(description = "ID du technicien à modifier", required = true) @PathVariable Long id,
            @Valid @RequestBody TechnicienRequestDto technicienRequest) {
        return new Response<>(technicienService.update(id, technicienRequest));
    }

    @Operation(summary = "Supprimer un technicien", description = "Supprime un technicien à partir de son ID.")
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(
            @Parameter(description = "ID du technicien à supprimer",
                    required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(technicienService.delete(id));
    }

    @Operation(summary = "Obtenir les compétences d'un technicien", description = "Récupère la liste des compétences" +
            " associées à un technicien spécifique.")
    @GetMapping("/{id}/competences")
    public Response<TechnicienCompetenceResponseDto> getCompetences(
            @Parameter(description = "ID du technicien", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(technicienService.getCompetencesByTechnicien(id));
    }

    @Operation(summary = "Associer une compétence à un technicien", description = "Ajoute une nouvelle compétence" +
            " au profil d'un technicien.")
    @PostMapping("/{id}/competences")
    public Response<TechnicienCompetenceResponseDto> associerCompetence(
            @Parameter(description = "ID du technicien", required = true) @PathVariable Long id,
            @Valid @RequestBody CompetenceRequestDto competenceRequest) {
        return new Response<>(technicienService.associerCompetence(id, competenceRequest));
    }

    @Operation(summary = "Consulter le planning d'un technicien", description = "Récupère les interventions " +
            "planifiées d'un technicien sur une plage de dates optionnelle.")
    @GetMapping("/{id}/planning")
    public Response<ResponseDto> getPlanning(
            @Parameter(description = "ID du technicien", required = true) @PathVariable Long id,
            @Parameter(description = "Date de début (Format: YYYY-MM-DD)", example = "2026-10-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @Parameter(description = "Date de fin (Format: YYYY-MM-DD)", example = "2026-10-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) throws Throwable {
        return new Response<>(interventionService.getPlanning(id, du, au));
    }

    @Operation(summary = "Rechercher les techniciens disponibles",
            description = "Recherche les techniciens disponibles " +
                    "sur une plage horaire donnée selon le type d'intervention.")
    @GetMapping("/disponibles")
    public Response<DisponiblesResponseDto> getDisponibles(
            @Parameter(description = "Date et heure de début (dd-MM-yyyy HH:mm:ss)",
                    example = "28-10-2026 08:00:00", required = true)
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime debut,
            @Parameter(description = "Date et heure de fin (dd-MM-yyyy HH:mm:ss)",
                    example = "28-10-2026 12:00:00", required = true)
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime fin,
            @Parameter(description = "Type d'intervention ou domaine d'expertise requis", required = true)
            @RequestParam String type) throws Throwable {
        return new Response<>(technicienService.getDisponibles(debut, fin, type));
    }
}