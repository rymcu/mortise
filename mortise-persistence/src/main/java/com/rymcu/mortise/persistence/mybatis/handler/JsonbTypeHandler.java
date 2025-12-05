package com.rymcu.mortise.persistence.mybatis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A MyBatis TypeHandler to map any Java Object to PostgreSQL's JSONB type.
 * It uses Jackson for serialization and deserialization.
 * Make sure you have the jackson-databind dependency in your pom.xml.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.persistence.mybatis.handler
 */
@MappedTypes(Object.class)
public class JsonbTypeHandler extends BaseTypeHandler<Object> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String JSONB_TYPE = "jsonb";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType(JSONB_TYPE);
        try {
            jsonObject.setValue(MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Failed to serialize object to JSONB", e);
        }
        ps.setObject(i, jsonObject);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return json == null ? null : parseJson(json);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return json == null ? null : parseJson(json);
    }



    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return json == null ? null : parseJson(json);
    }

    private Object parseJson(String json) throws SQLException {
        try {
            // Note: This will deserialize into basic Maps/Lists.
            // For deserialization into specific POJOs, a more advanced handler would be needed.
            // But for general Map<String, Object> usage, this is sufficient.
            return MAPPER.readValue(json, Object.class);
        } catch (Exception e) {
            throw new SQLException("Failed to parse JSONB string", e);
        }
    }
}
