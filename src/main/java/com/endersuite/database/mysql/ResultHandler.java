package com.endersuite.database.mysql;

import com.endersuite.database.exception.BuildResultException;
import com.endersuite.database.exception.InvalidResultException;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TheRealDomm
 * @since 08.05.2021
 */
public class ResultHandler {

    @Getter private final List<Row> rowList = new ArrayList<>();
    @Getter private final Map<String, List<Row>> rowMap = new LinkedHashMap<>();

    protected ResultHandler(ResultSet resultSet) {
        if (resultSet == null) {
            throw new InvalidResultException("Received an invalid result set!");
        }
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Row row = new Row();
                String tableName = resultSetMetaData.getTableName(resultSetMetaData.getColumnCount());
                row.setTableName(tableName);
                for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
                    String colName = resultSetMetaData.getColumnName(i+1);
                    Object colData = resultSet.getObject(colName);
                    row.add(colName, colData);
                }
                this.rowList.add(row);
                List<Row> rows = this.rowMap.containsKey(tableName) ? this.rowMap.get(tableName) : new ArrayList<>();
                rows.add(row);
                this.rowMap.put(tableName, rows);
            }
        } catch (SQLException e) {
            throw new BuildResultException("Error while building result!", e);
        }
    }

    public static ResultHandler getInstance(ResultSet resultSet) {
        return new ResultHandler(resultSet);
    }

}
