package src.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import src.controller.ThreadPoolController;
import src.model.Luogo;
import src.model.TipiVisitaClass;

/**
 * Gestisce le operazioni CRUD per i luoghi nel database.
 * Mantiene una cache in memoria sincronizzata con il database
 * per accesso rapido ai dati dei luoghi.
 * 
 */
public class LuoghiManager extends DatabaseManager {
    /** Mappa concorrente dei luoghi indicizzata per nome */
    private ConcurrentHashMap<String, Luogo> luoghiMap = new ConcurrentHashMap<>();

    /**
     * Costruttore del manager dei luoghi.
     * Inizializza il thread pool e carica i luoghi dal database.
     * 
     * @param threadPoolManager il controller del thread pool
     */
    public LuoghiManager(ThreadPoolController threadPoolManager) {
        super(threadPoolManager);
        caricaLuoghi();
    }
    
    /**
     * Carica tutti i luoghi dal database nella mappa in memoria.
     * Svuota la mappa esistente e la riempie con i dati aggiornati.
     */
    protected void caricaLuoghi() {
        String sql = "SELECT nome, descrizione, collocazione, tipi_di_visita FROM luoghi";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            synchronized (luoghiMap) {
                luoghiMap.clear();
                while (rs.next()) {
                    String nome = rs.getString("nome");
                    String tipiVisitaStr = rs.getString("tipi_di_visita");
                    List<TipiVisitaClass> tipiVisitaList = new ArrayList<>();
                    if (tipiVisitaStr != null && !tipiVisitaStr.trim().isEmpty()) {
                        String[] tipiArray = tipiVisitaStr.split(",");
                        for (String tipo : tipiArray) {
                            try {
                                tipiVisitaList.add(TipiVisitaClass.valueOf(tipo.trim()));
                            } catch (IllegalArgumentException e) {
                                System.err.println("Valore non valido per TipiVisitaClass: " + tipo);
                            }
                        }
                    }
                    Luogo luogo = new Luogo(
                        nome,
                        rs.getString("descrizione"),
                        rs.getString("collocazione"),
                        tipiVisitaList
                    );
                luoghiMap.putIfAbsent(nome, luogo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento dei luoghi: " + e.getMessage());
        }
    }

    /**
     * Aggiorna un luogo esistente nel database in modo asincrono.
     * 
     * @param nome il nome del luogo da aggiornare
     * @param luogoAggiornato il luogo con i dati aggiornati
     */
    private void aggiornaLuogo(String nome, Luogo luogoAggiornato) {
        String sql = "UPDATE luoghi SET descrizione = ?, collocazione = ?, tipi_di_visita = ? WHERE nome = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, luogoAggiornato.getDescrizione());
                pstmt.setString(2, luogoAggiornato.getCollocazione());
                String tipiVisitaStr = String.join(",", luogoAggiornato.getTipiVisitaClass()
                                                    .stream().map(t -> t.getNome().toUpperCase()).toList());
                pstmt.setString(3, tipiVisitaStr);
                pstmt.setString(4, nome);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                consoleIO.mostraMessaggio("Errore durante l'aggiornamento del luogo: " + e.getMessage());
            }
        });
    }

    /**
     * Aggiunge un nuovo luogo al database.
     * 
     * @param luogo il luogo da aggiungere
     */
    private void aggiungiLuogo(Luogo luogo) {
        String inserisciSql = "INSERT INTO luoghi (nome, descrizione, collocazione, tipi_di_visita) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(inserisciSql)) {
    
                pstmt.setString(1, luogo.getNome());
                pstmt.setString(2, luogo.getDescrizione());
                pstmt.setString(3, luogo.getCollocazione());
                String tipiVisitaStr = String.join(",", luogo.getTipiVisitaClass().stream().map(TipiVisitaClass::getNome).toList());
                pstmt.setString(4, tipiVisitaStr);
                pstmt.executeUpdate();
    
                consoleIO.mostraMessaggio("Luogo aggiunto con successo.");
            } catch (SQLException e) {
                consoleIO.mostraMessaggio("Errore durante l'aggiunta del luogo: " + e.getMessage());
            }
    }

    /**
     * Aggiunge un nuovo luogo verificando prima che non esista già.
     * 
     * @param nuovoLuogo il nuovo luogo da aggiungere
     */
    public void aggiungiNuovoLuogo(Luogo nuovoLuogo) {
        String verificaSql = "SELECT 1 FROM luoghi WHERE nome = ?";
        if(!recordEsiste(verificaSql, nuovoLuogo.getNome())){
            consoleIO.mostraMessaggio("Il luogo non esiste già. Procedo con l'aggiunta.");
            aggiungiLuogo(nuovoLuogo);
        } else {
            consoleIO.mostraMessaggio("Il luogo esiste già.");
            return;
        }
    }

    /**
     * Rimuove un luogo dal database in modo asincrono.
     * 
     * @param luogoDaEliminare il luogo da eliminare
     */
    private void rimuoviLuogoDalDatabase(Luogo luogoDaEliminare) {
        String sql = "DELETE FROM luoghi WHERE nome = ?";
        executorService.submit(() -> {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, luogoDaEliminare.getNome());
                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted > 0) {
                    consoleIO.mostraMessaggio("Luogo rimosso con successo.");
                } else {
                    consoleIO.mostraMessaggio("Errore: Nessun luogo trovato con il nome specificato.");
                }
            } catch (SQLException e) {
                consoleIO.mostraMessaggio("Errore durante la rimozione del luogo: " + e.getMessage());
            }
        });
    }

    /**
     * Restituisce la mappa di tutti i luoghi.
     * 
     * @return la mappa concorrente dei luoghi indicizzata per nome
     */
    public ConcurrentHashMap<String, Luogo> getLuoghiMap() {
        return luoghiMap;
    }
    
    /**
     * Imposta la mappa dei luoghi.
     * 
     * @param luoghiMap la nuova mappa dei luoghi
     */
    public void setLuoghiMap(ConcurrentHashMap<String, Luogo> luoghiMap) {
        this.luoghiMap = luoghiMap;
    }

    /**
     * Rimuove un luogo sia dal database che dalla mappa in memoria.
     * 
     * @param luogoDaEliminare il luogo da rimuovere
     */
    public void rimuoviLuogo(Luogo luogoDaEliminare) {
        rimuoviLuogoDalDatabase(luogoDaEliminare);
        luoghiMap.remove(luogoDaEliminare.getNome());
    }

    /**
     * Aggiorna un luogo esistente.
     * 
     * @param luogo il luogo con i dati aggiornati
     */
    public void aggiornaLuoghi(Luogo luogo) {
        aggiornaLuogo(luogo.getNome(), luogo);
    }

}
