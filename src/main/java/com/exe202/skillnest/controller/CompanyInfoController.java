package com.exe202.skillnest.controller;

import com.exe202.skillnest.dto.CompanyInfoDTO;
import com.exe202.skillnest.dto.CreateCompanyInfoRequest;
import com.exe202.skillnest.dto.UpdateCompanyInfoRequest;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.CompanyInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company-info")
@RequiredArgsConstructor
@Tag(name = "Company Info", description = "Company Information Management APIs for CLIENT users")
@SecurityRequirement(name = "bearer-jwt")
public class CompanyInfoController {

    private final CompanyInfoService companyInfoService;

    @PostMapping
    @Operation(summary = "Create company info (CLIENT only)",
               description = "Create company information profile. Only one company info per user is allowed.")
    public ResponseEntity<BaseResponse> createCompanyInfo(
            @Valid @RequestBody CreateCompanyInfoRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        CompanyInfoDTO companyInfoDTO = companyInfoService.createCompanyInfo(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Company info created successfully", companyInfoDTO));
    }

    @PutMapping
    @Operation(summary = "Update company info (CLIENT only)",
               description = "Update company information profile")
    public ResponseEntity<BaseResponse> updateCompanyInfo(
            @Valid @RequestBody UpdateCompanyInfoRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        CompanyInfoDTO companyInfoDTO = companyInfoService.updateCompanyInfo(request, email);
        return ResponseEntity.ok(new BaseResponse(200, "Company info updated successfully", companyInfoDTO));
    }

    @GetMapping
    @Operation(summary = "Get my company info (CLIENT only)",
               description = "Retrieve company information for the authenticated user")
    public ResponseEntity<BaseResponse> getMyCompanyInfo(Authentication authentication) {
        String email = authentication.getName();
        CompanyInfoDTO companyInfoDTO = companyInfoService.getMyCompanyInfo(email);
        return ResponseEntity.ok(new BaseResponse(200, "Company info retrieved successfully", companyInfoDTO));
    }

    @DeleteMapping
    @Operation(summary = "Delete company info (CLIENT only)",
               description = "Delete company information profile")
    public ResponseEntity<BaseResponse> deleteCompanyInfo(Authentication authentication) {
        String email = authentication.getName();
        companyInfoService.deleteCompanyInfo(email);
        return ResponseEntity.ok(new BaseResponse(200, "Company info deleted successfully", null));
    }
}

