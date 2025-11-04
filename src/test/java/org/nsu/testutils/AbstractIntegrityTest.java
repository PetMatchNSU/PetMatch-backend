package org.nsu.testutils;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // или NONE, если не нужен web
public abstract class AbstractIntegrityTest {

    static {
        org.nsu.testutils.TestContainerManager.postgres.start();
        org.nsu.testutils.TestContainerManager.minio.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", org.nsu.testutils.TestContainerManager.postgres::getJdbcUrl);
        registry.add("spring.datasource.username", org.nsu.testutils.TestContainerManager.postgres::getUsername);
        registry.add("spring.datasource.password", org.nsu.testutils.TestContainerManager.postgres::getPassword);
        registry.add("minio.endpoint", org.nsu.testutils.TestContainerManager.minio::getS3URL);
        registry.add("minio.access-key", org.nsu.testutils.TestContainerManager.minio::getUserName);
        registry.add("minio.secret-key", org.nsu.testutils.TestContainerManager.minio::getPassword);
        registry.add("minio.bucket-name", () -> "test-bucket");
    }
}
