package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.CompanyInfoDTO;
import com.exe202.skillnest.dto.CreateCompanyInfoRequest;
import com.exe202.skillnest.dto.UpdateCompanyInfoRequest;
import com.exe202.skillnest.entity.CompanyInfo;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.repository.CompanyInfoRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.CompanyInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyInfoServiceImpl implements CompanyInfoService {

    private final CompanyInfoRepository companyInfoRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CompanyInfoDTO createCompanyInfo(CreateCompanyInfoRequest request, String email) {
        log.info("Creating company info for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if company info already exists
        if (companyInfoRepository.existsByUser_UserId(user.getUserId())) {
            throw new BadRequestException("Company info already exists. Use update endpoint instead.");
        }

        CompanyInfo companyInfo = CompanyInfo.builder()
                .user(user)
                .name(request.getName())
                .location(request.getLocation())
                .size(request.getSize())
                .industry(request.getIndustry())
                .build();

        companyInfo = companyInfoRepository.save(companyInfo);
        log.info("Company info created successfully for user: {}", email);

        return convertToDTO(companyInfo);
    }

    @Override
    @Transactional
    public CompanyInfoDTO updateCompanyInfo(UpdateCompanyInfoRequest request, String email) {
        log.info("Updating company info for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        CompanyInfo companyInfo = companyInfoRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new NotFoundException("Company info not found. Please create it first."));

        // Update fields
        if (request.getName() != null) {
            companyInfo.setName(request.getName());
        }
        if (request.getLocation() != null) {
            companyInfo.setLocation(request.getLocation());
        }
        if (request.getSize() != null) {
            companyInfo.setSize(request.getSize());
        }
        if (request.getIndustry() != null) {
            companyInfo.setIndustry(request.getIndustry());
        }

        companyInfo = companyInfoRepository.save(companyInfo);
        log.info("Company info updated successfully for user: {}", email);

        return convertToDTO(companyInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyInfoDTO getMyCompanyInfo(String email) {
        log.info("Getting company info for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        CompanyInfo companyInfo = companyInfoRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new NotFoundException("Company info not found"));

        return convertToDTO(companyInfo);
    }

    @Override
    @Transactional
    public void deleteCompanyInfo(String email) {
        log.info("Deleting company info for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        CompanyInfo companyInfo = companyInfoRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new NotFoundException("Company info not found"));

        companyInfoRepository.delete(companyInfo);
        log.info("Company info deleted successfully for user: {}", email);
    }

    private CompanyInfoDTO convertToDTO(CompanyInfo companyInfo) {
        return CompanyInfoDTO.builder()
                .name(companyInfo.getName())
                .location(companyInfo.getLocation())
                .size(companyInfo.getSize())
                .industry(companyInfo.getIndustry())
                .build();
    }
}

