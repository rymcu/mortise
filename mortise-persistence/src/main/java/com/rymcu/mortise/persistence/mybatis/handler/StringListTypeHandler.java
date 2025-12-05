package com.rymcu.mortise.persistence.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义 TypeHandler，用于在 Java 的 List<String> 和 PostgreSQL 的 TEXT[] 之间进行转换。
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.common.mybatis.handler
 */
public class StringListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        // 将 List<String> 转换为 String[]
        String[] stringArray = parameter.toArray(new String[0]);
        // 获取数据库连接并创建 SQL Array
        Array array = ps.getConnection().createArrayOf("text", stringArray);
        ps.setArray(i, array);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getListFromStringArray(rs.getArray(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getListFromStringArray(rs.getArray(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getListFromStringArray(cs.getArray(columnIndex));
    }

    private List<String> getListFromStringArray(Array sqlArray) throws SQLException {
        if (sqlArray == null) {
            return null;
        }
        try {
            // 将 SQL Array 转换为 Java String[]
            String[] array = (String[]) sqlArray.getArray();
            return Arrays.asList(array);
        } finally {
            sqlArray.free();
        }
    }
}
