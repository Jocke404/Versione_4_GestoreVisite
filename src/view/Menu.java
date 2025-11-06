package src.view;

/**
 * Interfaccia per la gestione dei menu del sistema di gestione visite.
 * Definisce il contratto che tutti i menu devono rispettare per fornire
 * un'esperienza utente coerente attraverso le diverse tipologie di utenti.
 * 
 * Implementazioni specifiche:
 * - MenuVolontario: menu per i volontari
 * - MenuFruitore: menu per i fruitori delle visite
 * - MenuConfiguratore: menu per i configuratori del sistema
 * 
 */
public interface Menu {
    /**
     * Visualizza e gestisce il menu principale per il tipo di utente specifico.
     * Questo metodo dovrebbe presentare le opzioni disponibili, gestire l'input
     * dell'utente e eseguire le azioni corrispondenti in un ciclo fino a quando
     * l'utente non sceglie di uscire.
     */
    void mostraMenu();
    
}
