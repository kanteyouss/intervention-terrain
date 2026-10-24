package com.interventionTerrain.interventionTerrain.controller;

import com.interventionTerrain.interventionTerrain.dto.CompetenceRequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TypeInterventionResponseDto;
import com.interventionTerrain.interventionTerrain.service.CompetenceService;
import com.interventionTerrain.interventionTerrain.utils.response.Response;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des competences.
 */
@RestController
@RequestMapping("/competences")
public class CompetenceController {

    private final CompetenceService competenceService;

    /**
     * Constructeur injectant le service des competences.
     *
     * @param competenceService le service des competences
     */
    public CompetenceController(CompetenceService competenceService) {
        this.competenceService = competenceService;
    }

    /**
     * Récupère une page de compétences.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return Response<ResponseDto> La page des compétences encapsulée.
     */
    @GetMapping
    public Response<ResponseDto> getAll(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int taille) {
        return new Response<>(competenceService.getAll(page, taille).getContent());
    }

    /**
     * Récupère la compétence à partir de son identifiant.
     *
     * @param id l'identifiant de la compétence
     * @return Response<ResponseDto> La compétence trouvée encapsulée.
     * @throws Throwable si la compétence n'est pas trouvée
     */
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(@PathVariable Long id) throws Throwable {
        return new Response<>(competenceService.getById(id));
    }

    /**
     * Recherche une compétence à partir de son nom.
     *
     * @param nom le nom de la compétence recherchée
     * @return Response<ResponseDto> Les détails de la compétence trouvée.
     * @throws Throwable si aucune compétence ne porte ce nom
     */
    /**
     * Liste les types d'intervention dans lesquels la compétence est mobilisée
     * (jointures sur les tables existantes, sans table de liaison).
     *
     * @param id l'identifiant de la compétence
     * @return Response<TypeInterventionResponseDto> Les types d'intervention concernés.
     * @throws Throwable si la compétence n'existe pas
     */
    @GetMapping("/{id}/types-intervention")
    public Response<TypeInterventionResponseDto> getTypesIntervention(@PathVariable Long id) throws Throwable {
        return new Response<>(competenceService.getTypesIntervention(id));
    }

    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(@RequestParam String nom) throws Throwable {
        return new Response<>(competenceService.getByNom(nom));
    }

    /**
     * Crée une compétence.
     *
     * @param competenceRequest les données de la requête
     * @return Response<ResponseDto> La compétence créée encapsulée.
     */
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody CompetenceRequestDto competenceRequest) {
        return new Response<>(competenceService.create(competenceRequest));
    }

    /**
     * Modifie la compétence.
     *
     * @param id l'identifiant de la compétence à modifier
     * @param competenceRequest les données de la requête
     * @return Response<ResponseDto> La compétence modifiée encapsulée.
     */
    @PutMapping("/{id}")
    public Response<ResponseDto> update(@PathVariable Long id,
                                        @Valid @RequestBody CompetenceRequestDto competenceRequest) {
        return new Response<>(competenceService.update(id, competenceRequest));
    }

    /**
     * Supprime la compétence à partir de son identifiant.
     *
     * @param id l'identifiant de la compétence
     * @return Response<ResponseDto> La compétence supprimée encapsulée.
     * @throws Throwable si la compétence n'est pas trouvée
     */
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(@PathVariable Long id) throws Throwable {
        return new Response<>(competenceService.delete(id));
    }
}
