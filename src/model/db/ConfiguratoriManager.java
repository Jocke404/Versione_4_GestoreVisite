package src.model.db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import src.controller.ThreadPoolController;
import src.model.Configuratore;

/**
 * Gestisce le operazioni CRUD per i configuratori (amministratori) nel database.
 * Mantiene una cache sincronizzata dei configuratori e gestisce
 * le loro informazioni personali e credenziali di accesso.
 * 
 *  
 *  
 */
public class ConfiguratoriManager extends DatabaseManager {
    /** Mappa concorrente dei configuratori indicizzata per email */
    private ConcurrentHashMap<String, Configuratore> configuratoriMap = new ConcurrentHashMap<>();

    /**
     * Costruttore del manager dei configuratori.
     * Inizializza il thread pool e carica i configuratori dal database.
     * 
     * @param threadPoolManager il controller del thread pool
     */
    public ConfiguratoriManager(ThreadPoolController threadPoolManager) {
        super(threadPoolManager);
        caricaConfiguratori();
    }
    
    /**
     * Sincronizza i dati dei configuratori in memoria con il database.
     * Aggiorna tutti i configuratori presenti nella mappa.
     */
    public void sincronizzaConfiguratori() {
        for (Configuratore configuratore : configuratoriMap.values()) {
            aggiornaConfiguratore(configuratore.getEmail(), configuratore);
        }
        consoleIO.mostraMessaggio("Sincronizzazione dei configuratori completata.");
    }
    
    //Logiche dei configuratori--------------------------------------------------
    
    /**
     * Carica tutti i configuratori dal database nella mappa in memoria.
     * Svuota la mappa esistente e la riempie con i dati aggiornati.
     */
    protected void caricaConfiguratori() {
        String sql = "SELECT nome, cognome, email, password FROM configuratori";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            synchronized (configuratoriMap) {
                configuratoriMap.clear();
                while (rs.next()) {
                    String email = rs.getString("email");
                    Configuratore configuratore = new Configuratore(
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            email,
                            rs.getString("password")
                    );
                    configuratoriMap.putIfAbsent(email, configuratore);
                }
            }
        } catch (SQLException e) {
            consoleIO.mostraMessaggio("Errore durante il caricamento dei configuratori: " + e.getMessage());
        }
    }

    /**
     * Aggiunge un nuovo configuratore al database.
     * Inserisce il configuratore sia nella tabella configuratori che nella tabella utenti_unificati.
     * Metodo sincronizzato per garantire consistenza in ambiente multi-thread.
     * 
     * @param configuratore il configuratore da aggiungere
     */
    private synchronized void aggiungiConfiguratore(Configuratore configuratore) {
        String inserisciSqlConfiguratori = "INSERT INTO configuratori (nome, cognome, email, password, password_modificata) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(inserisciSqlConfiguratori)) {
            pstmt.setString(1, configuratore.getNome());
            pstmt.setString(2, configuratore.getCognome());
            pstmt.setString(3, configuratore.getEmail());
            pstmt.setString(4, configuratore.getPassword());
            pstmt.setBoolean(5, true);
            pstmt.executeUpdate();
            consoleIO.mostraMessaggio("Configuratore aggiunto con successo nella tabella 'configuratori'.");
    
             
            aggiungiUtenteUnificato(configuratore, true);
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiunta del configuratore: " + e.getMessage());
        }
    }

    /**
     * Aggiorna i dati di un configuratore esistente in modo asincrono.
     * Aggiorna sia la tabella configuratori che la tabella utenti_unificati.
     * Metodo sincronizzato per garantire consistenza in ambiente multi-thread.
     * 
     * @param email l'email corrente del configuratore
     * @param configuratoreAggiornato il configuratore con i dati aggiornati
     */
    protected synchronized void aggiornaConfiguratore(String email, Configuratore configuratoreAggiornato) {
        String sqlConfiguratori = "UPDATE configuratori SET nome = ?, cognome = ?, password = ?, email = ? WHERE email = ?";
        String sqlUtentiUnificati = "UPDATE utenti_unificati SET nome = ?, cognome = ?, password = ?, email = ? WHERE email = ?";
    
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect()) {
                 
                try (PreparedStatement pstmtConfiguratori = conn.prepareStatement(sqlConfiguratori)) {
                    pstmtConfiguratori.setString(1, configuratoreAggiornato.getNome());
                    pstmtConfiguratori.setString(2, configuratoreAggiornato.getCognome());
                    pstmtConfiguratori.setString(3, configuratoreAggiornato.getPassword());
                    pstmtConfiguratori.setString(4, configuratoreAggiornato.getEmail());  
                    pstmtConfiguratori.setString(5, email);  
                    pstmtConfiguratori.executeUpdate();
                }
    
                 
                try (PreparedStatement pstmtUtentiUnificati = conn.prepareStatement(sqlUtentiUnificati)) {
                    pstmtUtentiUnificati.setString(1, configuratoreAggiornato.getNome());
                    pstmtUtentiUnificati.setString(2, configuratoreAggiornato.getCognome());
                    pstmtUtentiUnificati.setString(3, configuratoreAggiornato.getPassword());
                    pstmtUtentiUnificati.setString(4, configuratoreAggiornato.getEmail());  
                    pstmtUtentiUnificati.setString(5, email);  
                    pstmtUtentiUnificati.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento del configuratore: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiorna la password di un configuratore nel database in modo asincrono.
     * Aggiorna sia la tabella configuratori che la tabella utenti_unificati.
     * Metodo sincronizzato per garantire consistenza in ambiente multi-thread.
     * 
     * @param email l'email del configuratore
     * @param nuovaPassword la nuova password
     */
    public synchronized void aggiornaPswConfiguratore(String email, String nuovaPassword) {
        String sqlConfiguratori = "UPDATE configuratori SET password = ?, password_modificata = ? WHERE email = ?";
        String sqlUtentiUnificati = "UPDATE utenti_unificati SET password = ?, password_modificata = ? WHERE email = ?";
    
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect()) {
                 
                try (PreparedStatement pstmtConfiguratori = conn.prepareStatement(sqlConfiguratori)) {
                    pstmtConfiguratori.setString(1, nuovaPassword);
                    pstmtConfiguratori.setBoolean(2, true);  
                    pstmtConfiguratori.setString(3, email);
                    pstmtConfiguratori.executeUpdate();
                }

                 
                try (PreparedStatement pstmtUtenti = conn.prepareStatement(sqlUtentiUnificati)) {
                    pstmtUtenti.setString(1, nuovaPassword);
                    pstmtUtenti.setBoolean(2, true);  
                    pstmtUtenti.setString(3, email);
                    pstmtUtenti.executeUpdate();
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
     * Aggiunge un nuovo configuratore verificando prima che non esista già.
     * 
     * @param nuovoConfiguratore il nuovo configuratore da aggiungere
     */
    public void aggiungiNuovoConf(Configuratore nuovoConfiguratore) {
        String verificaSql = "SELECT 1 FROM configuratori WHERE email = ?";
        if(!recordEsiste(verificaSql, nuovoConfiguratore.getEmail())){
            consoleIO.mostraMessaggio("Il configuratore non esiste già. Procedo con l'aggiunta.");
            aggiungiConfiguratore(nuovoConfiguratore);
        }
        else{
            consoleIO.mostraMessaggio("Il configuratore esiste già.");
            return;
        }
    }

    /**
     * Restituisce la mappa di tutti i configuratori.
     * 
     * @return la mappa concorrente dei configuratori indicizzata per email
     */
    public ConcurrentHashMap<String, Configuratore> getConfiguratoriMap() {
        return configuratoriMap;
    }
    
    /**
     * Imposta la mappa dei configuratori.
     * 
     * @param configuratoriMap la nuova mappa dei configuratori
     */
    public void setConfiguratoriMap(ConcurrentHashMap<String, Configuratore> configuratoriMap) {
        this.configuratoriMap = configuratoriMap;
    }

    /**
     * Aggiorna nome e cognome di un configuratore.
     * Aggiorna sia la mappa in memoria che il database.
     * 
     * @param email l'email del configuratore
     * @param newConfiguratore il configuratore con i nuovi dati
     */
    public void aggiornaNomeCognome(String email, Configuratore newConfiguratore) {
        configuratoriMap.put(email, newConfiguratore);
        aggiornaConfiguratore(email, newConfiguratore);
    }

}
