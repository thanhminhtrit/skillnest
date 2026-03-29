package com.exe202.skillnest.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final BlobContainerClient blobContainerClient;

    @Value("${azure.storage.base-url:https://skillneststorage.blob.core.windows.net/skillnest-files}")
    private String baseUrl;

    @Override
    public String storeFile(MultipartFile file, String directory) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (originalFileName.contains("..")) {
                throw new BadRequestException("Filename contains invalid path sequence: " + originalFileName);
            }

            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            String blobName = directory + "/" + uniqueFileName;

            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType());
            blobClient.setHttpHeaders(headers);

            String fileUrl = baseUrl + "/" + blobName;
            log.info("File uploaded to Azure: {}", fileUrl);
            return fileUrl;

        } catch (IOException ex) {
            log.error("Failed to upload file to Azure: {}", originalFileName, ex);
            throw new BadRequestException("Failed to upload file: " + originalFileName);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String blobName = fileUrl.replace(baseUrl + "/", "");

            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            if (blobClient.exists()) {
                blobClient.delete();
                log.info("File deleted from Azure: {}", fileUrl);
            } else {
                log.warn("File not found in Azure: {}", fileUrl);
            }
        } catch (Exception ex) {
            log.error("Failed to delete file from Azure: {}", fileUrl, ex);
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

    @Override
    public String storeBytes(byte[] content, String fileName, String directory) {
        try {
            String blobName = directory + "/" + fileName;

            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            blobClient.upload(new ByteArrayInputStream(content), content.length, true);

            if (fileName.endsWith(".png")) {
                BlobHttpHeaders headers = new BlobHttpHeaders()
                        .setContentType("image/png");
                blobClient.setHttpHeaders(headers);
            }

            String fileUrl = baseUrl + "/" + blobName;
            log.info("Bytes stored to Azure: {}", fileUrl);
            return fileUrl;

        } catch (Exception ex) {
            log.error("Failed to store bytes to Azure: {}", fileName, ex);
            throw new BadRequestException("Failed to store file: " + fileName);
        }
    }
}
