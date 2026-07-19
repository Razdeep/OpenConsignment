package openconsignment.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseHandler {

    private static DatabaseHandler databaseHandler = null;
    private Connection conn = null;

    private String getInitSQL() throws IOException {
        return new String(
                Objects.requireNonNull(DatabaseHandler.class.getResourceAsStream("/sql/init.sql"))
                        .readAllBytes(),
                StandardCharsets.UTF_8);
    }

    private void initDatabase(Connection conn) {
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
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    private DatabaseHandler() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + Constants.DATABASE_FILE_NAME);
            initDatabase(conn);
        } catch (ClassNotFoundException | SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    public static DatabaseHandler getInstance() {
        if (databaseHandler == null) {
            databaseHandler = new DatabaseHandler();
        }
        return databaseHandler;
    }

    public Connection getConnection() {
        return conn;
    }
}
