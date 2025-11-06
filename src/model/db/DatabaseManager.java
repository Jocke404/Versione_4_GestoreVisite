package src.model.db;

import src.model.Configuratore;
import src.model.Fruitore;
import src.model.Utente;
import src.model.Volontario;
import src.view.ConsoleIO;
import src.controller.ThreadPoolController;
import java.sql.*;
import java.util.concurrent.ExecutorService;

/**
 * Classe astratta base per tutti i manager del database.
 * Fornisce funzionalità comuni per la gestione delle connessioni,
 * operazioni asincrone tramite thread pool e utility per le query.
 * 
 */
public abstract class DatabaseManager {
    /** Oggetto per l'output su console */
    protected ConsoleIO consoleIO;
    
    /** Executor service per operazioni asincrone sul database */
    protected ExecutorService executorService;

    /**
     * Costruttore del manager del database.
     * Inizializza il thread pool e l'oggetto per l'output.
     * 
     * @param threadPoolManager il controller del thread pool
     */
    public DatabaseManager(ThreadPoolController threadPoolManager) {
        this.executorService = threadPoolManager.createThreadPool(4);
        this.consoleIO = new ConsoleIO();
    }

    /**
     * Verifica se esiste almeno un record nel database che soddisfa la query.
     * 
     * @param sql la query SQL da eseguire
     * @param parametri i parametri della query (opzionali)
     * @return true se esiste almeno un record, false altrimenti
     */
    public boolean recordEsiste(String sql, Object... parametri) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametri.length; i++) {
                pstmt.setObject(i + 1, parametri[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            consoleIO.mostraErrore("Errore verifica record: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Aggiorna l'indirizzo email di un utente nel database.
     * Sincronizza la modifica sia nella tabella specifica dell'utente
     * che nella tabella utenti_unificati.
     * 
     * @param utente l'utente di cui aggiornare l'email
     * @param nuovaEmail il nuovo indirizzo email
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    public synchronized boolean aggiornaEmail(Utente utente, String nuovaEmail) {
        String vecchiaEmail = utente.getEmail();
        String tipoUtente = null;
        if (utente instanceof Volontario) tipoUtente = "volontari";
        else if (utente instanceof Configuratore) tipoUtente = "configuratori";
        else if (utente instanceof Fruitore) tipoUtente = "fruitori";
        String sql = "UPDATE " + tipoUtente + " SET email = ? WHERE email = ?";
        String sqlUtentiUnificati = "UPDATE utenti_unificati SET email = ? WHERE email = ?";

        try (Connection conn = DatabaseConnection.connect()) {
             
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                consoleIO.mostraMessaggio("Aggiorno email: " + vecchiaEmail + " -> " + nuovaEmail + " (tabella: " + tipoUtente + ")");

                pstmt.setString(1, nuovaEmail);
                pstmt.setString(2, vecchiaEmail);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                     
                    try (PreparedStatement pstmtUt = conn.prepareStatement(sqlUtentiUnificati)) {
                        pstmtUt.setString(1, nuovaEmail);
                        pstmtUt.setString(2, vecchiaEmail);
                        pstmtUt.executeUpdate();
                    } catch (SQLException ex) {
                        consoleIO.mostraErrore("Errore aggiornamento utenti_unificati: " + ex.getMessage());
                         
                    }
                    utente.setEmail(nuovaEmail);
                    consoleIO.mostraMessaggio("Email aggiornata con successo.");
                    return true;
                } else {
                     
                    if (recordEsiste("SELECT 1 FROM " + tipoUtente + " WHERE email = ?", nuovaEmail)) {
                         
                        try (PreparedStatement pstmtUt = conn.prepareStatement(sqlUtentiUnificati)) {
                            pstmtUt.setString(1, nuovaEmail);
                            pstmtUt.setString(2, vecchiaEmail);
                            pstmtUt.executeUpdate();
                        } catch (SQLException ex) {
                            consoleIO.mostraErrore("Errore aggiornamento utenti_unificati: " + ex.getMessage());
                        }
                         
                        utente.setEmail(nuovaEmail);
                        consoleIO.mostraMessaggio("Email già aggiornata nel DB (persistita da altro processo).");
                        return true;
                    } else {
                        consoleIO.mostraErrore("Nessun record aggiornato. Email non trovata.");
                    }
                }
            }
        } catch (SQLException e) {
            consoleIO.mostraErrore("Errore aggiornamento email: " + e.getMessage());
        }
        return false;
    }

    /**
     * Aggiunge un utente alla tabella unificata degli utenti.
     * Questa tabella contiene tutti gli utenti del sistema per facilitare
     * l'autenticazione centralizzata.
     * 
     * @param utente l'utente da aggiungere
     * @param passwordModificata indica se la password è stata già modificata
     */
    protected void aggiungiUtenteUnificato(Utente utente, boolean passwordModificata) {
        String nome = utente.getNome();
        String cognome = utente.getCognome();
        String email = utente.getEmail();
        String password = utente.getPassword();
        String tipoUtente = utente.getClass().getSimpleName();

        String inserisciSqlUtentiUnificati = "INSERT INTO utenti_unificati (nome, cognome, email, password, tipo_utente, password_modificata) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(inserisciSqlUtentiUnificati)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setString(5, tipoUtente);
            pstmt.setBoolean(6, passwordModificata);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiunta dell'utente nella tabella 'utenti_unificati': " + e.getMessage());
        }
    }
}

