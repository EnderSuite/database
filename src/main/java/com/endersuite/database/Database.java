package com.endersuite.database;

import com.endersuite.database.mysql.ResultHandler;
import com.endersuite.database.mysql.builder.QueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author TheRealDomm
 * @since 08.05.2021
 */
public interface Database {

    int DEFAULT_UPDATE = -1;

    void connect();

    void disconnect();

    boolean isConnected();

    AtomicBoolean getOpenable();

    long execUpdate(String sql, Object... replacements);

    ResultHandler execQuery(String sql, Object... replacements);

    Connection getRawConnection();

    void removeConnection(Connection connection);

    QueryBuilder select(String tokens, String table);

    QueryBuilder insert(String table);

    QueryBuilder update(String table);

    QueryBuilder delete(String table);

}
