package src.controller;

import src.model.TipiVisitaClass;
import src.model.Visita;
import src.model.db.VisiteManagerDB;

import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller per la gestione delle visite guidate.
 * Fornisce un'interfaccia semplificata per accedere a visite,
 * tipi di visita e date precluse, delegando le operazioni al manager.
 * 
 *  
 *  
 */
public class VisiteController {
    /** Manager per le operazioni sulle visite nel database */
    private final VisiteManagerDB visiteManagerDB;

    /**
     * Costruttore del controller delle visite.
     * 
     * @param visiteManagerDB il manager delle visite
     */
    public VisiteController(VisiteManagerDB visiteManagerDB) {
        this.visiteManagerDB = visiteManagerDB;
    }

    /**
     * Restituisce la lista di tutti i tipi di visita disponibili.
     * 
     * @return lista dei tipi di visita
     */
    public List<TipiVisitaClass> getTipiVisitaClassList() {
        return visiteManagerDB.getTipiVisitaClassList();
    }

    /**
     * Restituisce la lista di tutte le visite.
     * 
     * @return copia immutabile della lista delle visite
     */
    public List<Visita> getVisite() {
        return List.copyOf(visiteManagerDB.getVisiteMap().values());
    }

    /**
     * Restituisce la mappa di tutte le visite indicizzata per ID.
     * 
     * @return la mappa concorrente delle visite
     */
    public ConcurrentHashMap<Integer, Visita> getVisiteMap() {
        return visiteManagerDB.getVisiteMap();
    }

    /**
     * Restituisce la lista delle date precluse con i relativi motivi.
     * 
     * @return copia immutabile della lista di entry (data, motivo)
     */
    public List<Map.Entry<LocalDate, String>> getDatePrecluse() {
        return List.copyOf(visiteManagerDB.getDatePrecluseMap().entrySet());
    }

    /**
     * Elimina una visita dal sistema.
     * 
     * @param visita la visita da eliminare
     */
    public void eliminaVisita(Visita visita) {
        visiteManagerDB.eliminaVisita(visita);
    }
}