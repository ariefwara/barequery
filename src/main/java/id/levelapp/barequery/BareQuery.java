package id.levelapp.barequery;

import java.sql.*;
import java.util.*;
import java.util.regex.*;

/**
 * BareQuery is a lightweight SQL templating framework for dynamic and parameterized query generation.
 * It allows for secure execution of SQL queries with dynamic blocks and parameter binding.
 */
public class BareQuery {
    private final String template;
    private static final String PLACEHOLDER_REGEX = "\\[(\\w+) \\| (.+?)\\]";

    /**
     * Constructs a BareQuery with the specified SQL template.
     *
     * @param template the SQL template containing dynamic blocks and placeholders
     */
    public BareQuery(String template) {
        this.template = template;
    }

    /**
     * Executes the SQL query using the provided database connection and parameters.
     *
     * @param connection the database connection to use for executing the query
     * @param params     a map of parameters to bind to the query
     * @return a list of maps, where each map represents a row of the result set
     * @throws SQLException if a database access error occurs or the SQL statement is invalid
     */
    public List<Map<String, Object>> execute(Connection connection, Map<String, Object> params) throws SQLException {
        String query = renderQuery(params);
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            bindParameters(stmt, params);
            return mapResultSet(stmt.executeQuery());
        }
    }

    /**
     * Renders the SQL query by replacing dynamic blocks with actual SQL fragments based on the parameters.
     *
     * @param params a map of parameters to determine which SQL fragments to include
     * @return the rendered SQL query as a string
     */
    private String renderQuery(Map<String, Object> params) {
        Matcher matcher = Pattern.compile(PLACEHOLDER_REGEX).matcher(template);
        StringBuffer rendered = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(rendered, params.getOrDefault(matcher.group(1), null) != null ? matcher.group(2) : "");
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }

    /**
     * Binds the parameters to the prepared statement.
     *
     * @param stmt   the prepared statement to bind parameters to
     * @param params a map of parameters to bind
     * @throws SQLException if a database access error occurs or the parameter type is unsupported
     */
    private void bindParameters(PreparedStatement stmt, Map<String, Object> params) throws SQLException {
        int index = 1;
        for (String key : extractKeys(":(\\w+)")) {
            Object value = params.getOrDefault(key, null);
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
            else throw new SQLException("Unsupported parameter type for key: " + key);
        }
    }

    /**
     * Extracts keys from the template based on the provided regular expression.
     *
     * @param regex the regular expression to match keys in the template
     * @return a list of keys extracted from the template
     */
    private List<String> extractKeys(String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(template);
        List<String> keys = new ArrayList<>();
        while (matcher.find()) keys.add(matcher.group(1));
        return keys;
    }

    /**
     * Maps the result set to a list of maps, where each map represents a row.
     *
     * @param rs the result set to map
     * @return a list of maps representing the result set
     * @throws SQLException if a database access error occurs
     */
    private List<Map<String, Object>> mapResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            results.add(row);
        }
        return results;
    }
}
