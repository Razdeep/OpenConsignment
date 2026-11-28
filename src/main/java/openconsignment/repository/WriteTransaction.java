package openconsignment.repository;

import java.sql.Connection;
import java.sql.SQLException;

/** Runs a complete write on the application's shared connection. */
final class WriteTransaction {
    private WriteTransaction() {}

    @FunctionalInterface
    interface Operation {
        void run() throws SQLException;
    }

    static void run(Connection connection, Operation operation) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        boolean safeToRestore = false;
        Throwable failure = null;
        try {
            operation.run();
            connection.commit();
            safeToRestore = true;
        } catch (SQLException | RuntimeException | Error ex) {
            failure = ex;
            try {
                connection.rollback();
                safeToRestore = true;
            } catch (SQLException rollbackFailure) {
                ex.addSuppressed(rollbackFailure);
            }
            throw ex;
        } finally {
            // Enabling auto-commit after a failed rollback could commit partial changes.
            if (safeToRestore && autoCommit) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException restoreFailure) {
                    if (failure != null) {
                        failure.addSuppressed(restoreFailure);
                    } else {
                        throw restoreFailure;
                    }
                }
            }
        }
    }
}
