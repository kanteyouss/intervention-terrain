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

/**
 * Service métier gérant les clients.
 * Fournit la création, la modification et les conversions entité/DTO.
 * La lecture et la suppression sont héritées de {@link AbstractCrudService}.
 */
@Service
public class ClientService extends AbstractCrudService {

    private final ClientRepository clientRepository;
    private final InterventionRepository interventionRepository;

    /**
     * Constructeur injectant le repository des clients.
     *
     * @param clientRepository le repository des clients
     * @param interventionRepository le repository des interventions (contrôle de suppression)
     */
    public ClientService(ClientRepository clientRepository, InterventionRepository interventionRepository) {
        super(clientRepository);
        this.clientRepository = clientRepository;
        this.interventionRepository = interventionRepository;
    }

    /**
     * Convertit les données d'une requête en entité client.
     *
     * @param clientRequest Les données de la requête pour créer le client.
     * @return Client L'entité client créée.
     */
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

    /**
     * Convertit une entité client en objet de réponse typé.
     * Seul point de construction de {@link ClientResponseDto}.
     *
     * @param client L'entité client à convertir.
     * @return ClientResponseDto Les détails du client converti.
     */
    public ClientResponseDto toClientResponse(Client client) {
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

    /**
     * Convertit une entité client en objet de réponse.
     *
     * @param entity L'entité client à convertir.
     * @return ResponseDto Les détails du client converti.
     */
    @Override
    public ResponseDto toDto(Object entity) {
        return toClientResponse((Client) entity);
    }

    /**
     * Crée un nouveau client.
     *
     * @param requestDto les informations du client à créer
     * @return le client créé
     */
    @Override
    public ResponseDto create(RequestDto requestDto) {
        ClientRequestDto clientRequest = (ClientRequestDto) requestDto;
        return toDto(clientRepository.save(toEntity(clientRequest)));
    }

    /**
     * Modifie un client existant.
     *
     * @param id L'identifiant du client à modifier.
     * @param requestDto Les données de la requête pour modifier le client.
     * @return ResponseDto Les détails du client modifié.
     * @throws NotFoundException si le client n'existe pas
     */
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

    /**
     * Recherche un client par son nom exact (premier résultat en cas d'homonymie).
     *
     * @param nom Le nom du client recherché.
     * @return ResponseDto Les détails du client trouvé.
     * @throws NotFoundException si aucun client ne porte ce nom
     */
    public ResponseDto getByNom(String nom) {
        Client client = clientRepository.findByNom(nom).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(messageIntrouvable()));
        return toDto(client);
    }
    /**
     * Récupère une page de clients triée par nom.
     *
     * @param page l'index de la page souhaitée
     * @param taille le nombre d'éléments souhaités par page
     * @return une page de ressources converties en DTO
     */
    @Override
    public Page<ResponseDto> getAll(int page, int taille) {
        Sort sort = Sort.by(Sort.Order.asc("nom"));
        Pageable pageable = PageRequest.of(pageAutorisee(page), tailleAutorisee(taille), sort);
        return clientRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * Supprime un client. La suppression est refusée si le client a des interventions.
     *
     * @param id L'identifiant du client à supprimer.
     * @return ResponseDto Les détails du client supprimé.
     * @throws NotFoundException si le client n'existe pas
     * @throws RessourceUtiliseeException si le client a des interventions
     */
    @Override
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

    /**
     * Nomme la ressource dans le message d'erreur d'un identifiant inconnu.
     *
     * @return le message d'introuvable propre à la ressource
     */
    @Override
    protected String messageIntrouvable() {
        return "Client non trouvé";
    }
}
