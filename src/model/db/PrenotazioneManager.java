package src.model.db;

import src.controller.ThreadPoolController;
import src.model.Fruitore;
import src.model.Prenotazione;
import src.model.Visita;


import java.sql.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gestisce le operazioni CRUD per le prenotazioni nel database.
 * Coordina la creazione e cancellazione delle prenotazioni assicurando
 * la consistenza tra prenotazioni e disponibilità delle visite.
 * 
 */
public class PrenotazioneManager extends DatabaseManager {
    /** Mappa concorrente delle prenotazioni indicizzata per codice prenotazione */
    private static ConcurrentHashMap<String, Prenotazione> prenotazioniMap = new ConcurrentHashMap<>();
    
    /** Manager delle visite per verificare disponibilità */
    private VisiteManagerDB visiteManager;

    /**
     * Costruttore del manager delle prenotazioni.
     * 
     * @param threadPoolManager il controller del thread pool
     * @param visiteManager il manager delle visite
     * @param fruitoreManager il manager dei fruitori
     */
    public PrenotazioneManager(ThreadPoolController threadPoolManager, 
                             VisiteManagerDB visiteManager) {
        super(threadPoolManager);
        this.visiteManager = visiteManager;
        caricaPrenotazioni();
    }

    /**
     * Aggiunge una nuova prenotazione al database con controlli di validità.
     * Verifica la disponibilità di posti e che il fruitore non abbia già prenotato.
     * Utilizza una transazione per garantire consistenza dei dati.
     * 
     * @param emailFruitore l'email del fruitore che prenota
     * @param idVisita l'ID della visita da prenotare
     * @param numeroPersone il numero di persone da prenotare
     * @return true se la prenotazione è stata creata con successo, false altrimenti
     */
    protected boolean addPrenotazione(String emailFruitore, int idVisita,  int numeroPersone) {
         
        Visita visita = visiteManager.getVisiteMap().get(idVisita);
        if (visita == null) {
            consoleIO.mostraErrore("Visita non trovata");
            return false;
        }

        if (!verificaDisponibilitaPosti(visita, numeroPersone)) {
            consoleIO.mostraErrore("Posti insufficienti. Disponibili: " + 
                (visita.getMaxPersone() - visita.getPostiPrenotati()));
            return false;
        }

         
        if (haFruitorePrenotatoVisita(emailFruitore, idVisita)) {
            consoleIO.mostraErrore("Hai già prenotato questa visita");
            return false;
        }

        Prenotazione prenotazione = new Prenotazione(emailFruitore, idVisita,  numeroPersone);

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);

            String sqlPrenotazione = "INSERT INTO prenotazioni (id_visita, email_fruitore, numero_persone, codice_prenotazione) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPrenotazione, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, idVisita);
                pstmt.setString(2, emailFruitore);
                pstmt.setInt(3, numeroPersone);
                pstmt.setString(4, prenotazione.getCodicePrenotazione());
                
                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        prenotazione.setId(generatedKeys.getInt(1));
                    }
                }
            }

            String sqlAggiornaVisita = "UPDATE visite SET posti_prenotati = posti_prenotati + ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlAggiornaVisita)) {
                pstmt.setInt(1, numeroPersone);
                pstmt.setInt(2, idVisita);
                pstmt.executeUpdate();
            }

            conn.commit();
            prenotazioniMap.put(prenotazione.getCodicePrenotazione(), prenotazione);
            
            consoleIO.mostraMessaggio("Prenotazione confermata! Codice: " + prenotazione.getCodicePrenotazione());
            return true;

        } catch (SQLException e) {
            System.err.println("Errore durante la creazione della prenotazione: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se ci sono abbastanza posti disponibili per una prenotazione.
     * 
     * @param visita la visita da verificare
     * @param numeroPersone il numero di posti richiesti
     * @return true se ci sono posti sufficienti, false altrimenti
     */
    private boolean verificaDisponibilitaPosti(Visita visita, int numeroPersone) {
        return (visita.getPostiPrenotati() + numeroPersone) <= visita.getMaxPersone();
    }

    /**
     * Verifica se un fruitore ha già prenotato una specifica visita.
     * 
     * @param emailFruitore l'email del fruitore
     * @param idVisita l'ID della visita
     * @return true se il fruitore ha già una prenotazione confermata, false altrimenti
     */
    private boolean haFruitorePrenotatoVisita(String emailFruitore, int idVisita) {
        String sql = "SELECT 1 FROM prenotazioni WHERE email_fruitore = ? AND id_visita = ? AND stato = 'CONFERMATA'";
        return recordEsiste(sql, emailFruitore, idVisita);
    }

    /**
     * Carica tutte le prenotazioni dal database nella mappa in memoria.
     * Svuota la mappa esistente e la riempie con i dati aggiornati.
     */
    protected void caricaPrenotazioni() {
        String sql = "SELECT id, id_visita, email_fruitore, numero_persone, data_prenotazione, codice_prenotazione, stato FROM prenotazioni";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            synchronized (prenotazioniMap) {
                prenotazioniMap.clear();
                while (rs.next()) {
                    Prenotazione prenotazione = new Prenotazione(
                        rs.getString("email_fruitore"),
                        rs.getInt("id_visita"),
                        rs.getInt("numero_persone")
                    );
                    prenotazione.setId(rs.getInt("id"));
                    prenotazione.setDataPrenotazione(rs.getDate("data_prenotazione").toLocalDate());
                    prenotazione.setCodicePrenotazione(rs.getString("codice_prenotazione"));
                    prenotazione.setStato(rs.getString("stato"));
                    
                    prenotazioniMap.put(prenotazione.getCodicePrenotazione(), prenotazione);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento delle prenotazioni: " + e.getMessage());
        }
    }

    /**
     * Aggiorna una prenotazione esistente nel database.
     * 
     * @param id l'ID della prenotazione da aggiornare
     * @param prenotazione l'oggetto prenotazione con i nuovi dati
     */
    protected void aggiornaPrenotazioneDB(int id, Prenotazione prenotazione) {
        String sql = "UPDATE prenotazioni SET id_visita = ?, email_fruitore = ?, numero_persone = ?, data_prenotazione = ?, codice_prenotazione = ?, stato = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, prenotazione.getIdVisita());
            pstmt.setString(2, prenotazione.getEmailFruitore());
            pstmt.setInt(3, prenotazione.getNumeroPersone());
            pstmt.setDate(4, Date.valueOf(prenotazione.getDataPrenotazione()));
            pstmt.setString(5, prenotazione.getCodicePrenotazione());
            pstmt.setString(6, prenotazione.getStato());
            pstmt.setInt(7, id);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiornamento della prenotazione: " + e.getMessage());
        }
    }

    /**
     * Recupera tutte le prenotazioni confermate di un fruitore.
     * 
     * @param emailFruitore l'email del fruitore
     * @return lista delle prenotazioni confermate del fruitore
     */
    protected List<Prenotazione> getPrenotazioniFruitore(String emailFruitore) {
        return prenotazioniMap.values().stream()
                .filter(p -> p.getEmailFruitore().equals(emailFruitore) && "CONFERMATA".equals(p.getStato()))
                .collect(Collectors.toList());
    }

    /**
     * Recupera tutte le prenotazioni confermate per una visita specifica.
     * 
     * @param idVisita l'ID della visita
     * @return lista delle prenotazioni confermate per la visita
     */
    public List<Prenotazione> getPrenotazioniVisita(int idVisita) {
        return prenotazioniMap.values().stream()
                .filter(p -> p.getIdVisita() == idVisita && "CONFERMATA".equals(p.getStato()))
                .collect(Collectors.toList());
    }

    /**
     * Cancella una prenotazione esistente.
     * Aggiorna lo stato della prenotazione e libera i posti nella visita.
     * Utilizza una transazione per garantire consistenza dei dati.
     * 
     * @param codicePrenotazione il codice della prenotazione da cancellare
     * @param emailFruitore l'email del fruitore (per verifica autorizzazione)
     * @return true se la cancellazione è andata a buon fine, false altrimenti
     */
    protected boolean cancellaPrenotazione(String codicePrenotazione, String emailFruitore) {
        Prenotazione prenotazione = prenotazioniMap.get(codicePrenotazione);

        if (prenotazione == null || !prenotazione.getEmailFruitore().equals(emailFruitore)) {
            consoleIO.mostraErrore("Prenotazione non trovata o non autorizzata");
            return false;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);

             
            String sqlCancella = "UPDATE prenotazioni SET stato = 'CANCELLATA' WHERE codice_prenotazione = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCancella)) {
                pstmt.setString(1, codicePrenotazione);
                pstmt.executeUpdate();
            }

             
            String sqlAggiornaVisita = "UPDATE visite SET posti_prenotati = posti_prenotati - ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlAggiornaVisita)) {
                pstmt.setInt(1, prenotazione.getNumeroPersone());
                pstmt.setInt(2, prenotazione.getIdVisita());
                pstmt.executeUpdate();
            }

            conn.commit();
            prenotazione.setStato("CANCELLATA");
            
            consoleIO.mostraMessaggio("Prenotazione cancellata");
            return true;

        } catch (SQLException e) {
            System.err.println("Errore durante la cancellazione: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea una nuova prenotazione per un fruitore.
     * Metodo di alto livello che aggiorna anche lo stato della visita in memoria.
     * 
     * @param fruitore il fruitore che effettua la prenotazione
     * @param visita la visita da prenotare
     * @param numeroPersone il numero di persone da prenotare
     */
    public void creaPrenotazione(Fruitore fruitore, Visita visita,  int numeroPersone) {
        addPrenotazione(fruitore.getEmail(), visita.getId(), numeroPersone);
         
        visita.setPostiPrenotati(visita.getPostiPrenotati() + numeroPersone);

    }

    /**
     * Rimuove una prenotazione esistente.
     * 
     * @param prenotazione la prenotazione da rimuovere
     * @return true se la rimozione è andata a buon fine, false altrimenti
     */
    public boolean rimuoviPrenotazione(Prenotazione prenotazione) {
        return cancellaPrenotazione(prenotazione.getCodicePrenotazione(), prenotazione.getEmailFruitore());
    }

    /**
     * Recupera tutte le prenotazioni di un fruitore.
     * 
     * @param fruitore il fruitore di cui recuperare le prenotazioni
     * @return lista delle prenotazioni del fruitore
     */
    public List<Prenotazione> miePrenotazioni(Fruitore fruitore) {
        return getPrenotazioniFruitore(fruitore.getEmail());
    }

    public static ConcurrentHashMap<String, Prenotazione> getPrenotazioniMap() {
        return prenotazioniMap;
    }

    public void caricaPrenotazioniAsync() {
       caricaPrenotazioni();
    }

    public void aggiornaPrenotazione(int id, Prenotazione prenotazione) {
        aggiornaPrenotazioneDB(id, prenotazione);
    }



}
