package com.endersuite.database.mysql;

import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author TheRealDomm
 * @since 08.05.2021
 */
@ToString
public class Row {

    public static final Boolean DEFAULT_BOOLEAN = false;
    public static final Byte DEFAULT_BYTE = -1;
    public static final Integer DEFAULT_INTEGER = -1;
    public static final Float DEFAULT_FLOAT = -1.0F;
    public static final Double DEFAULT_DOUBLE = -1.0D;
    public static final Long DEFAULT_LONG = -1L;
    public static final String DEFAULT_STRING = "";

    @Getter private Map<String, Object> dataSet = new LinkedHashMap<>();

    public void add(String colName, Object colData) {
        this.dataSet.put(colName, colData);
    }

    public Boolean getBoolean(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return DEFAULT_BOOLEAN;
    }

    public Byte getByte(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Byte) {
            return (Byte) o;
        }
        return DEFAULT_BYTE;
    }

    public Integer getInteger(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Integer) {
            return (Integer) o;
        }
        return DEFAULT_INTEGER;
    }

    public Double getDouble(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Double) {
            return (Double) o;
        }
        return DEFAULT_DOUBLE;
    }

    public Long getLong(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Long) {
            return (Long) o;
        }
        return DEFAULT_LONG;
    }

    public Float getFloat(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Float) {
            return (Float) o;
        }
        return DEFAULT_FLOAT;
    }

    public String getString(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof String) {
            return (String) o;
        }
        return DEFAULT_STRING;
    }

    public UUID getUuid(String name) {
        Object o = this.dataSet.get(name);
        try {
            if (o instanceof String) {
                return UUID.fromString((String) o);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        return null;
    }

    public Timestamp getTimestamp(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Timestamp) {
            return (Timestamp)o;
        }
        return null;
    }

}
