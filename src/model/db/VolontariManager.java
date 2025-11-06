package src.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import src.controller.ThreadPoolController;
import src.model.TipiVisitaClass;
import src.model.Visita;
import src.model.Volontario;

/**
 * Gestisce le operazioni CRUD per i volontari nel database.
 * Mantiene una cache sincronizzata dei volontari e gestisce
 * le loro disponibilità e tipi di visita supportati.
 * 
 */
public class VolontariManager extends DatabaseManager {

    /** Mappa concorrente dei volontari indicizzata per email */
    private ConcurrentHashMap<String, Volontario> volontariMap = new ConcurrentHashMap<>();

    /**
     * Costruttore del manager dei volontari.
     * Inizializza il thread pool e carica i volontari dal database.
     * 
     * @param threadPoolManager il controller del thread pool
     */
    public VolontariManager(ThreadPoolController threadPoolManager) {
        super(threadPoolManager);
        caricaVolontari();
    }

    /**
     * Sincronizza i dati dei volontari in memoria con il database.
     * Aggiunge e aggiorna tutti i volontari presenti nella mappa.
     */
    public void sincronizzaVolontari() {
        for (Volontario volontario : volontariMap.values()) {
            aggiungiVolontario(volontario);
            aggiornaPswVolontario(volontario.getEmail(), volontario.getPassword());
        }
        consoleIO.mostraMessaggio("Sincronizzazione dei volontari completata.");
    }

    /**
     * Carica tutti i volontari dal database nella mappa in memoria.
     * Svuota la mappa esistente e la riempie con i dati aggiornati.
     */
    protected void caricaVolontari() {
        String sql = "SELECT nome, cognome, email, password, tipi_di_visite FROM volontari";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            synchronized (volontariMap) {
                volontariMap.clear();
                while (rs.next()) {
                    String email = rs.getString("email");
                    String tipiDiVisite = rs.getString("tipi_di_visite");
                    List<TipiVisitaClass> listaTipiVisite = new ArrayList<>();
                    if (tipiDiVisite != null && !tipiDiVisite.isEmpty()) {
                        for (String tipo : tipiDiVisite.split(",")) {
                            listaTipiVisite.add(TipiVisitaClass.valueOf(tipo.trim()));
                        }
                    }
                    Volontario volontario = new Volontario(
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            email,
                            rs.getString("password"),
                            listaTipiVisite
                    );
                    volontariMap.putIfAbsent(email, volontario);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento dei volontari: " + e.getMessage());
        }
    }

    /**
     * Aggiunge un nuovo volontario al database.
     * Inserisce il volontario sia nella tabella volontari che nella tabella utenti_unificati.
     * 
     * @param volontario il volontario da aggiungere
     */
    protected void aggiungiVolontario(Volontario volontario) {
        String inserisciSqlVolontari = "INSERT INTO volontari (nome, cognome, email, password, tipi_di_visite, password_modificata) VALUES (?, ?, ?, ?, ?, ?)";
    
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(inserisciSqlVolontari)) {
            pstmt.setString(1, volontario.getNome());
            pstmt.setString(2, volontario.getCognome());
            pstmt.setString(3, volontario.getEmail());
            pstmt.setString(4, volontario.getPassword());
            pstmt.setString(5, String.join(",", volontario.getTipiDiVisite().stream().map(TipiVisitaClass::getNome).toArray(String[]::new)));
            pstmt.setBoolean(6, false);
            pstmt.executeUpdate();
            consoleIO.mostraMessaggio("Volontario aggiunto con successo nella tabella 'volontari'.");
    
             
            aggiungiUtenteUnificato(volontario, false);
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiunta del volontario: " + e.getMessage());
        }
    }

    /**
     * Aggiorna la password di un volontario nel database in modo asincrono.
     * Aggiorna sia la tabella volontari che la tabella utenti_unificati.
     * 
     * @param email l'email del volontario
     * @param nuovaPassword la nuova password
     */
    protected void aggiornaPswVolontario(String email, String nuovaPassword) {
        String sqlVolontari = "UPDATE volontari SET password = ?, password_modificata = ? WHERE email = ?";
        String sqlUtentiUnificati = "UPDATE utenti_unificati SET password = ?, password_modificata = ? WHERE email = ?";
    
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect()) {
                 
                try (PreparedStatement pstmtVolontari = conn.prepareStatement(sqlVolontari)) {
                    pstmtVolontari.setString(1, nuovaPassword);
                    pstmtVolontari.setBoolean(2, true);  
                    pstmtVolontari.setString(3, email);
                    pstmtVolontari.executeUpdate();

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
     * Elimina un volontario dal database in modo asincrono.
     * Rimuove il volontario sia dalla tabella volontari che dalla tabella utenti_unificati.
     * 
     * @param volontarioDaEliminare il volontario da eliminare
     */
    private void eliminaVol(Volontario volontarioDaEliminare) {
        String sqlVolontari = "DELETE FROM volontari WHERE email = ?";
        String sqlUtentiUnificati = "DELETE FROM utenti_unificati WHERE email = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect()) {
                 
                try (PreparedStatement pstmt = conn.prepareStatement(sqlVolontari)) {
                    pstmt.setString(1, volontarioDaEliminare.getEmail());
                    pstmt.executeUpdate();
                }

                 
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUtentiUnificati)) {
                    pstmt.setString(1, volontarioDaEliminare.getEmail());
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'eliminazione del volontario: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiorna la disponibilità di un volontario nel database in modo asincrono.
     * 
     * @param email l'email del volontario
     * @param disponibilita la nuova disponibilità
     */
    public void aggiornaDisponibilitaVolontario(String email, String disponibilita) {
        String sql = "UPDATE volontari SET disponibilita = ? WHERE email = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, disponibilita);
                pstmt.setString(2, email);
                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Disponibilità aggiornata con successo per il volontario " + email);
                } else {
                    System.out.println("Nessun volontario trovato con l'email " + email);
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento della disponibilità: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiunge un nuovo volontario verificando prima che non esista già.
     * 
     * @param nuovoVolontario il nuovo volontario da aggiungere
     */
    public void aggiungiNuovoVolontario(Volontario nuovoVolontario) {
        String verificaSql = "SELECT 1 FROM volontari WHERE email = ?";
        if(!recordEsiste(verificaSql, nuovoVolontario.getEmail())){
            aggiungiVolontario(nuovoVolontario);
            consoleIO.mostraMessaggio("Volontario aggiunto con successo.");
        } else {
            consoleIO.mostraMessaggio("Il volontario con email " + nuovoVolontario.getEmail() + " esiste già.");
        }
    }

    /**
     * Aggiorna i tipi di visita supportati da un volontario in modo asincrono.
     * 
     * @param email l'email del volontario
     * @param nuoviTipiVisitaClass la nuova lista di tipi di visita supportati
     */
    protected void aggiornaTipiVisitaClassVolontario(String email, List<TipiVisitaClass> nuoviTipiVisitaClass) {
        String sql= "UPDATE volontari SET tipi_di_visite = ? WHERE email = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, String.join(",", nuoviTipiVisitaClass.stream().map(TipiVisitaClass::getNome).toArray(String[]::new)));
                pstmt.setString(2, email);
                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                     
                    synchronized (volontariMap) {
                        Volontario volontario = volontariMap.get(email);
                        if (volontario != null) {
                            volontario.setTipiDiVisite(nuoviTipiVisitaClass);
                        }
                    }
                    consoleIO.mostraMessaggio("Tipi di visita aggiornati con successo per il volontario " + email);
                }else {
                    consoleIO.mostraMessaggio("Nessun volontario trovato con l'email " + email);
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento dei tipi di visita: " + e.getMessage());
            }
        });
    }

    /**
     * Rimuove una visita assegnata a un volontario in modo asincrono.
     * 
     * @param visitaSelezionata la visita da rimuovere
     * @param volontarioSelezionato il volontario da cui rimuovere la visita
     */
    protected void rimuoviVisitaDaVolontario(Visita visitaSelezionata, Volontario volontarioSelezionato) {
        String sql = "DELETE FROM visite WHERE id = ? AND volontario_id = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, visitaSelezionata.getId());
                pstmt.setInt(2, getIdByEmail(volontarioSelezionato.getEmail()));
                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted > 0) {
                    consoleIO.mostraMessaggio("Visita rimossa con successo dal volontario " + volontarioSelezionato.getEmail());
                } else {
                    consoleIO.mostraMessaggio("Nessuna visita trovata per il volontario specificato.");
                }
            } catch (SQLException e) {
                System.err.println("Errore durante la rimozione della visita dal volontario: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiunge un tipo di visita ai tipi supportati da un volontario.
     * 
     * @param email l'email del volontario
     * @param tipoVisita il tipo di visita da aggiungere
     */
    public void aggiungiTipoVisitaAVolontari (String email, TipiVisitaClass tipoVisita){
        synchronized (volontariMap){
            Volontario volontario = volontariMap.get(email);
            if (volontario !=null){
                List<TipiVisitaClass> tipiEsistenti = new ArrayList<>(volontario.getTipiDiVisite());
                if (!tipiEsistenti.contains(tipoVisita)){
                    tipiEsistenti.add(tipoVisita);
                    aggiornaTipiVisitaClassVolontario(email, tipiEsistenti);
                } 
            }
        }
    }

    /**
     * Rimuove uno o più tipi di visita dai tipi supportati da un volontario in modo asincrono.
     * 
     * @param email l'email del volontario
     * @param tipiVisitaDaRimuovere la lista dei tipi di visita da rimuovere
     */
    public void rimuoviTipiVisitaClassVolontario (String email, List<TipiVisitaClass> tipiVisitaDaRimuovere){
        String sql = "UPDATE volontari SET tipi_di_visite = ? WHERE email = ?";
        executorService.submit(() -> {
            try (Connection conn= DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)){
                
                synchronized (volontariMap) {
                    Volontario volontario = volontariMap.get(email);
                    if (volontario != null) {
                         
                        List<TipiVisitaClass> nuoviTipiVisitaClass = new ArrayList<>(volontario.getTipiDiVisite());
                        nuoviTipiVisitaClass.removeAll(tipiVisitaDaRimuovere);

                        pstmt.setString (1, String.join(",", nuoviTipiVisitaClass.stream().map(TipiVisitaClass::getNome).toArray(String[]::new)));
                        pstmt.setString(2, email);
                        int rowsUpdated = pstmt.executeUpdate();

                        if (rowsUpdated > 0) {
                             
                            volontario.setTipiDiVisite(nuoviTipiVisitaClass);
                            consoleIO.mostraMessaggio("Tipi di visita rimossi con successo per il volontario " + email);
                        } else {
                            consoleIO.mostraMessaggio("Nessun volontario trovato con l'email " + email);
                        }
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Errore durante la rimozione dei tipi di visita: " + e.getMessage());
            }
        });
    }

    /**
     * Recupera l'ID di un volontario dato il suo indirizzo email.
     * 
     * @param volontario l'email del volontario
     * @return l'ID del volontario, o -1 se non trovato
     */
    public int getIdByEmail(String volontario) {
        String sql = "SELECT id FROM volontari WHERE email = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, volontario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero dell'ID del volontario: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Rimuove un singolo tipo di visita dai tipi supportati da un volontario.
     * 
     * @param email l'email del volontario
     * @param tipoVisita il tipo di visita da rimuovere
     */
    public void rimuoviTipoVisitaDaVolontario (String email, TipiVisitaClass tipoVisita){
        rimuoviTipiVisitaClassVolontario(email, Arrays.asList(tipoVisita));
    }

    /**
     * Recupera tutti i volontari che supportano un determinato tipo di visita.
     * 
     * @param tipoVisita il tipo di visita richiesto
     * @return lista dei volontari che supportano quel tipo di visita
     */
    public List<Volontario> getVolontariPerTipoVisita (TipiVisitaClass tipoVisita){
        List<Volontario> volontariPerTipo = new ArrayList<>();
        synchronized (volontariMap) {
            for (Volontario volontario : volontariMap.values()) {
                if (volontario.getTipiDiVisite().contains(tipoVisita)) {
                    volontariPerTipo.add(volontario);
                }
            }
        } return volontariPerTipo;
    }

    /**
     * Organizza tutti i volontari per tipo di visita supportato.
     * 
     * @return mappa con tipo di visita come chiave e lista di volontari come valore
     */
    public Map<TipiVisitaClass, List<Volontario>> getVolontariPerTipoVisita(){
        Map<TipiVisitaClass, List<Volontario>> volontariPerTipo = new HashMap<>();
        synchronized (volontariMap) {
            for (Volontario volontario : volontariMap.values()) {
                for (TipiVisitaClass tipoVisita : volontario.getTipiDiVisite()) {
                    volontariPerTipo.computeIfAbsent(tipoVisita, k -> new ArrayList<>()).add(volontario);
                }
            }
        } return volontariPerTipo;
    }

    /**
     * Restituisce la mappa di tutti i volontari.
     * 
     * @return la mappa concorrente dei volontari indicizzata per email
     */
    public ConcurrentHashMap<String, Volontario> getVolontariMap() {
        return volontariMap;
    }
    
    /**
     * Imposta la mappa dei volontari.
     * 
     * @param volontariMap la nuova mappa dei volontari
     */
    public void setVolontariMap(ConcurrentHashMap<String, Volontario> volontariMap) {
        this.volontariMap = volontariMap;
    }

    /**
     * Elimina un volontario sia dal database che dalla mappa in memoria.
     * 
     * @param volontarioDaEliminare il volontario da eliminare
     */
    public void eliminaVolontario(Volontario volontarioDaEliminare) {
        eliminaVol(volontarioDaEliminare);
        volontariMap.remove(volontarioDaEliminare.getEmail());
    }

    /**
     * Modifica la password di un volontario.
     * 
     * @param email l'email del volontario
     * @param nuovaPassword la nuova password
     */
    public void modificaPsw(String email, String nuovaPassword) {
        aggiornaPswVolontario(email, nuovaPassword);
    }

    /**
     * Recupera l'email di un volontario dato il suo ID.
     * 
     * @param volontarioId l'ID del volontario
     * @return l'email del volontario, o null se non trovato
     */
    public String getEmailById(int volontarioId) {
        String sql = "SELECT email FROM volontari WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, volontarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero dell'email del volontario: " + e.getMessage());
        }
        return null;
    }

    /**
     * Rimuove una visita da un volontario.
     * 
     * @param visitaSelezionata la visita da rimuovere
     * @param volontarioSelezionato il volontario da cui rimuovere la visita
     */
    public void rimuoviVisitaVolontario(Visita visitaSelezionata, Volontario volontarioSelezionato) {
        rimuoviVisitaDaVolontario(visitaSelezionata, volontarioSelezionato);
    }


}
