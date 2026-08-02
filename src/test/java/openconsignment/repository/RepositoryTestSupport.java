package openconsignment.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

final class RepositoryTestSupport {

    private RepositoryTestSupport() {}

    static Connection newInMemoryConnection() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = conn.createStatement()) {
            for (String statement : getInitSQL().split(";")) {
                String cleanedStatement = statement.trim();
                if (!cleanedStatement.isEmpty()) {
                    st.execute(cleanedStatement);
                }
            }
        }
        return conn;
    }

    private static String getInitSQL() throws IOException {
        return new String(
                Objects.requireNonNull(RepositoryTestSupport.class.getResourceAsStream("/sql/init.sql"))
                        .readAllBytes(),
                StandardCharsets.UTF_8);
    }
}
