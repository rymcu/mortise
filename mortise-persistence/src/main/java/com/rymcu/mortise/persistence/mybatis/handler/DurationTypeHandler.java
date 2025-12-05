package com.rymcu.mortise.persistence.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import org.postgresql.util.PGInterval;

/**
 * Created on 2025/10/29 15:27.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.persistence.mybatis.handler
 */
@MappedTypes(Duration.class)
public class DurationTypeHandler extends BaseTypeHandler<Duration> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Duration parameter, JdbcType jdbcType) throws SQLException {
        PGInterval interval = new PGInterval();
        interval.setSeconds(parameter.getSeconds());
        // PGInterval uses double for seconds, so fractional seconds go there.
        // We add nanoseconds as a fractional part of seconds.
        double fractionalSeconds = parameter.getNano() / 1_000_000_000.0;
        interval.setSeconds(interval.getSeconds() + fractionalSeconds);
        ps.setObject(i, interval);
    }

    @Override
    public Duration getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        return toDuration(obj);
    }

    @Override
    public Duration getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        return toDuration(obj);
    }

    @Override
    public Duration getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        return toDuration(obj);
    }

    private Duration toDuration(Object obj) throws SQLException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof PGInterval) {
            PGInterval interval = (PGInterval) obj;
            long daysAsSeconds = (long) (interval.getDays() * 24 * 60 * 60);
            long hoursAsSeconds = (long) (interval.getHours() * 60 * 60);
            long minutesAsSeconds = (long) (interval.getMinutes() * 60);
            long wholeSeconds = (long) interval.getSeconds();

            // Extract nanoseconds from the fractional part of seconds
            int nanos = (int) ((interval.getSeconds() - wholeSeconds) * 1_000_000_000);

            long totalSeconds = daysAsSeconds + hoursAsSeconds + minutesAsSeconds + wholeSeconds;

            return Duration.ofSeconds(totalSeconds, nanos);
        }
        throw new SQLException("Expected a PGInterval, but got " + obj.getClass().getName());
    }
}
