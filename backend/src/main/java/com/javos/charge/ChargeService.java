package com.javos.charge;

import com.javos.charge.dto.ChargeRequest;
import com.javos.charge.dto.ChargeResponse;
import com.javos.client.Client;
import com.javos.client.ClientRepository;
import com.javos.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargeService {
    private final ChargeRepository chargeRepository;
    private final ClientRepository clientRepository;

    public List<ChargeResponse> findAll() { return chargeRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList()); }
    public ChargeResponse findById(Long id) { return toResponse(getCharge(id)); }

    @Transactional
    public ChargeResponse create(ChargeRequest request) {
        Client client = null;
        if (request.getClientId() != null) client = clientRepository.findById(request.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.getClientId()));
        return toResponse(chargeRepository.save(Charge.builder().client(client).referenceId(request.getReferenceId()).referenceType(request.getReferenceType()).amount(request.getAmount()).dueDate(request.getDueDate()).status(request.getStatus() != null ? request.getStatus() : ChargeStatus.PENDING).method(request.getMethod()).externalId(request.getExternalId()).notes(request.getNotes()).build()));
    }

    @Transactional
    public ChargeResponse update(Long id, ChargeRequest request) {
        Charge charge = getCharge(id);
        if (request.getClientId() != null) charge.setClient(clientRepository.findById(request.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.getClientId())));
        charge.setReferenceId(request.getReferenceId()); charge.setReferenceType(request.getReferenceType()); charge.setAmount(request.getAmount()); charge.setDueDate(request.getDueDate());
        if (request.getStatus() != null) charge.setStatus(request.getStatus());
        charge.setMethod(request.getMethod()); charge.setExternalId(request.getExternalId()); charge.setNotes(request.getNotes());
        return toResponse(chargeRepository.save(charge));
    }

    @Transactional
    public ChargeResponse updateStatus(Long id, ChargeStatus status) { Charge charge = getCharge(id); charge.setStatus(status); return toResponse(chargeRepository.save(charge)); }

    @Transactional
    public void delete(Long id) { chargeRepository.delete(getCharge(id)); }

    private Charge getCharge(Long id) { return chargeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Charge not found: " + id)); }
    private ChargeResponse toResponse(Charge charge) { return ChargeResponse.builder().id(charge.getId()).clientId(charge.getClient() != null ? charge.getClient().getId() : null).clientName(charge.getClient() != null ? charge.getClient().getName() : null).referenceId(charge.getReferenceId()).referenceType(charge.getReferenceType()).amount(charge.getAmount()).dueDate(charge.getDueDate()).status(charge.getStatus()).method(charge.getMethod()).externalId(charge.getExternalId()).notes(charge.getNotes()).createdAt(charge.getCreatedAt()).updatedAt(charge.getUpdatedAt()).build(); }
}
