package com.endersuite.database.mysql.builder;

import com.endersuite.database.Database;
import com.endersuite.database.mysql.ResultHandler;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

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

    private final Map<String, Object> values = new LinkedHashMap<>();
    private final Map<String, Object> where = new LinkedHashMap<>();

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

    public QueryBuilder value(String colName, Object colData) {
        this.values.put(colName, colData);
        return this;
    }

    public QueryBuilder where(String colName, Object colData) {
        this.where.put(colName, colData);
        return this;
    }

    public QueryBuilder setQueryOperator(QueryOperator queryOperator) {
        this.queryOperator = queryOperator;
        return this;
    }

    public void dispatchAsyncUpdate() {
        this.dispatchAsyncUpdate((ignored) -> {});
    }

    public void dispatchAsyncUpdate(Consumer<Boolean> callback) {
        this.database.getExecutorService().execute(() -> {
            boolean success = this.dispatchUpdate();
            callback.accept(success);
        });
    }

    public void dispatchAsyncQuery(Consumer<ResultHandler> callback) {
        this.database.getExecutorService().execute(() -> {
            ResultHandler resultHandler = this.dispatchQuery();
            callback.accept(resultHandler);
        });
    }

    public boolean dispatchUpdate() {
        if (this.queryType == QueryType.SELECT) {
            throw new IllegalStateException("Can not execute update with select query!");
        }
        switch (this.queryType) {
            case INSERT:
                return this.insert();
            case UPDATE:
                return this.update();
            case DELETE:
                return this.delete();
        }
        return false;
    }

    public ResultHandler dispatchQuery() {
        if (this.queryType != QueryType.SELECT) {
            throw new IllegalStateException("Can only execute select queries with this method!");
        }
        String initial = this.queryType.format(this.tokens, this.table);
        Object[] colData = new Object[this.where.size()];
        StringBuilder keys = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> entry : this.where.entrySet()) {
            colData[i] = entry.getValue();
            keys.append(entry.getKey()).append("=? ").append(this.queryOperator.name()).append(" ");
        }
        String query;
        if (!keys.toString().trim().isEmpty()) {
            query = initial + "WHERE " + keys.substring(0, keys.length()-(2+this.queryOperator.name().length()));
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

    private boolean insert() {
        String initial = this.queryType.format(this.table);
        Object[] colData = new Object[this.values.size()];
        StringBuilder names = new StringBuilder();
        StringBuilder data = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> entry : this.values.entrySet()) {
            colData[i] = entry.getValue();
            names.append(entry.getKey()).append(", ");
            data.append("?, ");
            i++;
        }
        if (data.toString().trim().isEmpty() || names.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Can not build query from no arguments!");
        }
        names = new StringBuilder(names.substring(0, names.length() - 2));
        data = new StringBuilder(data.substring(0, data.length() - 2));
        String query = initial + "(" + names + ") VALUES (" + data + ")";
        return this.execute(colData, query);
    }

    private boolean update() {
        String initial = this.queryType.format(this.table);
        Object[] colData = new Object[this.values.size()+this.where.size()];
        StringBuilder keys = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> entry : this.values.entrySet()) {
            colData[i] = entry.getValue();
            keys.append(entry.getKey()).append("=?, ");
            i++;
        }
        if (keys.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Can not build query from no arguments!");
        }
        keys = new StringBuilder(keys.substring(0, keys.length() - 2));
        StringBuilder where = new StringBuilder();
        if (!this.where.isEmpty()) {
            where = new StringBuilder(" WHERE ");
            for (Map.Entry<String, Object> entry : this.where.entrySet()) {
                colData[i] = entry.getValue();
                where.append(entry.getKey()).append("=? ").append(this.queryOperator.name()).append(" ");
            }
            where = new StringBuilder(where.substring(0, where.length()-(2+this.queryOperator.name().length())));
        }
        String query = initial + keys + where;
        return this.execute(colData, query);
    }

    private boolean delete() {
        String initial = this.queryType.format(this.table);
        Object[] colData = new Object[this.where.size()];
        StringBuilder keys = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> entry : this.where.entrySet()) {
            colData[i] = entry.getValue();
            keys.append(entry.getKey()).append("=?, ");
            i++;
        }
        if (keys.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Can not build query from no arguments!");
        }
        keys = new StringBuilder(keys.substring(0, keys.length() - 2));
        String query = initial + "WHERE " + keys;
        return this.execute(colData, query);
    }

    private boolean execute(Object[] colData, String query) {
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
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
