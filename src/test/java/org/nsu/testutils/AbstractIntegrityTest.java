package org.nsu.testutils;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // или NONE, если не нужен web
public abstract class AbstractIntegrityTest {

    static {
        TestContainerManager.postgres.start();
        TestContainerManager.minio.start();
        TestContainerManager.redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestContainerManager.postgres::getJdbcUrl);
        registry.add("spring.datasource.username", TestContainerManager.postgres::getUsername);
        registry.add("spring.datasource.password", TestContainerManager.postgres::getPassword);
        registry.add("minio.endpoint", TestContainerManager.minio::getS3URL);
        registry.add("minio.access-key", TestContainerManager.minio::getUserName);
        registry.add("minio.secret-key", TestContainerManager.minio::getPassword);
        registry.add("minio.bucket-name", () -> "test-bucket");
        registry.add("spring.data.redis.host", TestContainerManager.redis::getHost);
        registry.add("spring.data.redis.port", () -> TestContainerManager.redis.getMappedPort(6379));
    }
}
