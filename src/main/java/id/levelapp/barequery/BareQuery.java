package id.levelapp.barequery;

import java.sql.*;
import java.util.*;
import java.util.regex.*;

public class BareQuery {
    private final String template;
    private static final String PLACEHOLDER_REGEX = "\\[(\\w+) \\| (.+?)\\]";

    public BareQuery(String template) {
        this.template = template;
    }

    public List<Map<String, Object>> execute(Connection connection, Map<String, Object> params) throws SQLException {
        String renderedQuery = renderQuery(params);
        try (PreparedStatement stmt = connection.prepareStatement(renderedQuery)) {
            bindParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

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

    private List<String> extractParameterKeys() {
        Matcher matcher = Pattern.compile(":(\\w+)").matcher(template);
        List<String> keys = new ArrayList<>();
        while (matcher.find()) keys.add(matcher.group(1));
        return keys;
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
