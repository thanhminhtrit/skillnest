package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {
    Optional<CompanyInfo> findByUser_UserId(Long userId);
    boolean existsByUser_UserId(Long userId);
}

