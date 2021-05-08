package com.endersuite.database;

import com.endersuite.database.mysql.ResultHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author TheRealDomm
 * @since 08.05.2021
 */
public interface Database {

    int DEFAULT_UPDATE = -1;

    void connect();

    void disconnect();

    boolean isConnected();

    long execUpdate(String sql, Object... replacements);

    ResultHandler execQuery(String sql, Object... replacements);

    Connection getRawConnection();

    void removeConnection(Connection connection);

}
