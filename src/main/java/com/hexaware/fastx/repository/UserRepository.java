package com.hexaware.fastx.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hexaware.fastx.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByFullNameContainingIgnoreCase(String fullName);

    List<User> findByIsActiveTrue();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.roleName = :roleName")
    Long countUsersByRoleName(@Param("roleName") String roleName);
}
