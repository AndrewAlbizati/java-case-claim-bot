package com.github.AndrewAlbizati.models;

import com.github.AndrewAlbizati.exceptions.CheckerMessageNotFoundException;
import com.github.AndrewAlbizati.exceptions.InvalidCaseNumberException;

import java.sql.*;

public record CompletedClaim(long checkerMessageId,
                             String caseNum,
                             User tech,
                             Timestamp claimTime,
                             Timestamp completeTime
) implements Claim {
    public CompletedClaim {
        if (caseNum.length() != 8) {
            throw new InvalidCaseNumberException();
        }
    }

    public CompletedClaim(Connection conn, long claimMessageId, String caseNum, long techId, Timestamp claimTime, Timestamp completeTime) {
        this(claimMessageId, caseNum, User.fromId(conn, techId), claimTime, completeTime);
    }

    public static CompletedClaim fromId(Connection conn, long id) throws CheckerMessageNotFoundException {
        try {
            String sql = "SELECT * FROM CompletedClaims WHERE checker_message_id=? LIMIT 1";

            PreparedStatement preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setLong(1, id);

            ResultSet resultSet = preparedStmt.executeQuery();

            resultSet.next();
            return new CompletedClaim(
                    conn,
                    id,
                    resultSet.getString(2),
                    resultSet.getLong(3),
                    resultSet.getTimestamp(4),
                    resultSet.getTimestamp(5)
            );
        } catch (SQLException e) {
            throw new CheckerMessageNotFoundException("Checker message not found, check id sent");
        }
    }

    @Override
    public void addToDatabase(Connection conn) throws SQLException {
        String sql = "INSERT INTO CompletedClaims (checker_message_id, case_num, tech_id, claim_time, complete_time) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setLong(1, checkerMessageId);
        preparedStmt.setString(2, caseNum);
        preparedStmt.setLong(3, tech.discordId());
        preparedStmt.setTimestamp(4, claimTime);
        preparedStmt.setTimestamp(5, completeTime);

        preparedStmt.executeUpdate();
    }

    @Override
    public void removeFromDatabase(Connection conn) throws SQLException {
        String sql = "DELETE FROM CompletedClaims WHERE checker_message_id=?";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setLong(1, checkerMessageId);

        preparedStmt.executeUpdate();
    }
}
