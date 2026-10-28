package com.interventionTerrain.interventionTerrain.controller;

import com.interventionTerrain.interventionTerrain.dto.InterventionRequestDto;
import com.interventionTerrain.interventionTerrain.dto.InterventionResponseDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.service.InterventionService;
import com.interventionTerrain.interventionTerrain.utils.response.Response;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/interventions")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @GetMapping
    public Response<ResponseDto> getAll(@RequestParam(required = false) String statut,
                                       @RequestParam(required = false) LocalDate du,
                                       @RequestParam(required = false) LocalDate au) {
        return new Response<>(interventionService.getAll(0, 100).getContent());
    }

    @GetMapping("/{id}")
    public Response<ResponseDto> getById(@PathVariable Long id) throws Throwable {
        return new Response<>(interventionService.getById(id));
    }

    @PostMapping
    public Response<ResponseDto> create(@Valid @RequestBody InterventionRequestDto interventionRequest) {
        return new Response<>(interventionService.create(interventionRequest));
    }

    @PutMapping("/{id}")
    public Response<ResponseDto> update(@PathVariable Long id,
                                      @Valid @RequestBody InterventionRequestDto interventionRequest) {
        return new Response<>(interventionService.update(id, interventionRequest));
    }

    @DeleteMapping("/{id}")
    public Response<ResponseDto> delete(@PathVariable Long id) throws Throwable {
        return new Response<>(interventionService.delete(id));
    }

    @PostMapping("/{id}/statut")
    public Response<ResponseDto> mettreAJourStatut(@PathVariable Long id,
                                                  @RequestParam String nouveauStatut) {
        return new Response<>(interventionService.toDto(interventionService.mettreAJourStatut(id, nouveauStatut)));
    }

    @PostMapping("/{id}/reprise")
    public Response<ResponseDto> creerReprise(@PathVariable Long id,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime datePlanifiee) {
        return new Response<>(interventionService.toDto(interventionService.creerInterventionDeReprise(id, datePlanifiee)));
    }
}