package src.view;
import java.util.List;

/**
 * Interfaccia base per la gestione della visualizzazione nel sistema di gestione visite.
 * Definisce i metodi fondamentali che tutte le implementazioni di view devono fornire
 * per l'interazione con l'utente, sia essa console-based o grafica.
 * 
 * Implementazioni note:
 * - ConsoleIO: interfaccia testuale via console
 * - GraphicalView: interfaccia grafica con Swing
 * 
 */
public interface View {
    /**
     * Mostra un messaggio informativo all'utente.
     * 
     * @param messaggio Il messaggio da visualizzare
     */
    void mostraMessaggio(String messaggio);

    /**
     * Mostra un messaggio di errore all'utente.
     * Il messaggio dovrebbe essere evidenziato come errore nell'interfaccia.
     * 
     * @param errore Il messaggio di errore da visualizzare
     */
    void mostraErrore(String errore);

    /**
     * Mostra un elenco di stringhe numerato all'utente.
     * 
     * @param elementi Lista delle stringhe da visualizzare
     */
    void mostraElenco(List<String> elementi);

    /**
     * Mostra un elenco di oggetti numerato all'utente.
     * Utilizza il metodo toString() degli oggetti per la visualizzazione.
     * 
     * @param oggetti Lista degli oggetti da visualizzare
     */
    void mostraElencoConOggetti(List<?> oggetti);
    
}
