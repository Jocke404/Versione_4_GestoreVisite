package src.view;

import java.time.LocalDate;

import lib.MyMenu;
import src.controller.FruitoreController;

public class MenuFruitore implements Menu {
    private FruitoreController visitatoreController;
    private static final String[] SELECT = {"Mostra visite disponibili", "Prenota visita", "Visualizza mie prenotazioni", "Cancella prenotazione"};

    public MenuFruitore(FruitoreController visitatoreController) {
        this.visitatoreController = visitatoreController;
    }

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
