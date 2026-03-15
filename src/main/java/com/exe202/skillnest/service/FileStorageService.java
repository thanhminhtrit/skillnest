package com.exe202.skillnest.service;

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
     * Delete file by URL
     * @param fileUrl The URL of file to delete
     */
    void deleteFile(String fileUrl);

    /**
     * Store multiple files
     * @param files Array of files to upload
     * @param directory The directory to store
     * @return Array of URLs to access the files
     */
    String[] storeMultipleFiles(MultipartFile[] files, String directory);
}

