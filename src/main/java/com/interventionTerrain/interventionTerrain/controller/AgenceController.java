package com.interventionTerrain.interventionTerrain.controller;

import com.interventionTerrain.interventionTerrain.dto.AgenceRequestDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienEffectifResponseDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.service.AgenceService;
import com.interventionTerrain.interventionTerrain.utils.response.Response;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des agences.
 */
@RestController
@RequestMapping("/agences")
public class AgenceController {

    private final AgenceService agenceService;

    /**
     * Constructeur injectant le service des agences.
     *
     * @param agenceService le service des agences
     */
    public AgenceController(AgenceService agenceService) {
        this.agenceService = agenceService;
    }

    /**
     * Récupère une page d'agences.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return Response<ResponseDto> La page des agences encapsulée.
     */
    @GetMapping
    public Response<ResponseDto> getAll(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int taille) {
        return new Response<>(agenceService.getAll(page, taille).getContent());
    }

    /**
     * Récupère l'effectif d'une agence : ses techniciens triés par nom, avec le
     * nombre de compétences valides de chacun. Une agence sans technicien
     * renvoie une liste vide.
     *
     * @param id l'identifiant de l'agence
     * @return Response<TechnicienEffectifResponseDto> L'effectif encapsulé.
     * @throws Throwable si l'agence n'est pas trouvée
     */
    @GetMapping("/{id}/techniciens")
    public Response<TechnicienEffectifResponseDto> getEffectif(@PathVariable Long id) throws Throwable {
        return new Response<>(agenceService.getEffectif(id));
    }

    /**
     * Récupère l'agence à partir de son identifiant.
     *
     * @param id l'identifiant de l'agence
     * @return Response<ResponseDto> L'agence trouvée encapsulée.
     * @throws Throwable si l'agence n'est pas trouvée
     */
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(@PathVariable Long id) throws Throwable {
        return new Response<>(agenceService.getById(id));
    }

    /**
     * Recherche une agence à partir de son nom.
     *
     * @param nom le nom de l'agence recherchée
     * @return Response<ResponseDto> Les détails de l'agence trouvée.
     * @throws Throwable si aucune agence ne porte ce nom
     */
    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(@RequestParam String nom) throws Throwable {
        return new Response<>(agenceService.getByNom(nom));
    }

    /**
     * Crée une agence.
     *
     * @param agenceRequest les données de la requête
     * @return Response<ResponseDto> L'agence créée encapsulée.
     */
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody AgenceRequestDto agenceRequest) {
        return new Response<>(agenceService.create(agenceRequest));
    }

    /**
     * Modifie l'agence.
     *
     * @param id l'identifiant de l'agence à modifier
     * @param agenceRequest les données de la requête
     * @return Response<ResponseDto> L'agence modifiée encapsulée.
     */
    @PutMapping("/{id}")
    public Response<ResponseDto> update(@PathVariable Long id,
                                        @Valid @RequestBody AgenceRequestDto agenceRequest) {
        return new Response<>(agenceService.update(id, agenceRequest));
    }

    /**
     * Supprime l'agence à partir de son identifiant.
     *
     * @param id l'identifiant de l'agence
     * @return Response<ResponseDto> L'agence supprimée encapsulée.
     * @throws Throwable si l'agence n'est pas trouvée
     */
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(@PathVariable Long id) throws Throwable {
        return new Response<>(agenceService.delete(id));
    }
}
