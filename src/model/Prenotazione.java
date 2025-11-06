package src.model;

import java.time.LocalDateTime;

/**
 * Rappresenta una prenotazione effettuata da un fruitore per una visita guidata.
 * Ogni prenotazione ha un codice univoco, uno stato e tiene traccia del numero di persone.
 * 
 */
public class Prenotazione {
    /** Identificatore univoco della prenotazione */
    private int id;
    
    /** Identificatore della visita prenotata */
    private int idVisita;
    
    /** Email del fruitore che ha effettuato la prenotazione */
    private String emailFruitore;
    
    /** Numero di persone incluse nella prenotazione */
    private int numeroPersone;
    
    /** Data e ora in cui è stata effettuata la prenotazione */
    private LocalDateTime dataPrenotazione;
    
    /** Codice univoco identificativo della prenotazione */
    private String codicePrenotazione;
    
    /** Stato corrente della prenotazione (es. CONFERMATA, CANCELLATA) */
    private String stato;

    /**
     * Costruttore per creare una nuova prenotazione.
     * Genera automaticamente il codice prenotazione e imposta lo stato come CONFERMATA.
     * 
     * @param emailFruitore l'email del fruitore che effettua la prenotazione
     * @param idVisita l'identificatore della visita da prenotare
     * @param numeroPersone il numero di persone per cui prenotare
     */
    public Prenotazione(String emailFruitore, int idVisita, int numeroPersone) {
        this.idVisita = idVisita;
        this.emailFruitore = emailFruitore;
        this.numeroPersone = numeroPersone;
        this.dataPrenotazione = LocalDateTime.now();
        this.codicePrenotazione = generaCodicePrenotazione();
        this.stato = "CONFERMATA";
    }

    /**
     * Genera un codice univoco per la prenotazione basato su timestamp e numero casuale.
     * 
     * @return il codice prenotazione generato
     */
    private String generaCodicePrenotazione() {
        return "PRN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Restituisce l'ID della prenotazione.
     * @return l'ID della prenotazione
     */
    public int getId() { return id; }
    
    /**
     * Imposta l'ID della prenotazione.
     * @param id il nuovo ID
     */
    public void setId(int id) { this.id = id; }
    
    /**
     * Restituisce l'ID della visita prenotata.
     * @return l'ID della visita
     */
    public int getIdVisita() { return idVisita; }
    
    /**
     * Imposta l'ID della visita.
     * @param idVisita il nuovo ID della visita
     */
    public void setIdVisita(int idVisita) { this.idVisita = idVisita; }

    /**
     * Restituisce l'email del fruitore.
     * @return l'email del fruitore
     */
    public String getEmailFruitore() { return emailFruitore; }
    
    /**
     * Imposta l'email del fruitore.
     * @param emailFruitore la nuova email
     */
    public void setEmailFruitore(String emailFruitore) { this.emailFruitore = emailFruitore; }

    /**
     * Restituisce il numero di persone prenotate.
     * @return il numero di persone
     */
    public int getNumeroPersone() { return numeroPersone; }
    
    /**
     * Imposta il numero di persone.
     * @param numeroPersone il nuovo numero di persone
     */
    public void setNumeroPersone(int numeroPersone) { this.numeroPersone = numeroPersone; }
    
    /**
     * Restituisce la data e ora della prenotazione.
     * @return la data e ora della prenotazione
     */
    public LocalDateTime getDataPrenotazione() { return dataPrenotazione; }
    
    /**
     * Imposta la data e ora della prenotazione.
     * @param dataPrenotazione la nuova data e ora
     */
    public void setDataPrenotazione(LocalDateTime dataPrenotazione) { this.dataPrenotazione = dataPrenotazione; }
    
    /**
     * Restituisce il codice univoco della prenotazione.
     * @return il codice prenotazione
     */
    public String getCodicePrenotazione() { return codicePrenotazione; }
    
    /**
     * Imposta il codice prenotazione.
     * @param codicePrenotazione il nuovo codice
     */
    public void setCodicePrenotazione(String codicePrenotazione) { this.codicePrenotazione = codicePrenotazione; }
    
    /**
     * Restituisce lo stato della prenotazione.
     * @return lo stato corrente
     */
    public String getStato() { return stato; }
    
    /**
     * Imposta lo stato della prenotazione.
     * @param stato il nuovo stato
     */
    public void setStato(String stato) { this.stato = stato; }

    /**
     * Restituisce una rappresentazione in formato stringa della prenotazione.
     * 
     * @return una stringa con codice, numero persone e stato
     */
    @Override
    public String toString() {
        return "Prenotazione [codice=" + codicePrenotazione + ", persone=" + numeroPersone + ", stato=" + stato + "]";
    }
}
