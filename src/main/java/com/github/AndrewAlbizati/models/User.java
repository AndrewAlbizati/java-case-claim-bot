package com.github.AndrewAlbizati.models;

import com.github.AndrewAlbizati.enums.PrivilegeLevel;

import java.sql.*;
import java.util.Optional;

public record User(long discordId, String firstName, String lastName, String email, Date birthday, Date joinDate, boolean active, PrivilegeLevel privilegeLevel) implements DatabaseItem {
    public static Optional<User> fromId(Connection conn, long id) {
        try {
            String sql = "SELECT * FROM Users WHERE discord_id = ? LIMIT 1";
            PreparedStatement preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setLong(1, id);
            ResultSet resultSet = preparedStmt.executeQuery();

            resultSet.next();
            return Optional.of(new User(
                    id,
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getDate(5),
                    resultSet.getDate(6),
                    resultSet.getBoolean(7),
                    PrivilegeLevel.fromInt(resultSet.getInt(8))
            ));
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public boolean activate(Connection conn) {
        try {
            String sql = "UPDATE Users SET active = ? WHERE discord_id = ?";
            PreparedStatement preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setBoolean(1, true);
            preparedStmt.setLong(2, discordId);

            preparedStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deactivate(Connection conn) {
        try {
            String sql = "UPDATE Users SET active = ? WHERE discord_id = ?";
            PreparedStatement preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setBoolean(1, false);
            preparedStmt.setLong(2, discordId);

            preparedStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void addToDatabase(Connection conn) throws SQLException {
        String sql = "INSERT INTO Users (discord_id, first_name, last_name, email, birthday, join_date, active, privilege_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setLong(1, discordId);
        preparedStmt.setString(2, firstName);
        preparedStmt.setString(3, lastName);
        preparedStmt.setString(4, email);
        preparedStmt.setDate(5, birthday);
        preparedStmt.setDate(6, joinDate);
        preparedStmt.setBoolean(7, active);
        preparedStmt.setInt(8, privilegeLevel.getValue());

        preparedStmt.executeUpdate();
    }

    @Override
    public void removeFromDatabase(Connection conn) throws SQLException {
        String sql = "DELETE FROM Users WHERE discord_id=?";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setLong(1, discordId);

        preparedStmt.executeUpdate();
    }
}
