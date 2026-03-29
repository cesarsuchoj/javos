package com.javos.systemconfig;

import com.javos.config.CacheConfig;
import com.javos.exception.ResourceNotFoundException;
import com.javos.systemconfig.dto.SystemConfigResponse;
import com.javos.systemconfig.dto.SystemConfigUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemConfigService {
    private final SystemConfigRepository systemConfigRepository;

    @Cacheable(CacheConfig.CACHE_SYSTEM_CONFIG_ALL)
    public List<SystemConfigResponse> findAll() { return systemConfigRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList()); }

    @Cacheable(value = CacheConfig.CACHE_SYSTEM_CONFIG, key = "#key")
    public SystemConfigResponse findByKey(String key) { return toResponse(systemConfigRepository.findByKey(key).orElseThrow(() -> new ResourceNotFoundException("Config not found: " + key))); }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_SYSTEM_CONFIG, key = "#key"),
            @CacheEvict(value = CacheConfig.CACHE_SYSTEM_CONFIG_ALL, allEntries = true)
    })
    public SystemConfigResponse update(String key, SystemConfigUpdateRequest request) {
        SystemConfig config = systemConfigRepository.findByKey(key).orElseThrow(() -> new ResourceNotFoundException("Config not found: " + key));
        config.setValue(request.getValue());
        return toResponse(systemConfigRepository.save(config));
    }

    private SystemConfigResponse toResponse(SystemConfig config) { return SystemConfigResponse.builder().id(config.getId()).key(config.getKey()).value(config.getValue()).description(config.getDescription()).updatedAt(config.getUpdatedAt()).build(); }
}
