package com.ecommerce.featureflag.service;

import com.ecommerce.featureflag.model.Flag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA-backed storage for feature flags.
 */
@Service
@RequiredArgsConstructor
public class DefaultFlagStore implements FlagStore {

    private final FlagRepository flagRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Flag> getFlag(String key) {
        return flagRepository.findByKey(key);
    }

    @Override
    @Transactional
    public void setFlag(String key, Flag flag) {
        // Ensure key matches
        flag.setKey(key);
        flagRepository.save(flag);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Flag> getAllFlags() {
        return flagRepository.findAll().stream()
                .collect(Collectors.toMap(Flag::getKey, flag -> flag));
    }
}
