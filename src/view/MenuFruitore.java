package src.view;

import java.time.LocalDate;

import lib.MyMenu;
import src.controller.FruitoreController;

/**
 * Menu specifico per i fruitori del sistema di gestione visite.
 * Fornisce un'interfaccia per le operazioni che i fruitori possono eseguire:
 * - Visualizzazione delle visite disponibili
 * - Prenotazione di visite
 * - Gestione delle proprie prenotazioni
 * 
 * Il menu mostra la data corrente e permette la navigazione tra le diverse funzionalità
 * disponibili per i fruitori autenticati.
 *  
 */
public class MenuFruitore implements Menu {
    /** Controller per la gestione delle operazioni dei fruitori */
    private FruitoreController visitatoreController;
    
    /** Opzioni disponibili nel menu dei fruitori */
    private static final String[] SELECT = {"Mostra visite disponibili", "Prenota visita", "Visualizza mie prenotazioni", "Cancella prenotazione"};

    /**
     * Costruttore del menu fruitore.
     * 
     * @param visitatoreController Controller per la gestione delle operazioni dei fruitori
     */
    public MenuFruitore(FruitoreController visitatoreController) {
        this.visitatoreController = visitatoreController;
    }

    /**
     * {@inheritDoc}
     * Visualizza il menu principale per i fruitori con la data corrente.
     * Gestisce la selezione delle opzioni e le delega al controller appropriato.
     */
    @Override
    public void mostraMenu() {
                boolean goOn = true;
        System.out.printf("oggi è il: %d/%d/%d\n", LocalDate.now().getDayOfMonth(), LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        do {
            MyMenu menu = new MyMenu("Digitare l'opzione desiderata\n", SELECT);
            int chosed = menu.scegli();

            switch (chosed) {
                case 1 -> visitatoreController.mostraVisiteDisponibili();
                case 2 -> visitatoreController.prenotaVisita();
                case 3 -> visitatoreController.visualizzaMiePrenotazioni();
                case 4 -> visitatoreController.cancellaPrenotazione();

                case 0 -> goOn = false;
                default -> System.out.println("Opzione non valida.");
            }
        } while (goOn);
    }

}
