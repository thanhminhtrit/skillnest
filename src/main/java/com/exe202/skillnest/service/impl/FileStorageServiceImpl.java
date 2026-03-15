package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;
    private final String baseUrl;

    public FileStorageServiceImpl(
            @Value("${file.upload-dir:uploads}") String uploadDir,
            @Value("${file.base-url:http://localhost:8080}") String baseUrl) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String directory) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new BadRequestException("Filename contains invalid path sequence: " + originalFileName);
            }

            // Generate unique filename
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Create directory if not exists
            Path targetLocation = fileStorageLocation.resolve(directory);
            Files.createDirectories(targetLocation);

            // Copy file to the target location
            Path filePath = targetLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return the file URL
            String fileUrl = baseUrl + "/files/" + directory + "/" + uniqueFileName;

            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;

        } catch (IOException ex) {
            log.error("Failed to store file: {}", originalFileName, ex);
            throw new BadRequestException("Failed to store file: " + originalFileName);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extract filename from URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            String directory = fileUrl.substring(fileUrl.lastIndexOf("/files/") + 7, fileUrl.lastIndexOf('/'));

            Path filePath = fileStorageLocation.resolve(directory).resolve(fileName);
            Files.deleteIfExists(filePath);

            log.info("File deleted successfully: {}", fileUrl);
        } catch (IOException ex) {
            log.error("Failed to delete file: {}", fileUrl, ex);
            throw new BadRequestException("Failed to delete file");
        }
    }

    @Override
    public String[] storeMultipleFiles(MultipartFile[] files, String directory) {
        String[] urls = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i] = storeFile(files[i], directory);
        }
        return urls;
    }
}

