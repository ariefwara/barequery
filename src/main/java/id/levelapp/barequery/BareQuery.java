package id.levelapp.barequery;

import java.sql.*;
import java.util.*;
import java.util.regex.*;

/**
 * BareQuery is a lightweight SQL templating framework for dynamic and parameterized query generation.
 */
public class BareQuery {
    private final String template;
    private static final String PLACEHOLDER_REGEX = "\\[(\\w+) \\| (.+?)\\]";

    /**
     * Constructs a BareQuery with the specified SQL template.
     *
     * @param template the SQL template containing placeholders for dynamic SQL blocks and parameters.
     */
    public BareQuery(String template) {
        this.template = template;
    }

    /**
     * Executes the SQL query with the provided database connection and parameters.
     *
     * @param connection the database connection to use for executing the query.
     * @param params     a map of parameter names to their corresponding values.
     * @return a list of maps, where each map represents a row in the result set with column names as keys.
     * @throws SQLException if a database access error occurs or the SQL statement is invalid.
     */
    public List<Map<String, Object>> execute(Connection connection, Map<String, Object> params) throws SQLException {
        String renderedQuery = renderQuery(params);
        try (PreparedStatement stmt = connection.prepareStatement(renderedQuery)) {
            bindParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    /**
     * Renders the SQL query by replacing placeholders with actual SQL blocks based on the provided parameters.
     *
     * @param params a map of parameter names to their corresponding values.
     * @return the rendered SQL query as a string.
     */
    private String renderQuery(Map<String, Object> params) {
        Matcher matcher = Pattern.compile(PLACEHOLDER_REGEX).matcher(template);
        StringBuffer rendered = new StringBuffer();
        while (matcher.find()) {
            String sqlBlock = params.getOrDefault(matcher.group(1), null) != null ? matcher.group(2) : "";
            matcher.appendReplacement(rendered, sqlBlock);
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }

    /**
     * Binds the parameters to the prepared statement.
     *
     * @param stmt   the prepared statement to bind parameters to.
     * @param params a map of parameter names to their corresponding values.
     * @throws SQLException if a database access error occurs or an unsupported parameter type is encountered.
     */
    private void bindParameters(PreparedStatement stmt, Map<String, Object> params) throws SQLException {
        int index = 1;
        for (String key : extractParameterKeys()) {
            Object value = params.getOrDefault(key, null);
            if (value == null) stmt.setNull(index++, Types.NULL);
            if (value instanceof Integer) stmt.setInt(index++, (Integer) value);
            if (value instanceof String) stmt.setString(index++, (String) value);
            if (value instanceof Double) stmt.setDouble(index++, (Double) value);
            throw new SQLException("Unsupported type for parameter: " + key);
        }
    }

    /**
     * Extracts the parameter keys from the SQL template.
     *
     * @return a list of parameter keys found in the template.
     */
    private List<String> extractParameterKeys() {
        Matcher matcher = Pattern.compile(":(\\w+)").matcher(template);
        List<String> keys = new ArrayList<>();
        while (matcher.find()) keys.add(matcher.group(1));
        return keys;
    }

    /**
     * Maps the result set to a list of maps, where each map represents a row with column names as keys.
     *
     * @param rs the result set to map.
     * @return a list of maps representing the result set.
     * @throws SQLException if a database access error occurs.
     */
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
