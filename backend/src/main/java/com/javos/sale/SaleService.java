package com.javos.sale;

import com.javos.client.Client;
import com.javos.client.ClientRepository;
import com.javos.exception.ResourceNotFoundException;
import com.javos.model.User;
import com.javos.product.Product;
import com.javos.product.ProductRepository;
import com.javos.repository.UserRepository;
import com.javos.sale.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<SaleResponse> findAll() {
        return saleRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SaleResponse findById(Long id) { return toResponse(getSale(id)); }

    @Transactional
    public SaleResponse create(SaleRequest request) {
        Client client = clientRepository.findById(request.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.getClientId()));
        User seller = null;
        if (request.getSellerId() != null) seller = userRepository.findById(request.getSellerId()).orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getSellerId()));
        Sale sale = Sale.builder().saleNumber(generateSaleNumber()).client(client).seller(seller).status(request.getStatus() != null ? request.getStatus() : SaleStatus.OPEN).discount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO).notes(request.getNotes()).saleDate(request.getSaleDate()).totalAmount(BigDecimal.ZERO).build();
        Sale savedSale = saleRepository.save(sale);
        if (request.getItems() != null) { for (SaleItemRequest itemReq : request.getItems()) addItemToSale(savedSale, itemReq); recalculateTotal(savedSale); savedSale = saleRepository.save(savedSale); }
        return toResponse(savedSale);
    }

    @Transactional
    public SaleResponse update(Long id, SaleRequest request) {
        Sale sale = getSale(id);
        Client client = clientRepository.findById(request.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.getClientId()));
        sale.setClient(client);
        if (request.getSellerId() != null) { User seller = userRepository.findById(request.getSellerId()).orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getSellerId())); sale.setSeller(seller); }
        if (request.getStatus() != null) sale.setStatus(request.getStatus());
        if (request.getDiscount() != null) sale.setDiscount(request.getDiscount());
        sale.setNotes(request.getNotes()); sale.setSaleDate(request.getSaleDate());
        return toResponse(saleRepository.save(sale));
    }

    @Transactional
    public SaleResponse changeStatus(Long id, SaleStatus status) { Sale sale = getSale(id); sale.setStatus(status); return toResponse(saleRepository.save(sale)); }

    @Transactional
    public SaleResponse addItem(Long saleId, SaleItemRequest request) { Sale sale = getSale(saleId); addItemToSale(sale, request); recalculateTotal(sale); return toResponse(saleRepository.save(sale)); }

    @Transactional
    public SaleResponse removeItem(Long saleId, Long itemId) { Sale sale = getSale(saleId); saleItemRepository.deleteById(itemId); recalculateTotal(sale); return toResponse(saleRepository.save(sale)); }

    @Transactional
    public void delete(Long id) { Sale sale = getSale(id); sale.setStatus(SaleStatus.CANCELLED); saleRepository.save(sale); }

    private void addItemToSale(Sale sale, SaleItemRequest request) {
        Product product = productRepository.findById(request.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal totalPrice = request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())).subtract(discount);
        saleItemRepository.save(SaleItem.builder().sale(sale).product(product).quantity(request.getQuantity()).unitPrice(request.getUnitPrice()).discount(discount).totalPrice(totalPrice).build());
    }

    private void recalculateTotal(Sale sale) {
        List<SaleItem> items = saleItemRepository.findBySaleId(sale.getId());
        BigDecimal total = items.stream().map(SaleItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        sale.setTotalAmount(total.subtract(sale.getDiscount() != null ? sale.getDiscount() : BigDecimal.ZERO));
    }

    private String generateSaleNumber() { return "VD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%04d", saleRepository.count() + 1); }
    private Sale getSale(Long id) { return saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + id)); }

    private SaleResponse toResponse(Sale sale) {
        List<SaleItemResponse> items = sale.getItems().stream().map(this::toItemResponse).collect(Collectors.toList());
        return SaleResponse.builder().id(sale.getId()).saleNumber(sale.getSaleNumber()).clientId(sale.getClient() != null ? sale.getClient().getId() : null).clientName(sale.getClient() != null ? sale.getClient().getName() : null).sellerId(sale.getSeller() != null ? sale.getSeller().getId() : null).sellerName(sale.getSeller() != null ? sale.getSeller().getName() : null).status(sale.getStatus()).totalAmount(sale.getTotalAmount()).discount(sale.getDiscount()).notes(sale.getNotes()).saleDate(sale.getSaleDate()).items(items).createdAt(sale.getCreatedAt()).updatedAt(sale.getUpdatedAt()).build();
    }
    private SaleItemResponse toItemResponse(SaleItem item) { return SaleItemResponse.builder().id(item.getId()).productId(item.getProduct() != null ? item.getProduct().getId() : null).productName(item.getProduct() != null ? item.getProduct().getName() : null).quantity(item.getQuantity()).unitPrice(item.getUnitPrice()).discount(item.getDiscount()).totalPrice(item.getTotalPrice()).build(); }
}
