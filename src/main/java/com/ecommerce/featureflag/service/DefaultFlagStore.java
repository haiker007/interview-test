package com.ecommerce.featureflag.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import com.ecommerce.featureflag.model.Flag;

/**
 * In-memory storage for feature flags.
 */
@Component
public class DefaultFlagStore implements FlagStore {

    private final Map<String, Flag> flags = new HashMap<>();

    public DefaultFlagStore() {
        // Initialize with sample flags for testing
        initializeSampleFlags();
    }

    public Optional<Flag> getFlag(String key) {
        return Optional.ofNullable(flags.get(key));
    }

    public void setFlag(String key, Flag flag) {
        flags.put(key, flag);
    }

    public Map<String, Flag> getAllFlags() {
        return new HashMap<>(flags);
    }

    private void initializeSampleFlags() {
        // Sample boolean flag - default to true when enabled
        flags.put("boolean-flag", Flag.builder()
                .id("flag-1")
                .key("boolean-flag")
                .name("Boolean Feature Flag")
                .type(Flag.FlagType.BOOLEAN)
                .status(Flag.FlagStatus.ENABLED)
                .variations(Map.of("true", true, "false", false))
                .defaultVariation("true")
                .trackEvents(true)
                .build());
    }
}
