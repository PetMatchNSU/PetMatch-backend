package org.nsu.files.storage.impl;

import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.nsu.files.config.MinIOConfigProperties;
import org.nsu.files.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class MinIOStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    @Autowired
    public MinIOStorageServiceImpl(MinioClient minioClient, MinIOConfigProperties properties) {
        this.minioClient = minioClient;
        this.bucketName = properties.bucketName();
    }

    @Override
    public String upload(MultipartFile file, String bucket, String objectName) throws Exception {
        if (bucket == null || bucket.isEmpty()) {
            bucket = bucketName;
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
        }
    }

    @Override
    public InputStream get(String bucket, String objectName) throws Exception {
        if (bucket == null || bucket.isEmpty()) {
            bucket = bucketName;
        }
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build()
        );
    }

    @Override
    public void delete(String bucket, String objectName) throws Exception {
        if (bucket == null || bucket.isEmpty()) {
            bucket = bucketName;
        }
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build()
        );
    }

    @Override
    public boolean bucketExists(String bucket) throws Exception {
        if (bucket == null || bucket.isEmpty()) {
            bucket = bucketName;
        }
        return minioClient.bucketExists(
            io.minio.BucketExistsArgs.builder().bucket(bucket).build()
        );
    }

    @Override
    public void createBucket(String bucket) throws Exception {
        if (bucket == null || bucket.isEmpty()) {
            bucket = bucketName;
        }
        minioClient.makeBucket(
            MakeBucketArgs.builder().bucket(bucket).build()
        );
    }
}
