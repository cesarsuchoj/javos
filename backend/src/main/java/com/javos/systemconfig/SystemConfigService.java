package com.javos.systemconfig;

import com.javos.exception.ResourceNotFoundException;
import com.javos.systemconfig.dto.SystemConfigResponse;
import com.javos.systemconfig.dto.SystemConfigUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemConfigService {
    private final SystemConfigRepository systemConfigRepository;

    public List<SystemConfigResponse> findAll() { return systemConfigRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList()); }
    public SystemConfigResponse findByKey(String key) { return toResponse(systemConfigRepository.findByKey(key).orElseThrow(() -> new ResourceNotFoundException("Config not found: " + key))); }

    @Transactional
    public SystemConfigResponse update(String key, SystemConfigUpdateRequest request) {
        SystemConfig config = systemConfigRepository.findByKey(key).orElseThrow(() -> new ResourceNotFoundException("Config not found: " + key));
        config.setValue(request.getValue());
        return toResponse(systemConfigRepository.save(config));
    }

    private SystemConfigResponse toResponse(SystemConfig config) { return SystemConfigResponse.builder().id(config.getId()).key(config.getKey()).value(config.getValue()).description(config.getDescription()).updatedAt(config.getUpdatedAt()).build(); }
}
