package com.github.AndrewAlbizati.models;

import com.github.AndrewAlbizati.enums.Status;
import com.github.AndrewAlbizati.exceptions.InvalidCaseNumberException;
import com.github.AndrewAlbizati.exceptions.UserNotFoundException;

import java.sql.*;
import java.util.Optional;

public record CheckedClaim(long checkerMessageId,
                           String caseNum,
                           User tech,
                           User lead,
                           Timestamp claimTime,
                           Timestamp completeTime,
                           Timestamp checkTime,
                           Status status,
                           Long pingThreadId
) implements Claim {
    public CheckedClaim {
        if (caseNum.length() != 8) {
            try {
                Integer.parseInt(caseNum);
            } catch (NumberFormatException e) {
                throw new InvalidCaseNumberException();
            }
        }
    }

    public CheckedClaim(Connection conn, long checkerMessageId, String caseNum, long techId, long leadId, Timestamp claimTime, Timestamp completeTime, Timestamp checkTime, Status status, Long pingThreadId) {
        this(checkerMessageId, caseNum, User.fromId(conn, techId).orElseThrow(() -> new UserNotFoundException()), User.fromId(conn, leadId).orElseThrow(() -> new UserNotFoundException()), claimTime, completeTime, checkTime, status, pingThreadId);
    }

    public static Optional<CheckedClaim> fromPingThreadId(Connection conn, long id) {
        try {
            String sql = "SELECT * FROM CheckedClaims WHERE ping_thread_id=? LIMIT 1";

            PreparedStatement preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setLong(1, id);

            ResultSet resultSet = preparedStmt.executeQuery();

            resultSet.next();
            return Optional.of(new CheckedClaim(
                    conn,
                    resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getLong(3),
                    resultSet.getLong(4),
                    resultSet.getTimestamp(5),
                    resultSet.getTimestamp(6),
                    resultSet.getTimestamp(7),
                    Status.fromStr(resultSet.getString(8)),
                    resultSet.getLong(9) == 0 ? null : resultSet.getLong(9)
            ));
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public void changeStatus(Connection conn, Status status) throws SQLException {
        if (status == Status.CHECKED) {
            String sql = "UPDATE CheckedClaims SET ping_thread_id=null WHERE checker_message_id=?";
            PreparedStatement preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setLong(1, checkerMessageId);
            preparedStmt.execute();
        }
        String sql = "UPDATE CheckedClaims SET status=? WHERE checker_message_id=?";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setString(1, status.value());
        preparedStmt.setLong(2, checkerMessageId);
        preparedStmt.execute();
    }

    @Override
    public void addToDatabase(Connection conn) throws SQLException {
        PreparedStatement preparedStmt;
        if (pingThreadId == null) {
            String sql = "INSERT INTO CheckedClaims (checker_message_id, case_num, tech_id, lead_id, claim_time, complete_time, check_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            preparedStmt = conn.prepareStatement(sql);
        } else {
            String sql = "INSERT INTO CheckedClaims (checker_message_id, case_num, tech_id, lead_id, claim_time, complete_time, check_time, status, ping_thread_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            preparedStmt = conn.prepareStatement(sql);
            preparedStmt.setLong(9, pingThreadId);
        }

        preparedStmt.setLong(1, checkerMessageId);
        preparedStmt.setString(2, caseNum);
        preparedStmt.setLong(3, tech.discordId());
        preparedStmt.setLong(4, lead.discordId());
        preparedStmt.setTimestamp(5, claimTime);
        preparedStmt.setTimestamp(6, completeTime);
        preparedStmt.setTimestamp(7, checkTime);
        preparedStmt.setString(8, status.value());

        preparedStmt.executeUpdate();
    }

    @Override
    public void removeFromDatabase(Connection conn) throws SQLException {
        String sql = "DELETE FROM CheckedClaims WHERE checker_message_id=?";
        PreparedStatement preparedStmt = conn.prepareStatement(sql);
        preparedStmt.setLong(1, checkerMessageId);

        preparedStmt.executeUpdate();
    }
}
