package com.javos.client;
import com.javos.client.dto.ClientRequest;
import com.javos.client.dto.ClientResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;
    @GetMapping
    public ResponseEntity<List<ClientResponse>> findAll(@RequestParam(required=false) String name, @RequestParam(required=false) String document) {
        if (name != null) return ResponseEntity.ok(clientService.searchByName(name));
        if (document != null) return ResponseEntity.ok(clientService.searchByDocument(document));
        return ResponseEntity.ok(clientService.findAll());
    }
    @GetMapping("/{id}") public ResponseEntity<ClientResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(clientService.findById(id)); }
    @PostMapping public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request)); }
    @PutMapping("/{id}") public ResponseEntity<ClientResponse> update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) { return ResponseEntity.ok(clientService.update(id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { clientService.delete(id); return ResponseEntity.noContent().build(); }
}
