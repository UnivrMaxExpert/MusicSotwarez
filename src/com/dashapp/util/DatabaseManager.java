package com.dashapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/musicasoftware";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Lascia vuoto se non hai impostato una password in XAMPP

    private static Connection connection;

    /**
     * Restituisce una connessione al database MySQL.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Carica il driver JDBC
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connessione al database riuscita.");
            } catch (ClassNotFoundException e) {
                System.err.println("Driver MySQL non trovato.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Errore di connessione al database.");
                e.printStackTrace();
                throw e;
            }
        }
        return connection;
    }

    /**
     * Chiude la connessione, se aperta.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connessione al database chiusa.");
            } catch (SQLException e) {
                System.err.println("Errore nella chiusura della connessione.");
                e.printStackTrace();
            }
        }
    }
}
