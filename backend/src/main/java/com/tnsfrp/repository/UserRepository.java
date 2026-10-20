package com.tnsfrp.repository;

import com.tnsfrp.document.Role;
import com.tnsfrp.document.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleIn(Collection<Role> roles);
    List<User> findByRoleInAndApproved(Collection<Role> roles, boolean approved);
}
