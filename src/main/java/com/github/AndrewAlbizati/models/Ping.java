package com.github.AndrewAlbizati.models;

import com.github.AndrewAlbizati.exceptions.PingNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public record Ping(long threadId, long messageId, String severity, String description) implements DatabaseItem {
    public static Optional<Ping> fromThreadId(Connection conn, long id) throws PingNotFoundException {
        try {
            String sql = "SELECT * FROM Pings WHERE thread_id=? LIMIT 1";

            PreparedStatement preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setLong(1, id);

            ResultSet resultSet = preparedStmt.executeQuery();
            resultSet.next();
            return Optional.of(new Ping(
                    id,
                    resultSet.getLong(2),
                    resultSet.getString(3),
                    resultSet.getString(4)
            ));
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public static Optional<Ping> fromMessageId(Connection conn, long id) throws PingNotFoundException {
        try {
            String sql = "SELECT * FROM Pings WHERE message_id=? LIMIT 1";

            PreparedStatement preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setLong(1, id);

            ResultSet resultSet = preparedStmt.executeQuery();
            resultSet.next();
            return Optional.of(new Ping(
                    resultSet.getLong(1),
                    id,
                    resultSet.getString(3),
                    resultSet.getString(4)
            ));
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addToDatabase(Connection conn) throws SQLException {
        String sql = "INSERT INTO Pings (thread_id, message_id, severity, description) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setLong(1, threadId);
        preparedStmt.setLong(2, messageId);
        preparedStmt.setString(3, severity);
        preparedStmt.setString(4, description);

        preparedStmt.executeUpdate();
    }

    @Override
    public void removeFromDatabase(Connection conn) throws SQLException {
        String sql = "DELETE FROM Pings WHERE thread_id=?";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setLong(1, threadId);

        preparedStmt.executeUpdate();
    }
}
