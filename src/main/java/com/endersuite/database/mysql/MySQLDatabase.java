package com.endersuite.database.mysql;

import com.endersuite.database.Database;
import com.endersuite.database.configuration.Credentials;
import com.endersuite.database.mysql.builder.QueryBuilder;
import com.endersuite.database.mysql.builder.QueryType;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.Getter;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author TheRealDomm
 * @since 08.05.2021
 */
public class MySQLDatabase implements Database {

    private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
    private static final String URI = "jdbc:mysql://%s:%d/%s?autoReconnect=false&useSSL=%s";
    @Getter private final ListeningExecutorService executorService =
            MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    protected AtomicBoolean connected = new AtomicBoolean(false);
    private final BasicDataSource basicDataSource;
    @Getter private final AtomicBoolean openable = new AtomicBoolean(false);

    private final Set<Connection> connections = new CopyOnWriteArraySet<>();

    public MySQLDatabase(Credentials credentials) {
        this.basicDataSource = new BasicDataSource();
        this.basicDataSource.setDriverClassName(DRIVER_CLASS);
        this.basicDataSource.setUrl(
                String.format(
                        URI,
                        credentials.getHostname(),
                        credentials.getPort(),
                        credentials.getDatabase(),
                        credentials.isUseSSL()
                )
        );
        this.basicDataSource.setUsername(credentials.getUsername());
        this.basicDataSource.setPassword(credentials.getPassword());
        this.openable.set(true);
        if (credentials.isAutoConnect()) {
            this.connect();
        }
    }

    @Override
    public void connect() {
        if (!this.openable.get()) {
            throw new IllegalStateException("The link to the database can not be established currently...!");
        }
        Connection connection;
        this.connected.set((connection = this.establish()) != null);
        if (connection != null) {
            this.close(connection);
        }
    }

    @Override
    public void disconnect() {
        this.openable.set(false);
        this.connected.set(false);
        this.purgeConnections();
    }

    @Override
    public boolean isConnected() {
        if (!this.openable.get()) {
            return false;
        }
        return this.connected.get();
    }

    @Override
    public long execUpdate(String sql, Object... replacements) {
        if (!this.connected.get()) {
            throw new IllegalStateException("Failed to establish connection to the database");
        }
        try {
            Connection connection = this.establish();
            if (connection == null) {
                throw new IllegalStateException("Failed to establish connection to the database");
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < replacements.length; i++) {
                preparedStatement.setObject(i+1, replacements[i]);
            }
            preparedStatement.executeUpdate();
            long updateLong = DEFAULT_UPDATE;
            if (preparedStatement.getGeneratedKeys() != null) {
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                while (resultSet.next()) {
                    updateLong = resultSet.getLong(1);
                }
            }
            this.close(connection);
            return updateLong;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return DEFAULT_UPDATE;
    }

    @Override
    public ResultHandler execQuery(String sql, Object... replacements) {
        if (!this.connected.get()) {
            throw new IllegalStateException("Failed to establish connection to the database");
        }
        try {
            Connection connection = this.establish();
            if (connection == null) {
                throw new IllegalStateException("Failed to establish connection to the database");
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < replacements.length; i++) {
                preparedStatement.setObject(i+1, replacements[i]);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultHandler resultHandler = ResultHandler.getInstance(resultSet);
            this.close(connection);
            return resultHandler;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void asyncUpdate(Consumer<Long> callback, String sql, Object... replacements) {
        executorService.execute(() -> {
            Long update = this.execUpdate(sql, replacements);
            callback.accept(update);
        });
    }

    @Override
    public void asyncQuery(Consumer<ResultHandler> callback, String sql, Object... replacements) {
        executorService.execute(() -> {
            ResultHandler resultHandler = this.execQuery(sql, replacements);
            callback.accept(resultHandler);
        });
    }

    @Override
    public Connection getRawConnection() {
        return this.establish();
    }

    @Override
    public void removeConnection(Connection connection) {
        this.close(connection);
    }

    @Override
    public QueryBuilder select(String tokens, String table) {
        return new QueryBuilder(this, QueryType.SELECT, tokens, table);
    }

    @Override
    public QueryBuilder insert(String table) {
        return new QueryBuilder(this, QueryType.INSERT, table);
    }

    @Override
    public QueryBuilder update(String table) {
        return new QueryBuilder(this, QueryType.UPDATE, table);
    }

    @Override
    public QueryBuilder delete(String table) {
        return new QueryBuilder(this, QueryType.DELETE, table);
    }

    private Connection establish() {
        if (!this.openable.get()) {
            return null;
        }
        try {
            Connection connection = this.basicDataSource.getConnection();
            this.connections.add(connection);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                this.connections.remove(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void purgeConnections() {
        for (Connection connection : this.connections) {
            this.close(connection);
        }
    }

}
