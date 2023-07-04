package com.github.AndrewAlbizati.models;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseItem {
    void addToDatabase(Connection conn) throws SQLException;
    void removeFromDatabase(Connection conn) throws SQLException;
}
