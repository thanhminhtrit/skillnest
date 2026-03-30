package com.exe202.skillnest.controller;

import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.enums.RelatedEntityType;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.FileStorageService;
import com.exe202.skillnest.util.FileValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "File upload APIs for avatars, chat attachments, and documents")
@SecurityRequirement(name = "bearer-jwt")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final FileValidator fileValidator;
    private final UserRepository userRepository;

    @PostMapping("/upload/avatar")
    @Operation(summary = "Upload user avatar (images only)")
    public ResponseEntity<BaseResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        fileValidator.validateImage(file);

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String fileUrl = fileStorageService.storeFile(file, "avatars", currentUser, RelatedEntityType.AVATAR);

        Map<String, String> response = new HashMap<>();
        response.put("fileUrl", fileUrl);
        response.put("fileName", file.getOriginalFilename());
        response.put("fileSize", String.valueOf(file.getSize()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Avatar uploaded successfully", response));
    }

    @PostMapping("/upload/chat-file")
    @Operation(summary = "Upload file for chat messages (images, documents)")
    public ResponseEntity<BaseResponse> uploadChatFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        fileValidator.validateChatFile(file);

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String fileUrl = fileStorageService.storeFile(file, "chat-files", currentUser, RelatedEntityType.CHAT);

        Map<String, String> response = new HashMap<>();
        response.put("fileUrl", fileUrl);
        response.put("fileName", file.getOriginalFilename());
        response.put("fileSize", String.valueOf(file.getSize()));
        response.put("fileType", file.getContentType());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "File uploaded successfully", response));
    }

    @PostMapping("/upload/document")
    @Operation(summary = "Upload document (PDF, Word, Excel)")
    public ResponseEntity<BaseResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        fileValidator.validateDocument(file);

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String fileUrl = fileStorageService.storeFile(file, "documents", currentUser, RelatedEntityType.PROJECT);

        Map<String, String> response = new HashMap<>();
        response.put("fileUrl", fileUrl);
        response.put("fileName", file.getOriginalFilename());
        response.put("fileSize", String.valueOf(file.getSize()));
        response.put("fileType", file.getContentType());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Document uploaded successfully", response));
    }

    @PostMapping("/upload/dispute-evidence")
    @Operation(summary = "Upload evidence for dispute (images, documents)")
    public ResponseEntity<BaseResponse> uploadDisputeEvidence(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        fileValidator.validateDisputeFile(file);

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String fileUrl = fileStorageService.storeFile(file, "dispute-evidence", currentUser, RelatedEntityType.DISPUTE);

        Map<String, String> response = new HashMap<>();
        response.put("fileUrl", fileUrl);
        response.put("fileName", file.getOriginalFilename());
        response.put("fileSize", String.valueOf(file.getSize()));
        response.put("fileType", file.getContentType());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Evidence uploaded successfully", response));
    }

    @PostMapping("/upload/multiple")
    @Operation(summary = "Upload multiple files at once")
    public ResponseEntity<BaseResponse> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(defaultValue = "documents") String directory,
            Authentication authentication) {

        fileValidator.validateMultipleFiles(files, 10, 50 * 1024 * 1024); // Max 10 files, 50MB total

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String[] fileUrls = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileUrls[i] = fileStorageService.storeFile(files[i], directory, currentUser, RelatedEntityType.PROJECT);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("fileUrls", fileUrls);
        response.put("count", fileUrls.length);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Files uploaded successfully", response));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete uploaded file")
    public ResponseEntity<BaseResponse> deleteFile(
            @RequestParam("fileUrl") String fileUrl,
            Authentication authentication) {

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        fileStorageService.deleteFile(fileUrl, currentUser.getUserId());

        return ResponseEntity.ok(
                new BaseResponse(200, "File deleted successfully", null));
    }
}
