package src.view;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provider per la gestione dei messaggi localizzati nel sistema.
 * Utilizza ResourceBundle per fornire supporto all'internazionalizzazione (i18n)
 * permettendo di cambiare la lingua dei messaggi dell'applicazione.
 * 
 * La classe gestisce automaticamente il caricamento dei bundle di messaggi
 * in base al locale corrente e fornisce metodi per recuperare i messaggi
 * tradotti tramite chiavi.
 * 
 */
public class MessageProvider {

    /** Bundle di messaggi per il locale corrente */
    private static ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());

    /**
     * Imposta il locale per i messaggi dell'applicazione.
     * Ricarica il ResourceBundle con il nuovo locale specificato.
     * 
     * @param locale Il nuovo locale da utilizzare per i messaggi
     */
    public static void setLocale(Locale locale) {
        messages = ResourceBundle.getBundle("messages", locale);
    }

    /**
     * Recupera un messaggio localizzato tramite la sua chiave.
     * 
     * @param key La chiave del messaggio nel file di risorse
     * @return Il messaggio localizzato corrispondente alla chiave
     * @throws java.util.MissingResourceException se la chiave non viene trovata
     */
    public static String getMessage(String key) {
        return messages.getString(key);
    }
}