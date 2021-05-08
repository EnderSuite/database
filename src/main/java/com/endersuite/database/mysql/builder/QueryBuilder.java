package com.endersuite.database.mysql.builder;

import com.endersuite.database.Database;
import com.endersuite.database.mysql.ResultHandler;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author TheRealDomm
 * @since 08.05.2021
 */
@Getter
public class QueryBuilder {

    private final Database database;
    private final QueryType queryType;
    private final String tokens;
    private final String table;

    private QueryOperator queryOperator = QueryOperator.AND;

    private Map<String, Object> values = new LinkedHashMap<>();
    private Map<String, Object> where = new LinkedHashMap<>();

    public QueryBuilder(Database database, QueryType queryType, String table) {
        if (queryType == QueryType.SELECT) {
            throw new IllegalArgumentException("Please specify the tokens you'd like to select!");
        }
        this.database = database;
        this.queryType = queryType;
        this.tokens = null;
        this.table = table;
    }

    public QueryBuilder(Database database, QueryType queryType, String tokens, String table) {
        this.database = database;
        this.queryType = queryType;
        this.tokens = tokens;
        this.table = table;
    }

    public QueryBuilder addValue(String colName, Object colData) {
        this.values.put(colName, colData);
        return this;
    }

    public QueryBuilder addWhere(String colName, Object colData) {
        this.where.put(colName, colData);
        return this;
    }

    public QueryBuilder setQueryOperator(QueryOperator queryOperator) {
        this.queryOperator = queryOperator;
        return this;
    }

    public void dispatchUpdate() {
        if (this.queryType == QueryType.SELECT) {
            throw new IllegalStateException("Can not execute update with select query!");
        }
        switch (this.queryType) {
            case INSERT:
                this.insert();
                break;
            case UPDATE:
                this.update();
                break;
            case DELETE:
                this.delete();
                break;
        }
    }

    public ResultHandler dispatchQuery() {
        if (this.queryType != QueryType.SELECT) {
            throw new IllegalStateException("Can only execute select queries with this method!");
        }
        String initial = this.queryType.format(this.tokens, this.table);
        Object[] colData = new Object[this.where.size()];
        String keys = "";
        int i = 0;
        for (Map.Entry<String, Object> entry : this.where.entrySet()) {
            colData[i] = entry.getValue();
            keys += entry.getKey() + " " + this.queryOperator.name() + " ";
        }
        String query;
        if (!keys.trim().isEmpty()) {
            query = initial + " " + keys.substring(0, keys.length()-(2+this.queryOperator.name().length()));
        }
        else {
            query = initial;
        }
        try {
            Connection connection = this.database.getRawConnection();
            if (connection == null) {
                throw new IllegalStateException("Could not open connection");
            }
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (i = 0; i < colData.length; i++) {
                preparedStatement.setObject(i+1, colData[i]);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultHandler resultHandler = ResultHandler.getInstance(resultSet);
            this.database.removeConnection(connection);
            return resultHandler;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void insert() {
        String initial = this.queryType.format(this.table);
        Object[] colData = new Object[this.values.size()];
        String names = "";
        String data = "";
        int i = 0;
        for (Map.Entry<String, Object> entry : this.values.entrySet()) {
            colData[i] = entry.getValue();
            names += entry.getKey() + ", ";
            data += "?, ";
            i++;
        }
        if (data.trim().isEmpty() || names.trim().isEmpty()) {
            throw new IllegalArgumentException("Can not build query from no arguments!");
        }
        names = names.substring(0, names.length()-2);
        data = data.substring(0, data.length()-2);
        String query = initial + "(" + names + ") VALUES (" + data + ")";
        this.execute(colData, query);
    }

    private void update() {
        String initial = this.queryType.format(this.table);
        Object[] colData = new Object[this.values.size()+this.where.size()];
        String keys = "";
        int i = 0;
        for (Map.Entry<String, Object> entry : this.values.entrySet()) {
            colData[i] = entry.getValue();
            keys += entry.getKey() + "=?, ";
            i++;
        }
        if (keys.trim().isEmpty()) {
            throw new IllegalArgumentException("Can not build query from no arguments!");
        }
        keys = keys.substring(0, keys.length()-2);
        String where = "";
        if (!this.where.isEmpty()) {
            where = " WHERE ";
            for (Map.Entry<String, Object> entry : this.where.entrySet()) {
                colData[i] = entry.getValue();
                where += entry.getKey() + " " + this.queryOperator.name() + " ";
            }
            where = where.substring(0, where.length()-(2+this.queryType.name().length()));
        }
        String query = initial + keys + " " + where;
        this.execute(colData, query);
    }

    private void delete() {
        String initial = this.queryType.format(this.table);
        Object[] colData = new Object[this.where.size()];
        String keys = "";
        int i = 0;
        for (Map.Entry<String, Object> entry : this.where.entrySet()) {
            colData[i] = entry.getValue();
            keys += entry.getKey() + ", ";
            i++;
        }
        if (keys.trim().isEmpty()) {
            throw new IllegalArgumentException("Can not build query from no arguments!");
        }
        keys = keys.substring(0, keys.length()-2);
        String query = initial + "WHERE " + keys;
        this.execute(colData, query);
    }

    private void execute(Object[] colData, String query) {
        try {
            Connection connection = this.database.getRawConnection();
            if (connection == null) {
                throw new IllegalStateException("Could not open connection");
            }
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < colData.length; i++) {
                preparedStatement.setObject(i+1, colData[i]);
            }
            preparedStatement.executeUpdate();
            this.database.removeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
