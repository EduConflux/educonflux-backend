package com.projects.EduConflux.files.service;

import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.files.dto.FileResponse;
import com.projects.EduConflux.files.entity.StoredFile;
import com.projects.EduConflux.files.repository.StoredFileRepository;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final StoredFileRepository fileRepository;
    private final UserRepository userRepository;


    /*
     * Local storage directory.
     *
     * Later this can be replaced by
     * AWS S3 storage.
     */
    private final Path uploadDirectory =
            Paths.get("uploads");


    /*
     * Upload a file.
     */
    @Transactional
    public FileResponse uploadFile(
            MultipartFile file) {

        if (file == null ||
                file.isEmpty()) {

            throw new RuntimeException(
                    "File cannot be empty"
            );
        }


        try {

            /*
             * Create uploads directory
             * if it does not exist.
             */
            Files.createDirectories(
                    uploadDirectory
            );


            /*
             * Get authenticated user.
             */
            Long userId =
                    SecurityUtils.getCurrentUserId();

            Long institutionId =
                    SecurityUtils.getCurrentInstitutionId();


            User user =
                    userRepository
                            .findByIdAndInstitutionId(
                                    userId,
                                    institutionId
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User not found"
                                    ));


            /*
             * Generate a unique stored name.
             */
            String originalName =
                    file.getOriginalFilename();

            String storedName =
                    UUID.randomUUID()
                            + getExtension(originalName);


            /*
             * Physical file path.
             */
            Path filePath =
                    uploadDirectory.resolve(
                            storedName
                    );


            /*
             * Save physical file.
             */
            Files.copy(
                    file.getInputStream(),
                    filePath
            );


            /*
             * Save metadata in database.
             */
            StoredFile storedFile =
                    StoredFile.builder()
                            .originalName(originalName)
                            .storedName(storedName)
                            .filePath(
                                    filePath
                                            .toAbsolutePath()
                                            .toString()
                            )
                            .contentType(
                                    file.getContentType() != null
                                            ? file.getContentType()
                                            : "application/octet-stream"
                            )
                            .fileSize(
                                    file.getSize()
                            )
                            .uploadedBy(user)
                            .build();


            StoredFile savedFile =
                    fileRepository.save(
                            storedFile
                    );


            return mapToResponse(
                    savedFile
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to store file",
                    e
            );
        }
    }


    /*
     * Download / retrieve a file.
     */
    @Transactional(readOnly = true)
    public Resource getFile(
            Long fileId) {

        StoredFile storedFile =
                fileRepository
                        .findById(fileId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "File not found"
                                ));


        try {

            Path path =
                    Paths.get(
                            storedFile.getFilePath()
                    );


            Resource resource =
                    new UrlResource(
                            path.toUri()
                    );


            if (!resource.exists() ||
                    !resource.isReadable()) {

                throw new RuntimeException(
                        "File cannot be read"
                );
            }


            return resource;

        } catch (MalformedURLException e) {

            throw new RuntimeException(
                    "Invalid file path",
                    e
            );
        }
    }


    /*
     * Get file metadata.
     */
    @Transactional(readOnly = true)
    public FileResponse getFileDetails(
            Long fileId) {

        StoredFile storedFile =
                fileRepository
                        .findById(fileId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "File not found"
                                ));

        return mapToResponse(
                storedFile
        );
    }


    /*
     * Extract extension from original file name.
     */
    private String getExtension(
            String fileName) {

        if (fileName == null ||
                !fileName.contains(".")) {

            return "";
        }

        return fileName.substring(
                fileName.lastIndexOf(".")
        );
    }


    /*
     * Convert entity to response DTO.
     */
    private FileResponse mapToResponse(
            StoredFile file) {

        return FileResponse.builder()
                .id(file.getId())
                .originalName(
                        file.getOriginalName()
                )
                .contentType(
                        file.getContentType()
                )
                .fileSize(
                        file.getFileSize()
                )
                .createdAt(
                        file.getCreatedAt()
                )
                .build();
    }
}