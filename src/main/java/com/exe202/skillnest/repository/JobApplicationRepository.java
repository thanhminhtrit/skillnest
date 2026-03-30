package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.JobApplication;
import com.exe202.skillnest.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUser_UserIdOrderByAppliedDateDesc(Long userId);
    long countByUser_UserId(Long userId);
    long countByUser_UserIdAndStatus(Long userId, ApplicationStatus status);
    List<JobApplication> findByUser_UserIdAndStatus(Long userId, ApplicationStatus status);
}

