package com.ecommerce.featureflag.service;

import com.ecommerce.featureflag.model.Flag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlagRepository extends JpaRepository<Flag, String> {
    Optional<Flag> findByKey(String key);
}
