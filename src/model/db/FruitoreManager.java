package src.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import src.controller.ThreadPoolController;
import src.model.Fruitore;

/**
 * Gestisce le operazioni CRUD per i fruitori nel database.
 * Estende DatabaseManager per utilizzare funzionalità comuni di gestione del database
 * e thread pool per operazioni asincrone.
 *  
 */
public class FruitoreManager extends DatabaseManager {

    /** Mappa concorrente dei fruitori indicizzata per email */
    private ConcurrentHashMap<String, Fruitore> fruitoriMap = new ConcurrentHashMap<>();

    /**
     * Costruttore del manager dei fruitori.
     * Inizializza il thread pool e carica i fruitori dal database.
     * 
     * @param threadPoolManager il controller del thread pool
     */
    public FruitoreManager(ThreadPoolController threadPoolManager) {
        super(threadPoolManager);
        caricaFruitori();
    }

    /**
     * Sincronizza i dati dei fruitori in memoria con il database.
     * Aggiunge e aggiorna tutti i fruitori presenti nella mappa.
     */
    public void sincronizzaFruitori() {
        for (Fruitore fruitore : fruitoriMap.values()) {
            aggiungiFruitore(fruitore);
            aggiornaPswFruitore(fruitore.getEmail(), fruitore.getPassword());
        }
        consoleIO.mostraMessaggio("Sincronizzazione dei fruitori completata.");
    }

    // Logiche dei fruitori--------------------------------------------------
    
    /**
     * Carica tutti i fruitori dal database nella mappa in memoria.
     * Svuota la mappa esistente e la riempie con i dati dal database.
     */
    protected void caricaFruitori() {
        String sql = "SELECT nome, cognome, email, password FROM fruitori";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            synchronized (fruitoriMap) {
                fruitoriMap.clear();
                while (rs.next()) {
                    String email = rs.getString("email");
                    Fruitore fruitore = new Fruitore(
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            email,
                            rs.getString("password")
                    );
                    fruitoriMap.putIfAbsent(email, fruitore);
                }
            }
        } catch (Exception e) {
            consoleIO.mostraErrore("Errore caricamento fruitori: " + e.getMessage());
        }
    }

    /**
     * Aggiorna la password di un fruitore nel database in modo asincrono.
     * Aggiorna sia la tabella fruitori che la tabella utenti_unificati.
     * 
     * @param email l'email del fruitore
     * @param nuovaPassword la nuova password
     */
    public void aggiornaPswFruitore(String email, String nuovaPassword) {
        String sqlFruitore = "UPDATE fruitori SET password = ?, password_modificata = ? WHERE email = ?";
        String sqlUtentiUnificati = "UPDATE utenti_unificati SET password = ?, password_modificata = ? WHERE email = ?";
    
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect()) {
                 
                try (PreparedStatement pstmtFruitori = conn.prepareStatement(sqlFruitore)) {
                    pstmtFruitori.setString(1, nuovaPassword);
                    pstmtFruitori.setBoolean(2, true);  
                    pstmtFruitori.setString(3, email);
                    pstmtFruitori.executeUpdate();
                }
    
                 
                try (PreparedStatement pstmtUtenti = conn.prepareStatement(sqlUtentiUnificati)) {
                    pstmtUtenti.setString(1, nuovaPassword);
                    pstmtUtenti.setBoolean(2, true);  
                    pstmtUtenti.setString(3, email);
                    pstmtUtenti.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento della password: " + e.getMessage());
            }
        });
    }  

    /**
     * Aggiunge un nuovo fruitore al database.
     * Inserisce il fruitore sia nella tabella fruitori che nella tabella utenti_unificati.
     * 
     * @param fruitore il fruitore da aggiungere
     */
    protected void aggiungiFruitore(Fruitore fruitore) {
        String inserisciSqlFruitore = "INSERT INTO fruitori (nome, cognome, email, password, password_modificata) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(inserisciSqlFruitore)) {
            pstmt.setString(1, fruitore.getNome());
            pstmt.setString(2, fruitore.getCognome());
            pstmt.setString(3, fruitore.getEmail());
            pstmt.setString(4, fruitore.getPassword());
            pstmt.setBoolean(5, true);
            pstmt.executeUpdate();
            aggiungiUtenteUnificato(fruitore, true);
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiunta del fruitore: " + e.getMessage());
        }
    }

    /**
     * Aggiunge un nuovo fruitore verificando prima che non esista già.
     * 
     * @param nuovoFruitore il nuovo fruitore da aggiungere
     */
    public void aggiungiNuovoFruitore(Fruitore nuovoFruitore) {
        String verificaSql = "SELECT 1 FROM fruitori WHERE email = ?";
        if(!recordEsiste(verificaSql, nuovoFruitore.getEmail())){
            aggiungiFruitore(nuovoFruitore);
        } else {
            consoleIO.mostraMessaggio("Il fruitore con email " + nuovoFruitore.getEmail() + " esiste già.");
        }
    }

    /**
     * Restituisce la mappa di tutti i fruitori.
     * 
     * @return la mappa concorrente dei fruitori indicizzata per email
     */
    public ConcurrentHashMap<String, Fruitore> getFruitoriMap() {
        return fruitoriMap;
    }


}
