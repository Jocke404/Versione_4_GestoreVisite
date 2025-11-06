package src.controller;

import java.util.List;

import src.model.AggiuntaUtilita;
import src.model.Fruitore;
import src.model.ModificaUtilita;
import src.model.Prenotazione;
import src.model.db.FruitoreManager;
import src.model.db.PrenotazioneManager;
import src.view.ViewUtilita;
import src.view.ConsoleIO;

/**
 * Controller per le operazioni dei fruitori (visitatori).
 * Gestisce la visualizzazione delle visite disponibili, le prenotazioni
 * e la loro cancellazione per l'utente fruitore corrente.
 * 
 *  
 *  
 */
public class FruitoreController {
    /** Utility per l'aggiunta di nuovi elementi */
    private final AggiuntaUtilita addUtilita;
    
    /** Utility per la visualizzazione dei dati */
    private final ViewUtilita viewUtilita;
    
    /** Utility per la modifica di elementi esistenti */
    private final ModificaUtilita modificaUtilita;
    
    /** Il fruitore attualmente autenticato */
    Fruitore fruitoreCorrente;
    
    /** Manager per le operazioni sulle prenotazioni */
    private final PrenotazioneManager prenotazioneManager;
    
    /** Interfaccia per l'interazione con l'utente */
    private final ConsoleIO consoleIO = new ConsoleIO();

    /**
     * Costruttore del controller del fruitore.
     * 
     * @param fruitoreManager il manager dei fruitori
     * @param addUtilita l'utility per aggiungere elementi
     * @param viewUtilita l'utility per visualizzare dati
     * @param modificaUtilita l'utility per modificare elementi
     * @param fruitoreCorrente il fruitore autenticato
     * @param prenotazioneManager il manager delle prenotazioni
     */
    public FruitoreController(FruitoreManager fruitoreManager, AggiuntaUtilita addUtilita, ViewUtilita viewUtilita, ModificaUtilita modificaUtilita,
                            Fruitore fruitoreCorrente, PrenotazioneManager prenotazioneManager) {
        this.modificaUtilita = modificaUtilita;
        this.addUtilita = addUtilita;
        this.viewUtilita = viewUtilita;
        this.fruitoreCorrente = fruitoreCorrente;
        this.prenotazioneManager = prenotazioneManager;
    }

    /**
     * Visualizza tutte le visite disponibili per la prenotazione.
     */
    public void mostraVisiteDisponibili() {
        viewUtilita.visualizzaVisiteDisponibili();
    }

    /**
     * Avvia il processo di prenotazione di una visita per il fruitore corrente.
     */
    public void prenotaVisita() {
        addUtilita.prenotaVisita(fruitoreCorrente);
    }

    /**
     * Visualizza tutte le prenotazioni del fruitore corrente.
     */
    public void visualizzaMiePrenotazioni() {
        viewUtilita.visualizzaPrenotazioni(fruitoreCorrente, prenotazioneManager);
    }

    /**
     * Gestisce la cancellazione di una prenotazione del fruitore corrente.
     * Mostra le prenotazioni esistenti, permette la selezione e richiede conferma
     * prima di procedere con la cancellazione.
     */
    public void cancellaPrenotazione() {
        List<Prenotazione> prenotazioni = prenotazioneManager.miePrenotazioni(fruitoreCorrente);
        if (prenotazioni.isEmpty()) {
            consoleIO.mostraMessaggio("Non hai prenotazioni da cancellare.");
            return;
        }
        int scelta = consoleIO.chiediSelezionePrenotazione(prenotazioni);
        Prenotazione prenotazioneDaCancellare = prenotazioni.get(scelta);
        if (consoleIO.chiediConfermaCancellazionePrenotazione(prenotazioneDaCancellare)) {
            boolean successo = modificaUtilita.cancellaPrenotazione(prenotazioneDaCancellare, prenotazioneManager);
            consoleIO.mostraRisultatoCancellazionePrenotazione(successo);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }


}
