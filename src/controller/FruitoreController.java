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



public class FruitoreController {
    private final AggiuntaUtilita addUtilita;
    private final ViewUtilita viewUtilita;
    private final ModificaUtilita modificaUtilita;
    Fruitore fruitoreCorrente;
    private final PrenotazioneManager prenotazioneManager;
    private final ConsoleIO consoleIO = new ConsoleIO();

    

    public FruitoreController(FruitoreManager fruitoreManager, AggiuntaUtilita addUtilita, ViewUtilita viewUtilita, ModificaUtilita modificaUtilita,
                            Fruitore fruitoreCorrente, PrenotazioneManager prenotazioneManager) {
        this.modificaUtilita = modificaUtilita;
        this.addUtilita = addUtilita;
        this.viewUtilita = viewUtilita;
        this.fruitoreCorrente = fruitoreCorrente;
        this.prenotazioneManager = prenotazioneManager;
    }

    public void mostraVisiteDisponibili() {
        viewUtilita.visualizzaVisiteDisponibili();
    }

    public void prenotaVisita() {
        addUtilita.prenotaVisita(fruitoreCorrente);
    }

    public void visualizzaMiePrenotazioni() {
        viewUtilita.visualizzaPrenotazioni(fruitoreCorrente, prenotazioneManager);
    }

    // public void cancellaPrenotazione() {
    //     modificaUtilita.cancellaPrenotazione(fruitoreCorrente, prenotazioneManager);
    // }

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
