package src.model.db;

import src.controller.ThreadPoolController;
import src.model.Fruitore;
import src.model.Prenotazione;
import src.model.Visita;


import java.sql.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PrenotazioneManager extends DatabaseManager {
    private ConcurrentHashMap<String, Prenotazione> prenotazioniMap = new ConcurrentHashMap<>();
    private VisiteManagerDB visiteManager;
    private FruitoreManager fruitoreManager;

    public PrenotazioneManager(ThreadPoolController threadPoolManager, 
                             VisiteManagerDB visiteManager, 
                             FruitoreManager fruitoreManager) {
        super(threadPoolManager);
        this.visiteManager = visiteManager;
        this.fruitoreManager = fruitoreManager;
        caricaPrenotazioni();
    }

    protected boolean addPrenotazione(String emailFruitore, int idVisita,  int numeroPersone) {
        // Verifica disponibilità posti
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

        // Verifica che il fruitore non abbia già prenotato questa visita
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

    private boolean verificaDisponibilitaPosti(Visita visita, int numeroPersone) {
        return (visita.getPostiPrenotati() + numeroPersone) <= visita.getMaxPersone();
    }

    private boolean haFruitorePrenotatoVisita(String emailFruitore, int idVisita) {
        String sql = "SELECT 1 FROM prenotazioni WHERE email_fruitore = ? AND id_visita = ? AND stato = 'CONFERMATA'";
        return recordEsiste(sql, emailFruitore, idVisita);
    }

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
                    prenotazione.setDataPrenotazione(rs.getTimestamp("data_prenotazione").toLocalDateTime());
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
     * Ottieni tutte le prenotazioni di un fruitore
     */
    protected List<Prenotazione> getPrenotazioniFruitore(String emailFruitore) {
        return prenotazioniMap.values().stream()
                .filter(p -> p.getEmailFruitore().equals(emailFruitore) && "CONFERMATA".equals(p.getStato()))
                .collect(Collectors.toList());
    }

    public List<Prenotazione> getPrenotazioniVisita(int idVisita) {
        return prenotazioniMap.values().stream()
                .filter(p -> p.getIdVisita() == idVisita && "CONFERMATA".equals(p.getStato()))
                .collect(Collectors.toList());
    }

    /**
     * Cancella una prenotazione
     */
    protected boolean cancellaPrenotazione(String codicePrenotazione, String emailFruitore) {
        Prenotazione prenotazione = prenotazioniMap.get(codicePrenotazione);

        if (prenotazione == null || !prenotazione.getEmailFruitore().equals(emailFruitore)) {
            consoleIO.mostraErrore("Prenotazione non trovata o non autorizzata");
            return false;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);

            // Aggiorna stato prenotazione
            String sqlCancella = "UPDATE prenotazioni SET stato = 'CANCELLATA' WHERE codice_prenotazione = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCancella)) {
                pstmt.setString(1, codicePrenotazione);
                pstmt.executeUpdate();
            }

            // Libera posti nella visita
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

    public void creaPrenotazione(Fruitore fruitore, Visita visita,  int numeroPersone) {
        addPrenotazione(fruitore.getEmail(), visita.getId(), numeroPersone);
        // Aggiorna la visita con il numero di posti prenotati
        visita.setPostiPrenotati(visita.getPostiPrenotati() + numeroPersone);

    }

    public boolean rimuoviPrenotazione(Prenotazione prenotazione) {
        return cancellaPrenotazione(prenotazione.getCodicePrenotazione(), prenotazione.getEmailFruitore());
    }

    public List<Prenotazione> miePrenotazioni(Fruitore fruitore) {
        return getPrenotazioniFruitore(fruitore.getEmail());
    }

}
