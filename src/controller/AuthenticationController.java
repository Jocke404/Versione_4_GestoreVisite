package src.controller;

import src.view.*;
import src.model.*;

/**
 * Controller per la gestione dell'autenticazione degli utenti.
 * Coordina il processo di login, la gestione delle credenziali temporanee
 * e la creazione di nuovi account fruitori.
 * 
 *  
 *  
 */
public class AuthenticationController {
    /** Manager delle credenziali per autenticazione e gestione utenti */
    private final CredentialManager credentialManager;
    
    /** Interfaccia per l'interazione con l'utente */
    private final ConsoleIO consoleIO;    
    
    /** Utente attualmente autenticato nel sistema */
    private Utente utenteLoggato;

    /**
     * Costruttore del controller di autenticazione.
     * 
     * @param credentialManager il manager delle credenziali
     * @param consoleIO l'interfaccia per l'input/output su console
     */
    public AuthenticationController(
        CredentialManager credentialManager,
        ConsoleIO consoleIO
    ) {
        this.credentialManager = credentialManager;
        this.consoleIO = consoleIO;
    }

    /**
     * Gestisce il processo di autenticazione dell'utente.
     * Se l'email è registrata, verifica le credenziali e gestisce eventuali
     * credenziali temporanee richiedendo la modifica di email e password.
     * Se l'email non è registrata, crea un nuovo account fruitore.
     * 
     * @return true se l'autenticazione ha successo, false altrimenti
     */
    public boolean autentica() {
        String email = consoleIO.chiediEmail();
        boolean emailPresente = credentialManager.isEmailPresente(email);
        String password = consoleIO.chiediPassword();

        if (emailPresente) {
            Utente utente = credentialManager.autentica(email, password);

            if (utente == null) {
                consoleIO.mostraMessaggio("Credenziali non valide.");
                return false;
            }

             
            if (!credentialManager.isPasswordModificata(email)) {
                consoleIO.mostraMessaggio("Hai credenziali temporanee. Ti preghiamo di modificarle.");
                boolean emailCorretta = consoleIO.chiediConfermaEmail(email);
                if (!emailCorretta) {
                    String nuovaEmail = consoleIO.chiediNuovaEmail(credentialManager);
                    credentialManager.aggiornaEmailUtente(utente, nuovaEmail);
                    consoleIO.mostraMessaggio("Email aggiornata con successo.");
                }
                modificaPasswordUtente(utente);
            }

            if (!credentialManager.isPasswordModificata(email)) {
                if(utente instanceof Configuratore) {
                    String nuovoNome = consoleIO.chiediNome();
                    String nuovoCognome = consoleIO.chiediCognome();
                    credentialManager.aggiornaNomeCognomeConf(utente, nuovoNome, nuovoCognome);
                }
            }
            this.utenteLoggato = utente;
            return true;
        } else {
            consoleIO.mostraMessaggio("Email non registrata. Procedi con la creazione di un nuovo account.");
            Utente nuovoUtente = creaNuovoUtente();
            this.utenteLoggato = nuovoUtente;
            return true;
        }
    }

    /**
     * Richiede e aggiorna la password di un utente.
     * 
     * @param utente l'utente di cui modificare la password
     */
    public void modificaPasswordUtente(Utente utente) {
        String nuovaPassword = consoleIO.chiediPassword();
        credentialManager.aggiornaPasswordUtente(utente, nuovaPassword);
        consoleIO.mostraMessaggio("Password aggiornata con successo.");
    }

    /**
     * Crea un nuovo utente fruitore raccogliendo i dati necessari.
     * Richiede nome, cognome, email e password, quindi crea le credenziali
     * nel sistema.
     * 
     * @return il nuovo utente fruitore creato
     */
    public Utente creaNuovoUtente() {
        String name = consoleIO.chiediNome();
        String surname = consoleIO.chiediCognome();
        String newEmail = consoleIO.chiediNuovaEmail(credentialManager);
        String newPassword = consoleIO.chiediPassword();
        String tipoUtente = "Fruitore";
        Utente nuovoUtente = credentialManager.creaNuoveCredenziali(tipoUtente, name, surname, newEmail, newPassword);
        return nuovoUtente;
    }

    /**
     * Restituisce l'utente attualmente autenticato.
     * 
     * @return l'utente loggato nel sistema
     */
    public Utente getUtenteCorrente() {
        return utenteLoggato;
    }
}
