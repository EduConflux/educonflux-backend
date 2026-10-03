package com.projects.EduConflux.files.controller;

import com.projects.EduConflux.files.dto.FileResponse;
import com.projects.EduConflux.files.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /*
     * Upload a file.
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file")
            MultipartFile file) {

        return ResponseEntity.ok(
                fileService.uploadFile(file)
        );
    }

    /*
     * Get file metadata.
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponse> getFileDetails(
            @PathVariable Long fileId) {

        return ResponseEntity.ok(
                fileService.getFileDetails(fileId)
        );
    }

    /*
     * Download file.
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId) {

        Resource resource =
                fileService.getFile(fileId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                resource.getFilename() +
                                "\""
                )
                .body(resource);
    }

    /*
     * Get direct Supabase public URL.
     */
    @GetMapping("/{fileId}/url")
    public ResponseEntity<Map<String, String>> getFileUrl(
            @PathVariable Long fileId) {

        return ResponseEntity.ok(
                Map.of("url", fileService.getFilePublicUrl(fileId))
        );
    }

    /*
     * Delete file.
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId) {

        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}