package com.projects.EduConflux.files.service;

import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.files.dto.FileResponse;
import com.projects.EduConflux.files.entity.StoredFile;
import com.projects.EduConflux.files.repository.StoredFileRepository;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final StoredFileRepository fileRepository;
    private final UserRepository userRepository;
    private final S3Client s3Client;
    private final HttpClient httpClient;

    @Value("${supabase.s3.bucketName:educonflux-bucket}")
    private String bucketName;

    @Value("${supabase.url:https://glikkukbfkflvdlptebb.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.anon-key:}")
    private String anonKey;

    @Value("${supabase.s3.accessKey:}")
    private String s3AccessKey;

    @Value("${supabase.s3.secretKey:}")
    private String s3SecretKey;

    public FileService(
            StoredFileRepository fileRepository,
            UserRepository userRepository,
            S3Client s3Client) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.s3Client = s3Client;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /*
     * Upload a file to Supabase Storage bucket.
     */
    @Transactional
    public FileResponse uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Long institutionId = SecurityUtils.getCurrentInstitutionId();

            User user = userRepository
                    .findByIdAndInstitutionId(userId, institutionId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String originalName = file.getOriginalFilename();
            String storedName = UUID.randomUUID() + getExtension(originalName);
            String contentType = file.getContentType() != null && !file.getContentType().isBlank()
                    ? file.getContentType()
                    : "application/octet-stream";

            uploadToSupabase(file, storedName, contentType);

            String fileUrl = getPublicUrl(storedName);

            StoredFile storedFile = StoredFile.builder()
                    .originalName(originalName)
                    .storedName(storedName)
                    .filePath(fileUrl)
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .uploadedBy(user)
                    .build();

            StoredFile savedFile = fileRepository.save(storedFile);

            return mapToResponse(savedFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file for upload", e);
        }
    }

    /*
     * Download / retrieve a file from Supabase Storage bucket.
     */
    @Transactional(readOnly = true)
    public Resource getFile(Long fileId) {
        StoredFile storedFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        byte[] content = downloadObjectBytes(storedFile.getStoredName());

        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return storedFile.getOriginalName();
            }

            @Override
            public long contentLength() {
                return content.length;
            }
        };
    }

    /*
     * Get file metadata.
     */
    @Transactional(readOnly = true)
    public FileResponse getFileDetails(Long fileId) {
        StoredFile storedFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return mapToResponse(storedFile);
    }

    /*
     * Get public URL for a file.
     */
    @Transactional(readOnly = true)
    public String getFilePublicUrl(Long fileId) {
        StoredFile storedFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return storedFile.getFilePath();
    }

    /*
     * Delete file from Supabase storage and database.
     */
    @Transactional
    public void deleteFile(Long fileId) {
        StoredFile storedFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        deleteFromSupabase(storedFile.getStoredName());
        fileRepository.delete(storedFile);
    }

    /*
     * Build public URL for an object in the Supabase bucket.
     */
    public String getPublicUrl(String storedName) {
        String base = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
        return String.format("%s/storage/v1/object/public/%s/%s", base, bucketName, storedName);
    }

    /*
     * Check if S3 credentials are configured.
     */
    private boolean isS3Configured() {
        return s3AccessKey != null && !s3AccessKey.trim().isEmpty() &&
                s3SecretKey != null && !s3SecretKey.trim().isEmpty() &&
                !s3AccessKey.equalsIgnoreCase("placeholder");
    }

    /*
     * Upload object to Supabase using S3 API (if configured) or fallback to Supabase REST API.
     */
    private void uploadToSupabase(MultipartFile file, String storedName, String contentType) throws IOException {
        if (isS3Configured()) {
            try {
                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storedName)
                        .contentType(contentType)
                        .contentLength(file.getSize())
                        .build();

                s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
                return;
            } catch (Exception e) {
                log.warn("S3 upload failed, falling back to Supabase REST API: {}", e.getMessage());
            }
        }

        uploadViaRest(file.getBytes(), storedName, contentType);
    }

    /*
     * Upload object to Supabase using the Storage REST API.
     */
    private void uploadViaRest(byte[] fileBytes, String storedName, String contentType) {
        try {
            String base = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
            String targetUrl = String.format("%s/storage/v1/object/%s/%s", base, bucketName, storedName);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", contentType)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes));

            if (anonKey != null && !anonKey.isBlank()) {
                builder.header("apikey", anonKey);
                builder.header("Authorization", "Bearer " + anonKey);
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Supabase Storage REST upload failed with status "
                        + response.statusCode() + ": " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("File upload was interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Supabase via REST", e);
        }
    }

    /*
     * Download object bytes from Supabase using S3 API or REST API.
     */
    private byte[] downloadObjectBytes(String storedName) {
        if (isS3Configured()) {
            try {
                GetObjectRequest getRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storedName)
                        .build();

                ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(getRequest);
                return s3Stream.readAllBytes();
            } catch (Exception e) {
                log.warn("S3 download failed, falling back to Supabase REST API: {}", e.getMessage());
            }
        }

        return downloadViaRest(storedName);
    }

    /*
     * Download object from Supabase via authenticated or public REST API.
     */
    private byte[] downloadViaRest(String storedName) {
        try {
            String base = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;

            // 1. Try authenticated endpoint first (for private buckets)
            String authUrl = String.format("%s/storage/v1/object/authenticated/%s/%s", base, bucketName, storedName);

            HttpRequest.Builder authBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(authUrl))
                    .timeout(Duration.ofSeconds(30))
                    .GET();

            if (anonKey != null && !anonKey.isBlank()) {
                authBuilder.header("apikey", anonKey);
                authBuilder.header("Authorization", "Bearer " + anonKey);
            }

            HttpResponse<byte[]> authResponse = httpClient.send(authBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (authResponse.statusCode() >= 200 && authResponse.statusCode() < 300) {
                return authResponse.body();
            }

            // 2. Fallback to public endpoint (for public buckets)
            String publicUrl = String.format("%s/storage/v1/object/public/%s/%s", base, bucketName, storedName);
            HttpRequest.Builder publicBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(publicUrl))
                    .timeout(Duration.ofSeconds(30))
                    .GET();

            if (anonKey != null && !anonKey.isBlank()) {
                publicBuilder.header("apikey", anonKey);
            }

            HttpResponse<byte[]> publicResponse = httpClient.send(publicBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (publicResponse.statusCode() >= 200 && publicResponse.statusCode() < 300) {
                return publicResponse.body();
            }

            String errorMsg = authResponse.body() != null && authResponse.body().length > 0
                    ? new String(authResponse.body(), java.nio.charset.StandardCharsets.UTF_8)
                    : (publicResponse.body() != null ? new String(publicResponse.body(), java.nio.charset.StandardCharsets.UTF_8) : "No error body");

            log.error("Supabase download failed. Auth status: {}, Public status: {}, Details: {}",
                    authResponse.statusCode(), publicResponse.statusCode(), errorMsg);

            throw new RuntimeException(String.format("Failed to download file from Supabase storage (status %d): %s",
                    authResponse.statusCode(), errorMsg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("File download was interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file from Supabase storage", e);
        }
    }

    /*
     * Delete object from Supabase bucket.
     */
    private void deleteFromSupabase(String storedName) {
        if (isS3Configured()) {
            try {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(storedName)
                        .build();

                s3Client.deleteObject(deleteRequest);
                return;
            } catch (Exception e) {
                log.warn("S3 delete failed, falling back to Supabase REST API: {}", e.getMessage());
            }
        }

        deleteViaRest(storedName);
    }

    /*
     * Delete object via Supabase REST API.
     */
    private void deleteViaRest(String storedName) {
        try {
            String base = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
            String deleteUrl = String.format("%s/storage/v1/object/%s/%s", base, bucketName, storedName);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(deleteUrl))
                    .timeout(Duration.ofSeconds(15))
                    .DELETE();

            if (anonKey != null && !anonKey.isBlank()) {
                builder.header("apikey", anonKey);
                builder.header("Authorization", "Bearer " + anonKey);
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Failed to delete object from Supabase REST: {}", response.body());
            }
        } catch (Exception e) {
            log.warn("Error deleting object from Supabase: {}", e.getMessage());
        }
    }

    /*
     * Extract extension from original file name.
     */
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf("."));
    }

    /*
     * Convert entity to response DTO.
     */
    private FileResponse mapToResponse(StoredFile file) {
        return FileResponse.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .storedName(file.getStoredName())
                .fileUrl(file.getFilePath())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .createdAt(file.getCreatedAt())
                .build();
    }
}