package com.ecommerce.featureflag.service;

import com.ecommerce.featureflag.model.Flag;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory storage for feature flags.
 */
public interface FlagStore {
     Optional<Flag> getFlag(String key);

     void setFlag(String key, Flag flag);
     Map<String, Flag> getAllFlags();
}
