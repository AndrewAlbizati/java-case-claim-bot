package com.github.AndrewAlbizati;

import com.github.AndrewAlbizati.exceptions.IncompleteConfigException;

import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Contains Main function that is run on start.
 */
public class Main {
    /**
     * Creates config.json if not already created.
     * @param args N/A
     */
    public static void main(String[] args) {
        // Check if token.txt is present, creates new files if absent
        try {
            File tokenFile = new File("config.properties");
            if (tokenFile.createNewFile()) {
                System.out.println("config.properties has been created");
                throw new IncompleteConfigException();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            FileInputStream fis = new FileInputStream("config.properties");
            Properties prop = new Properties();
            prop.load(fis);

            String token = prop.getProperty("token");

            Color primaryEmbedColor = Color.decode(prop.getProperty("primaryEmbedColor"));
            Color successEmbedColor = Color.decode(prop.getProperty("successEmbedColor"));
            Color dangerEmbedColor = Color.decode(prop.getProperty("dangerEmbedColor"));

            long claimChannelId = Long.parseLong(prop.getProperty("claimChannelId"));
            long checkChannelId = Long.parseLong(prop.getProperty("checkChannelId"));

            String host = prop.getProperty("host");
            int port = Integer.parseInt(prop.getProperty("port"));
            String databaseName = prop.getProperty("databaseName");
            String username = prop.getProperty("username");
            String password = prop.getProperty("password");

            // Connect to database
            String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connection = DriverManager.getConnection(url, username, password);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }));

            // Create and run bot
            Bot bot = new Bot(token, primaryEmbedColor, successEmbedColor, dangerEmbedColor, claimChannelId, checkChannelId, connection);
            bot.run();
        } catch (IOException e) {
            throw new IncompleteConfigException();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}