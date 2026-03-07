package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.CompanyInfoDTO;
import com.exe202.skillnest.dto.CreateCompanyInfoRequest;
import com.exe202.skillnest.dto.UpdateCompanyInfoRequest;

public interface CompanyInfoService {
    CompanyInfoDTO createCompanyInfo(CreateCompanyInfoRequest request, String email);
    CompanyInfoDTO updateCompanyInfo(UpdateCompanyInfoRequest request, String email);
    CompanyInfoDTO getMyCompanyInfo(String email);
    void deleteCompanyInfo(String email);
}

