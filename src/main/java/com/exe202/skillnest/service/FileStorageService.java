package com.exe202.skillnest.service;

import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.enums.RelatedEntityType;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Store uploaded file and return the URL
     * @param file The file to upload
     * @param directory The directory to store (e.g., "avatars", "chat-files", "documents")
     * @return The URL to access the file
     */
    String storeFile(MultipartFile file, String directory);

    /**
     * Store uploaded file with ownership tracking
     * @param file The file to upload
     * @param directory The directory to store
     * @param uploadedBy The user uploading the file
     * @param entityType The type of entity the file is related to
     * @return The URL to access the file
     */
    String storeFile(MultipartFile file, String directory, User uploadedBy, RelatedEntityType entityType);

    /**
     * Delete file by URL
     * @param fileUrl The URL of file to delete
     */
    void deleteFile(String fileUrl);

    /**
     * Delete file by URL with ownership check
     * @param fileUrl The URL of file to delete
     * @param currentUserId The ID of the user attempting deletion
     */
    void deleteFile(String fileUrl, Long currentUserId);

    /**
     * Store multiple files
     * @param files Array of files to upload
     * @param directory The directory to store
     * @return Array of URLs to access the files
     */
    String[] storeMultipleFiles(MultipartFile[] files, String directory);

    /**
     * Store raw bytes as a file
     */
    String storeBytes(byte[] content, String fileName, String directory);
}

