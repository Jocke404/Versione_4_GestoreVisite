package src.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lib.InputDati;
import src.model.AggiuntaUtilita;
import src.model.ModificaUtilita;
import src.model.TipiVisitaClass;
import src.model.Visita;
import src.model.Volontario;
import src.model.db.LuoghiManager;
import src.model.db.PrenotazioneManager;
import src.model.db.VisiteManagerDB;
import src.model.db.VolontariManager;
import src.view.ViewUtilita;
import src.model.AmbitoTerritoriale;
import src.model.Luogo;
import src.view.ConsoleIO;

/**
 * Controller per le operazioni dei configuratori (amministratori).
 * Gestisce tutte le funzionalità amministrative del sistema: gestione volontari,
 * luoghi, visite, date precluse, tipi di visita e configurazioni globali.
 * 
 *  
 *  
 */
public class ConfiguratoriController {
    /** Utility per l'aggiunta di nuovi elementi */
    private final AggiuntaUtilita addUtilita;
    
    /** Utility per la modifica di elementi esistenti */
    private final ModificaUtilita modificaUtilita;
    
    /** Utility per la visualizzazione dei dati */
    private final ViewUtilita viewUtilita;
    
    /** Gestione dell'ambito territoriale delle visite */
    private final AmbitoTerritoriale ambitoTerritoriale = new AmbitoTerritoriale();

    /** Controller dedicato ai volontari */
    private final VolontariController volontariController;
    
    /** Controller dedicato ai luoghi */
    private final LuoghiController luoghiController;
    
    /** Controller dedicato alle visite */
    private final VisiteController visiteController;
    
    /** Interfaccia per l'interazione con l'utente */
    private ConsoleIO consoleIO = new ConsoleIO();

    /** Manager per le operazioni sui volontari */
    private VolontariManager volontariManager;
    
    /** Manager per le operazioni sui luoghi */
    private LuoghiManager luoghiManager;
    
    /** Manager per le operazioni sulle visite */
    private VisiteManagerDB visiteManagerDB;

    /** Manager per le operazioni sulle prenotazioni */
    private PrenotazioneManager prenotazioneManager;

    /**
     * Costruttore del controller dei configuratori.
     * 
     * @param addUtilita l'utility per aggiungere elementi
     * @param modificaUtilita l'utility per modificare elementi
     * @param viewUtilita l'utility per visualizzare dati
     * @param volontariController il controller dei volontari
     * @param luoghiController il controller dei luoghi
     * @param visiteController il controller delle visite
     * @param visiteManagerDB il manager delle visite
     * @param volontariManager il manager dei volontari
     * @param luoghiManager il manager dei luoghi
     * @param prenotazioneManager il manager delle prenotazioni
     */
    public ConfiguratoriController(
        AggiuntaUtilita addUtilita, 
        ModificaUtilita modificaUtilita, 
        ViewUtilita viewUtilita, 
        VolontariController volontariController,
        LuoghiController luoghiController,
        VisiteController visiteController,
        VisiteManagerDB visiteManagerDB,
        VolontariManager volontariManager,
        LuoghiManager luoghiManager,
        PrenotazioneManager prenotazioneManager
    ) {
        this.addUtilita = addUtilita;
        this.modificaUtilita = modificaUtilita;
        this.viewUtilita = viewUtilita;
        this.volontariController = volontariController;
        this.luoghiController = luoghiController;
        this.visiteController = visiteController;
        this.visiteManagerDB = visiteManagerDB;
        this.volontariManager = volontariManager;   
        this.luoghiManager = luoghiManager;
        this.prenotazioneManager = prenotazioneManager;
    }

    /**
     * Aggiunge un nuovo volontario al sistema.
     * Mostra l'elenco dei volontari esistenti, raccoglie i dati e richiede conferma.
     */
    public void aggiungiVolontario() {
        consoleIO.mostraElencoConOggetti(volontariManager.getVolontariMap().values().stream().toList());
         
        Volontario nuovoVolontario = consoleIO.chiediDatiNuovoVolontario();
        if (nuovoVolontario != null && InputDati.yesOrNo("Vuoi confermare e aggiungere il volontario?")) {
            addUtilita.aggiungiVolontario(nuovoVolontario);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    /**
     * Visualizza l'elenco di tutti i volontari.
     */
    public void mostraVolontari() {
        viewUtilita.stampaVolontari(volontariController);
    }

    /**
     * Aggiunge un nuovo luogo al sistema.
     * Mostra i luoghi esistenti, raccoglie i dati verificando l'ambito territoriale
     * e richiede conferma prima dell'aggiunta.
     */
    public void aggiungiLuogo() {
        consoleIO.mostraElencoConOggetti(luoghiManager.getLuoghiMap().values().stream().toList());
         
        Luogo nuovoLuogo = consoleIO.chiediDatiNuovoLuogo(ambitoTerritoriale);
        if (nuovoLuogo != null && InputDati.yesOrNo("Vuoi confermare e aggiungere il luogo?")) {
            addUtilita.aggiungiLuogo(nuovoLuogo);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    /**
     * Visualizza l'elenco di tutti i luoghi.
     */
    public void mostraLuoghi() {
        viewUtilita.stampaLuoghi(luoghiController);
    }

    /**
     * Visualizza l'elenco di tutte le visite.
     */
    public void mostraVisite() {
        viewUtilita.stampaVisite(visiteController);
    }
    
    /**
     * Visualizza le visite raggruppate per stato (Proposta, Confermata, ecc.).
     */
    public void visualizzaVisitePerStato(){
        viewUtilita.stampaVisitePerStato();
    }

    /**
     * Modifica il numero massimo di persone per visita.
     * Mostra il valore attuale e permette di modificarlo previa conferma.
     */
    public void modificaMaxPersone() {
        consoleIO.mostraMaxPersonePerVisita(visiteManagerDB);
        if (consoleIO.chiediAnnullaOperazione()) return;
        int numeroMax = consoleIO.chiediNumeroMaxPersone(visiteManagerDB);
        if (consoleIO.chiediConfermaNumeroMax(numeroMax)) {
            modificaUtilita.aggiornaMaxPersone(numeroMax);
            consoleIO.mostraRisultatoAggiornamentoMaxPersone(true, numeroMax);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void modificaDataVisita() {
        List<Visita> visite = new ArrayList<>(visiteManagerDB.getVisiteMap().values());
        if (visite.isEmpty()) {
            consoleIO.mostraMessaggio("Non ci sono visite disponibili da modificare.");
            return;
        }
        int scelta = consoleIO.chiediSelezioneVisita(visite);
        Visita visitaSelezionata = visite.get(scelta);
        LocalDate dataOriginale = visitaSelezionata.getData();
        LocalDate nuovaData = consoleIO.chiediNuovaDataVisita(dataOriginale);
        String statoOriginale = visitaSelezionata.getStato();
        String[] stati = {"Proposta", "Completa", "Confermata", "Cancellata", "Effettuata"};
        String nuovoStato = consoleIO.chiediNuovoStato(stati);
        if (consoleIO.chiediConfermaModificaData(dataOriginale, nuovaData)) {
            boolean successo = modificaUtilita.aggiornaDataVisita(visitaSelezionata.getId(), nuovaData);
            consoleIO.mostraRisultatoModificaData(successo);
            if (successo) {
                // se la nuova data è futura rispetto ad oggi, forziamo lo stato a "Proposta"
                if (nuovaData.isAfter(LocalDate.now())) {
                    boolean statoAggiornato = modificaUtilita.aggiornaStatoVisita(visitaSelezionata.getId(), "Proposta");
                    if (statoAggiornato) {
                        consoleIO.mostraMessaggio("Data aggiornata: la visita è stata spostata in futuro e lo stato è stato impostato a 'Proposta'.");
                        // Aggiorna anche l'oggetto in memoria per coerenza
                        visitaSelezionata.setData(nuovaData);
                        visitaSelezionata.setStato("Proposta");
                    } else {
                        consoleIO.mostraMessaggio("Data aggiornata ma non è stato possibile aggiornare lo stato a 'Proposta'.");
                    }
                } else {
                    // se la data non è futura, lasciare la scelta di stato all'operatore (se diversa)
                    if (nuovoStato != null && !nuovoStato.equals(statoOriginale)) {
                        boolean statoAggiornato = modificaUtilita.aggiornaStatoVisita(visitaSelezionata.getId(), nuovoStato);
                        if (statoAggiornato) {
                            consoleIO.mostraMessaggio("Stato della visita aggiornato a: " + nuovoStato);
                            visitaSelezionata.setStato(nuovoStato);
                        } else {
                            consoleIO.mostraMessaggio("Impossibile aggiornare lo stato della visita.");
                        }
                    } else {
                        // aggiorniamo comunque la data in memoria
                        visitaSelezionata.setData(nuovaData);
                    }
                }
            }
        } else {
            consoleIO.mostraMessaggio("Modifica annullata. Nessun cambiamento effettuato.");
        }
    }

    public void aggiungiVisita() {
        consoleIO.mostraElencoConOggetti(visiteManagerDB.getVisiteMap().values().stream().toList());
        Visita nuovaVisita = null;
        if (consoleIO.chiediAnnullaOperazione())
            return;

        if (InputDati.yesOrNo("Vuoi pianificare la visita usando le disponibilità dei volontari? (s/n)")) {
            nuovaVisita = consoleIO.pianificazioneGuidata(visiteManagerDB, volontariManager, luoghiManager);
        } else {
            nuovaVisita = consoleIO.pianificazioneLibera(visiteManagerDB, volontariManager, luoghiManager, prenotazioneManager);
        }


        if (nuovaVisita != null && InputDati.yesOrNo("Vuoi confermare e aggiungere la visita?")) {
            addUtilita.aggiungiVisita(nuovaVisita);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void modificaStatoVisita() {
        List<Visita> visite = new ArrayList<>(visiteManagerDB.getVisiteMap().values());
        if (visite.isEmpty()) {
            consoleIO.mostraMessaggio("Non ci sono visite disponibili da modificare.");
            return;
        }
        consoleIO.mostraVisiteDisponibili(visite);

        if (consoleIO.chiediAnnullaOperazione()) {
            return;
        }
        int scelta = consoleIO.chiediSelezioneVisita(visite.size());
        Visita visitaSelezionata = visite.get(scelta - 1);

        String statoOriginale = visitaSelezionata.getStato();
        String[] stati = {"Proposta", "Completa", "Confermata", "Cancellata", "Effettuata"};
        String nuovoStato = consoleIO.chiediNuovoStato(stati);

        if (consoleIO.chiediConfermaModifica(statoOriginale, nuovoStato)) {
            boolean successo = modificaUtilita.aggiornaStatoVisita(visitaSelezionata.getId(), nuovoStato);
            if (successo) {
                consoleIO.mostraMessaggio("Stato della visita aggiornato con successo.");
            } else {
                consoleIO.mostraMessaggio("Errore nell'aggiornamento dello stato.");
            }
        } else {
            consoleIO.mostraMessaggio("Modifica annullata. Nessun cambiamento effettuato.");
        }
    }

    public void visualizzaArchivioStorico() {
        viewUtilita.stampaArchivioStorico(visiteController);
    }

    public void aggiungiDatePrecluse() {
        consoleIO.mostraElencoConOggetti(visiteManagerDB.getDatePrecluseMap().entrySet().stream().toList());
        if (consoleIO.chiediAnnullaOperazione()) return;
        LocalDate data = null;
        boolean continua = true;
        do {
            if (InputDati.yesOrNo("Vuoi aggiungere una data personale? (s/n)")) {
                data = consoleIO.chiediDataPreclusa();
                continua = false;
            } else {
                data = consoleIO.chiediDataPreclusaStandard();
                continua = false;
            }
        } while (continua);            
        if (data != null) {
                String motivo = consoleIO.chiediMotivoPreclusione(data);
                addUtilita.aggiungiDataPreclusa(data, motivo);
                consoleIO.mostraMessaggio("Data preclusa aggiunta con successo.");
            }
    }

    public void mostraDatePrecluse() {
        viewUtilita.stampaDatePrecluse(visiteController);
    }

    public void eliminaDatePrecluse() {
        ConcurrentHashMap<LocalDate, String> datePrecluse = visiteManagerDB.getDatePrecluseMap();
        if (datePrecluse.isEmpty()) {
            consoleIO.mostraMessaggio("Non ci sono date precluse da eliminare.");
            return;
        }
        int scelta = consoleIO.chiediDataPreclusaDaEliminare(datePrecluse);
        LocalDate dataDaEliminare = new ArrayList<>(datePrecluse.keySet()).get(scelta);
        if (consoleIO.chiediConfermaEliminazioneData(dataDaEliminare)) {
            boolean successo = modificaUtilita.eliminaDataPreclusa(dataDaEliminare);
            consoleIO.mostraRisultatoEliminazioneData(successo);
        } else {
            consoleIO.mostraRisultatoEliminazioneData(false);
        }
    }

    public void mostraAmbitoTerritoriale() {
        viewUtilita.stampaAmbitoTerritoriale(ambitoTerritoriale);
    }

    public void stampaTipiVisitaClassPerLuogo(){
       viewUtilita.stampaTipiVisitaClassPerLuogo(luoghiController);
    }

    public void eliminaLuogo() {
        List<Luogo> luoghi = luoghiController.getLuoghi();
        if (luoghi.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun luogo disponibile per la modifica.");
            return;
        }
        int scelta = consoleIO.chiediSelezioneLuogo(luoghi);
        Luogo luogoDaEliminare = luoghi.get(scelta);
        if (consoleIO.chiediConfermaEliminazioneLuogo(luogoDaEliminare)) {
            modificaUtilita.eliminaLuogo(luogoDaEliminare, luoghiController);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void modificaLuogo() {
        List<Luogo> luoghi = luoghiController.getLuoghi();
        if (luoghi.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun luogo disponibile per la modifica.");
            return;
        }
        int scelta = consoleIO.chiediSelezioneLuogo(luoghi);
        Luogo luogoDaModificare = luoghi.get(scelta);

        String nuovoNome = consoleIO.chiediNuovoNomeLuogo(luogoDaModificare.getNome());
        String nuovaDescrizione = consoleIO.chiediNuovaDescrizioneLuogo(luogoDaModificare.getDescrizione());
        String nuovaCollocazione = consoleIO.chiediNuovaCollocazioneLuogo(luogoDaModificare.getCollocazione());
        List<TipiVisitaClass> nuoviTipi = consoleIO.chiediNuoviTipiVisitaClass(luogoDaModificare.getTipiVisitaClass());

        consoleIO.mostraConfrontoLuogo(luogoDaModificare, nuovoNome, nuovaDescrizione, nuovaCollocazione, nuoviTipi);

        if (InputDati.yesOrNo("Vuoi confermare e salvare le modifiche?")) {
            modificaUtilita.aggiornaLuogo(luogoDaModificare, nuovoNome, nuovaDescrizione, nuovaCollocazione, nuoviTipi, luoghiController);
        } else {
            consoleIO.mostraMessaggio("Modifiche annullate. Nessun cambiamento effettuato.");
        }
    }

    public void eliminaVolontario() {
        List<Volontario> volontari = volontariController.getVolontari();
        if (volontari.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun volontario disponibile per la modifica.");
            return;
        }
        List<Volontario> selezionati = consoleIO.chiediVolontariMultipli(volontari);
        for (Volontario volontarioDaEliminare : selezionati) {
            if (consoleIO.chiediConfermaEliminazioneVolontario(volontarioDaEliminare)) {
                modificaUtilita.eliminaVolontario(volontarioDaEliminare, volontariController);
            } else {
                consoleIO.mostraMessaggio("Operazione annullata.");
            }
        }
    }

    public void aggiungiVolontariATipoVisita() {
        if (consoleIO.chiediAnnullaOperazione()) return;

        Map<String, Volontario> volontariMap = volontariManager.getVolontariMap();
        List<TipiVisitaClass> tipiVisitaList = visiteManagerDB.getTipiVisitaClassList();

        if (tipiVisitaList.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun tipo di visita disponibile.");
            return;
        }
        TipiVisitaClass tipoVisitaScelto = consoleIO.chiediTipoVisita(tipiVisitaList);

        if (volontariMap.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun volontario disponibile.");
            return;
        }
        List<Volontario> volontariDisponibili = new ArrayList<>(volontariMap.values());
        List<Volontario> volontariSelezionati = consoleIO.chiediVolontariMultipli(volontariDisponibili);

        addUtilita.assegnaTipoVisitaAVolontari(volontariSelezionati, tipoVisitaScelto);
        consoleIO.mostraMessaggio("Tipo di visita " + tipoVisitaScelto + " assegnato a " + volontariSelezionati.size() + " volontari.");
    }

    public void rimuoviTipoVisitaDaVolontari() {
        if (consoleIO.chiediAnnullaOperazione()) return;

        Map<String, Volontario> volontariMap = volontariManager.getVolontariMap();
        List<TipiVisitaClass> tipiVisitaList = visiteManagerDB.getTipiVisitaClassList();

        if (tipiVisitaList.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun tipo di visita disponibile.");
            return;
        }
        TipiVisitaClass tipoVisitaScelto = consoleIO.chiediTipoVisita(tipiVisitaList);

        List<Volontario> volontariConTipoVisita = volontariMap.values().stream()
            .filter(v -> v.getTipiDiVisite().contains(tipoVisitaScelto))
            .toList();

        if (volontariConTipoVisita.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun volontario ha questo tipo di visita assegnato.");
            return;
        }
        List<Volontario> volontariSelezionati = consoleIO.chiediVolontariMultipli(volontariConTipoVisita);

        modificaUtilita.rimuoviTipoVisitaDaVolontari(volontariSelezionati, tipoVisitaScelto, volontariManager);
        consoleIO.mostraMessaggio("Tipo di visita " + tipoVisitaScelto + " rimosso da " + volontariSelezionati.size() + " volontari.");
    }

    public void visualizzaVolontariPerTipoVisita(){
        viewUtilita.visualizzaVolontariPerTipoVisita(volontariManager);
    }

    public void eliminaVisita() {
        if (consoleIO.chiediAnnullaOperazione()) return;
        List<Visita> visite = visiteController.getVisite();
        if (visite.isEmpty()) {
            consoleIO.mostraMessaggio("Nessuna visita disponibile per la modifica.");
            return;
        }
        int scelta = consoleIO.chiediSelezioneVisita(visite);
        Visita visitaDaEliminare = visite.get(scelta);
        if (consoleIO.chiediConfermaEliminazioneVisita(visitaDaEliminare)) {
            modificaUtilita.eliminaVisita(visitaDaEliminare, visiteController);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void modificaNumeroPersoneIscrivibili() {
        viewUtilita.stampaMaxPersoneIscrivibiliNow();
        if (consoleIO.chiediAnnullaOperazione()) return;
        int numeroMax = consoleIO.chiediNumeroMaxPersone(visiteManagerDB);
        if (consoleIO.chiediConfermaNumeroMax(numeroMax)) {
            modificaUtilita.aggiornaNumeroPersoneIscrivibili(numeroMax);
            consoleIO.mostraRisultatoAggiornamentoNumeroMax(true, numeroMax);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void assegnaVisitaAVolontario() {
        consoleIO.mostraElencoConOggetti(visiteManagerDB.getVisiteMap().values().stream().toList());
        if (consoleIO.chiediAnnullaOperazione()) return;
        List<Visita> visite = new ArrayList<>(visiteManagerDB.getVisiteMap().values());
        List<Visita> visiteNonAssegnate = new ArrayList<>();
        for(Visita v : visite){
            if (v.getVolontario() == null) {
                visiteNonAssegnate.add(v);
            }
        }
        if (visiteNonAssegnate.isEmpty()) {
            consoleIO.mostraMessaggio("Nessuna visita disponibile per l'assegnazione.");
        }
        int sceltaVisita = consoleIO.chiediSelezioneVisita(visiteNonAssegnate);
        Visita visitaSelezionata = visiteNonAssegnate.get(sceltaVisita);

        List<Volontario> volontari = new ArrayList<>(volontariManager.getVolontariMap().values());
        if (volontari.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun volontario disponibile per l'assegnazione.");
        }
        int sceltaVolontario = consoleIO.chiediSelezioneVolontario(volontari);
        Volontario volontarioSelezionato = volontari.get(sceltaVolontario);
        if (!consoleIO.chiediAnnullaOperazione()) {
                addUtilita.assegnaVisitaAVolontario(visitaSelezionata, volontarioSelezionato);
                consoleIO.mostraRisultatoAggiornamentoVisitaVolontario(true);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }

    }

    public void rimuoviVisitaDaVolontario() {
        consoleIO.mostraElencoConOggetti(visiteManagerDB.getVisiteMap().values().stream().toList());
        if (consoleIO.chiediAnnullaOperazione()) return;
        List<Visita> visite = new ArrayList<>(visiteManagerDB.getVisiteMap().values());
        if (visite.isEmpty()) {
            consoleIO.mostraMessaggio("Nessuna visita disponibile per la rimozione.");
        }
        int sceltaVisita = consoleIO.chiediSelezioneVisita(visite);
        Visita visitaSelezionata = visite.get(sceltaVisita);

        List<Volontario> volontari = new ArrayList<>(volontariManager.getVolontariMap().values());
        if (volontari.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun volontario disponibile per la rimozione.");
        }
        int sceltaVolontario = consoleIO.chiediSelezioneVolontario(volontari);
        Volontario volontarioSelezionato = volontari.get(sceltaVolontario);
        if (!consoleIO.chiediAnnullaOperazione()) {
            modificaUtilita.rimuoviVisitaDaVolontario(visitaSelezionata, volontarioSelezionato, volontariController);
            consoleIO.mostraRisultatoAggiornamentoVisitaVolontario(true);
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }

    }

    public void aggiungiNuovoTipoVisita() {
        TipiVisitaClass nuovoTipo = consoleIO.chiediNuovoTipoVisita();
        if (nuovoTipo != null && InputDati.yesOrNo("Vuoi confermare e aggiungere il nuovo tipo di visita?")) {
            addUtilita.aggiungiNuovoTipoVisita(nuovoTipo);
            consoleIO.mostraMessaggio("Nuovo tipo di visita aggiunto con successo.");
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }

    public void rimuoviTipoDiVisita() {
        List<TipiVisitaClass> tipiVisitaList = VisiteManagerDB.getTipiVisitaClassList();
        if (tipiVisitaList.isEmpty()) {
            consoleIO.mostraMessaggio("Nessun tipo di visita disponibile per la rimozione.");
            return;
        }
        TipiVisitaClass tipoDaRimuovere = consoleIO.chiediTipoVisita(tipiVisitaList);
        if (consoleIO.chiediConfermaRimozioneTipoVisita(tipoDaRimuovere)) {
            modificaUtilita.rimuoviTipoDiVisita(tipoDaRimuovere);
            consoleIO.mostraMessaggio("Tipo di visita rimosso con successo.");
        } else {
            consoleIO.mostraMessaggio("Operazione annullata.");
        }
    }
}
