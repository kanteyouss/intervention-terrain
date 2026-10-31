package com.interventionTerrain.interventionTerrain.service;

import com.interventionTerrain.interventionTerrain.dto.ClientRequestDto;
import com.interventionTerrain.interventionTerrain.dto.ClientResponseDto;
import com.interventionTerrain.interventionTerrain.dto.RequestDto;
import com.interventionTerrain.interventionTerrain.dto.ResponseDto;
import com.interventionTerrain.interventionTerrain.exception.NotFoundException;
import com.interventionTerrain.interventionTerrain.exception.RessourceUtiliseeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.interventionTerrain.interventionTerrain.entity.Client;
import com.interventionTerrain.interventionTerrain.repository.ClientRepository;
import com.interventionTerrain.interventionTerrain.repository.InterventionRepository;

@Service
public class ClientService extends AbstractCrudService {

    private final ClientRepository clientRepository;
    private final InterventionRepository interventionRepository;

    public ClientService(ClientRepository clientRepository, InterventionRepository interventionRepository) {
        super(clientRepository);
        this.clientRepository = clientRepository;
        this.interventionRepository = interventionRepository;
    }

    public Client toEntity(ClientRequestDto clientRequest) {
        return Client.builder()
                .nom(clientRequest.getNom())
                .prenom(clientRequest.getPrenom())
                .email(clientRequest.getEmail())
                .telephone(clientRequest.getTelephone())
                .adresse(clientRequest.getAdresse())
                .commune(clientRequest.getCommune())
                .build();
    }

    @Override
    public ResponseDto toDto(Object entity) {
        Client client = (Client) entity;
        return ClientResponseDto.builder()
                .id(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .email(client.getEmail())
                .telephone(client.getTelephone())
                .adresse(client.getAdresse())
                .commune(client.getCommune())
                .build();
    }

    public Page<ResponseDto> getAll(int page, int taille) {
        Sort sort = Sort.by(Sort.Order.asc("nom"));
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), sort);
        return clientRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public ResponseDto create(RequestDto requestDto) {
        ClientRequestDto clientRequest = (ClientRequestDto) requestDto;
        return toDto(clientRepository.save(toEntity(clientRequest)));
    }

    @Override
    public ResponseDto update(Long id, RequestDto requestDto) {
        ClientRequestDto clientRequest = (ClientRequestDto) requestDto;
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        client.setNom(clientRequest.getNom());
        client.setPrenom(clientRequest.getPrenom());
        client.setEmail(clientRequest.getEmail());
        client.setTelephone(clientRequest.getTelephone());
        client.setAdresse(clientRequest.getAdresse());
        client.setCommune(clientRequest.getCommune());
        return toDto(clientRepository.save(client));
    }

    public ResponseDto delete(Long id) throws Throwable {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        if (interventionRepository.existsByClientId(id)) {
            throw new RessourceUtiliseeException("Client", id, "Intervention");
        }
        ResponseDto response = toDto(client);
        clientRepository.deleteById(id);
        return response;
    }

    public ResponseDto getByNom(String nom) {
        Client client = clientRepository.findByNom(nom)
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(client);
    }

    protected String messageIntrouvable() {
        return "Client non trouvé";
    }
}