package org.nsu.config;

import io.minio.MinioClient;
import org.nsu.files.config.MinIOConfigProperties;
import org.nsu.files.config.FileValidationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MinIOConfigProperties.class, FileValidationProperties.class})
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinIOConfigProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }
}
