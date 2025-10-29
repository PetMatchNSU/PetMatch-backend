package org.nsu.files.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "file-validation")
public record FileValidationProperties(List<String> photoFormats, List<String> docFormats, long maxSizeBytes) {
}
