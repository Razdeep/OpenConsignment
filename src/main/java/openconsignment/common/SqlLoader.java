package openconsignment.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SqlLoader {

    private static final Map<SqlQuery, String> CACHE = new ConcurrentHashMap<>();

    private SqlLoader() {}

    /** Loads and caches the UTF-8 SQL resource registered for the query. */
    public static String load(SqlQuery query) throws SQLException {
        String cachedSql = CACHE.get(query);
        if (cachedSql != null) {
            return cachedSql;
        }

        String sql = readResource(query.resourcePath());
        String previousSql = CACHE.putIfAbsent(query, sql);
        return previousSql != null ? previousSql : sql;
    }

    static String readResource(String name) throws SQLException {
        String path = "/sql/" + name;
        try (InputStream stream = SqlLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new SQLException("SQL resource not found: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new SQLException("Failed to read SQL resource: " + path, ex);
        }
    }
}
