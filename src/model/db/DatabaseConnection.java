package src.model.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import src.view.ConsoleIO;

/**
 * Gestisce la connessione al database MySQL.
 * Fornisce un metodo statico per ottenere una connessione al database.
 * 
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/gestione_visite?useSSL=false";
    
    private static final String USER = "root"; 
    
    private static final String PASSWORD = ""; 

    private static ConsoleIO consoleIO = new ConsoleIO();

    /**
     * Crea e restituisce una connessione al database MySQL.
     * 
     * @return un oggetto Connection se la connessione ha successo, null altrimenti
     */
    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            consoleIO.mostraMessaggio("Errore di connessione al database: " + e.getMessage());
            return null;
        }
    }

}
