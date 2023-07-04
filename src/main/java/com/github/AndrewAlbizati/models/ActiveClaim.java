package com.github.AndrewAlbizati.models;

import com.github.AndrewAlbizati.exceptions.ClaimNotFoundException;
import com.github.AndrewAlbizati.exceptions.InvalidCaseNumberException;

import java.sql.*;


public record ActiveClaim(long claimMessageId,
                          String caseNum,
                          User tech,
                          Timestamp claimTime
) implements Claim {
    public ActiveClaim {
        if (caseNum.length() != 8) {
            throw new InvalidCaseNumberException();
        }
    }

    public ActiveClaim(Connection conn, long claimMessageId, String caseNum, long techId, Timestamp claimTime) {
        this(claimMessageId, caseNum, User.fromId(conn, techId), claimTime);
    }

    public static ActiveClaim fromId(Connection conn, long id) throws ClaimNotFoundException {
        try {
            String sql = "SELECT * FROM ActiveClaims WHERE claim_message_id=? LIMIT 1";

            PreparedStatement preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setLong(1, id);

            ResultSet resultSet = preparedStmt.executeQuery();
            resultSet.next();
            return new ActiveClaim(
                    conn,
                    id,
                    resultSet.getString(2),
                    resultSet.getLong(3),
                    resultSet.getTimestamp(4)
            );
        } catch (SQLException e) {
            throw new ClaimNotFoundException("Claim not found, check id sent");
        }
    }

    public static ActiveClaim fromCaseNum(Connection conn, String caseNum) throws ClaimNotFoundException {
        try {
            String sql = "SELECT * FROM ActiveClaims WHERE case_num=? LIMIT 1";

            PreparedStatement preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setString(1, caseNum);

            ResultSet resultSet = preparedStmt.executeQuery();
            resultSet.next();
            return new ActiveClaim(
                    conn,
                    resultSet.getLong(1),
                    caseNum,
                    resultSet.getLong(3),
                    resultSet.getTimestamp(4)
            );
        } catch (SQLException e) {
            throw new ClaimNotFoundException("Claim not found, check caseNum sent");
        }
    }

    @Override
    public void addToDatabase(Connection conn) throws SQLException {
        String sql = "INSERT INTO ActiveClaims (claim_message_id, case_num, tech_id, claim_time) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setLong(1, claimMessageId);
        preparedStmt.setString(2, caseNum);
        preparedStmt.setLong(3, tech.discordId());
        preparedStmt.setTimestamp(4, claimTime);

        preparedStmt.executeUpdate();
    }

    @Override
    public void removeFromDatabase(Connection conn) throws SQLException {
        String sql = "DELETE FROM ActiveClaims WHERE claim_message_id=?";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setLong(1, claimMessageId);

        preparedStmt.executeUpdate();
    }
}
