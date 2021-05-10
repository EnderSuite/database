package com.endersuite.database.mysql;

import com.google.common.primitives.Primitives;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author TheRealDomm
 * @since 08.05.2021
 */
@ToString
public class Row implements Serializable {

    @Getter private final Map<String, Object> dataSet = new LinkedHashMap<>();
    @Getter @Setter private String tableName;

    public void add(String colName, Object colData) {
        this.dataSet.put(colName, colData);
        this.tableName = tableName;
    }

    public Boolean getBoolean(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Boolean)
            return (Boolean) o;

        return null;
    }

    public Boolean getBoolean(String name, Boolean def) {
        Object o = this.dataSet.get(name);
        if (o instanceof Boolean)
            return (Boolean) o;

        return def;
    }

    public Byte getByte(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Byte)
            return (Byte) o;

        return null;
    }

    public Byte getByte(String name, Byte def) {
        Object o = this.dataSet.get(name);
        if (o instanceof Byte)
            return (Byte) o;

        return def;
    }

    public Integer getInteger(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Integer)
            return (Integer) o;

        return null;
    }

    public Integer getInteger(String name, Integer def) {
        Object o = this.dataSet.get(name);
        if (o instanceof Integer)
            return (Integer) o;

        return def;
    }

    public Double getDouble(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Double)
            return (Double) o;

        return null;
    }

    public Double getDouble(String name, Double def) {
        Object o = this.dataSet.get(name);
        if (o instanceof Double)
            return (Double) o;

        return def;
    }

    public Long getLong(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Long)
            return (Long) o;

        return null;
    }

    public Long getLong(String name, Long def) {
        Object o = this.dataSet.get(name);
        if (o instanceof Long)
            return (Long) o;

        return def;
    }

    public Float getFloat(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Float)
            return (Float) o;

        return null;
    }

    public Float getFloat(String name, Float def) {
        Object o = this.dataSet.get(name);
        if (o instanceof Float)
            return (Float) o;

        return def;
    }

    public String getString(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof String)
            return (String) o;

        return null;
    }

    public String getString(String name, String def) {
        Object o = this.dataSet.get(name);
        if (o instanceof String)
            return (String) o;

        return def;
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

    public UUID getUuid(String name, UUID def) {
        Object o = this.dataSet.get(name);
        try {
            if (o instanceof String) {
                return UUID.fromString((String) o);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        return def;
    }

    public Timestamp getTimestamp(String name) {
        Object o = this.dataSet.get(name);
        if (o instanceof Timestamp)
            return (Timestamp) o;

        return null;
    }

    public Timestamp getTimestamp(String name, Timestamp def) {
        Object o = this.dataSet.get(name);
        if (o instanceof Timestamp)
            return (Timestamp) o;

        return def;
    }

    public <T> T get(String name, Class<? extends T> clazz) {
        Object o = this.dataSet.get(name);
        if (o != null)
            return Primitives.wrap(clazz).cast(o);

        return null;
    }

    public <T> T get(String name, Class<? extends T> clazz, T def) {
        Object o = this.dataSet.get(name);
        if (o != null)
            return Primitives.wrap(clazz).cast(o);

        return Primitives.wrap(clazz).cast(def);
    }

}
