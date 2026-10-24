package com.interventionTerrain.interventionTerrain.controller;

import com.interventionTerrain.interventionTerrain.dto.TechnicienCompetenceResponseDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienRequestDto;
import com.interventionTerrain.interventionTerrain.dto.InterventionResponseDto;
import com.interventionTerrain.interventionTerrain.dto.TechnicienResponseDto;
import com.interventionTerrain.interventionTerrain.service.InterventionService;
import com.interventionTerrain.interventionTerrain.service.TechnicienService;
import com.interventionTerrain.interventionTerrain.utils.response.Response;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Contrôleur REST pour la gestion des techniciens.
 * Outre le CRUD standard, il expose les techniciens d'une agence
 * et les compétences d'un technicien.
 */
@RestController
@RequestMapping("/techniciens")
public class TechnicienController {

    private final TechnicienService technicienService;
    private final InterventionService interventionService;

    /**
     * Constructeur injectant le service des techniciens.
     *
     * @param technicienService le service des techniciens
     * @param interventionService le service des interventions (planning du technicien)
     */
    public TechnicienController(TechnicienService technicienService,
                                InterventionService interventionService) {
        this.technicienService = technicienService;
        this.interventionService = interventionService;
    }

    /**
     * Récupère une page de techniciens.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return Response<ResponseDto> La page des techniciens encapsulée.
     */
    @GetMapping
    public Response<ResponseDto> getAll(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int taille) {
        return new Response<>(technicienService.getAll(page, taille).getContent());
    }

    /**
     * Récupère un technicien à partir de son identifiant.
     *
     * @param id l'identifiant du technicien
     * @return Response<ResponseDto> Le technicien trouvé encapsulé.
     * @throws Throwable si le technicien n'est pas trouvé
     */
    @GetMapping("/{id}")
    public Response<ResponseDto> getById(@PathVariable Long id) throws Throwable {
        return new Response<>(technicienService.getById(id));
    }

    /**
     * Recherche un technicien à partir de son nom.
     *
     * @param nom le nom du technicien recherché
     * @return Response<ResponseDto> Les détails du technicien trouvé.
     * @throws Throwable si aucun technicien ne porte ce nom
     */
    @GetMapping("/recherche")
    public Response<ResponseDto> getByNom(@RequestParam String nom) throws Throwable {
        return new Response<>(technicienService.getByNom(nom));
    }

    /**
     * Crée un nouveau technicien.
     *
     * @param technicienRequest les données de la requête
     * @return Response<ResponseDto> Le technicien créé encapsulé.
     */
    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody TechnicienRequestDto technicienRequest) {
        return new Response<>(technicienService.create(technicienRequest));
    }

    /**
     * Modifie un technicien existant.
     *
     * @param id l'identifiant du technicien à modifier
     * @param technicienRequest les données de la requête
     * @return Response<ResponseDto> Le technicien modifié encapsulé.
     */
    @PutMapping("/{id}")
    public Response<ResponseDto> update(@PathVariable Long id,
                                        @Valid @RequestBody TechnicienRequestDto technicienRequest) {
        return new Response<>(technicienService.update(id, technicienRequest));
    }

    /**
     * Supprime un technicien à partir de son identifiant.
     *
     * @param id l'identifiant du technicien
     * @return Response<ResponseDto> Le technicien supprimé encapsulé.
     * @throws Throwable si le technicien n'est pas trouvé
     */
    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(@PathVariable Long id) throws Throwable {
        return new Response<>(technicienService.delete(id));
    }

    /**
     * Récupère les compétences d'un technicien.
     *
     * @param id L'identifiant du technicien.
     * @return Response<TechnicienCompetenceResponseDto> Les compétences du
     *         technicien encapsulées, avec les dates et le drapeau d'expiration.
     */
    @GetMapping("/{id}/competences")
    public Response<TechnicienCompetenceResponseDto> getCompetencesByTechnicien(@PathVariable Long id) {
        return new Response<>(technicienService.getCompetencesByTechnicien(id));
    }

    /**
     * Récupère le planning d'un technicien : ses interventions sur la période
     * demandée, triées par date puis par heure de début du créneau, avec le
     * client, l'adresse et le créneau. Sans paramètre, la semaine en cours est
     * utilisée ; une période supérieure à trois mois renvoie une erreur 400.
     *
     * @param id l'identifiant du technicien
     * @param du début de période au format date (optionnel)
     * @param au fin de période au format date (optionnel)
     * @return Response<InterventionResponseDto> Le planning encapsulé.
     * @throws Throwable si le technicien n'est pas trouvé ou la période est refusée
     */
    @GetMapping("/{id}/planning")
    public Response<InterventionResponseDto> getPlanning(@PathVariable Long id,
                                                         @RequestParam(required = false) LocalDate du,
                                                         @RequestParam(required = false) LocalDate au) throws Throwable {
        return new Response<>(interventionService.planner(id, du, au));
    }
}
