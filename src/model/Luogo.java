package src.model;

import java.util.*;

/**
 * Rappresenta un luogo dove possono svolgersi visite guidate.
 * Ogni luogo ha un nome, una descrizione, una collocazione geografica
 * e una lista di tipi di visita disponibili.
 * 
 */
public class Luogo {
    
    /** Nome del luogo */
    private String nome;
    
    /** Descrizione dettagliata del luogo */
    private String descrizione;
    
    /** Collocazione geografica del luogo */
    private String collocazione;
    
    /** Lista dei tipi di visita disponibili in questo luogo */
    private List<TipiVisitaClass> tipiVisita; 

    /**
     * Costruttore per creare un nuovo luogo.
     * 
     * @param nome il nome del luogo
     * @param descrizione la descrizione del luogo
     * @param collocazione la collocazione geografica del luogo
     * @param tipiVisita la lista dei tipi di visita disponibili
     */
    public Luogo(String nome, String descrizione, String collocazione, List<TipiVisitaClass> tipiVisita) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.collocazione = collocazione;
        this.tipiVisita = tipiVisita;
    }

    /**
     * Restituisce la lista dei tipi di visita disponibili per questo luogo.
     * 
     * @return la lista dei tipi di visita
     */
    public List<TipiVisitaClass> getTipiVisitaClass() {
        return tipiVisita;
    }

    /**
     * Imposta i tipi di visita disponibili per questo luogo.
     * 
     * @param tipiVisita la nuova lista di tipi di visita
     */
    public void setTipiVisitaClass(List<TipiVisitaClass> tipiVisita) {
        this.tipiVisita = tipiVisita;
    }

    /**
     * Restituisce il nome del luogo.
     * 
     * @return il nome del luogo
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce la descrizione del luogo.
     * 
     * @return la descrizione del luogo
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Restituisce la collocazione geografica del luogo.
     * 
     * @return la collocazione del luogo
     */
    public String getCollocazione() {
        return collocazione;
    }

    /**
     * Imposta un nuovo nome per il luogo.
     * 
     * @param nuovoNome il nuovo nome del luogo
     */
    public void setName(String nuovoNome) {
        this.nome = nuovoNome;
    }

    /**
     * Imposta una nuova descrizione per il luogo.
     * 
     * @param nuovaDescrizione la nuova descrizione
     */
    public void setDescrizione(String nuovaDescrizione) {
        this.descrizione = nuovaDescrizione;
    }

    /**
     * Imposta una nuova collocazione per il luogo.
     * 
     * @param nuovaCollocazione la nuova collocazione
     */
    public void setCollocazione(String nuovaCollocazione) {
        this.collocazione = nuovaCollocazione;
    }

    /**
     * Restituisce una rappresentazione in formato stringa del luogo,
     * includendo nome, descrizione, collocazione e tipi di visita disponibili.
     * 
     * @return una stringa contenente le informazioni del luogo
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nome: ").append(getNome())
          .append("\nDescrizione: ").append(getDescrizione())
          .append("\nCollocazione: ").append(getCollocazione())
          .append("\nTipi di visita:");

        List<TipiVisitaClass> tipi = getTipiVisitaClass();
        if (tipi == null || tipi.isEmpty()) {
            sb.append(" []");
        } else {
            for (TipiVisitaClass t : tipi) {
                sb.append("\n - ");
                if (t == null) {
                    sb.append("null");
                } else {
                    sb.append(t.toString());
                }
            }
        }
        return sb.toString();
    }

}
