package src.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import src.model.db.ApplicationSettingsDAO;
import src.model.db.PrenotazioneManager;
import src.model.db.VisiteManagerDB;
import src.model.db.VolontariManager;
import src.controller.LuoghiController;
import src.controller.VisiteController;
import src.controller.VolontariController;

/**
 * Classe di utilità per le operazioni di modifica e gestione del sistema.
 * Fornisce metodi per aggiornare, modificare ed eliminare entità del sistema
 * come visite, luoghi, volontari e prenotazioni.
 * 
 * Questa classe coordina le operazioni di modifica tra i diversi controller
 * e manager del database, garantendo la coerenza dei dati.
 * 
 */
public class ModificaUtilita {

    /** Manager per la gestione delle visite nel database */
    private final VisiteManagerDB visiteManagerDB;
    
    /** Percorso del file di configurazione per il numero massimo di persone */
    private static final String NUMERO_PERSONE_FILE = "src/utility/max_persone_iscrivibili.config";


    /**
     * Costruttore della classe ModificaUtilita.
     * 
     * @param visiteManagerDB Manager per la gestione delle visite nel database
     */
    public ModificaUtilita(VisiteManagerDB visiteManagerDB) {
        this.visiteManagerDB = visiteManagerDB;
    }

    /**
     * Aggiorna lo stato di una visita specifica.
     * 
     * @param visitaId ID della visita da aggiornare
     * @param nuovoStato Nuovo stato da assegnare alla visita
     * @return true se l'operazione è riuscita, false se la visita non esiste
     */
    public boolean aggiornaStatoVisita(int visitaId, String nuovoStato) {
        Visita visita = visiteManagerDB.getVisiteMap().get(visitaId);
        if (visita == null) return false;
        visita.setStato(nuovoStato);
        visiteManagerDB.aggiornaVisita(visitaId, visita);
        return true;
    }

    /**
     * Aggiorna la data di una visita specifica.
     * 
     * @param visitaId ID della visita da aggiornare
     * @param nuovaData Nuova data da assegnare alla visita
     * @return true se l'operazione è riuscita, false se la visita non esiste
     */
    public boolean aggiornaDataVisita(int visitaId, LocalDate nuovaData) {
        Visita visita = visiteManagerDB.getVisiteMap().get(visitaId);
        if (visita == null) return false;
        visita.setData(nuovaData);
        visiteManagerDB.aggiornaVisita(visitaId, visita);
        return true;
    }

    /**
     * Aggiorna il numero massimo di persone per visita.
     * Validazione: il numero deve essere almeno 2.
     * 
     * @param numeroMax Nuovo numero massimo di persone per visita
     * @return true se l'operazione è riuscita, false se il numero è inferiore a 2
     */
    public boolean aggiornaMaxPersone(int numeroMax) {
        if (numeroMax < 2) return false;
        visiteManagerDB.aggiornaMaxPersone(numeroMax);
        return true;
    }

    /**
     * Elimina una data preclusa dal sistema.
     * 
     * @param data Data da rimuovere dall'elenco delle date precluse
     * @return true se l'operazione è riuscita
     */
    public boolean eliminaDataPreclusa(LocalDate data) {
        visiteManagerDB.eliminaData(data);
        return true;
    }

    /**
     * Elimina un luogo dal sistema tramite il controller appropriato.
     * 
     * @param luogo Luogo da eliminare
     * @param luoghiController Controller per la gestione dei luoghi
     */
    public void eliminaLuogo(Luogo luogo, LuoghiController luoghiController) {
        luoghiController.eliminaLuogo(luogo);
    }

    /**
     * Aggiorna le informazioni di un luogo esistente.
     * I campi vuoti non vengono modificati (vengono mantenuti i valori esistenti).
     * 
     * @param luogo Luogo da aggiornare
     * @param nuovoNome Nuovo nome del luogo (se non vuoto)
     * @param nuovaDescrizione Nuova descrizione del luogo (se non vuota)
     * @param nuovaCollocazione Nuova collocazione del luogo (se non vuota)
     * @param nuoviTipi Nuovi tipi di visita supportati dal luogo
     * @param luoghiController Controller per la gestione dei luoghi
     */
    public void aggiornaLuogo(Luogo luogo, String nuovoNome, String nuovaDescrizione, 
                            String nuovaCollocazione, List<TipiVisitaClass> nuoviTipi, LuoghiController luoghiController) {
        if (!nuovoNome.isEmpty()) luogo.setName(nuovoNome);
        if (!nuovaDescrizione.isEmpty()) luogo.setDescrizione(nuovaDescrizione);
        if (!nuovaCollocazione.isEmpty()) luogo.setCollocazione(nuovaCollocazione);
        luogo.setTipiVisitaClass(nuoviTipi);
        luoghiController.aggiornaLuoghi(luogo);
    }

    /**
     * Elimina un volontario dal sistema tramite il controller appropriato.
     * 
     * @param volontario Volontario da eliminare
     * @param volontariController Controller per la gestione dei volontari
     */
    public void eliminaVolontario(Volontario volontario, VolontariController volontariController) {
        volontariController.eliminaVolontario(volontario);
    }

    /**
     * Cancella una prenotazione specifica dal sistema.
     * 
     * @param prenotazione Prenotazione da cancellare
     * @param prenotazioneManager Manager per la gestione delle prenotazioni
     * @return true se la cancellazione è riuscita, false altrimenti
     */
    public boolean cancellaPrenotazione(Prenotazione prenotazione, PrenotazioneManager prenotazioneManager) {
        return prenotazioneManager.rimuoviPrenotazione(prenotazione);
    }

    /**
     * Elimina una visita dal sistema tramite il controller appropriato.
     * 
     * @param visita Visita da eliminare
     * @param visiteController Controller per la gestione delle visite
     */
    public void eliminaVisita(Visita visita, VisiteController visiteController) {
        visiteController.eliminaVisita(visita);
    }

    /**
     * Aggiorna il numero massimo di persone iscrivibili per visita.
     * Tenta di salvare prima nel database, poi su file come fallback.
     * Validazione: il numero deve essere almeno 1.
     * 
     * @param numeroMax Nuovo numero massimo di persone iscrivibili
     * @return true se l'operazione è riuscita, false se il numero è inferiore a 1 o si è verificato un errore
     */
    public boolean aggiornaNumeroPersoneIscrivibili(int numeroMax) {
        if (numeroMax < 1) return false;

        boolean ok = false;
        try {
            ok = ApplicationSettingsDAO.setMaxPeoplePerVisit(numeroMax);
        } catch (Throwable t) {
            ok = false;
        }

        if (!ok) {
             
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(NUMERO_PERSONE_FILE))) {
                writer.write(String.valueOf(numeroMax));
                ok = true;
            } catch (IOException e) {
                ok = false;
            }
        }
        return ok;
    }

    /**
     * Rimuove un tipo di visita specifico da una lista di volontari.
     * Aggiorna il database per ogni volontario modificato.
     * 
     * @param volontari Lista dei volontari da cui rimuovere il tipo di visita
     * @param tipoVisita Tipo di visita da rimuovere
     * @param volontariManager Manager per la gestione dei volontari nel database
     */
    public void rimuoviTipoVisitaDaVolontari(List<Volontario> volontari, TipiVisitaClass tipoVisita, VolontariManager volontariManager) {
        for (Volontario volontario : volontari) {
            volontariManager.rimuoviTipoVisitaDaVolontario(volontario.getEmail(), tipoVisita);
        }
    }

    /**
     * Rimuove l'assegnazione di una visita specifica da un volontario.
     * 
     * @param visitaSelezionata Visita da cui rimuovere il volontario
     * @param volontarioSelezionato Volontario da rimuovere dalla visita
     * @param volontariController Controller per la gestione dei volontari
     */
    public void rimuoviVisitaDaVolontario(Visita visitaSelezionata, Volontario volontarioSelezionato, VolontariController volontariController) {
        volontariController.rimuoviVisitaDaVolontario(visitaSelezionata, volontarioSelezionato);
    }

    /**
     * Rimuove completamente un tipo di visita dal sistema.
     * ATTENZIONE: Questa operazione rimuove il tipo di visita da tutto il sistema.
     * 
     * @param tipoDaRimuovere Tipo di visita da rimuovere dal sistema
     */
    public void rimuoviTipoDiVisita(TipiVisitaClass tipoDaRimuovere) {
        visiteManagerDB.rimuoviTipoDiVisita(tipoDaRimuovere);
    }

}