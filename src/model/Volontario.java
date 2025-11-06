package src.model;

import java.util.List;

/**
 * Rappresenta un volontario che può condurre visite guidate.
 * Estende la classe Utente aggiungendo la gestione dei tipi di visita che il volontario può effettuare.
 * 
 */
public class Volontario extends Utente {
    /** Lista dei tipi di visita che il volontario è qualificato a condurre */
    private List<TipiVisitaClass> tipiDiVisite;  

    /**
     * Costruttore per creare un nuovo volontario.
     * 
     * @param nome il nome del volontario
     * @param cognome il cognome del volontario
     * @param email l'indirizzo email del volontario (utilizzato come identificatore univoco)
     * @param password la password per l'autenticazione
     * @param tipiDiVisite la lista dei tipi di visita che il volontario può condurre
     */
    public Volontario(String nome, String cognome, String email, String password, List<TipiVisitaClass> tipiDiVisite) {
        super(email, password, nome, cognome);
        this.tipiDiVisite = tipiDiVisite;
    }

    /**
     * Restituisce la lista dei tipi di visita che il volontario può condurre.
     * 
     * @return la lista dei tipi di visita associati al volontario
     */
    public List<TipiVisitaClass> getTipiDiVisite() {
        return tipiDiVisite;
    }

    /**
     * Imposta la lista dei tipi di visita che il volontario può condurre.
     * 
     * @param tipiDiVisite la nuova lista di tipi di visita
     */
    public void setTipiDiVisite(List<TipiVisitaClass> tipiDiVisite) {
        this.tipiDiVisite = tipiDiVisite;
    }

    /**
     * Aggiunge un tipo di visita alla lista delle competenze del volontario.
     * Se il tipo è già presente, non viene aggiunto nuovamente.
     * 
     * @param tipoVisita il tipo di visita da aggiungere
     */
    public void aggiungiTipoVisita(TipiVisitaClass tipoVisita) {
        if (!this.tipiDiVisite.contains(tipoVisita)) {
            this.tipiDiVisite.add(tipoVisita);
        }
    }
    
    /**
     * Rimuove un tipo di visita dalla lista delle competenze del volontario.
     * 
     * @param tipoVisita il tipo di visita da rimuovere
     */
    public void rimuoviTipoVisita(TipiVisitaClass tipoVisita) {
        this.tipiDiVisite.remove(tipoVisita);
    }

    /**
     * Verifica se il volontario è qualificato per un determinato tipo di visita.
     * 
     * @param tipoVisita il tipo di visita da verificare
     * @return true se il volontario può condurre questo tipo di visita, false altrimenti
     */
    public boolean contieneTipoVisita(TipiVisitaClass tipoVisita) {
        return this.tipiDiVisite.contains(tipoVisita);
    }

    /**
     * Confronta questo volontario con un altro oggetto per verificarne l'uguaglianza.
     * Due volontari sono considerati uguali se hanno lo stesso indirizzo email (case-insensitive).
     * 
     * @param o l'oggetto da confrontare
     * @return true se i volontari hanno la stessa email, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Volontario)) return false;
        Volontario that = (Volontario) o;
        String e1 = this.getEmail();
        String e2 = that.getEmail();
        if (e1 == null && e2 == null) return true;
        if (e1 == null || e2 == null) return false;
        return e1.trim().equalsIgnoreCase(e2.trim());
    }
 
    /**
     * Calcola l'hash code del volontario basato sull'email.
     * 
     * @return l'hash code dell'email del volontario
     */
    @Override
    public int hashCode() {
        String e = getEmail();
        return e == null ? 0 : e.trim().toLowerCase().hashCode();
    }

    /**
     * Restituisce una rappresentazione in formato stringa del volontario,
     * includendo i dati personali e i tipi di visita che può condurre.
     * 
     * @return una stringa contenente le informazioni del volontario
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append("\nTipi di Visite:");

        if (tipiDiVisite == null || tipiDiVisite.isEmpty()) {
            sb.append(" []");
        } else {
            for (TipiVisitaClass t : tipiDiVisite) {
                sb.append("\n - ");
                sb.append(t == null ? "null" : t.toString());
            }
        }
        return sb.toString();
    }

}
