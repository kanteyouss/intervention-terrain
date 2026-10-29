package com.interventionTerrain.interventionTerrain.controller;

import com.interventionTerrain.interventionTerrain.dto.ClientRequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.service.ClientService;
import com.interventionTerrain.interventionTerrain.service.InterventionService;
import com.interventionTerrain.interventionTerrain.utils.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
@Tag(name = "Clients", description = "Endpoints pour la gestion des clients et la consultation de leur historique d'interventions")
public class ClientController {

    private final ClientService clientService;
    private final InterventionService interventionService;

    public ClientController(ClientService clientService, InterventionService interventionService) {
        this.clientService = clientService;
        this.interventionService = interventionService;
    }

    @Operation(summary = "Lister tous les clients", description = "Récupère la liste paginée de tous les clients enregistrés.")
    @GetMapping
    public Response<ResponseDto> getAll(
            @Parameter(description = "Numéro de la page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int taille) {
        return new Response<>(clientService.getAll(page, taille));
    }

    @Operation(summary = "Obtenir un client par ID", description = "Retourne les détails d'un client spécifique à partir de son identifiant.")
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(
            @Parameter(description = "ID unique du client", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(clientService.getById(id));
    }

    @Operation(summary = "Rechercher un client par nom", description = "Permet de rechercher un client en fonction de son nom ou raison sociale.")
    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(
            @Parameter(description = "Nom ou partie du nom du client", required = true) @RequestParam String nom) throws Throwable {
        return new Response<>(clientService.getByNom(nom));
    }

    @Operation(summary = "Créer un nouveau client", description = "Enregistre un nouveau client dans le système.")
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody ClientRequestDto clientRequest) {
        return new Response<>(clientService.create(clientRequest));
    }

    @Operation(summary = "Mettre à jour un client", description = "Modifie les informations d'un client existant.")
    @PutMapping("/{id}")
    public Response<ResponseDto> update(
            @Parameter(description = "ID du client à modifier", required = true) @PathVariable Long id,
            @Valid @RequestBody ClientRequestDto clientRequest) {
        return new Response<>(clientService.update(id, clientRequest));
    }

    @Operation(summary = "Supprimer un client", description = "Supprime un client à partir de son ID.")
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(
            @Parameter(description = "ID du client à supprimer", required = true) @PathVariable Long id) throws Throwable {
        return new Response<>(clientService.delete(id));
    }

    @Operation(summary = "Obtenir l'historique des interventions", description = "Récupère la liste paginée de toutes les interventions réalisées pour un client spécifique.")
    @GetMapping("/{id}/interventions")
    public Response<ResponseDto> getInterventions(
            @Parameter(description = "ID du client concerné", required = true) @PathVariable Long id,
            @Parameter(description = "Numéro de la page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int taille) throws Throwable {
        return new Response<>(interventionService.getHistoriqueByClient(id, page, taille));
    }
}