package src.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import src.model.db.VolontariManager;
import src.model.db.DisponibilitaManager;
import src.model.db.ApplicationSettingsDAO;

/**
 * Gestisce le disponibilità dei volontari per le visite guidate.
 * Questa classe coordina la raccolta, il salvataggio e la consultazione delle
 * disponibilità dei volontari, tenendo conto dei periodi di raccolta prestabiliti.
 * 
 * Le disponibilità vengono raccolte dal 1° al 15 di ogni mese e gestite
 * automaticamente dal sistema. La classe mantiene una mappa thread-safe
 * delle disponibilità e si occupa della sincronizzazione con il database.
 *  
 */
public class Disponibilita {
    /** Mappa thread-safe delle disponibilità per volontario */
    private final Map<Volontario, List<LocalDate>> disponibilitaVolontari = new ConcurrentHashMap<>();
    
    /** Manager per la persistenza delle disponibilità */
    private final DisponibilitaManager disponibilitaManager = new DisponibilitaManager();
    
    /** DAO per le impostazioni dell'applicazione */
    private final ApplicationSettingsDAO applicationSettings = new ApplicationSettingsDAO();
    
    /**
     * Determina se il sistema è attualmente in periodo di raccolta disponibilità.
     * Il periodo di raccolta va dal 1° al 15 di ogni mese.
     * 
     * @return true se è periodo di raccolta (giorni 1-15), false altrimenti
     */
    public Boolean getStato_raccolta() {
        LocalDate oggi = LocalDate.now();
        int giorno = oggi.getDayOfMonth();
         
        return giorno >= 1 && giorno <= 15;
    }

    /**
     * Sincronizza le disponibilità dei volontari caricandole dal database.
     * Pulisce la mappa corrente e ricarica tutte le disponibilità per ogni volontario.
     * Gestisce eventuali errori durante il caricamento dal database.
     * 
     * @param volontariManager Manager per l'accesso ai dati dei volontari
     */
    public void sincronizzaDisponibilitaVolontari(VolontariManager volontariManager) {
        disponibilitaVolontari.clear();
        if (volontariManager == null) return;

         
        for (Volontario v : volontariManager.getVolontariMap().values()) {
            if (v == null || v.getEmail() == null) continue;
            List<LocalDate> dates = Collections.emptyList();
            try {
                 
                dates = disponibilitaManager.getDisponibilitaByVolontarioId(volontariManager.getIdByEmail(v.getEmail()));
            } catch (Exception e) {
                 
            }
            disponibilitaVolontari.put(v, new ArrayList<>(dates == null ? List.of() : dates));
        }
    }

    /**
     * Gestisce i volontari che non hanno ancora inserito le loro disponibilità.
     * Dopo il 15 del mese, assegna automaticamente una lista vuota di disponibilità
     * ai volontari che non hanno ancora fornito i propri dati.
     * 
     * @param volontariManager Manager per l'accesso ai dati dei volontari
     */
    public void gestisciVolontariSenzaDisponibilita(VolontariManager volontariManager) {
        LocalDate oggi = LocalDate.now();
        
        if (oggi.getDayOfMonth() > 15) {
            for (Volontario volontario : volontariManager.getVolontariMap().values()) {
                if (!disponibilitaVolontari.containsKey(volontario)) {
                    disponibilitaVolontari.put(volontario, new ArrayList<>());
                }
            }
             
            salvaStatoERaccolta(disponibilitaVolontari, volontariManager);
        }
    }

    /**
     * Legge le disponibilità di un volontario specifico dal database.
     * 
     * @param volontario Volontario di cui leggere le disponibilità
     * @param volontariManager Manager per ottenere l'ID del volontario
     * @return Lista delle date di disponibilità del volontario
     */
    public List<LocalDate> leggiDisponibilita(Volontario volontario, VolontariManager volontariManager) {
        if (volontario == null) return new ArrayList<>();
        int id = volontariManager.getIdByEmail(volontario.getEmail());
        return disponibilitaManager.getDisponibilitaByVolontarioId(id);
    }

    /**
     * Salva lo stato di raccolta e le disponibilità nel database.
     * Aggiorna lo stato di raccolta corrente e persiste tutte le disponibilità.
     * 
     * @param disponibilita Mappa delle disponibilità per volontario da salvare
     * @param volontariManager Manager per l'accesso ai dati dei volontari
     */
    public void salvaStatoERaccolta(Map<Volontario, List<LocalDate>> disponibilita, VolontariManager volontariManager) {
        if (disponibilita == null) return;
        applicationSettings.setStatoRaccolta(getStato_raccolta());
        disponibilitaManager.salvaDisponibilitaVolontari(disponibilita, volontariManager);
    }

    /**
     * Trova i giorni del mese in cui un volontario è disponibile.
     * 
     * @param volontario Volontario di cui cercare le disponibilità
     * @param ym Anno e mese di interesse
     * @return Lista dei giorni del mese in cui il volontario è disponibile
     */
    public List<Integer> trovaGiorniDisponibili(Volontario volontario, YearMonth ym) {
        List<Integer> giorni = new ArrayList<>();
        List<LocalDate> dates = disponibilitaVolontari.getOrDefault(volontario, new ArrayList<>());
        for (LocalDate d : dates) {
            if (d.getYear() == ym.getYear() && d.getMonthValue() == ym.getMonthValue()) {
                giorni.add(d.getDayOfMonth());
            }
        }
        return giorni;
    }

    /**
     * Ottiene le disponibilità di un volontario tramite la sua email.
     * Effettua una ricerca case-insensitive nell'elenco dei volontari.
     * 
     * @param email Email del volontario di cui cercare le disponibilità
     * @return Lista immutabile delle date di disponibilità, lista vuota se non trovato
     */
    public List<LocalDate> getDisponibilitaByEmail(String email) {
        if (email == null) return new ArrayList<>();
        String target = email.trim().toLowerCase();
        for (Volontario v : disponibilitaVolontari.keySet()) {
            String e = v == null ? null : v.getEmail();
            if (e != null && e.trim().toLowerCase().equals(target)) {
                return Collections.unmodifiableList(disponibilitaVolontari.getOrDefault(v, new ArrayList<>()));
            }
        }
        return new ArrayList<>();
    }

    /**
     * Ottiene le disponibilità di un volontario specifico.
     * 
     * @param volontarioCorrente Volontario di cui ottenere le disponibilità
     * @return Lista delle date di disponibilità del volontario
     */
    public List<LocalDate> getDisponibilitaVolontario(Volontario volontarioCorrente) {
        return disponibilitaVolontari.getOrDefault(volontarioCorrente, new ArrayList<>());
    }

    /**
     * Unico punto di gestione delle disponibilità.
     * @param volontario volontario interessato
     * @param nuoveDisponibilita lista di date (null => lista vuota se replace; ignorata se merge && null)
     * @param volontariManager manager DB necessario per la persistenza
     * @param merge se true aggiunge le date a quelle esistenti (evita duplicati); se false sostituisce
     */
    public void salvaDisponibilita(Volontario volontario, List<LocalDate> nuoveDisponibilita,
            VolontariManager volontariManager, boolean merge) {
        if (volontario == null) return;

        List<LocalDate> corrente = new ArrayList<>(disponibilitaVolontari.getOrDefault(volontario, new ArrayList<>()));

        if (merge) {
            if (nuoveDisponibilita != null) {
                for (LocalDate d : nuoveDisponibilita) {
                    if (d == null) continue;
                    if (!corrente.contains(d)) corrente.add(d);
                }
            }
        } else {
             
            corrente = nuoveDisponibilita == null ? new ArrayList<>() : new ArrayList<>(nuoveDisponibilita);
            corrente.removeIf(d -> d == null);
        }

        corrente.sort(LocalDate::compareTo);
        disponibilitaVolontari.put(volontario, corrente);

         
        salvaStatoERaccolta(disponibilitaVolontari, volontariManager);
    }
}