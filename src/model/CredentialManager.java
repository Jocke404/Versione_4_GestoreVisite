package src.model;

import java.util.List;

import src.factory.UserFactory;
import src.model.db.*;

/**
 * Gestisce l'autenticazione e la gestione delle credenziali degli utenti del sistema.
 * Questa classe coordina l'accesso ai diversi tipi di utenti (Volontari, Configuratori, Fruitori)
 * e fornisce funzionalità per l'autenticazione, la modifica delle credenziali e la gestione degli account.
 * 
 * Mantiene riferimenti agli utenti attualmente autenticati e coordina le operazioni
 * con i rispettivi manager del database.
 *  
 */
public class CredentialManager {

    /** Gestore degli aggiornamenti del database */
    private final DatabaseUpdater databaseUpdater;
    
    /** Manager per la gestione dei volontari */
    private final VolontariManager volontariManager;
    
    /** Manager per la gestione dei configuratori */
    private final ConfiguratoriManager configuratoriManager;
    
    /** Manager per la gestione dei fruitori */
    private final FruitoreManager fruitoreManager;
    
    /** Volontario attualmente autenticato */
    private Volontario volontarioCorrente = null;
    
    /** Configuratore attualmente autenticato */
    private Configuratore configuratoreCorrente = null;
    
    /** Fruitore attualmente autenticato */
    private Fruitore fruitoreCorrente = null;

    /**
     * Costruttore del CredentialManager.
     * Inizializza il gestore delle credenziali con i manager necessari per
     * l'accesso ai dati degli utenti.
     * 
     * @param databaseUpdater Gestore degli aggiornamenti del database
     * @param volontariManager Manager per la gestione dei volontari
     * @param configuratoriManager Manager per la gestione dei configuratori
     * @param fruitoreManager Manager per la gestione dei fruitori
     * @param databaseManager Manager generale del database
     */
    public CredentialManager(DatabaseUpdater databaseUpdater, VolontariManager volontariManager, 
                            ConfiguratoriManager configuratoriManager, FruitoreManager fruitoreManager, DatabaseManager databaseManager) {
        this.databaseUpdater = databaseUpdater;
        this.volontariManager = volontariManager;
        this.configuratoriManager = configuratoriManager;
        this.fruitoreManager = fruitoreManager;
    }

    /**
     * Autentica un utente nel sistema utilizzando email e password.
     * Determina il tipo di utente e crea l'oggetto utente appropriato.
     * 
     * @param email Email dell'utente
     * @param password Password dell'utente
     * @return Oggetto Utente se l'autenticazione ha successo, null altrimenti
     */
    public Utente autentica(String email, String password) {
        String tipoUtente = estraiTipoUtente(email, password);

        if (tipoUtente == null) {
            return null;
        }

        String nome = null;
        String cognome = null;
        List<TipiVisitaClass> tipidiVisite = null;

        switch (tipoUtente) {
            case UserFactory.VOLONTARIO:
                volontarioCorrente = volontariManager.getVolontariMap().get(email);
                if (volontarioCorrente == null) {
                    return null;
                }
                nome = volontarioCorrente.getNome();
                cognome = volontarioCorrente.getCognome();
                tipidiVisite = volontarioCorrente.getTipiDiVisite();
                break;

            case UserFactory.CONFIGURATORE:
                configuratoreCorrente = configuratoriManager.getConfiguratoriMap().get(email);
                if (configuratoreCorrente == null) {
                    return null;
                }
                nome = configuratoreCorrente.getNome();
                cognome = configuratoreCorrente.getCognome();
                break;

            case UserFactory.FRUITORE:
                fruitoreCorrente = fruitoreManager.getFruitoriMap().get(email);
                if (fruitoreCorrente == null) {
                    return null;
                }
                nome = fruitoreCorrente.getNome();
                cognome = fruitoreCorrente.getCognome();
                break;

            default:
                return null;
        }

        return UserFactory.createUser(tipoUtente, email, password, nome, cognome, tipidiVisite);
    }

    /**
     * Carica le credenziali temporanee dal database.
     * Questo metodo è utilizzato per gestire account temporanei o di prova.
     */
    public void caricaCredenzialiTemporanee() {
        databaseUpdater.getTemporaryCredentials();
    }

    /**
     * Aggiorna la password di un utente esistente.
     * La modifica viene applicata sia all'oggetto utente che persistita nel database.
     * 
     * @param utente Utente di cui modificare la password
     * @param nuovaPassword Nuova password da impostare
     */
    public void aggiornaPasswordUtente(Utente utente, String nuovaPassword) {
        utente.setPassword(nuovaPassword);
        if (utente instanceof Volontario) {
            volontariManager.getVolontariMap().put(utente.getEmail(), (Volontario) utente);
            volontariManager.modificaPsw(utente.getEmail(), nuovaPassword);
        }
        else if (utente instanceof Configuratore) {
            configuratoriManager.getConfiguratoriMap().put(utente.getEmail(), (Configuratore) utente);
            configuratoriManager.aggiornaPswConfiguratore(utente.getEmail(), nuovaPassword);
        } else if (utente instanceof Fruitore) {
            fruitoreManager.getFruitoriMap().put(utente.getEmail(), (Fruitore) utente);
            fruitoreManager.aggiornaPswFruitore(utente.getEmail(), nuovaPassword);
        }
    }

    /**
     * Crea nuove credenziali per un utente e lo registra nel sistema.
     * Supporta la creazione di Configuratori e Fruitori.
     * 
     * @param tipoUtente Tipo di utente da creare (UserFactory.CONFIGURATORE o UserFactory.FRUITORE)
     * @param name Nome dell'utente
     * @param surname Cognome dell'utente
     * @param newEmail Email dell'utente
     * @param newPassword Password dell'utente
     * @return Nuovo oggetto Utente creato
     */
    public Utente creaNuoveCredenziali(String tipoUtente, String name, String surname, String newEmail, String newPassword) {
        Utente nuovoUtente = UserFactory.createUser(tipoUtente, newEmail, newPassword, name, surname, null);

        switch (tipoUtente) {
            case UserFactory.CONFIGURATORE:
                configuratoriManager.getConfiguratoriMap().put(newEmail, (Configuratore) nuovoUtente);
                configuratoriManager.aggiungiNuovoConf((Configuratore) nuovoUtente);
                break;
            case UserFactory.FRUITORE:
                fruitoreManager.getFruitoriMap().put(newEmail, (Fruitore) nuovoUtente);
                fruitoreManager.aggiungiNuovoFruitore((Fruitore) nuovoUtente);
                break;
        }
        return nuovoUtente;
    }

    /**
     * Estrae il tipo di utente basandosi su email e password.
     * Consulta il database per determinare se l'utente è un volontario,
     * configuratore o fruitore.
     * 
     * @param email Email dell'utente
     * @param password Password dell'utente
     * @return Tipo di utente (UserFactory.VOLONTARIO, UserFactory.CONFIGURATORE, UserFactory.FRUITORE) o null se non trovato
     */
    public String estraiTipoUtente(String email, String password) {
        String tipo_utente = databaseUpdater.getTipoUtente(email, password);
        return tipo_utente;
    }

    /**
     * Verifica se la password di un utente è stata modificata rispetto a quella di default.
     * Utile per forzare il cambio password al primo accesso.
     * 
     * @param email Email dell'utente da verificare
     * @return true se la password è stata modificata, false altrimenti
     */
    public boolean isPasswordModificata(String email) {
        Boolean passwordModificata = databaseUpdater.isPasswordModificata(email);
        return passwordModificata;
    }

    /**
     * Verifica se un'email è già presente nel sistema.
     * Controlla tra tutti i tipi di utenti registrati.
     * 
     * @param email Email da verificare
     * @return true se l'email è già presente, false altrimenti
     */
    public boolean isEmailPresente(String email) {
        return databaseUpdater.isEmailPresente(email);
    }

    /**
     * Aggiorna l'email di un utente esistente.
     * La modifica viene applicata sia nelle mappe in memoria che nel database.
     * 
     * @param utente Utente di cui modificare l'email
     * @param nuovaEmail Nuova email da impostare
     */
    public void aggiornaEmailUtente(Utente utente, String nuovaEmail) {
        
        if (utente instanceof Volontario) {
            volontariManager.getVolontariMap().remove(utente.getEmail());
            volontariManager.getVolontariMap().put(nuovaEmail, (Volontario) utente);
            volontariManager.aggiornaEmail(utente, nuovaEmail);
        }
        else if (utente instanceof Configuratore) {
            configuratoriManager.getConfiguratoriMap().remove(utente.getEmail());
            configuratoriManager.getConfiguratoriMap().put(nuovaEmail, (Configuratore) utente);
            configuratoriManager.aggiornaEmail(utente, nuovaEmail);
        } else if (utente instanceof Fruitore) {
            fruitoreManager.getFruitoriMap().remove(utente.getEmail());
            fruitoreManager.getFruitoriMap().put(nuovaEmail, (Fruitore) utente);
            fruitoreManager.aggiornaEmail(utente, nuovaEmail);
        }
        utente.setEmail(nuovaEmail);
    }

    /**
     * Aggiorna nome e cognome di un configuratore.
     * Questa operazione è specifica per i configuratori e aggiorna
     * sia l'oggetto in memoria che i dati persistiti.
     * 
     * @param utente Utente configuratore di cui modificare i dati
     * @param nuovoNome Nuovo nome da impostare
     * @param nuovoCognome Nuovo cognome da impostare
     */
    public void aggiornaNomeCognomeConf(Utente utente, String nuovoNome, String nuovoCognome) {
        configuratoreCorrente = (Configuratore) utente;
        configuratoreCorrente.setNome(nuovoNome);
        configuratoreCorrente.setCognome(nuovoCognome);
        configuratoriManager.aggiornaNomeCognome(utente.getEmail(), configuratoreCorrente);
    }

}
