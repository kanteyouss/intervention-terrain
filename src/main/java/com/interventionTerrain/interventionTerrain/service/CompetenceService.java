package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.CompetenceRequestDto;
import com.interventionTerrain.interventionTerrain.dto.CompetenceResponseDto;
import com.interventionTerrain.interventionTerrain.dto.RequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.exception.DoublonRessourceException;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.exception.RessourceUtiliseeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.interventionTerrain.interventionTerrain.entity.Competence;
import com.interventionTerrain.interventionTerrain.repository.CompetenceRepository;

@Service
public class CompetenceService extends AbstractCrudService {

    private final CompetenceRepository competenceRepository;

    public CompetenceService(CompetenceRepository competenceRepository) {
        super(competenceRepository);
        this.competenceRepository = competenceRepository;
    }

    public Competence toEntity(CompetenceRequestDto competenceRequest) {
        return Competence.builder()
                .libelle(competenceRequest.getLibelle())
                .description(competenceRequest.getDescription())
                .build();
    }

    @Override
    public ResponseDto toDto(Object entity) {
        Competence competence = (Competence) entity;
        return CompetenceResponseDto.builder()
                .id(competence.getId())
                .libelle(competence.getLibelle())
                .description(competence.getDescription())
                .build();
    }

    public Page<ResponseDto> getAll(int page, int taille) {
        Sort sort = Sort.by(Sort.Order.asc("libelle"));
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), sort);
        return competenceRepository.findAll(pageable).map(this::toDto);
    }

    public ResponseDto create(RequestDto requestDto) {
        CompetenceRequestDto competenceRequest = (CompetenceRequestDto) requestDto;
        competenceRequest.setLibelle(competenceRequest.getLibelle());
        competenceRequest.setDescription(competenceRequest.getDescription());
        if (competenceRepository.existsByLibelleIgnoreCase(competenceRequest.getLibelle())) {
            throw new DoublonRessourceException("Une compétence nommée '"
                    + competenceRequest.getLibelle() + "' existe déjà");
        }

        return toDto(competenceRepository.save(toEntity(competenceRequest)));
    }

    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        CompetenceRequestDto competenceRequest = (CompetenceRequestDto) requestDto;
        competenceRequest.setLibelle(competenceRequest.getLibelle());
        competenceRequest.setDescription(competenceRequest.getDescription());
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (competenceRepository.existsByLibelleIgnoreCaseAndIdNot(competenceRequest.getLibelle(), id)) {
            throw new DoublonRessourceException("Une compétence nommée '"
                    + competenceRequest.getLibelle() + "' existe déjà");
        }
        competence.setLibelle(
                competenceRequest.getLibelle());
        competence.setDescription(competenceRequest.getDescription());
        return toDto(competenceRepository.save(competence));
    }

    public ResponseDto delete(Long id) throws Throwable {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (competenceRepository.existsTechnicienByCompetenceId(id)) {
            throw new RessourceUtiliseeException("Competence", id, "Technicien");
        }
        ResponseDto response = toDto(competence);
        competenceRepository.deleteById(id);
        return response;
    }

    public ResponseDto getByNom(String nom) {
        Competence competence = competenceRepository.findByLibelle(nom)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(competence);
    }

    protected String messageIntrouvable() {
        return "Compétence non trouvée";
    }
}