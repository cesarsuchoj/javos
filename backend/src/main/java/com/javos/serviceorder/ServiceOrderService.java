package com.javos.serviceorder;

import com.javos.client.Client;
import com.javos.client.ClientRepository;
import com.javos.exception.ResourceNotFoundException;
import com.javos.model.User;
import com.javos.repository.UserRepository;
import com.javos.serviceorder.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final OsNoteRepository osNoteRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> findAll() {
        return serviceOrderRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceOrderResponse findById(Long id) {
        return toResponse(getServiceOrder(id));
    }

    @Transactional
    public ServiceOrderResponse create(ServiceOrderRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.getClientId()));
        User technician = null;
        if (request.getTechnicianId() != null) {
            technician = userRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getTechnicianId()));
        }
        ServiceOrder order = ServiceOrder.builder()
                .orderNumber(generateOrderNumber())
                .client(client)
                .technician(technician)
                .status(request.getStatus() != null ? request.getStatus() : ServiceOrderStatus.OPEN)
                .priority(request.getPriority() != null ? request.getPriority() : ServiceOrderPriority.NORMAL)
                .description(request.getDescription())
                .diagnosis(request.getDiagnosis())
                .solution(request.getSolution())
                .laborCost(request.getLaborCost() != null ? request.getLaborCost() : java.math.BigDecimal.ZERO)
                .estimatedCompletion(request.getEstimatedCompletion())
                .build();
        return toResponse(serviceOrderRepository.save(order));
    }

    @Transactional
    public ServiceOrderResponse update(Long id, ServiceOrderRequest request) {
        ServiceOrder order = getServiceOrder(id);
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.getClientId()));
        order.setClient(client);
        if (request.getTechnicianId() != null) {
            User technician = userRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getTechnicianId()));
            order.setTechnician(technician);
        }
        if (request.getStatus() != null) order.setStatus(request.getStatus());
        if (request.getPriority() != null) order.setPriority(request.getPriority());
        order.setDescription(request.getDescription());
        order.setDiagnosis(request.getDiagnosis());
        order.setSolution(request.getSolution());
        if (request.getLaborCost() != null) order.setLaborCost(request.getLaborCost());
        order.setEstimatedCompletion(request.getEstimatedCompletion());
        return toResponse(serviceOrderRepository.save(order));
    }

    @Transactional
    public ServiceOrderResponse changeStatus(Long id, ServiceOrderStatus status) {
        ServiceOrder order = getServiceOrder(id);
        order.setStatus(status);
        if (status == ServiceOrderStatus.DONE || status == ServiceOrderStatus.CLOSED) {
            order.setCompletedAt(LocalDateTime.now());
        }
        return toResponse(serviceOrderRepository.save(order));
    }

    @Transactional
    public void delete(Long id) {
        ServiceOrder order = getServiceOrder(id);
        order.setStatus(ServiceOrderStatus.CANCELLED);
        serviceOrderRepository.save(order);
    }

    @Transactional
    public OsNoteResponse addNote(Long serviceOrderId, OsNoteRequest request) {
        ServiceOrder order = getServiceOrder(serviceOrderId);
        User author = null;
        if (request.getAuthorId() != null) {
            author = userRepository.findById(request.getAuthorId()).orElse(null);
        }
        OsNote note = OsNote.builder()
                .serviceOrder(order)
                .author(author)
                .content(request.getContent())
                .build();
        return toNoteResponse(osNoteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public List<OsNoteResponse> getNotes(Long serviceOrderId) {
        return osNoteRepository.findByServiceOrderIdOrderByCreatedAtAsc(serviceOrderId)
                .stream().map(this::toNoteResponse).collect(Collectors.toList());
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = serviceOrderRepository.count() + 1;
        return "OS" + date + String.format("%04d", count);
    }

    private ServiceOrder getServiceOrder(Long id) {
        return serviceOrderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service order not found: " + id));
    }

    private ServiceOrderResponse toResponse(ServiceOrder order) {
        return ServiceOrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .clientId(order.getClient() != null ? order.getClient().getId() : null)
                .clientName(order.getClient() != null ? order.getClient().getName() : null)
                .technicianId(order.getTechnician() != null ? order.getTechnician().getId() : null)
                .technicianName(order.getTechnician() != null ? order.getTechnician().getName() : null)
                .status(order.getStatus())
                .priority(order.getPriority())
                .description(order.getDescription())
                .diagnosis(order.getDiagnosis())
                .solution(order.getSolution())
                .laborCost(order.getLaborCost())
                .estimatedCompletion(order.getEstimatedCompletion())
                .completedAt(order.getCompletedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OsNoteResponse toNoteResponse(OsNote note) {
        return OsNoteResponse.builder()
                .id(note.getId())
                .serviceOrderId(note.getServiceOrder().getId())
                .authorId(note.getAuthor() != null ? note.getAuthor().getId() : null)
                .authorName(note.getAuthor() != null ? note.getAuthor().getName() : null)
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .build();
    }
}
