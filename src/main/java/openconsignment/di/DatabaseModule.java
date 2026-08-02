package openconsignment.di;

import dagger.Module;
import dagger.Provides;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;
import openconsignment.common.Constants;
import openconsignment.common.Utils;

@Module
public class DatabaseModule {

    @Provides
    @Singleton
    static Connection provideConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Constants.DATABASE_FILE_NAME);
            initDatabase(conn);
            return conn;
        } catch (ClassNotFoundException | SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(DatabaseModule.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw new RuntimeException(ex);
        }
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
            Logger.getLogger(DatabaseModule.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    private static String getInitSQL() throws IOException {
        return new String(
                Objects.requireNonNull(DatabaseModule.class.getResourceAsStream("/sql/init.sql"))
                        .readAllBytes(),
                StandardCharsets.UTF_8);
    }
}
