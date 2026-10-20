package com.tnsfrp.repository;

import com.tnsfrp.document.NgoProfile;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NgoProfileRepository extends MongoRepository<NgoProfile, String> {
    Optional<NgoProfile> findByUserId(String userId);
}
