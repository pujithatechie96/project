package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // 🔍 Search by email (partial)
    List<User> findByEmailContainingIgnoreCase(String email);

    // 🔍 Search by phone (partial)
    List<User> findByPhoneContaining(String phone);

    // 🔍 Search by email OR phone
    List<User> findByEmailContainingIgnoreCaseOrPhoneContaining(
            String email,
            String phone
    );

}
