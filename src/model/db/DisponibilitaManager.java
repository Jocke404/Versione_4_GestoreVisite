package src.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.Date;

import src.model.Volontario;

/**
 * Gestisce le disponibilità dei volontari nel database.
 * Fornisce metodi per salvare, recuperare e gestire le date in cui
 * i volontari sono disponibili per condurre visite guidate.
 *  
 */
public class DisponibilitaManager{

    /**
     * Salva le disponibilità di tutti i volontari nel database.
     * Utilizza una transazione per garantire la consistenza dei dati:
     * prima elimina tutte le disponibilità esistenti, poi inserisce quelle nuove.
     * 
     * @param merged mappa dei volontari con le rispettive liste di date disponibili
     * @param volontariManager il manager dei volontari per recuperare gli ID
     * @throws RuntimeException se si verifica un errore durante il salvataggio
     */
    public void salvaDisponibilitaVolontari(Map<Volontario, List<LocalDate>> merged, VolontariManager volontariManager) {
        if (merged == null || merged.isEmpty()) {
            return;
        }
        
        String deleteSql = "DELETE FROM disponibilita WHERE volontario_id = ?";
        String insertSql = "INSERT INTO disponibilita (volontario_id, data_disponibile) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            
            conn.setAutoCommit(false);
            
            try {
                 
                for (Volontario volontario : merged.keySet()) {
                    int id = volontariManager.getIdByEmail(volontario.getEmail());
                    deleteStmt.setInt(1, id);
                    deleteStmt.addBatch();
                }
                deleteStmt.executeBatch();
                
                 
                for (Map.Entry<Volontario, List<LocalDate>> entry : merged.entrySet()) {
                    Volontario volontario = entry.getKey();
                    List<LocalDate> dateDisponibili = entry.getValue();
                    int id = volontariManager.getIdByEmail(volontario.getEmail());
                    
                    if (dateDisponibili != null) {
                        for (LocalDate data : dateDisponibili) {
                            insertStmt.setInt(1, id);
                            insertStmt.setDate(2, Date.valueOf(data));
                            insertStmt.addBatch();
                        }
                    }
                }
                insertStmt.executeBatch();
                
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Errore durante il salvataggio delle disponibilità", e);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione al database", e);
        }
    }

    /**
     * Recupera tutte le date di disponibilità di un volontario specifico.
     * 
     * @param volontarioId l'ID del volontario
     * @return lista delle date in cui il volontario è disponibile, ordinata cronologicamente
     */
    public List<LocalDate> getDisponibilitaByVolontarioId(int volontarioId) {
        List<LocalDate> disponibilita = new ArrayList<>();
        String sql = "SELECT data_disponibile FROM disponibilita WHERE volontario_id = ? ORDER BY data_disponibile";

        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, volontarioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("data_disponibile");
                disponibilita.add(sqlDate.toLocalDate());
            }

        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento delle disponibilità per volontario ID: " + volontarioId + ": " + e.getMessage());
        }

        return disponibilita;
    }

    /**
     * Recupera tutte le disponibilità di tutti i volontari organizzate per email.
     * 
     * @param volontariManager il manager dei volontari per recuperare le email dagli ID
     * @return mappa con email del volontario come chiave e lista di date disponibili come valore
     */
    public ConcurrentHashMap<String, List<LocalDate>> getDisponibilitaMap(VolontariManager volontariManager) {
        ConcurrentHashMap<String, List<LocalDate>> disponibilitaMap = new ConcurrentHashMap<>();
        String sql = "SELECT volontario_id, data_disponibile FROM disponibilita";

        try (Connection conn = DatabaseConnection.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int volontarioId = rs.getInt("volontario_id");
                Date sqlDate = rs.getDate("data_disponibile");
                LocalDate data = sqlDate.toLocalDate();

                disponibilitaMap.computeIfAbsent(volontariManager.getEmailById(volontarioId), k -> new ArrayList<>()).add(data);
            }

        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento delle disponibilità: " + e.getMessage());
        }

        return disponibilitaMap;
    }

}