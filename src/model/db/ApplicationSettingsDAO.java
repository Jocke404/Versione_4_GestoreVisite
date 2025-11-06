package src.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object per la gestione delle impostazioni dell'applicazione.
 * Gestisce il campo territorial_scope (ambito territoriale) salvato come JSON array
 * e altre impostazioni globali come il numero massimo di persone per visita.
 * 
 *  
 *  
 */
public class ApplicationSettingsDAO {

    private static final String SELECT_SQL = "SELECT territorial_scope FROM application_settings LIMIT 1";
    private static final String UPDATE_SQL = "UPDATE application_settings SET territorial_scope = ?, updated_at = CURRENT_TIMESTAMP";
    private static final String INSERT_SQL = "INSERT INTO application_settings(territorial_scope, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    private static final String UPDATE_STATO_RACCOLTA_SQL = "UPDATE application_settings SET stato_raccolta = ?, updated_at = CURRENT_TIMESTAMP";
    
    private static final String SELECT_MAX_SQL = "SELECT max_people_per_visit FROM application_settings LIMIT 1";
    private static final String UPDATE_MAX_SQL = "UPDATE application_settings SET max_people_per_visit = ?, updated_at = CURRENT_TIMESTAMP";
    private static final String INSERT_MAX_SQL = "INSERT INTO application_settings(max_people_per_visit, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

    /**
     * Recupera il numero massimo di persone per visita dalle impostazioni.
     * 
     * @return il numero massimo di persone, o null se non impostato
     */
    public static Integer getMaxPeoplePerVisit() {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(SELECT_MAX_SQL);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int v = rs.getInt("max_people_per_visit");
                if (rs.wasNull()) return null;
                return v;
            }
        } catch (SQLException e) {
            System.err.println("ApplicationSettingsDAO.getMaxPeoplePerVisit error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Imposta il numero massimo di persone per visita nelle impostazioni.
     * Aggiorna se esiste già un record, altrimenti ne inserisce uno nuovo.
     * 
     * @param max il numero massimo di persone
     * @return true se l'operazione ha successo, false altrimenti
     */
    public static boolean setMaxPeoplePerVisit(int max) {
        try (Connection conn = DatabaseConnection.connect()) {
            try (PreparedStatement upd = conn.prepareStatement(UPDATE_MAX_SQL)) {
                upd.setInt(1, max);
                int u = upd.executeUpdate();
                if (u > 0) return true;
            }
            try (PreparedStatement ins = conn.prepareStatement(INSERT_MAX_SQL)) {
                ins.setInt(1, max);
                ins.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ApplicationSettingsDAO.setMaxPeoplePerVisit error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica se è stato impostato un ambito territoriale.
     * 
     * @return true se l'ambito territoriale è impostato, false altrimenti
     */
    public static boolean hasTerritorialScope() {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String v = rs.getString("territorial_scope");
                return v != null && !v.trim().isEmpty();
            }
        } catch (SQLException e) {
            System.err.println("ApplicationSettingsDAO.hasTerritorialScope error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Recupera l'ambito territoriale dalle impostazioni.
     * L'ambito è salvato come JSON array e viene parsato in una lista di stringhe.
     * 
     * @return lista degli ambiti territoriali configurati
     */
    public static List<String> getTerritorialScope() {
        List<String> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String v = rs.getString("territorial_scope");
                if (v != null && !v.trim().isEmpty()) {
                    String s = v.trim();

                     
                    int idxOpen = s.indexOf('[');
                    int idxClose = s.lastIndexOf(']');
                    String target = s;
                    if (idxOpen >= 0 && idxClose > idxOpen) {
                        target = s.substring(idxOpen + 1, idxClose);
                    }

                     
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"([^\"]+)\"");
                    java.util.regex.Matcher m = p.matcher(target);
                    while (m.find()) {
                        String item = m.group(1).trim();
                        if (!item.isEmpty()) result.add(item);
                    }

                     
                    if (result.isEmpty()) {
                        for (String part : target.split("\\R|,")) {
                            String tt = part.trim().replaceAll("[\\[\\]\\{\\}\\\"]", "");
                            if (!tt.isEmpty()) result.add(tt);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ApplicationSettingsDAO.getTerritorialScope error: " + e.getMessage());
        }
        return result;
    }

    /**
     * Imposta l'ambito territoriale nelle impostazioni.
     * Salva la lista come JSON array nel database.
     * Aggiorna se esiste già un record, altrimenti ne inserisce uno nuovo.
     * 
     * @param ambiti lista degli ambiti territoriali da salvare
     * @return true se l'operazione ha successo, false altrimenti
     */
    public static boolean setTerritorialScope(List<String> ambiti) {
        String json = toJsonArray(ambiti);
        try (Connection conn = DatabaseConnection.connect()) {
             
            try (PreparedStatement upd = conn.prepareStatement(UPDATE_SQL)) {
                upd.setString(1, json);
                int u = upd.executeUpdate();
                if (u > 0) return true;
            }
             
            try (PreparedStatement ins = conn.prepareStatement(INSERT_SQL)) {
                ins.setString(1, json);
                ins.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ApplicationSettingsDAO.setTerritorialScope error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Converte una lista di stringhe in un array JSON.
     * 
     * @param items la lista di stringhe da convertire
     * @return la rappresentazione JSON array delle stringhe
     */
    private static String toJsonArray(List<String> items) {
        if (items == null || items.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (String s : items) {
            if (!first) sb.append(", ");
            first = false;
            sb.append("\"").append(s.replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Imposta lo stato della raccolta dati nelle impostazioni.
     * 
     * @param stato true se la raccolta è attiva, false altrimenti
     */
    public void setStatoRaccolta(Boolean stato) {
        try (Connection conn = DatabaseConnection.connect()) {
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_STATO_RACCOLTA_SQL)) {
                ps.setBoolean(1, stato);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("ApplicationSettingsDAO.setStatoRaccolta error: " + e.getMessage());
        }
    }
}
