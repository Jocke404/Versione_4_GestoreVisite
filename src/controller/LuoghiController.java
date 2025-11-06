package src.controller;

import src.model.Luogo;
import src.model.db.LuoghiManager;
import src.view.ViewUtilita;

import java.util.List;

/**
 * Controller per la gestione dei luoghi delle visite.
 * Fornisce un'interfaccia semplificata per visualizzare, modificare
 * ed eliminare luoghi, delegando le operazioni al manager appropriato.
 * 
 *  
 *  
 */
public class LuoghiController {
    /** Manager per le operazioni sui luoghi nel database */
    private final LuoghiManager luoghiManager;
    
    /** Utility per la visualizzazione dei dati */
    private final ViewUtilita viewUtilita;

    /**
     * Costruttore del controller dei luoghi.
     * 
     * @param luoghiManager il manager dei luoghi
     * @param viewUtilita l'utility per la visualizzazione
     */
    public LuoghiController(LuoghiManager luoghiManager, ViewUtilita viewUtilita) {
        this.luoghiManager = luoghiManager;
        this.viewUtilita = viewUtilita;
    }

    /**
     * Visualizza l'elenco di tutti i luoghi.
     */
    public void mostraLuoghi() {
        viewUtilita.stampaLuoghi(this);
    }

    /**
     * Restituisce la lista di tutti i luoghi.
     * 
     * @return copia immutabile della lista dei luoghi
     */
    public List<Luogo> getLuoghi() {
        return List.copyOf(luoghiManager.getLuoghiMap().values());
    }

    /**
     * Elimina un luogo dal sistema.
     * 
     * @param luogoDaEliminare il luogo da rimuovere
     */
    public void eliminaLuogo(Luogo luogoDaEliminare) {
        luoghiManager.rimuoviLuogo(luogoDaEliminare);
    }

    /**
     * Aggiorna i dati di un luogo esistente.
     * 
     * @param luogoDaModificare il luogo con i dati aggiornati
     */
    public void aggiornaLuoghi(Luogo luogoDaModificare) {
        luoghiManager.aggiornaLuoghi(luogoDaModificare);
    }


}
