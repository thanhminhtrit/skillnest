package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Admin/Manager queries
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    Page<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String email, String fullName, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE r.name = :roleName
            """)
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    @Query("""
            SELECT DISTINCT u FROM User u
            JOIN u.roles r
            JOIN UserProfile up ON up.user = u
            JOIN up.skills s
            WHERE r.name = 'STUDENT'
            AND u.status = 'ACTIVE'
            AND LOWER(s) IN :skillNames
            """)
    List<User> findStudentsBySkills(@Param("skillNames") List<String> skillNames);

    @Query("""
            SELECT DISTINCT u FROM User u
            JOIN u.roles r
            WHERE r.name = 'STUDENT'
            AND u.status = 'ACTIVE'
            """)
    List<User> findAllActiveStudents();
}
