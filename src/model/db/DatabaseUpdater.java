package src.model.db;

import src.controller.ThreadPoolController;
import src.model.TemporaryCredential;
import src.view.ConsoleIO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Gestisce la sincronizzazione periodica dei dati dal database.
 * Coordina l'aggiornamento automatico di volontari, configuratori, luoghi e visite.
 * Fornisce anche funzionalità per la gestione delle credenziali temporanee e autenticazione.
 * 
 */
public class DatabaseUpdater {
    /** Mappa delle credenziali temporanee per il primo accesso */
    private ConcurrentHashMap<String, TemporaryCredential> temporaryCredentials = new ConcurrentHashMap<>();
    
    /** Interfaccia per output su console */
    private ConsoleIO consoleIO = new ConsoleIO();
    
    /** Manager dei volontari */
    private final VolontariManager volontariManager;
    
    /** Manager dei configuratori */
    private final ConfiguratoriManager configuratoriManager;
    
    /** Manager dei luoghi */
    private final LuoghiManager luoghiManager;
    
    /** Manager delle visite */
    private final VisiteManagerDB visiteManagerDB;
    
    /** Thread pool per operazioni asincrone */
    private final ExecutorService executorService = ThreadPoolController.getInstance().createThreadPool(4);  
    
    /** Thread dedicato alla sincronizzazione periodica */
    private Thread aggiornamentoThread;
    
    /** Flag per controllare l'esecuzione del thread di aggiornamento */
    private volatile boolean eseguiAggiornamento = true;  

    /**
     * Costruttore del database updater.
     * 
     * @param volontariManager il manager dei volontari
     * @param configuratoriManager il manager dei configuratori
     * @param luoghiManager il manager dei luoghi
     * @param visiteManagerDB il manager delle visite
     */
    public DatabaseUpdater(
        VolontariManager volontariManager, 
        ConfiguratoriManager configuratoriManager, 
        LuoghiManager luoghiManager, 
        VisiteManagerDB visiteManagerDB
    ) {
        this.volontariManager = volontariManager;
        this.configuratoriManager = configuratoriManager;
        this.luoghiManager = luoghiManager;
        this.visiteManagerDB = visiteManagerDB;
    }

    //Logiche Thread------------------------------------------------------------------
    
    /**
     * Sincronizza i dati in memoria con il database in modo asincrono.
     * Ricarica volontari, configuratori, luoghi, visite e date precluse.
     */
    public void sincronizzaDalDatabase() {
        executorService.submit(() -> {
            if (eseguiAggiornamento) {
                try {
                     
                    volontariManager.caricaVolontari();
                    configuratoriManager.caricaConfiguratori();
                    luoghiManager.caricaLuoghi();
                    visiteManagerDB.caricaVisite();
                    visiteManagerDB.caricaDatePrecluse();
                } catch (Exception e) {
                    System.err.println("Errore durante la sincronizzazione dal database: " + e.getMessage());
                }
            } else {
                return;
            }            
        });
    }

    /**
     * Avvia la sincronizzazione periodica automatica dal database.
     * La sincronizzazione viene eseguita ogni 5 secondi in un thread separato.
     */
    public void avviaSincronizzazioneConSleep() {
        eseguiAggiornamento = true;  
        aggiornamentoThread = new Thread(() -> {
            while (eseguiAggiornamento) {
                try {
                    sincronizzaDalDatabase();
                    Thread.sleep(5000);  
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  
                    break;  
                }
            }
        });
        aggiornamentoThread.start();
    }

    /**
     * Arresta la sincronizzazione periodica automatica dal database.
     * Interrompe il thread di sincronizzazione e attende la sua terminazione.
     */
    public void arrestaSincronizzazioneConSleep() {
        eseguiAggiornamento = false;  
        if (aggiornamentoThread != null) {
            aggiornamentoThread.interrupt();  
            try {
                aggiornamentoThread.join();  
            } catch (InterruptedException e) {
                System.err.println("Errore durante l'arresto del thread di aggiornamento.");
                Thread.currentThread().interrupt();  
            }
        }
    }

    /**
     * Verifica se un record esiste nel database.
     * 
     * @param sql la query SQL da eseguire
     * @param parametri i parametri della query
     * @return true se il record esiste, false altrimenti
     */
    private boolean recordEsiste(String sql, Object... parametri) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            for (int i = 0; i < parametri.length; i++) {
                pstmt.setObject(i + 1, parametri[i]);
            }
    
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();  
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la verifica dell'esistenza del record: " + e.getMessage());
        }
        return false;
    }



    //Logiche per Credenziali Temporanee--------------------------------------------------
    
    /**
     * Carica tutte le credenziali temporanee dal database.
     * Le credenziali temporanee sono usate per il primo accesso degli utenti.
     */
    public void caricaCredenzialiTemporanee() {
        String sql = "SELECT username, password FROM credenziali_temporanee";
    
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
    
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                temporaryCredentials.put(username, new TemporaryCredential(username, password));
            }
    
            consoleIO.mostraMessaggio("Credenziali temporanee caricate con successo.");
        } catch (SQLException e) {
            consoleIO.mostraMessaggio("Errore durante il caricamento delle credenziali temporanee: " + e.getMessage());
        }
    }

//Getters e Setters--------------------------------------------------
    
    /**
     * Determina il tipo di utente verificando le credenziali.
     * 
     * @param email l'email dell'utente
     * @param password la password dell'utente
     * @return il tipo di utente (volontario, fruitore, configuratore) o null se non valido
     */
    public String getTipoUtente(String email, String password){
        String tipo_utente = null;
        String sql = "SELECT tipo_utente, password FROM utenti_unificati WHERE email = ?";

        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                     
                    if (dbPassword != null && dbPassword.equals(password)) {
                        tipo_utente = rs.getString("tipo_utente");
                    }
                }
            }
        } catch (SQLException e) {
            consoleIO.mostraMessaggio("Errore durante la verifica delle credenziali: " + e.getMessage());
        }
        return tipo_utente;
    }

    /**
     * Verifica se un utente ha già modificato la password iniziale.
     * 
     * @param email l'email dell'utente
     * @return true se la password è stata modificata, false altrimenti
     */
    public boolean isPasswordModificata(String email) {
        String sql = "SELECT password_modificata FROM utenti_unificati WHERE email = ?";
        boolean passwordModificata = false;
    
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
             
            pstmt.setString(1, email);
    
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    passwordModificata = rs.getBoolean("password_modificata");  
                } else {
                    consoleIO.mostraMessaggio("Nessun record trovato per l'email: " + email);
                }
            }
        } catch (SQLException e) {
            consoleIO.mostraMessaggio("Errore durante la verifica del campo password_modificata: " + e.getMessage());
        }
    
        return passwordModificata;
    }

    /**
     * Restituisce la mappa delle credenziali temporanee.
     * 
     * @return la mappa concorrente delle credenziali temporanee
     */
    public ConcurrentHashMap<String, TemporaryCredential> getTemporaryCredentials() {
        return temporaryCredentials;
    }

    /**
     * Verifica se un'email è presente nel database.
     * 
     * @param email l'email da verificare
     * @return true se l'email esiste, false altrimenti
     */
    public boolean isEmailPresente(String email) {
        String sql = "SELECT 1 FROM utenti_unificati WHERE email = ?";
        return recordEsiste(sql, email);
    }
}