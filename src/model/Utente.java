package src.model;

/**
 * Classe astratta che rappresenta un utente generico del sistema.
 * Fornisce le proprietà e i metodi comuni a tutti i tipi di utenti
 * (Volontario, Configuratore, Fruitore).
 *  
 */
public abstract class Utente {
    /** Indirizzo email dell'utente (identificatore univoco) */
    private String email;
    
    /** Password per l'autenticazione */
    private String password;
    
    /** Nome dell'utente */
    private String nome;
    
    /** Cognome dell'utente */
    private String cognome;

    /**
     * Costruttore per creare un nuovo utente.
     * 
     * @param email l'indirizzo email dell'utente
     * @param password la password per l'autenticazione
     * @param nome il nome dell'utente
     * @param cognome il cognome dell'utente
     */
    public Utente(String email, String password, String nome, String cognome) {
        this.email = email;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
    }

    /**
     * Restituisce l'indirizzo email dell'utente.
     * 
     * @return l'email dell'utente
     */
    public String getEmail() {
        return email;
    }

    /**
     * Imposta l'indirizzo email dell'utente.
     * 
     * @param email il nuovo indirizzo email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Restituisce la password dell'utente.
     * 
     * @return la password dell'utente
     */
    public String getPassword() {
        return password;
    }

    /**
     * Imposta la password dell'utente.
     * 
     * @param password la nuova password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Imposta il nome dell'utente.
     * 
     * @param nome il nuovo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce il nome dell'utente.
     * 
     * @return il nome dell'utente
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce il cognome dell'utente.
     * 
     * @return il cognome dell'utente
     */
    public String getCognome() {
        return cognome;
    }

    /**
     * Imposta il cognome dell'utente.
     * 
     * @param cognome il nuovo cognome
     */
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    /**
     * Restituisce una rappresentazione in formato stringa dell'utente.
     * 
     * @return una stringa contenente il tipo di utente e i suoi dati principali
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + 
                        "\nNome=" + nome + 
                        "\nCognome=" + cognome + 
                        "\nEmail=" + email;
    }
}
