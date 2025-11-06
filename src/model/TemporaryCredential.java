package src.model;

/**
 * Rappresenta una credenziale temporanea per l'accesso al sistema.
 * Utilizzata per gestire credenziali provvisorie che devono essere modificate al primo accesso.
 *  
 */
public class TemporaryCredential {
    /** Nome utente della credenziale temporanea */
    private String username;
    
    /** Password temporanea */
    private String password;

    /**
     * Costruttore per creare una nuova credenziale temporanea.
     * 
     * @param username il nome utente
     * @param password la password temporanea
     */
    public TemporaryCredential(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Restituisce il nome utente.
     * 
     * @return il nome utente
     */
    public String getUsername() {
        return username;
    }

    /**
     * Imposta il nome utente.
     * 
     * @param username il nuovo nome utente
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Restituisce la password.
     * 
     * @return la password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Imposta la password.
     * 
     * @param password la nuova password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
