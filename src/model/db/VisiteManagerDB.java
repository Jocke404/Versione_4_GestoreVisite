package src.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import src.controller.ThreadPoolController;
import src.model.TipiVisitaClass;
import src.model.Visita;
import src.model.Volontario;

/**
 * Gestisce le operazioni CRUD per le visite nel database.
 * Mantiene cache in memoria per visite, date precluse e tipi di visita.
 * Fornisce metodi per gestire tutto il ciclo di vita delle visite guidate.
 * 
 */
public class VisiteManagerDB extends DatabaseManager {
    /** Mappa concorrente delle visite indicizzata per ID */
    private static ConcurrentHashMap<Integer, Visita> visiteMap = new ConcurrentHashMap<>();
    
    /** Mappa delle date in cui non è possibile organizzare visite */
    private ConcurrentHashMap<LocalDate, String> datePrecluseMap = new ConcurrentHashMap<>();

    /**
     * Costruttore del manager delle visite.
     * Carica visite e date precluse dal database.
     * 
     * @param threadPoolManager il controller del thread pool
     */
    public VisiteManagerDB(ThreadPoolController threadPoolManager) {
        super(threadPoolManager);
        caricaVisite();
        caricaDatePrecluse();
    }

    public void caricaVisiteAsync() {
        caricaVisite();
        caricaDatePrecluse();
    }

    /**
     * Restituisce un'istanza singleton del manager delle visite.
     * 
     * @return istanza singleton di VisiteManagerDB
     */
    public static VisiteManagerDB getInstance() {
        return new VisiteManagerDB(ThreadPoolController.getInstance());
    }

    //Logiche delle visite--------------------------------------------------
    
    /**
     * Carica tutte le visite dal database nella mappa in memoria.
     * Svuota la mappa esistente e la riempie con i dati aggiornati.
     */
    protected void caricaVisite() {
        String sql = "SELECT id, titolo, luogo, tipo_visita, volontario, data, stato, max_persone, ora_inizio, durata_minuti, posti_prenotati, min_partecipanti, biglietto, barriere_architettoniche FROM visite";
        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {

            synchronized (visiteMap) {
                visiteMap.clear();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String titolo = rs.getString("titolo");
                    String luogo = rs.getString("luogo");
                    List<TipiVisitaClass> tipoVisita = TipiVisitaClass.fromString(rs.getString("tipo_visita"));
                    String volontario = rs.getString("volontario");
                    LocalDate data = rs.getDate("data") != null ? rs.getDate("data").toLocalDate() : null;  
                    int maxPersone = rs.getInt("max_persone");
                    String stato = rs.getString("stato");
                    LocalTime oraInizio = rs.getTime("ora_inizio") != null ? rs.getTime("ora_inizio").toLocalTime() : null;
                    int durataMinuti = rs.getInt("durata_minuti");
                    int postiPrenotati = rs.getInt("posti_prenotati");
                    int minPartecipanti = rs.getInt("min_partecipanti");
                    boolean biglietto = rs.getBoolean("biglietto");
                    boolean barriereArchitettoniche = rs.getBoolean("barriere_architettoniche");

                     
                    Visita visita = new Visita(id, titolo, luogo, tipoVisita, volontario,
                                                data, maxPersone, stato, oraInizio,
                                                durataMinuti, postiPrenotati, minPartecipanti, biglietto, barriereArchitettoniche);
                    visiteMap.putIfAbsent(id, visita);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento delle visite: " + e.getMessage());
        }
    }

    /**
     * Aggiunge una nuova visita al database.
     * 
     * @param visita la visita da aggiungere
     */
    protected void aggiungiVisita(Visita visita) {
        String inserisciSql = "INSERT INTO visite (luogo, titolo, tipo_visita, volontario, data, stato, max_persone, ora_inizio, durata_minuti, min_partecipanti, biglietto, barriere_architettoniche) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(inserisciSql)) {
    
                pstmt.setString(1, visita.getLuogo());
                pstmt.setString(2, visita.getTitolo());
                pstmt.setString(3, visita.getTipiVisitaClassString());
                pstmt.setString(4, visita.getVolontario());
                pstmt.setDate(5, visita.getData() != null ? java.sql.Date.valueOf(visita.getData()) : null);
                pstmt.setString(6, visita.getStato());
                pstmt.setInt(7, visita.getMaxPersone());
                pstmt.setTime(8, visita.getOraInizio() != null ? java.sql.Time.valueOf(visita.getOraInizio()) : null);
                pstmt.setInt(9, visita.getDurataMinuti());
                pstmt.setInt(10, visita.getMinPartecipanti());
                pstmt.setBoolean(11, visita.isBiglietto());
                pstmt.setBoolean(12, visita.getBarriereArchitettoniche());
                pstmt.executeUpdate();
    
                consoleIO.mostraMessaggio("Visita aggiunta con successo.");
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiunta della visita: " + e.getMessage());
            }
    }

    /**
     * Aggiunge un nuovo tipo di visita al database in modo asincrono.
     * 
     * @param nuovoTipo il nuovo tipo di visita da aggiungere
     */
    protected void aggiungiNuovoTipoVisita(TipiVisitaClass nuovoTipo) {
        String sql = "INSERT INTO tipi_visita (nome, descrizione) VALUES (?, ?)";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nuovoTipo.getNome());
                pstmt.setString(2, nuovoTipo.getDescrizione());
                pstmt.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiunta del nuovo tipo di visita: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiunge una data preclusa al database in modo asincrono.
     * Le date precluse sono giorni in cui non è possibile organizzare visite.
     * 
     * @param data la data da precludere
     * @param motivo il motivo della preclusione
     */
    protected void aggiungiDataPreclusa(LocalDate data, String motivo) {
        String sql = "INSERT INTO date_precluse (data, motivo) VALUES (?, ?)";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(data));
                pstmt.setString(2, motivo);
                pstmt.executeUpdate();

                synchronized (datePrecluseMap) {
                    datePrecluseMap.putIfAbsent(data, motivo);
                }

            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiunta della data preclusa: " + e.getMessage());
            }
        });
    }

    /**
     * Carica tutte le date precluse dal database nella mappa in memoria.
     * Svuota la mappa esistente e la riempie con i dati aggiornati.
     */
    protected void caricaDatePrecluse() {
        String sql = "SELECT data, motivo FROM date_precluse";
        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            synchronized (datePrecluseMap) {
                datePrecluseMap.clear();
                while (rs.next()) {
                    LocalDate data = rs.getDate("data").toLocalDate();
                    String motivo = rs.getString("motivo");
                    datePrecluseMap.putIfAbsent(data, motivo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento delle date precluse: " + e.getMessage());
        }
    }

    /**
     * Elimina una data preclusa dal database in modo asincrono.
     * 
     * @param dataDaEliminare la data da rimuovere dalle date precluse
     */
    protected void eliminaDataPreclusa(LocalDate dataDaEliminare) {
        String sql = "DELETE FROM date_precluse WHERE data = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(dataDaEliminare));
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    caricaDatePrecluse();
                } else {
                    System.err.println("Nessuna data preclusa trovata da eliminare.");
                }
            } catch (SQLException e) {
                System.err.println("Errore durante l'eliminazione della data preclusa: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiorna i dati di una visita esistente nel database in modo asincrono.
     * 
     * @param visitaId l'ID della visita da aggiornare
     * @param visitaAggiornata la visita con i dati aggiornati
     */
    protected void aggiornaVisitaDB(int visitaId, Visita visitaAggiornata) {
        String sql = "UPDATE visite SET luogo = ?, tipo_visita = ?, volontario = ?, data = ?, stato = ?, max_persone = ?, ora_inizio = ?, durata_minuti = ? WHERE id = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, visitaAggiornata.getLuogo());
                pstmt.setString(2, visitaAggiornata.getTipiVisitaClassString());
                pstmt.setString(3, visitaAggiornata.getVolontario());
                pstmt.setDate(4, visitaAggiornata.getData() != null ? java.sql.Date.valueOf(visitaAggiornata.getData()) : null);
                pstmt.setString(5, visitaAggiornata.getStato());
                pstmt.setInt(6, visitaAggiornata.getMaxPersone());
                pstmt.setTime(7, visitaAggiornata.getOraInizio() != null ? java.sql.Time.valueOf(visitaAggiornata.getOraInizio()) : null);
                pstmt.setInt(8, visitaAggiornata.getDurataMinuti());
                pstmt.setInt(9, visitaId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento della visita: " + e.getMessage());
            }
        });
    }

    /**
     * Assegna una visita a un volontario nel database in modo asincrono.
     * 
     * @param volontarioSelezionato il volontario a cui assegnare la visita
     * @param visitaSelezionata la visita da assegnare
     */
    protected void assegnaVisitaAVolontarioDB(Volontario volontarioSelezionato, Visita visitaSelezionata) {
        String sql = "UPDATE visite SET volontario = ? WHERE id = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, volontarioSelezionato.getNome()+" "+volontarioSelezionato.getCognome());
                pstmt.setInt(2, visitaSelezionata.getId());
                pstmt.executeUpdate();

                consoleIO.mostraMessaggio("Visita assegnata con successo al volontario.");
            } catch (SQLException e) {
                System.err.println("Errore durante l'assegnazione della visita al volontario: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiorna il numero massimo di persone per tutte le visite in modo asincrono.
     * 
     * @param maxPersonePerVisita il nuovo numero massimo di persone
     */
    protected void aggiornaMaxPersonePerVisita(int maxPersonePerVisita) {
        String sql = "UPDATE visite SET max_persone = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, maxPersonePerVisita);
                pstmt.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Errore durante l'aggiornamento del numero massimo di persone per visita: " + e.getMessage());
            }
        });
    }

    /**
     * Rimuove un tipo di visita dal sistema aggiornando luoghi, volontari e visite.
     * Cancella le visite associate e rimuove il tipo dai luoghi e dai volontari.
     * Operazione eseguita in modo asincrono.
     * 
     * @param tipoDaRimuovere il tipo di visita da rimuovere
     */
    protected void rimuoviTipoDiVisitaDB(TipiVisitaClass tipoDaRimuovere) {
        String sqlSelectLuoghi = "SELECT nome, tipi_di_visita FROM luoghi WHERE tipi_di_visita LIKE ?";
        String sqlUpdateLuoghi = "UPDATE luoghi SET tipi_di_visita = ? WHERE nome = ?";
        String sqlSelectVolontari = "SELECT id, tipi_di_visite FROM volontari WHERE tipi_di_visite LIKE ?";
        String sqlUpdateVolontari = "UPDATE volontari SET tipi_di_visite = ? WHERE id = ?";
        String sqlTipiVisita = "DELETE FROM tipi_visita WHERE nome = ?";
        String sqlVisite = "UPDATE visite SET stato = ? WHERE tipo_visita = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmtSelectLuoghi = conn.prepareStatement(sqlSelectLuoghi);
                 PreparedStatement pstmtUpdateLuoghi = conn.prepareStatement(sqlUpdateLuoghi);
                 PreparedStatement pstmtSelectVolontari = conn.prepareStatement(sqlSelectVolontari);
                 PreparedStatement pstmtUpdateVolontari = conn.prepareStatement(sqlUpdateVolontari);
                 PreparedStatement pstmtTipiVisita = conn.prepareStatement(sqlTipiVisita);
                 PreparedStatement pstmtVisite = conn.prepareStatement(sqlVisite)) {

                 
                pstmtSelectLuoghi.setString(1, "%" + tipoDaRimuovere.getNome() + "%");
                try (ResultSet rs = pstmtSelectLuoghi.executeQuery()) {
                    while (rs.next()) {
                        String luogoNome = rs.getString("nome");
                        String tipi = rs.getString("tipi_di_visita");
                        if (tipi == null) continue;

                        String[] parts = tipi.split("\\s*,\\s*");
                        List<String> keep = new ArrayList<>();
                        for (String p : parts) {
                            if (!p.equalsIgnoreCase(tipoDaRimuovere.getNome()) && !p.trim().isEmpty()) {
                                keep.add(p.trim());
                            }
                        }
                        String newVal = keep.isEmpty() ? null : String.join(", ", keep);

                        if (newVal == null) {
                            pstmtUpdateLuoghi.setNull(1, java.sql.Types.VARCHAR);
                        } else {
                            pstmtUpdateLuoghi.setString(1, newVal);
                        }
                        pstmtUpdateLuoghi.setString(2, luogoNome);
                        pstmtUpdateLuoghi.executeUpdate();
                    }
                }

                 
                pstmtSelectVolontari.setString(1, "%" + tipoDaRimuovere.getNome() + "%");
                try (ResultSet rs = pstmtSelectVolontari.executeQuery()) {
                    while (rs.next()) {
                        int volontarioId = rs.getInt("id");
                        String tipi = rs.getString("tipi_di_visite");
                        if (tipi == null) continue;

                        String[] parts = tipi.split("\\s*,\\s*");
                        List<String> keep = new ArrayList<>();
                        for (String p : parts) {
                            if (!p.equalsIgnoreCase(tipoDaRimuovere.getNome()) && !p.trim().isEmpty()) {
                                keep.add(p.trim());
                            }
                        }
                        String newVal = keep.isEmpty() ? null : String.join(", ", keep);

                        if (newVal == null) {
                            pstmtUpdateVolontari.setNull(1, java.sql.Types.VARCHAR);
                        } else {
                            pstmtUpdateVolontari.setString(1, newVal);
                        }
                        pstmtUpdateVolontari.setInt(2, volontarioId);
                        pstmtUpdateVolontari.executeUpdate();
                    }
                }

                 
                pstmtVisite.setString(1, "CANCELLATA");
                pstmtVisite.setString(2, tipoDaRimuovere.getNome());
                pstmtVisite.executeUpdate();

                 
                pstmtTipiVisita.setString(1, tipoDaRimuovere.getNome());
                pstmtTipiVisita.executeUpdate();

                 
                caricaVisite();

            } catch (SQLException e) {
                System.err.println("Errore durante la rimozione del tipo di visita: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiunge un nuovo tipo di visita verificando prima che non esista già.
     * 
     * @param nuovoTipo il nuovo tipo di visita da aggiungere
     */
    public void addNuovoTipoVisita(TipiVisitaClass nuovoTipo) {
        String verificaSql = "SELECT 1 FROM tipi_visita WHERE nome = ?";
        if(!recordEsiste(verificaSql, nuovoTipo.getNome())){
            aggiungiNuovoTipoVisita(nuovoTipo);
        } else {
            consoleIO.mostraMessaggio("Il tipo di visita esiste già. Non posso aggiungerlo.");
            return;
        }
    }

    /**
     * Aggiunge una nuova visita verificando prima che non esista già.
     * 
     * @param nuovaVisita la nuova visita da aggiungere
     */
    public void aggiungiNuovaVisita(Visita nuovaVisita) {
        String verificaSql = "SELECT 1 FROM visite WHERE luogo = ? AND data = ? AND volontario = ? AND ora_inizio = ?";
        if(!recordEsiste(verificaSql, nuovaVisita.getLuogo(), nuovaVisita.getData(), nuovaVisita.getVolontario(), nuovaVisita.getOraInizio())){
            consoleIO.mostraMessaggio("La visita non esiste. Procedo con l'aggiunta.");
            aggiungiVisita(nuovaVisita);
        } else {
            consoleIO.mostraMessaggio("La visita esiste già. Non posso aggiungerla.");
            return;
        }
    }

    /**
     * Aggiunge una nuova data preclusa verificando prima che non esista già.
     * 
     * @param data la data da precludere
     * @param motivo il motivo della preclusione
     */
    public void aggiungiNuovaDataPreclusa(LocalDate data, String motivo) {
        String verificaSql = "SELECT 1 FROM date_precluse WHERE data = ?";
        if(!recordEsiste(verificaSql, data)){
            aggiungiDataPreclusa(data, motivo);
        } else {
            consoleIO.mostraMessaggio("La data preclusa esiste già. Non posso aggiungerla.");
            return;
        }
    }

    /**
     * Recupera il numero massimo predefinito di persone per visita dal database.
     * 
     * @return il numero massimo di persone, o 10 se non trovato
     */
    protected int getMaxPersoneDefault() {
        String sql = "SELECT max_persone FROM visite";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
    
            if (rs.next()) {
                return rs.getInt("max_persone");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero del numero massimo di persone: " + e.getMessage());
        }
        return 10;
    }    
    
    /**
     * Elimina una visita dal database.
     * 
     * @param visitaId l'ID della visita da eliminare
     * @return true se l'eliminazione è andata a buon fine, false altrimenti
     */
    protected boolean eliminaVisitaDB(int visitaId){
        String sql = "DELETE FROM visite WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, visitaId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                visiteMap.remove(visitaId);
                consoleIO.mostraMessaggio("Visita eliminata con successo.");
                return true;
            } else {
                consoleIO.mostraMessaggio("Nessuna visita trovata da eliminare.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Errore durante l'eliminazione della visita: " + e.getMessage());
            return false;
        }
    }

    /**
     * Restituisce il numero massimo di persone per visita.
     * 
     * @return il numero massimo di persone
     */
    public int getMaxPersone() {
        return getMaxPersoneDefault();
    }

    /**
     * Restituisce la mappa di tutte le visite.
     * 
     * @return la mappa concorrente delle visite indicizzata per ID
     */
    public ConcurrentHashMap<Integer, Visita> getVisiteMap() {
        return visiteMap;
    }

    /**
     * Recupera tutti i tipi di visita disponibili dal database.
     * 
     * @return lista di tutti i tipi di visita
     */
    public static List<TipiVisitaClass> getTipiVisitaClassList() {
        List<TipiVisitaClass> listTipiVisite = new ArrayList<>();
        String sql = "SELECT nome, descrizione FROM tipi_visita";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String nome = rs.getString("nome");
                String descrizione = rs.getString("descrizione");
                TipiVisitaClass tipoVisita = new TipiVisitaClass(nome, descrizione);
                listTipiVisite.add(tipoVisita);
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero dei tipi di visita: " + e.getMessage());
        }
        return listTipiVisite;
    }

    /**
     * Restituisce la mappa delle date precluse.
     * 
     * @return la mappa concorrente delle date precluse con relativi motivi
     */
    public ConcurrentHashMap<LocalDate, String> getDatePrecluseMap() {
        return datePrecluseMap;
    }

    /**
     * Elimina una data dalle date precluse.
     * 
     * @param data la data da eliminare
     */
    public void eliminaData(LocalDate data){
        eliminaDataPreclusa(data);
        datePrecluseMap.remove(data);
    }

    /**
     * Aggiorna una visita esistente.
     * 
     * @param visitaId l'ID della visita da aggiornare
     * @param visitaAggiornata la visita con i dati aggiornati
     */
    public void aggiornaVisita(int visitaId, Visita visitaAggiornata){
        aggiornaVisitaDB(visitaId, visitaAggiornata);
    }

    /**
     * Aggiorna il numero massimo di persone per tutte le visite.
     * 
     * @param numeroMax il nuovo numero massimo di persone
     */
    public void aggiornaMaxPersone(int numeroMax) {
        aggiornaMaxPersonePerVisita(numeroMax);
    }
    
    /**
     * Elimina una visita dal sistema.
     * 
     * @param visita la visita da eliminare
     */
    public void eliminaVisita(Visita visita){
        eliminaVisitaDB(visita.getId());
    }

    /**
     * Assegna una visita a un volontario.
     * 
     * @param volontarioSelezionato il volontario a cui assegnare la visita
     * @param visitaSelezionata la visita da assegnare
     */
    public void assegnaVisitaAVolontario(Volontario volontarioSelezionato, Visita visitaSelezionata) {
        assegnaVisitaAVolontarioDB(volontarioSelezionato, visitaSelezionata);
    }

    /**
     * Rimuove un tipo di visita dal sistema.
     * 
     * @param tipoDaRimuovere il tipo di visita da rimuovere
     */
    public void rimuoviTipoDiVisita(TipiVisitaClass tipoDaRimuovere) {
        rimuoviTipoDiVisitaDB(tipoDaRimuovere);
    }

    public static Visita getVisitaById(int visitaId) {
        return visiteMap.get(visitaId);
    }
}
