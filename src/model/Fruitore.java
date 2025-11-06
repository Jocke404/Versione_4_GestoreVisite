package src.model;

/**
 * Rappresenta un fruitore, ovvero un utente che può prenotare e partecipare alle visite guidate.
 * Estende la classe Utente senza aggiungere proprietà specifiche.
 *  
 */
public class Fruitore extends Utente {

    /**
     * Costruttore per creare un nuovo fruitore.
     * 
     * @param nome il nome del fruitore
     * @param cognome il cognome del fruitore
     * @param email l'indirizzo email del fruitore (utilizzato come identificatore univoco)
     * @param password la password per l'autenticazione
     */
    public Fruitore(String nome, String cognome, String email, String password) {
        super(email, password, nome, cognome);
    }

    /**
     * Restituisce una rappresentazione in formato stringa del fruitore.
     * 
     * @return una stringa contenente le informazioni del fruitore
     */
    @Override
    public String toString() {
        return "Fruitore{" +
                "nome='" + getNome() + '\'' +
                ", cognome='" + getCognome() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }

    

}
