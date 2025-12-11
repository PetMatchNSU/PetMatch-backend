package org.nsu.testutils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // или NONE, если не нужен web
public abstract class AbstractIntegrityTest {

    static {
        TestContainerManager.postgres.start();
        TestContainerManager.minio.start();
        TestContainerManager.redis.start();
    }

    /**
     * Полная очистка всех таблиц в БД перед тестами
     */
    protected void truncateAllTables() {
        try (Connection connection = TestContainerManager.postgres.createConnection("");
             Statement statement = connection.createStatement()) {

            // Отключаем foreign key проверки для TRUNCATE (формально смысл есть только внутри одной транзакции,
            // но TRUNCATE ... CASCADE и так всё потянет)
            statement.execute("SET CONSTRAINTS ALL DEFERRED");

            // Получаем список всех таблиц
            ResultSet rs = statement.executeQuery(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public'"
            );

            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString("tablename"));
            }

            // TRUNCATE всех таблиц с CASCADE
            for (String table : tables) {
                    if (!table.startsWith("pg_") && !table.startsWith("sql_")) {
                    statement.execute("TRUNCATE TABLE " + table + " CASCADE");
                }
            }

            // Включаем foreign key проверки обратно
            statement.execute("SET CONSTRAINTS ALL IMMEDIATE");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to truncate tables", e);
        }
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
