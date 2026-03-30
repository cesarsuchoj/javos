package com.javos.client;

import com.javos.client.dto.ClientRequest;
import com.javos.client.dto.ClientResponse;
import com.javos.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return clientRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        return toResponse(getClient(id));
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        Client client = Client.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .document(request.getDocument())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .active(request.isActive())
                .notes(request.getNotes())
                .build();
        return toResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = getClient(id);
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setDocument(request.getDocument());
        client.setAddress(request.getAddress());
        client.setCity(request.getCity());
        client.setState(request.getState());
        client.setZipCode(request.getZipCode());
        client.setActive(request.isActive());
        client.setNotes(request.getNotes());
        return toResponse(clientRepository.save(client));
    }

    @Transactional
    public void delete(Long id) {
        Client client = getClient(id);
        client.setActive(false);
        clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> searchByName(String name) {
        return clientRepository.findByNameContainingIgnoreCase(name).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> searchByDocument(String document) {
        return clientRepository.findByDocumentContaining(document).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Client getClient(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
    }

    private ClientResponse toResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .document(client.getDocument())
                .address(client.getAddress())
                .city(client.getCity())
                .state(client.getState())
                .zipCode(client.getZipCode())
                .active(client.isActive())
                .notes(client.getNotes())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
