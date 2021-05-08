package com.endersuite.database.mysql;

import com.endersuite.database.exception.BuildResultException;
import com.endersuite.database.exception.InvalidResultException;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TheRealDomm
 * @since 08.05.2021
 */
public class ResultHandler {

    @Getter
    private List<Row> rowList = new ArrayList<>();

    protected ResultHandler(ResultSet resultSet) {
        if (resultSet == null) {
            throw new InvalidResultException("Received an invalid result set!");
        }
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Row row = new Row();
                for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
                    String colName = resultSetMetaData.getColumnName(i+1);
                    Object colData = resultSet.getObject(colName);
                    row.add(colName, colData);
                }
                this.rowList.add(row);
            }
        } catch (SQLException e) {
            throw new BuildResultException("Error while building result!", e);
        }
    }

    public static ResultHandler getInstance(ResultSet resultSet) {
        return new ResultHandler(resultSet);
    }

}
