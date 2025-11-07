package src.view;

import lib.MyMenu;
import src.controller.VolontariController;

import java.time.LocalDate;

/**
 * Menu specifico per i volontari del sistema di gestione visite.
 * Fornisce un'interfaccia per le operazioni che i volontari possono eseguire:
 * - Visualizzazione delle visite assegnate
 * - Inserimento e modifica delle disponibilità
 * - Modifica della password personale
 * 
 * Il menu mostra la data corrente e permette la navigazione tra le diverse funzionalità
 * disponibili per i volontari autenticati.
 *  
 */
public class MenuVolontario implements Menu {
    /** Controller per la gestione delle operazioni dei volontari */
    private final VolontariController volontariController;
    
    /** Opzioni disponibili nel menu dei volontari */
    private static final String[] OPZIONI_VOLONTARIO = {
        "Visualizza visite assegnate", "Visualizza Tipi Visita Assegnati",
        "Inserisci disponibilità", "Modifica disponibilità", 
        "Modifica password",
    };

    /**
     * Costruttore del menu volontario.
     * 
     * @param volontariController Controller per la gestione delle operazioni dei volontari
     */
    public MenuVolontario(VolontariController volontariController) {
        this.volontariController = volontariController;
    }

    /**
     * {@inheritDoc}
     * Visualizza il menu principale per i volontari con la data corrente.
     * Gestisce la selezione delle opzioni e le delega al controller appropriato.
     */
    @Override
    public void mostraMenu() {
        boolean goOn = true;
        System.out.printf("Oggi è il: %d/%d/%d\n", LocalDate.now().getDayOfMonth(), LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        do {
            MyMenu menu = new MyMenu("Digitare l'opzione desiderata\n", OPZIONI_VOLONTARIO);
            int chosed = menu.scegli();

            switch (chosed) {
                case 1 -> volontariController.visualizzaVisiteVolontario();
                case 2 -> volontariController.visualizzaTipiVisitaVolontario();
                case 3 -> volontariController.raccogliDisponibilitaVolontario();
                case 4 -> volontariController.modificaDisponibilitaVolontario();
                case 5 -> volontariController.modificaPassword();
                case 0 -> goOn = false;
                default -> System.out.println("Opzione non valida.");
            }
        } while (goOn);
    }
}
