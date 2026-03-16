package com.ecommerce.featureflag.controller;

import com.ecommerce.featureflag.dto.FlagDto;
import com.ecommerce.featureflag.dto.FlagMapper;
import com.ecommerce.featureflag.model.Flag;
import com.ecommerce.featureflag.service.FlagRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management/flags")
@RequiredArgsConstructor
@Tag(name = "Management", description = "Feature flag management (CRUD) endpoints")
public class ManagementController {

    private final FlagRepository flagRepository;
    private final FlagMapper flagMapper;

    @Operation(summary = "List all flags")
    @GetMapping
    public List<FlagDto> listFlags() {
        return flagMapper.toDtoList(flagRepository.findAll());
    }

    @Operation(summary = "Get flag by key")
    @GetMapping("/{key}")
    public ResponseEntity<FlagDto> getFlag(@PathVariable String key) {
        return flagRepository.findByKey(key)
                .map(flagMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new flag")
    @PostMapping
    @Transactional
    public ResponseEntity<FlagDto> createFlag(@Valid @RequestBody FlagDto flagDto) {
        if (flagRepository.findByKey(flagDto.getKey()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Flag flag = flagMapper.toEntity(flagDto);
        // Ensure rules back-reference the flag
        if (flag.getRules() != null) {
            flag.getRules().forEach(rule -> rule.setFlag(flag));
        }
        Flag saved = flagRepository.save(flag);
        return ResponseEntity.status(HttpStatus.CREATED).body(flagMapper.toDto(saved));
    }

    @Operation(summary = "Update existing flag")
    @PutMapping("/{key}")
    @Transactional
    public ResponseEntity<FlagDto> updateFlag(@PathVariable String key, @Valid @RequestBody FlagDto flagDto) {
        return flagRepository.findByKey(key)
                .map(existing -> {
                    Flag updated = flagMapper.toEntity(flagDto);
                    updated.setId(existing.getId());
                    updated.setKey(key);
                    // Handle rule relationships
                    if (updated.getRules() != null) {
                        updated.getRules().forEach(rule -> rule.setFlag(updated));
                    }
                    Flag saved = flagRepository.save(updated);
                    return ResponseEntity.ok(flagMapper.toDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete flag")
    @DeleteMapping("/{key}")
    @Transactional
    public ResponseEntity<Void> deleteFlag(@PathVariable String key) {
        return flagRepository.findByKey(key)
                .map(flag -> {
                    flagRepository.delete(flag);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
