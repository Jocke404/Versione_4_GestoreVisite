package src.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import lib.InputDati;
import src.model.AggiuntaUtilita;
import src.model.Disponibilita;
import src.model.ValidatoreVisite;
import src.model.Visita;
import src.model.Volontario;
import src.view.ConsoleIO;
import src.view.ViewUtilita;
import src.model.db.VolontariManager;

/**
 * Controller per la gestione delle operazioni dei volontari.
 * Gestisce la raccolta e modifica delle disponibilità, la visualizzazione
 * delle visite assegnate e la modifica della password.
 * 
 *  
 *  
 */
public class VolontariController {
    /** Manager per le operazioni sui volontari nel database */
    private final VolontariManager volontariManager;
    
    /** Utility per l'aggiunta di nuovi elementi */
    private final AggiuntaUtilita addUtilita;
    
    /** Interfaccia per l'interazione con l'utente */
    private final ConsoleIO consoleIO;
    
    /** Gestione delle disponibilità dei volontari */
    private final Disponibilita disponibilita = new Disponibilita();
    
    /** Il volontario attualmente autenticato */
    Volontario volontarioCorrente;
    
    /** Validatore per le visite e le date disponibili */
    private final ValidatoreVisite validatore;
    
    /** Utility per la visualizzazione dei dati */
    private final ViewUtilita viewUtilita;

    /**
     * Costruttore del controller dei volontari.
     * Inizializza anche la sincronizzazione delle disponibilità dal database.
     * 
     * @param volontariManager il manager dei volontari
     * @param addUtilita l'utility per aggiungere elementi
     * @param consoleIO l'interfaccia I/O
     * @param volontarioCorrente il volontario autenticato
     * @param validatore il validatore delle visite
     * @param viewUtilita l'utility per la visualizzazione
     */
    public VolontariController(VolontariManager volontariManager, AggiuntaUtilita addUtilita, 
                                ConsoleIO consoleIO, Volontario volontarioCorrente, ValidatoreVisite validatore, 
                                ViewUtilita viewUtilita) {
        this.volontariManager = volontariManager;
        this.addUtilita = addUtilita;
        this.consoleIO = consoleIO;
        this.volontarioCorrente = volontarioCorrente;
        this.validatore = validatore;
        this.viewUtilita = viewUtilita;
        
         
        try {
            this.disponibilita.sincronizzaDisponibilitaVolontari(this.volontariManager);
        } catch (Exception e) {
            System.err.println("Errore sincronizzazione disponibilità: " + e.getMessage());
        }
    }

    /**
     * Raccoglie le disponibilità del volontario per il mese successivo.
     * Verifica che la data corrente sia entro il 15 del mese, mostra un calendario
     * dei giorni disponibili e permette la selezione delle date.
     * Le nuove disponibilità vengono aggiunte a quelle esistenti senza duplicati.
     */
    public void raccogliDisponibilitaVolontario() {
        LocalDate oggi = LocalDate.now();
        if (oggi.getDayOfMonth() > 15) {
            consoleIO.mostraMessaggio("Non è possibile inserire disponibilità dopo il 15 del mese corrente.");
            return;
        }
        YearMonth ym = YearMonth.now().plusMonths(1);
        List<Integer> giorniDisponibili = validatore.trovaGiorniDisponibili(volontarioCorrente, ym);
        if (giorniDisponibili.isEmpty()) {
            consoleIO.mostraMessaggio("Non ci sono giorni disponibili per dichiarare la disponibilità.");
            return;
        }
        consoleIO.mostraCalendarioMese(ym, giorniDisponibili);
        List<Integer> giorniSelezionati = consoleIO.chiediGiorniDisponibili(ym, new ArrayList<>(giorniDisponibili));
        List<LocalDate> dateDisponibili = validatore.filtraDateDisponibili(giorniSelezionati, ym);
        
         
        if (dateDisponibili == null || dateDisponibili.isEmpty()) {
            consoleIO.mostraMessaggio("Nessuna data selezionata. Operazione annullata.");
            return;
        }

         
        List<LocalDate> esistenti = disponibilita.getDisponibilitaVolontario(volontarioCorrente);
        boolean hasEsistenti = esistenti != null && !esistenti.isEmpty();

        if (hasEsistenti) {
            consoleIO.mostraMessaggio("Trovate disponibilità precedenti: le nuove date verranno aggiunte (no duplicati).");
             
            disponibilita.salvaDisponibilita(volontarioCorrente, dateDisponibili, volontariManager, true);
        } else {
            consoleIO.mostraMessaggio("Nessuna disponibilità precedente: verranno create le nuove disponibilità.");
             
            disponibilita.salvaDisponibilita(volontarioCorrente, dateDisponibili, volontariManager, false);
        }
    }
    
    /**
     * Visualizza tutte le visite assegnate al volontario corrente.
     */
    public void visualizzaVisiteVolontario(){
        viewUtilita.stampaVisiteVolontario(volontarioCorrente);
    }

    /**
     * Restituisce la lista di tutti i volontari.
     * 
     * @return copia immutabile della lista dei volontari
     */
    public List<Volontario> getVolontari() {
        return List.copyOf(volontariManager.getVolontariMap().values());
    }

    /**
     * Elimina un volontario dal sistema.
     * 
     * @param volontarioDaEliminare il volontario da rimuovere
     */
    public void eliminaVolontario(Volontario volontarioDaEliminare) {
        volontariManager.eliminaVolontario(volontarioDaEliminare);
    }

    /**
     * Gestisce la modifica della password del volontario corrente.
     * Richiede una nuova password diversa da quella attuale e la aggiorna nel sistema.
     */
    public void modificaPassword() {
        String nuovaPassword;
        String vecchiaPassword = volontarioCorrente.getPassword();

        do {
            nuovaPassword = consoleIO.chiediNuovaPassword();
            if (nuovaPassword == null) {
                consoleIO.mostraMessaggio("Password non valida. Riprova.");
                continue;
            }
            if (nuovaPassword.equals(vecchiaPassword)) {
                consoleIO.mostraMessaggio("La nuova password non può essere uguale a quella attuale. Inserisci una password diversa.");
            }
        } while (nuovaPassword == null || nuovaPassword.equals(vecchiaPassword));

        volontariManager.modificaPsw(volontarioCorrente.getEmail(), nuovaPassword);
    }

    /**
     * Permette al volontario di modificare le disponibilità precedentemente dichiarate.
     * Verifica che la data corrente sia entro il 15 del mese, mostra le disponibilità
     * esistenti e permette di modificarle sostituendole completamente.
     */
    public void modificaDisponibilitaVolontario() {
        LocalDate oggi = LocalDate.now();
        if (oggi.getDayOfMonth() > 15) {
            consoleIO.mostraMessaggio("Non è possibile modificare le disponibilità dopo il 15 del mese corrente.");
            return;
        }
        List<LocalDate> disponibilitaEsistenti = disponibilita.getDisponibilitaVolontario(volontarioCorrente);
        if (disponibilitaEsistenti == null || disponibilitaEsistenti.isEmpty()) {
            consoleIO.mostraMessaggio("Non ci sono disponibilità da modificare.");
            return;
        }

        YearMonth ym = YearMonth.now().plusMonths(1);
        List<Integer> giorniDisponibili = validatore.trovaGiorniDisponibili(volontarioCorrente, ym);
        consoleIO.mostraDisponibilitaEsistenti(disponibilitaEsistenti);
        List<LocalDate> disponibilitaEsistentiMod = consoleIO.chiediNuoveDisponibilita(disponibilitaEsistenti, giorniDisponibili, validatore);
        if (disponibilitaEsistentiMod == null) {
            consoleIO.mostraMessaggio("Nessuna modifica effettuata.");
            return;
        }

        if(InputDati.yesOrNo("Confermi le modifiche alle disponibilità?: ")) {
            disponibilita.salvaDisponibilita(volontarioCorrente, disponibilitaEsistentiMod, 
                                                volontariManager, false);
        }
    }

    /**
     * Rimuove l'assegnazione di una visita da un volontario specifico.
     * 
     * @param visitaSelezionata la visita da rimuovere
     * @param volontarioSelezionato il volontario da cui rimuovere la visita
     */
    public void rimuoviVisitaDaVolontario(Visita visitaSelezionata, Volontario volontarioSelezionato) {
        volontariManager.rimuoviVisitaVolontario(visitaSelezionata, volontarioSelezionato);
    }
}