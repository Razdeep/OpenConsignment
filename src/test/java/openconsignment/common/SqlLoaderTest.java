package openconsignment.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SqlLoaderTest {

    @Test
    void loadsSqlFromTheClasspath() throws SQLException {
        assertEquals("SELECT * FROM `Bill_Entry_Table`;\n", SqlLoader.load(SqlQuery.BILL_GET_ALL));
    }

    @Test
    void decodesResourcesAsUtf8() throws SQLException {
        assertEquals("SELECT 'café ₹';\n", SqlLoader.readResource("utf8.sql"));
    }

    @Test
    void cachesLoadedSql() throws SQLException {
        assertSame(SqlLoader.load(SqlQuery.BUYER_GET_ALL), SqlLoader.load(SqlQuery.BUYER_GET_ALL));
    }

    @Test
    void missingResourceReportsItsPath() {
        SQLException exception = assertThrows(SQLException.class, () -> SqlLoader.readResource("missing.sql"));

        assertTrue(exception.getMessage().contains("/sql/missing.sql"));
    }

    @ParameterizedTest
    @EnumSource(SqlQuery.class)
    void everyRegisteredQueryHasANonEmptyResource(SqlQuery query) throws SQLException {
        assertFalse(SqlLoader.load(query).isBlank(), query.resourcePath());
    }
}
