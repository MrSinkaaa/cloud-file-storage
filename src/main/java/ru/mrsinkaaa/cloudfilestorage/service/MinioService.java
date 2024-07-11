package ru.mrsinkaaa.cloudfilestorage.service;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketStorageName;
    private static final String OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";


    public void uploadFile(MultipartFile file, String path) {
        ensureBucketExists();

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketStorageName).object(path)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("File uploaded to MinIO: {}", path);
        } catch (MinioException | IOException e) {
            log.error("Error uploading file to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Error uploading file to MinIO", e);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error with algorithm or key: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void renameFile(String oldFileName, String newFileName) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .source(CopySource.builder().bucket(bucketStorageName).object(oldFileName).build())
                            .bucket(bucketStorageName)
                            .object(newFileName)
                            .build()
            );
            deleteFile(oldFileName);
            log.info("File renamed from {} to {}", oldFileName, newFileName);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error renaming file: {}", e.getMessage(), e);
            throw new RuntimeException("Error renaming file: ", e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketStorageName)
                            .object(fileName)
                            .build()
            );
            log.info("File deleted from MinIO: {}", fileName);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            throw new RuntimeException("Error deleting file: ", e);
        }
    }

    public void createFolder(String path) {
        ensureBucketExists();

        try {
            byte[] emptyContent = new byte[0];
            ByteArrayInputStream inputStream = new ByteArrayInputStream(emptyContent);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketStorageName)
                            .object(path)
                            .stream(inputStream, 0, -1)
                            .contentType(OCTET_STREAM_CONTENT_TYPE)
                            .build()
            );
            log.info("Folder created in MinIO: {}", path);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error creating directory: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating directory: ", e);
        }
    }

    private void ensureBucketExists() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketStorageName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketStorageName).build());
                log.info("Bucket created: {}", bucketStorageName);
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error ensuring bucket exists: {}", e.getMessage(), e);
            throw new RuntimeException("Error ensuring bucket exists", e);
        }
    }
}






