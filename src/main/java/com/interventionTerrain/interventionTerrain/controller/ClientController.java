package com.interventionTerrain.interventionTerrain.controller;

import com.interventionTerrain.interventionTerrain.dto.ClientRequestDto;
import com.interventionTerrain.interventionTerrain.dto.InterventionResponseDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.service.ClientService;
import com.interventionTerrain.interventionTerrain.service.InterventionService;
import com.interventionTerrain.interventionTerrain.utils.response.Response;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des clients.
 */
@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;
    private final InterventionService interventionService;

    /**
     * Constructeur injectant le service des clients.
     *
     * @param clientService le service des clients
     * @param interventionService le service des interventions (historique du client)
     */
    public ClientController(ClientService clientService, InterventionService interventionService) {
        this.clientService = clientService;
        this.interventionService = interventionService;
    }

    /**
     * Récupère une page de clients.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return Response<ResponseDto> La page des clients encapsulée.
     */
    @GetMapping
    public Response<ResponseDto> getAll(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int taille) {
        return new Response<>(clientService.getAll(page, taille).getContent());
    }

    /**
     * Recherche un client à partir de son nom.
     *
     * @param nom le nom du client recherché
     * @return Response<ResponseDto> Les détails du client trouvé.
     * @throws Throwable si aucun client ne porte ce nom
     */
    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(@RequestParam String nom) throws Throwable {
        return new Response<>(clientService.getByNom(nom));
    }

    /**
     * Récupère le client à partir de son identifiant.
     *
     * @param id l'identifiant du client
     * @return Response<ResponseDto> Le client trouvé encapsulé.
     * @throws Throwable si le client n'est pas trouvé
     */
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(@PathVariable Long id) throws Throwable {
        return new Response<>(clientService.getById(id));
    }

    /**
     * Récupère l'historique des interventions d'un client, de la plus récente
     * à la plus ancienne, avec le technicien, le type et le statut.
     * Un client sans intervention renvoie une liste vide.
     *
     * @param id l'identifiant du client
     * @return Response<InterventionResponseDto> L'historique encapsulé.
     * @throws Throwable si le client n'est pas trouvé
     */
    @GetMapping("/{id}/interventions")
    public Response<InterventionResponseDto> getInterventions(@PathVariable Long id) throws Throwable {
        return new Response<>(interventionService.getInterventionsByClient(id));
    }

    /**
     * Crée un client.
     *
     * @param clientRequest les données de la requête
     * @return Response<ResponseDto> Le client créé encapsulé.
     */
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody ClientRequestDto clientRequest) {
        return new Response<>(clientService.create(clientRequest));
    }

    /**
     * Modifie le client.
     *
     * @param id l'identifiant du client à modifier
     * @param clientRequest les données de la requête
     * @return Response<ResponseDto> Le client modifié encapsulé.
     */
    @PutMapping("/{id}")
    public Response<ResponseDto> update(@PathVariable Long id,
                                        @Valid @RequestBody ClientRequestDto clientRequest) {
        return new Response<>(clientService.update(id, clientRequest));
    }

    /**
     * Supprime le client à partir de son identifiant.
     *
     * @param id l'identifiant du client
     * @return Response<ResponseDto> Le client supprimé encapsulé.
     * @throws Throwable si le client n'est pas trouvé
     */
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(@PathVariable Long id) throws Throwable {
        return new Response<>(clientService.delete(id));
    }
}
