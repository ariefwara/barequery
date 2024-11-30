package id.levelapp.barequery;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class BareOperation {
    private final String table;

    public BareOperation(String table) {
        this.table = table;
    }

    /**
     * Performs an INSERT operation.
     *
     * @param connection The database connection.
     * @param values     A map of column names to values.
     * @return The number of rows inserted.
     * @throws SQLException If an error occurs during the operation.
     */
    public int insert(Connection connection, Map<String, Object> values) throws SQLException {
        String columns = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(key -> "?").collect(Collectors.joining(", "));
        String query = String.format("INSERT INTO %s (%s) VALUES (%s)", table, columns, placeholders);

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            bindParameters(stmt, values);
            return stmt.executeUpdate();
        }
    }

    /**
     * Performs an UPDATE operation.
     *
     * @param connection The database connection.
     * @param values     A map of column names to updated values.
     * @param conditions A map of conditions for the WHERE clause.
     * @return The number of rows updated.
     * @throws SQLException If an error occurs during the operation.
     */
    public int update(Connection connection, Map<String, Object> values, Map<String, Object> conditions) throws SQLException {
        String setClause = values.keySet().stream().map(key -> key + " = ?").collect(Collectors.joining(", "));
        String whereClause = conditions.keySet().stream().map(key -> key + " = ?").collect(Collectors.joining(" AND "));
        String query = String.format("UPDATE %s SET %s WHERE %s", table, setClause, whereClause);

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            bindParameters(stmt, values);
            bindParameters(stmt, conditions, values.size());
            return stmt.executeUpdate();
        }
    }

    /**
     * Performs a DELETE operation.
     *
     * @param connection The database connection.
     * @param conditions A map of conditions for the WHERE clause.
     * @return The number of rows deleted.
     * @throws SQLException If an error occurs during the operation.
     */
    public int delete(Connection connection, Map<String, Object> conditions) throws SQLException {
        String whereClause = conditions.keySet().stream().map(key -> key + " = ?").collect(Collectors.joining(" AND "));
        String query = String.format("DELETE FROM %s WHERE %s", table, whereClause);

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            bindParameters(stmt, conditions);
            return stmt.executeUpdate();
        }
    }

    /**
     * Performs a SELECT operation.
     *
     * @param connection The database connection.
     * @param columns    A list of column names to select.
     * @param conditions A map of conditions for the WHERE clause.
     * @return A list of maps, where each map represents a row in the result set.
     * @throws SQLException If an error occurs during the operation.
     */
    public List<Map<String, Object>> select(Connection connection, List<String> columns, Map<String, Object> conditions) throws SQLException {
        String columnList = String.join(", ", columns.isEmpty() ? List.of("*") : columns);
        String whereClause = conditions.keySet().stream().map(key -> key + " = ?").collect(Collectors.joining(" AND "));
        String query = String.format("SELECT %s FROM %s WHERE %s", columnList, table, whereClause);

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            bindParameters(stmt, conditions);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    /**
     * Binds parameters to a PreparedStatement.
     *
     * @param stmt   The prepared statement.
     * @param params A map of parameters to bind.
     * @throws SQLException If an error occurs during parameter binding.
     */
    private void bindParameters(PreparedStatement stmt, Map<String, Object> params) throws SQLException {
        bindParameters(stmt, params, 0);
    }

    private void bindParameters(PreparedStatement stmt, Map<String, Object> params, int offset) throws SQLException {
        int index = 1 + offset;
        for (Object value : params.values()) {
            if (value == null) stmt.setNull(index++, Types.NULL);
            else if (value instanceof String) stmt.setString(index++, (String) value);
            else if (value instanceof Integer) stmt.setInt(index++, (Integer) value);
            else if (value instanceof Long) stmt.setLong(index++, (Long) value);
            else if (value instanceof Double) stmt.setDouble(index++, (Double) value);
            else if (value instanceof Float) stmt.setFloat(index++, (Float) value);
            else if (value instanceof Boolean) stmt.setBoolean(index++, (Boolean) value);
            else if (value instanceof Byte) stmt.setByte(index++, (Byte) value);
            else if (value instanceof Short) stmt.setShort(index++, (Short) value);
            else if (value instanceof java.util.Date) stmt.setTimestamp(index++, new Timestamp(((java.util.Date) value).getTime()));
            else if (value instanceof java.sql.Date) stmt.setDate(index++, (java.sql.Date) value);
            else if (value instanceof Time) stmt.setTime(index++, (Time) value);
            else if (value instanceof Timestamp) stmt.setTimestamp(index++, (Timestamp) value);
            else if (value instanceof byte[]) stmt.setBytes(index++, (byte[]) value);
            else throw new SQLException("Unsupported parameter type: " + value.getClass());
        }
    }

    private List<Map<String, Object>> mapResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            results.add(row);
        }
        return results;
    }
}
