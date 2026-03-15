package com.exe202.skillnest.util;

import com.exe202.skillnest.exception.FileTooLargeException;
import com.exe202.skillnest.exception.InvalidFileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class FileValidator {

    private final Tika tika = new Tika();

    // File size limits in bytes
    private static final long IMAGE_MAX_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final long DOCUMENT_MAX_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long ARCHIVE_MAX_SIZE = 50 * 1024 * 1024; // 50 MB
    private static final long CHAT_FILE_MAX_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long DISPUTE_FILE_MAX_SIZE = 20 * 1024 * 1024; // 20 MB

    // Allowed content types
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    ));

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = new HashSet<>(Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    ));

    private static final Set<String> ALLOWED_ARCHIVE_TYPES = new HashSet<>(Arrays.asList(
            "application/zip",
            "application/x-zip-compressed",
            "application/x-rar-compressed"
    ));

    private static final Set<String> DANGEROUS_TYPES = new HashSet<>(Arrays.asList(
            "application/x-msdownload",
            "application/x-sh",
            "application/x-executable",
            "text/x-shellscript"
    ));

    /**
     * Validate image file (for avatars, thumbnails, portfolio)
     */
    public void validateImage(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, IMAGE_MAX_SIZE, "5MB");
        String contentType = detectContentType(file);

        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException(
                    "Invalid image type. Allowed types: JPEG, PNG, GIF, WebP");
        }

        validateNotDangerous(contentType);
    }

    /**
     * Validate document file (for requirements, proposals)
     */
    public void validateDocument(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, DOCUMENT_MAX_SIZE, "10MB");
        String contentType = detectContentType(file);

        if (!ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException(
                    "Invalid document type. Allowed types: PDF, Word, Excel, PowerPoint");
        }

        validateNotDangerous(contentType);
    }

    /**
     * Validate archive file (for source code, bulk deliverables)
     */
    public void validateArchive(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, ARCHIVE_MAX_SIZE, "50MB");
        String contentType = detectContentType(file);

        if (!ALLOWED_ARCHIVE_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException(
                    "Invalid archive type. Allowed types: ZIP, RAR");
        }

        validateNotDangerous(contentType);
    }

    /**
     * Validate chat attachment (images or documents)
     */
    public void validateChatFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, CHAT_FILE_MAX_SIZE, "10MB");
        String contentType = detectContentType(file);

        if (!ALLOWED_IMAGE_TYPES.contains(contentType) &&
            !ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException(
                    "Invalid file type for chat. Allowed: images and documents");
        }

        validateNotDangerous(contentType);
    }

    /**
     * Validate dispute evidence file
     */
    public void validateDisputeFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, DISPUTE_FILE_MAX_SIZE, "20MB");
        String contentType = detectContentType(file);

        if (!ALLOWED_IMAGE_TYPES.contains(contentType) &&
            !ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException(
                    "Invalid file type for dispute. Allowed: images and documents");
        }

        validateNotDangerous(contentType);
    }

    /**
     * Detect actual content type using Apache Tika (checks magic bytes)
     */
    private String detectContentType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream());
        } catch (IOException e) {
            log.error("Failed to detect content type for file: {}", file.getOriginalFilename(), e);
            throw new InvalidFileTypeException("Failed to detect file type");
        }
    }

    private void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("File cannot be empty");
        }
    }

    private void validateFileSize(MultipartFile file, long maxSize, String maxSizeStr) {
        if (file.getSize() > maxSize) {
            throw new FileTooLargeException(
                    String.format("File size exceeds limit (max %s)", maxSizeStr));
        }
    }

    private void validateNotDangerous(String contentType) {
        if (DANGEROUS_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException("Dangerous file type detected");
        }
    }

    /**
     * Validate multiple files at once
     */
    public void validateMultipleFiles(MultipartFile[] files, int maxCount, long maxTotalSize) {
        if (files.length > maxCount) {
            throw new InvalidFileTypeException(
                    String.format("Maximum %d files allowed per upload", maxCount));
        }

        long totalSize = Arrays.stream(files)
                .mapToLong(MultipartFile::getSize)
                .sum();

        if (totalSize > maxTotalSize) {
            throw new FileTooLargeException(
                    String.format("Total file size exceeds limit (max %dMB)",
                            maxTotalSize / (1024 * 1024)));
        }
    }
}

