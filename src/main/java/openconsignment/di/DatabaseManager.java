package openconsignment.di;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import openconsignment.common.Constants;

@Slf4j
public class DatabaseManager {

    private Connection connection;

    public DatabaseManager(Connection connection) {
        this.connection = connection;
    }

    public synchronized Connection getConnection() throws SQLException {

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + Constants.DATABASE_FILE_NAME);
            initDatabase(connection);
        }

        return connection;
    }

    public synchronized void closeConnection() throws SQLException {

        if (connection != null && !connection.isClosed()) {
            connection.close();
        }

        connection = null;
    }

    private static void initDatabase(Connection conn) {
        try {
            String initSQL = getInitSQL();
            try (Statement st = conn.createStatement()) {
                conn.setAutoCommit(false);

                for (String statement : initSQL.split(";")) {
                    String cleanedStatement = statement.trim();
                    if (!cleanedStatement.isEmpty()) {
                        st.execute(cleanedStatement);
                    }
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
    }

    private static String getInitSQL() throws IOException {
        return new String(
                Objects.requireNonNull(DatabaseModule.class.getResourceAsStream("/sql/init.sql"))
                        .readAllBytes(),
                StandardCharsets.UTF_8);
    }
}
