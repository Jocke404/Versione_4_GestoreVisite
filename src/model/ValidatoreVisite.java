package src.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import src.model.db.VisiteManagerDB;
import src.view.ConsoleIO;

/**
 * Gestisce la validazione e l'aggiornamento automatico delle visite nel sistema.
 * Questa classe si occupa di:
 * - Validazione della programmazione delle visite (orari, disponibilità volontari)
 * - Gestione automatica degli stati delle visite (confermata, cancellata, completata)
 * - Gestione automatica delle date precluse e festività
 * - Ricerca di slot temporali disponibili per nuove visite
 * 
 * La classe mantiene la coerenza del sistema aggiornando automaticamente
 * lo stato delle visite in base alle regole di business.
 *  
 */
public class ValidatoreVisite {
    /** Manager per la gestione delle visite nel database */
    private VisiteManagerDB visiteManager;
    
    /** Mappa thread-safe delle visite attive nel sistema */
    private ConcurrentHashMap<Integer, Visita> visiteMap = new ConcurrentHashMap<>();
    
    /** Interfaccia per l'input/output con la console */
    private ConsoleIO consoleIO = new ConsoleIO();

    /**
     * Costruttore del ValidatoreVisite.
     * Inizializza il validatore con il manager delle visite e carica la mappa delle visite.
     * 
     * @param visiteManager Manager per l'accesso ai dati delle visite
     */
    public ValidatoreVisite(VisiteManagerDB visiteManager) {
        this.visiteManager = visiteManager;
        this.visiteMap = visiteManager.getVisiteMap();
    }

    /**
     * Gestisce automaticamente l'aggiornamento dello stato delle visite.
     * Regole applicate:
     * - Visite passate con partecipanti >= minimo: diventano "Confermata"
     * - Visite passate con partecipanti < minimo: diventano "Cancellata" 
     * - Visite di oggi già terminate: diventano "Completata"
     * 
     * Viene eseguita automaticamente dal sistema per mantenere coerenza dei dati.
     */
    public void gestioneVisiteAuto(){
        visiteMap = visiteManager.getVisiteMap();
        for(Visita visita : visiteMap.values()){
            if(visita.getData().isBefore(LocalDate.now()) && 
               !visita.getStato().equals("Completata") && 
               !visita.getStato().equals("Cancellata")){
                if(visita.getPostiPrenotati() >= visita.getMinPartecipanti()){
                    visita.setStato("Confermata");
                    visiteManager.aggiornaVisita(visita.getId(), visita);
                } else if (visita.getPostiPrenotati() < visita.getMinPartecipanti()) {
                    visita.setStato("Cancellata");
                    visiteManager.aggiornaVisita(visita.getId(), visita);
                } else if (visita.getPostiPrenotati() == visita.getMaxPersone()) {
                    visita.setStato("Completa");
                    visiteManager.aggiornaVisita(visita.getId(), visita);
                }
            } else  if (visita.getData().isEqual(LocalDate.now())) {
                if (visita.getOraInizio().plusMinutes(visita.getDurataMinuti()).isAfter(LocalTime.now())
                    && visita.getStato().equals("Completa")) {
                        visita.setStato("Effettuata");
                        visiteManager.aggiornaVisita(visita.getId(), visita);
                }

            }
        }
    }

    /**
     * Gestisce automaticamente le date precluse del sistema.
     * Operazioni eseguite:
     * - Rimuove date precluse passate
     * - Rimuove date di anni precedenti
     * - Aggiunge automaticamente le festività fisse per l'anno corrente (1° gennaio)
     * 
     * Le festività fisse includono: Capodanno, Epifania, Festa della Liberazione,
     * Festa dei Lavoratori, Ferragosto, Ognissanti, Immacolata, Natale, Santo Stefano.
     */
    public void gestioneDatePrecluseAuto() {
        try {
            Map<LocalDate, String> precluse = visiteManager.getDatePrecluseMap();
            LocalDate today = LocalDate.now();
             
            for (LocalDate d : precluse.keySet()) {
                if (d.isBefore(today)) {
                    try {
                        visiteManager.eliminaData(d);
                    } catch (Throwable t) {
                        System.err.println("Errore cancellazione data preclusa " + d + ": " + t.getMessage());
                    }
                }
            }
            List<LocalDate> toRemove = new ArrayList<>();
            for (LocalDate d : precluse.keySet()) {
                if (d.getYear() != today.getYear() && d.isBefore(today)) {
                    toRemove.add(d);
                }
            }
            for (LocalDate d : toRemove) {
                try {
                    visiteManager.eliminaData(d);
                } catch (Throwable t) {
                    System.err.println("Errore cancellazione data preclusa " + d + ": " + t.getMessage());
                }
            }

             
            if (today.getDayOfMonth() == 1 && today.getMonthValue() == 1) {
                Map<LocalDate, String> holidays = generateFixedHolidays(today.getYear());
                for (Map.Entry<LocalDate, String> e : holidays.entrySet()) {
                    LocalDate h = e.getKey();
                    String descr = e.getValue();
                    try {
                         
                        visiteManager.aggiungiNuovaDataPreclusa(h, descr);
                    } catch (Throwable t) {
                        System.err.println("Errore inserimento festività " + h + ": " + t.getMessage());
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("gestioneDatePrecluseAuto error: " + t.getMessage());
        }
    }

    /**
     * Genera la mappa delle festività fisse per un anno specifico.
     * Include le principali festività italiane che non cambiano data.
     * 
     * @param year Anno per cui generare le festività
     * @return Mappa delle festività con data come chiave e nome come valore
     */
    private Map<LocalDate, String> generateFixedHolidays(int year) {
        Map<LocalDate, String> h = new LinkedHashMap<>();
        h.put(LocalDate.of(year, 1, 1), "Capodanno");
        h.put(LocalDate.of(year, 1, 6), "Epifania");
        h.put(LocalDate.of(year, 4, 25), "Festa della Liberazione");
        h.put(LocalDate.of(year, 5, 1), "Festa dei Lavoratori");
        h.put(LocalDate.of(year, 8, 15), "Ferragosto");
        h.put(LocalDate.of(year, 11, 1), "Ognissanti");
        h.put(LocalDate.of(year, 12, 8), "Immacolata Concezione");
        h.put(LocalDate.of(year, 12, 25), "Natale");
        h.put(LocalDate.of(year, 12, 26), "Santo Stefano");
         
        return h;
    }

    /**
     * Verifica se un volontario è già impegnato in un'altra visita nello stesso orario
     * @param volontarioEmail Email del volontario da verificare
     * @param dataVisita Data della nuova visita
     * @param oraInizio Ora di inizio della nuova visita
     * @param durataMinuti Durata in minuti della nuova visita
     * @return true se il volontario è disponibile, false se è già impegnato
     */
    public boolean isVolontarioDisponibile(String volontarioEmail, LocalDate dataVisita, 
                                         LocalTime oraInizio, int durataMinuti) {
         
        LocalTime oraFine = oraInizio.plusMinutes(durataMinuti);
        
         
        List<Visita> visiteVolontario = visiteManager.getVisiteMap().values().stream()
                .filter(v -> v.getVolontario() != null && 
                             v.getVolontario().contains(volontarioEmail) &&  
                             v.getData().equals(dataVisita) &&
                             !v.getStato().equals("Cancellata"))  
                .collect(Collectors.toList());
        
         
        for (Visita visitaEsistente : visiteVolontario) {
            if (visitaEsistente.getOraInizio() == null) continue;
            
            LocalTime inizioEsistente = visitaEsistente.getOraInizio();
            LocalTime fineEsistente = visitaEsistente.getOraInizio()
                                        .plusMinutes(visitaEsistente.getDurataMinuti());
            
             
            if (siSovrappongono(oraInizio, oraFine, inizioEsistente, fineEsistente)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Verifica se due intervalli temporali si sovrappongono.
     * Utilizzato per validare che non ci siano conflitti di orario.
     * 
     * @param inizio1 Ora di inizio del primo intervallo
     * @param fine1 Ora di fine del primo intervallo
     * @param inizio2 Ora di inizio del secondo intervallo
     * @param fine2 Ora di fine del secondo intervallo
     * @return true se gli intervalli si sovrappongono, false altrimenti
     */
    private boolean siSovrappongono(LocalTime inizio1, LocalTime fine1, 
                                  LocalTime inizio2, LocalTime fine2) {
        return (inizio1.isBefore(fine2) && fine1.isAfter(inizio2));
    }

    /**
     * Metodo completo per validare l'assegnazione di un volontario a una visita
     * @param visita La visita da validare
     * @param volontarioEmail Email del volontario
     * @return Messaggio di validazione (vuoto se valido, messaggio errore altrimenti)
     */
    public String validaAssegnazioneVolontario(Visita visita, String volontarioEmail) {
        if (visita.getOraInizio() == null) {
            return "La visita deve avere un orario di inizio";
        }
        
        if (!isVolontarioDisponibile(volontarioEmail, visita.getData(), 
                                    visita.getOraInizio(), visita.getDurataMinuti())) {
            return "Il volontario è già impegnato in un'altra visita nello stesso orario";
        }
        
         
        if (visita.getOraInizio().isBefore(LocalTime.of(9, 0))) {
            return "Orario troppo presto (minimo 09:00)";
        }
        
        LocalTime oraFine = visita.getOraInizio().plusMinutes(visita.getDurataMinuti());
        if (oraFine.isAfter(LocalTime.of(19, 0))) {
            return "Orario troppo tardo (massimo 19:00)";
        }
        
        return "";  
    }

    /**
     * Valida completamente la programmazione di una visita.
     * Verifica se non ci sono conflitti di orario con altre visite nello stesso luogo
     * e nella stessa data.
     * 
     * @param nuovaVisita Visita da validare
     * @return true se la visita può essere programmata, false se ci sono conflitti
     */
    public boolean validaVisita(Visita nuovaVisita){
        List<Visita> visiteEsistenti = visiteMap.values().stream()
                .filter(v -> v.getData().equals(nuovaVisita.getData()) && 
                             v.getLuogo().equals(nuovaVisita.getLuogo()))
                .collect(Collectors.toList());
        
         
        for (Visita visitaEsistente : visiteEsistenti) {
            if (siSovrappongono(nuovaVisita.getOraInizio(), nuovaVisita.getOraInizio().plusMinutes(nuovaVisita.getDurataMinuti()),
                                visitaEsistente.getOraInizio(), visitaEsistente.getOraInizio().plusMinutes(visitaEsistente.getDurataMinuti()))) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Trova tutti gli slot temporali disponibili per una nuova visita.
     * Considera gli orari di apertura (9:00-19:00) e controlla i conflitti con visite esistenti.
     * Gli slot sono proposti ogni 30 minuti.
     * 
     * @param data Data per cui cercare slot disponibili
     * @param luogo Luogo in cui programmare la visita
     * @param durataMinuti Durata della visita in minuti
     * @return Lista degli orari di inizio disponibili per la visita
     */
    public List<LocalTime> trovaSlotDisponibili(LocalDate data, String luogo, int durataMinuti) {
        List<Visita> visiteGiorno = visiteMap.values().stream()
                .filter(v -> v.getData().equals(data) && v.getLuogo().equals(luogo))
                .collect(Collectors.toList());
        
        List<LocalTime> slotDisponibili = new ArrayList<>();
        final LocalTime INIZIO_GIORNATA = LocalTime.of(9, 0);
        final LocalTime FINE_GIORNATA = LocalTime.of(19, 0);
        final LocalTime ULTIMO_ORARIO_CONSENTITO = LocalTime.of(17, 40);
        
         
        if (INIZIO_GIORNATA.plusMinutes(durataMinuti).isAfter(FINE_GIORNATA)) {
            consoleIO.mostraErrore("Durata troppo lunga: la visita non rientra nell'orario di apertura");
            return slotDisponibili;  
        }
        
        LocalTime slotCorrente = INIZIO_GIORNATA;
        
        while (slotCorrente.isBefore(ULTIMO_ORARIO_CONSENTITO)) {
            LocalTime fineVisita = slotCorrente.plusMinutes(durataMinuti);
            
             
            if (fineVisita.isAfter(FINE_GIORNATA)) {
                 
                slotCorrente = slotCorrente.plusMinutes(30);
                continue;
            }
            
             
            if (slotCorrente.isAfter(ULTIMO_ORARIO_CONSENTITO)) {
                break;
            }
            
            boolean slotLibero = true;
            
             
            Visita visitaTemp = new Visita(-1, null, luogo, List.of(), "", data, 0, "", slotCorrente, durataMinuti, 0, 0, false, false);
            
            for (Visita visitaEsistente : visiteGiorno) {
                if (siSovrappongono(visitaTemp.getOraInizio(), visitaTemp.getOraInizio().plusMinutes(visitaTemp.getDurataMinuti()),
                                    visitaEsistente.getOraInizio(), visitaEsistente.getOraInizio().plusMinutes(visitaEsistente.getDurataMinuti()))) {
                    slotLibero = false;
                    break;
                }
            }
            
            if (slotLibero) {
                slotDisponibili.add(slotCorrente);
            }
            
            slotCorrente = slotCorrente.plusMinutes(30);
        }
        
        return slotDisponibili;
    }

    /**
     * Trova i giorni del mese in cui un volontario può essere disponibile per visite.
     * Considera sia le visite già programmate che i tipi di visita che il volontario può gestire.
     * 
     * @param volontario Volontario di cui verificare la disponibilità
     * @param ym Anno e mese di interesse
     * @return Lista dei giorni del mese in cui il volontario può essere disponibile
     */
    public List<Integer> trovaGiorniDisponibili(Volontario volontario, YearMonth ym) {
        List<Integer> giorniDisponibili = new ArrayList<>();
        List<TipiVisitaClass> tipiVisitaVolontario = volontario.getTipiDiVisite();

        for (int giorno = 1; giorno <= ym.lengthOfMonth(); giorno++) {
            LocalDate data = ym.atDay(giorno);
            if (isGiornoDisponibile(data, visiteMap, tipiVisitaVolontario)) {
                giorniDisponibili.add(giorno);
            }
        }
        return giorniDisponibili;
    }

    /**
     * Verifica se un giorno specifico è disponibile per programmare visite.
     * Un giorno è disponibile se:
     * - Non ci sono già visite programmate non cancellate
     * - Il volontario può gestire tipi di visita programmabili in quel giorno
     * 
     * @param data Data da verificare
     * @param visiteMap Mappa delle visite esistenti
     * @param tipiVisitaVolontario Tipi di visita che il volontario può gestire
     * @return true se il giorno è disponibile, false altrimenti
     */
    private boolean isGiornoDisponibile(LocalDate data, ConcurrentHashMap<Integer, Visita> visiteMap, 
                                    List<TipiVisitaClass> tipiVisitaVolontario) {
        boolean visitaProgrammata = visiteMap.values().stream()
            .anyMatch(v -> v.getData() != null
                        && v.getData().equals(data) 
                        && (v.getStato() == null || !v.getStato().equalsIgnoreCase("Cancellata")) 
                        );

        boolean tipoVisitaConsentito = tipiVisitaVolontario.stream()
            .anyMatch(tipo -> isTipoVisitaProgrammabileInGiorno(tipo, data.getDayOfWeek().toString()));

        return !visitaProgrammata && tipoVisitaConsentito;
    }

    /**
     * Converte una lista di giorni del mese in una lista di date complete.
     * 
     * @param giorniSelezionati Lista dei giorni del mese (1-31)
     * @param ym Anno e mese di riferimento
     * @return Lista delle date complete corrispondenti ai giorni selezionati
     */
    public List<LocalDate> filtraDateDisponibili(List<Integer> giorniSelezionati, YearMonth ym) {
        List<LocalDate> dateDisponibili = new ArrayList<>();
        for (Integer giorno : giorniSelezionati) {
            dateDisponibili.add(ym.atDay(giorno));
        }
        return dateDisponibili;
    }

    /**
     * Filtra le date disponibili restituendo una singola data se ce n'è esattamente una.
     * Utilizzato quando si aspetta una selezione univoca.
     * 
     * @param giorniSelezionati Lista dei giorni del mese selezionati
     * @param ym Anno e mese di riferimento
     * @return La data se è una sola, null se ci sono zero o più date
     */
    public LocalDate filtraDateDisponibiliSingola(List<Integer> giorniSelezionati, YearMonth ym) {
        List<LocalDate> dates = filtraDateDisponibili(giorniSelezionati, ym);
        if (dates.size() == 1) {
            return dates.get(0);
        }
        return null;
    }

    /**
     * Verifica se un tipo di visita può essere programmato in un giorno specifico della settimana.
     * Attualmente esclude i weekend (sabato e domenica) per tutti i tipi di visita.
     * 
     * @param tipoVisita Tipo di visita da verificare
     * @param giornoSettimana Giorno della settimana (formato inglese: MONDAY, TUESDAY, ecc.)
     * @return true se il tipo di visita può essere programmato in quel giorno, false altrimenti
     */
    private boolean isTipoVisitaProgrammabileInGiorno(TipiVisitaClass tipoVisita, String giornoSettimana) {
        String giorno = giornoSettimana.trim().toUpperCase();
        if (giorno.equals("SATURDAY") || giorno.equals("SUNDAY")) {
            return false;
        }
        return true;
    }

}
