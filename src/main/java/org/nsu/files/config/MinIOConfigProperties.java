package org.nsu.files.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinIOConfigProperties(String endpoint, String accessKey, String secretKey, String bucketName) {
}
