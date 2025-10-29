package org.nsu.files.storage.impl;

import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.nsu.files.config.MinIOConfigProperties;
import org.nsu.files.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinIOStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final MinIOConfigProperties properties;

    private String getBucketName() {
        return properties.bucketName();
    }

    @Override
    public String upload(MultipartFile file, String bucket, String objectName) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = getBucketName();
        }
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public InputStream get(String bucket, String objectName) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = getBucketName();
        }
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file", e);
        }
    }

    @Override
    public void delete(String bucket, String objectName) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = getBucketName();
        }
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Override
    public boolean bucketExists(String bucket) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = getBucketName();
        }
        try {
            return minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder().bucket(bucket).build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to check bucket existence", e);
        }
    }

    @Override
    public void createBucket(String bucket) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = getBucketName();
        }
        try {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucket).build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bucket", e);
        }
    }
}
