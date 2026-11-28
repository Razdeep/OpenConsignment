package openconsignment.repository;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class WriteTransactionTest {
    @Test
    void runtimeFailureRollsBackAndRestoresAutoCommit() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getAutoCommit()).thenReturn(true);
        RuntimeException failure = new IllegalStateException("bad item");

        assertSame(
                failure,
                assertThrows(
                        RuntimeException.class,
                        () -> WriteTransaction.run(connection, () -> {
                            throw failure;
                        })));
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection, never()).commit();
    }

    @Test
    void rollbackFailureIsSuppressedWithoutEnablingAutoCommit() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getAutoCommit()).thenReturn(true);
        SQLException failure = new SQLException("insert failed");
        SQLException rollbackFailure = new SQLException("rollback failed");
        doThrow(rollbackFailure).when(connection).rollback();

        assertSame(
                failure,
                assertThrows(
                        SQLException.class,
                        () -> WriteTransaction.run(connection, () -> {
                            throw failure;
                        })));
        assertSame(rollbackFailure, failure.getSuppressed()[0]);
        verify(connection, never()).setAutoCommit(true);
    }

    @Test
    void doesNotEnableAutoCommitWhenItWasAlreadyDisabled() throws SQLException {
        Connection connection = mock(Connection.class);
        WriteTransaction.run(connection, () -> {});
        verify(connection).commit();
        verify(connection, never()).setAutoCommit(true);
    }
}
