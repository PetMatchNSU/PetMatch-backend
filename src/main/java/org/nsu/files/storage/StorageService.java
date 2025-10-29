package org.nsu.files.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    String upload(MultipartFile file, String bucket, String objectName) throws Exception;
    InputStream get(String bucket, String objectName) throws Exception;
    void delete(String bucket, String objectName) throws Exception;
    boolean bucketExists(String bucket) throws Exception;
    void createBucket(String bucket) throws Exception;
}
