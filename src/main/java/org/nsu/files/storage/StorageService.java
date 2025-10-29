package org.nsu.files.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    String upload(MultipartFile file, String bucket, String objectName);
    InputStream get(String bucket, String objectName);
    void delete(String bucket, String objectName);
    boolean bucketExists(String bucket);
    void createBucket(String bucket);
}
