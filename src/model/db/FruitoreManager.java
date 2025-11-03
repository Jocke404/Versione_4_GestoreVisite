package src.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import src.controller.ThreadPoolController;
import src.model.Fruitore;



public class FruitoreManager extends DatabaseManager {

    private ConcurrentHashMap<String, Fruitore> fruitoriMap = new ConcurrentHashMap<>();

    public FruitoreManager(ThreadPoolController threadPoolManager) {
        super(threadPoolManager);
        caricaFruitori();
    }

    public void sincronizzaFruitori() {
        for (Fruitore fruitore : fruitoriMap.values()) {
            aggiungiFruitore(fruitore);
            aggiornaPswFruitore(fruitore.getEmail(), fruitore.getPassword());
        }
        consoleIO.mostraMessaggio("Sincronizzazione dei fruitori completata.");
    }

    // Logiche dei fruitori--------------------------------------------------
    // Metodo per caricare i fruitori dal database e memorizzarli nella HashMap
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

    public void aggiornaPswFruitore(String email, String nuovaPassword) {
        String sqlFruitore = "UPDATE fruitori SET password = ?, password_modificata = ? WHERE email = ?";
        String sqlUtentiUnificati = "UPDATE utenti_unificati SET password = ?, password_modificata = ? WHERE email = ?";
    
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect()) {
                // Aggiorna la tabella "fruitori"
                try (PreparedStatement pstmtFruitori = conn.prepareStatement(sqlFruitore)) {
                    pstmtFruitori.setString(1, nuovaPassword);
                    pstmtFruitori.setBoolean(2, true); // Imposta password_modificata a true
                    pstmtFruitori.setString(3, email);
                    pstmtFruitori.executeUpdate();
                }
    
                // Aggiorna la tabella "utenti_unificati"
                try (PreparedStatement pstmtUtenti = conn.prepareStatement(sqlUtentiUnificati)) {
                    pstmtUtenti.setString(1, nuovaPassword);
                    pstmtUtenti.setBoolean(2, true); // Imposta password_modificata a true
                    pstmtUtenti.setString(3, email);
                    pstmtUtenti.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento della password: " + e.getMessage());
            }
        });
    }  

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

    public void aggiungiNuovoFruitore(Fruitore nuovoFruitore) {
        String verificaSql = "SELECT 1 FROM fruitori WHERE email = ?";
        if(!recordEsiste(verificaSql, nuovoFruitore.getEmail())){
            aggiungiFruitore(nuovoFruitore);
        } else {
            consoleIO.mostraMessaggio("Il fruitore con email " + nuovoFruitore.getEmail() + " esiste già.");
        }
    }

    public ConcurrentHashMap<String, Fruitore> getFruitoriMap() {
        return fruitoriMap;
    }


}
