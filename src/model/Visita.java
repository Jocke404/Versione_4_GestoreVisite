package src.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Rappresenta una visita guidata con tutte le sue caratteristiche.
 * Include informazioni su titolo, luogo, data, ora, volontario assegnato,
 * numero di partecipanti e altre proprietà rilevanti.
 *  
 */
public class Visita {
    /** Identificatore univoco della visita */
    private int id;
    
    /** Titolo della visita */
    private String titolo;
    
    /** Nome del luogo dove si svolge la visita */
    private String luogo;
    
    /** Lista dei tipi di visita associati */
    private List<TipiVisitaClass> tipiVisita;
    
    /** Email del volontario assegnato alla visita */
    private String volontario;
    
    /** Data in cui si svolge la visita */
    private LocalDate data;
    
    /** Numero massimo di persone che possono partecipare */
    private int maxPersone; 
    
    /** Stato corrente della visita (es. Programmata, Confermata, Cancellata) */
    private String stato; 
    
    /** Ora di inizio della visita */
    private LocalTime oraInizio; 
    
    /** Durata della visita in minuti */
    private int durataMinuti;
    
    /** Numero di posti già prenotati */
    private int postiPrenotati;
    
    /** Numero minimo di partecipanti per confermare la visita */
    private int minPartecipanti;
    
    /** Indica se è richiesto un biglietto per partecipare */
    private boolean biglietto;
    
    /** Indica se il luogo presenta barriere architettoniche */
    private boolean barriereArchitettoniche;

    /**
     * Costruttore completo per creare una nuova visita.
     * 
     * @param id identificatore univoco della visita
     * @param titolo titolo della visita
     * @param luogo luogo dove si svolge la visita
     * @param tipiVisita lista dei tipi di visita
     * @param volontario email del volontario assegnato
     * @param data data della visita
     * @param maxPersone numero massimo di partecipanti
     * @param stato stato della visita
     * @param oraInizio ora di inizio
     * @param durataMinuti durata in minuti
     * @param postiPrenotati posti già prenotati
     * @param minPartecipanti numero minimo di partecipanti
     * @param biglietto se è richiesto un biglietto
     * @param barriereArchitettoniche se ci sono barriere architettoniche
     */
    public Visita(int id, String titolo, String luogo, List<TipiVisitaClass> tipiVisita, String volontario, 
                LocalDate data, int maxPersone, String stato, LocalTime oraInizio, 
                int durataMinuti, int postiPrenotati, int minPartecipanti, boolean biglietto, 
                boolean barriereArchitettoniche) {
        this.id = id;
        this.titolo = titolo;
        this.luogo = luogo;
        this.tipiVisita = tipiVisita;
        this.volontario = volontario;
        this.data = data;
        this.maxPersone = maxPersone;
        this.stato = stato;
        this.oraInizio = oraInizio;
        this.durataMinuti = durataMinuti;
        this.postiPrenotati = postiPrenotati;
        this.minPartecipanti = minPartecipanti;
        this.biglietto = biglietto;
        this.barriereArchitettoniche = barriereArchitettoniche;
    }

    /**
     * Restituisce il numero di posti già prenotati.
     * @return il numero di posti prenotati
     */
    public int getPostiPrenotati() {
        return postiPrenotati;
    }

    /**
     * Imposta il numero di posti prenotati.
     * @param postiPrenotati il nuovo numero di posti prenotati
     */
    public void setPostiPrenotati(int postiPrenotati) {
        this.postiPrenotati = postiPrenotati;
    }

    /**
     * Calcola e restituisce il numero di posti ancora disponibili.
     * @return il numero di posti disponibili
     */
    public int getPostiDisponibili() {
        return maxPersone - postiPrenotati;
    }

    /**
     * Verifica se ci sono ancora posti disponibili.
     * @return true se ci sono posti disponibili, false altrimenti
     */
    public boolean isDisponibile() {
        return getPostiDisponibili() > 0;
    }

    /**
     * Restituisce l'ora di inizio della visita.
     * @return l'ora di inizio
     */
    public LocalTime getOraInizio() {
        return oraInizio;
    }

    /**
     * Imposta l'ora di inizio della visita.
     * @param oraInizio la nuova ora di inizio
     */
    public void setOraInizio(LocalTime oraInizio) {
        this.oraInizio = oraInizio;
    }

    /**
     * Restituisce la durata della visita in minuti.
     * @return la durata in minuti
     */
    public int getDurataMinuti() {
        return durataMinuti;
    }

    /**
     * Imposta la durata della visita in minuti.
     * @param durataMinuti la nuova durata
     */
    public void setDurataMinuti(int durataMinuti) {
        this.durataMinuti = durataMinuti;
    }

    /**
     * Restituisce l'ID della visita.
     * @return l'identificatore univoco
     */
    public int getId() {
        return id;
    }

    /**
     * Restituisce il titolo della visita.
     * @return il titolo
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Restituisce il nome del luogo.
     * @return il luogo
     */
    public String getLuogo() {
        return luogo;
    }

    /**
     * Restituisce la lista dei tipi di visita.
     * @return la lista dei tipi di visita
     */
    public List<TipiVisitaClass> getTipiVisitaClass() {
        return tipiVisita;
    }

    /**
     * Restituisce i tipi di visita come stringa formattata.
     * @return una stringa con i nomi dei tipi di visita separati da virgola
     */
    public String getTipiVisitaClassString() {
        if (tipiVisita == null || tipiVisita.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (TipiVisitaClass tipo : tipiVisita) {
            sb.append(tipo.getNome()).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    /**
     * Restituisce l'email del volontario assegnato.
     * @return l'email del volontario
     */
    public String getVolontario() {
        return volontario;
    }

    /**
     * Restituisce la data della visita.
     * @return la data
     */
    public LocalDate getData() {
        return data;
    }

    /**
     * Imposta la data della visita.
     * @param data la nuova data
     */
    public void setData(LocalDate data) {
        this.data = data;
    }

    /**
     * Restituisce il numero massimo di persone.
     * @return il numero massimo di partecipanti
     */
    public int getMaxPersone() {
        return maxPersone;
    }

    /**
     * Restituisce lo stato della visita.
     * @return lo stato corrente
     */
    public String getStato() {
        return stato;
    }

    /**
     * Imposta lo stato della visita.
     * @param stato il nuovo stato
     */
    public void setStato(String stato) {
        this.stato = stato;
    }

    /**
     * Restituisce la descrizione del primo tipo di visita.
     * @return la descrizione o stringa vuota se non disponibile
     */
    public String getDescrizione() {
        return tipiVisita != null && !tipiVisita.isEmpty() ? tipiVisita.get(0).getDescrizione() : "";
    }

    /**
     * Restituisce il numero minimo di partecipanti.
     * @return il numero minimo
     */
    public int getMinPartecipanti() {
        return minPartecipanti;
    }

    /**
     * Verifica se è richiesto un biglietto.
     * @return true se il biglietto è richiesto
     */
    public boolean isBiglietto() {
        return biglietto;
    }

    /**
     * Verifica se ci sono barriere architettoniche.
     * @return true se ci sono barriere architettoniche
     */
    public boolean getBarriereArchitettoniche() {
        return barriereArchitettoniche;
    }

    /**
     * Restituisce una rappresentazione in formato stringa della visita
     * con tutte le informazioni dettagliate.
     * @return una stringa formattata con i dettagli della visita
     */
    @Override
    public String toString() {
        return  "Titolo: " + getTitolo()+
            "\nTipo di visita: " + getTipiVisitaClassString() +
            "\nDescrizione: " + getDescrizione() +
            "\nLuogo: " + getLuogo() +
            "\nData: " + getData() +
            "\nOra: " + getOraInizio() +
            "\nStato: " + getStato() +
            "\nDurata: " + getDurataMinuti() + " minuti" +
            "\nNumero massimo di persone: " + getMaxPersone() +
            "\nPosti disponibili: " + getPostiDisponibili() +
            "\nMinimo partecipanti: " + getMinPartecipanti() +
            "\nBiglietto richiesto: " + (isBiglietto() ? "Sì" : "No") +
            "\nBarriere architettoniche: " + (getBarriereArchitettoniche() ? "Sì" : "No");
    }
}
   