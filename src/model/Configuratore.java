package src.model;

/**
 * Rappresenta un configuratore del sistema, ovvero un utente con privilegi amministrativi
 * che può gestire luoghi, volontari, visite e altre configurazioni del sistema.
 * Estende la classe Utente senza aggiungere proprietà specifiche.
 * 
 */
public class Configuratore extends Utente {

    /**
     * Costruttore per creare un nuovo configuratore.
     * 
     * @param nome il nome del configuratore
     * @param cognome il cognome del configuratore
     * @param email l'indirizzo email del configuratore (utilizzato come identificatore univoco)
     * @param password la password per l'autenticazione
     */
    public Configuratore(String nome, String cognome, String email, String password) {
        super(email, password, nome, cognome);
    }

    /**
     * Restituisce una rappresentazione in formato stringa del configuratore.
     * 
     * @return una stringa contenente le informazioni del configuratore
     */
    @Override
    public String toString() {
        return "Configuratore{" +
                "nome='" + getNome() + '\'' +
                ", cognome='" + getCognome() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }

}
