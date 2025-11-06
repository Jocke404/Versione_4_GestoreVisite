package src.view;

import java.time.LocalDate;

import lib.MyMenu;
import src.controller.ConfiguratoriController;

/**
 * Menu specifico per i configuratori del sistema di gestione visite.
 * Fornisce un'interfaccia completa per tutte le operazioni amministrative:
 * - Gestione delle configurazioni del sistema (date precluse, parametri)
 * - Gestione dei luoghi di visita
 * - Gestione delle visite (creazione, modifica, eliminazione)
 * - Gestione dei volontari
 * 
 * Il menu è organizzato in sottomenu tematici per facilitare la navigazione
 * e fornisce accesso a tutte le funzionalità amministrative del sistema.
 *  
 */
public class MenuConfiguratore implements Menu {
    /** Opzioni del menu principale */
    private static final String[] MENU = {
        "Gestione Configurazioni", "Gestione Luoghi", 
        "Gestione Visite", "Gestione Volontari"
    };

    /** Opzioni del sottomenu configurazioni */
    private static final String [] SOTTOMENU_CONFIGURAZIONI={
        "Aggiungi date precluse", "Visualizza date precluse", 
        "Visualizza ambito territoriale", "Modifica numero massimo persone per visita",
        "Modifica numero persone iscrivibili da un fruitore", "Elimina date precluse"
    };
    
    /** Opzioni del sottomenu volontari */
    private static final String [] SOTTOMENU_VOLONTARI={
        "Aggiungi Volontario", "Aggiungi volontari a un tipo di visita",
        "Rimuovi volontari da un tipo di visita", "Visualizza tutti i Volontari",
        "Visualizza volontari per tipo di visita",
        "Elimina Volontario" 
    };

    /** Opzioni del sottomenu visite */
    private static final String [] SOTTOMENU_VISITE={
        "Aggiungi Visita", "Aggiungi nuovo tipo di visita", "Visualizza Visite",
        "Modifica stato della visita", "Modifica data della visita",  
        "Visualizza visite per stato", "Visualizza archivio storico",
        "Elimina Visita", "Assegna Visita a Volontario", 
        "Rimuovi Visita da Volontario",
        "Rimuovi tipo di visita"
    };

    /** Opzioni del sottomenu luoghi */
    private static final String [] SOTTOMENU_LUOGHI={
        "Aggiungi Luogo", "Visualizza Luoghi", "Stampa Tipi Visita per Luogo",
        "Modifica Luogo", "Elimina Luogo"
    };

    /** Controller per la gestione delle operazioni dei configuratori */
    private final ConfiguratoriController configuratoriController;

    /**
     * Costruttore del menu configuratore.
     * 
     * @param configuratoriController Controller per la gestione delle operazioni dei configuratori
     */
    public MenuConfiguratore(ConfiguratoriController configuratoriController) {
        this.configuratoriController = configuratoriController;
    }
    
    /**
     * {@inheritDoc}
     * Visualizza il menu principale per i configuratori con la data corrente.
     * Gestisce la navigazione tra i diversi sottomenu di amministrazione.
     */
    @Override
    public void mostraMenu() {
        boolean goOn = true;
        System.out.printf("oggi è il: %d/%d/%d\n", LocalDate.now().getDayOfMonth(), LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        do {
            MyMenu menu = new MyMenu("Digitare l'opzione desiderata\n", MENU);
            int chosed = menu.scegli();

            switch (chosed) {
                case 1 -> sottoMenuConfig();
                case 2 -> sottoMenuLuoghi();
                case 3 -> sottoMenuVisite();
                case 4 -> sottomenuVolontari();
                
                case 0 -> goOn = false;
                default -> System.out.println("Opzione non valida.");
            }
        } while (goOn);
    }

    /**
     * Gestisce il sottomenu per le configurazioni del sistema.
     * Permette di gestire date precluse, parametri del sistema e ambito territoriale.
     */
    private void sottoMenuConfig(){
        boolean tornaIndietro = false;

        do{
            MyMenu sottomenu = new MyMenu ("GESTIONE CONFIGURAZIONI", SOTTOMENU_CONFIGURAZIONI);
            int sceltaSottomenu = sottomenu.scegli();

            switch (sceltaSottomenu){
                case 1 -> configuratoriController.aggiungiDatePrecluse();
                case 2 -> configuratoriController.mostraDatePrecluse();
                case 3 -> configuratoriController.mostraAmbitoTerritoriale();
                case 4 -> configuratoriController.modificaMaxPersone();
                case 5 -> configuratoriController.modificaNumeroPersoneIscrivibili();
                case 6 -> configuratoriController.eliminaDatePrecluse();
                 

                case 0 -> tornaIndietro = true;
                default -> System.out.println("Opzione non valida.");
            }
        } while (!tornaIndietro);
    }
    
    /**
     * Gestisce il sottomenu per la gestione dei luoghi.
     * Permette di aggiungere, modificare, eliminare e visualizzare i luoghi del sistema.
     */
    private void sottoMenuLuoghi(){
            boolean tornaIndietro = false;

            do{
                MyMenu sottomenu = new MyMenu ("GESTIONE LUOGHI", SOTTOMENU_LUOGHI);
                
                int sceltaSottomenu = sottomenu.scegli();

                switch (sceltaSottomenu){
                    case 1 -> configuratoriController.aggiungiLuogo();
                    case 2 -> configuratoriController.mostraLuoghi();
                    case 3 -> configuratoriController.stampaTipiVisitaClassPerLuogo();
                    case 4 -> configuratoriController.modificaLuogo();
                    case 5 -> configuratoriController.eliminaLuogo();

                    case 0 -> tornaIndietro = true;
                    default -> System.out.println("Opzione non valida.");
                }
            } while (!tornaIndietro);
    }

    /**
     * Gestisce il sottomenu per la gestione delle visite.
     * Permette di gestire tutti gli aspetti delle visite: creazione, modifica, 
     * assegnazione ai volontari, gestione dei tipi di visita.
     */
    private void sottoMenuVisite(){
        boolean tornaIndietro = false;

        do{
            MyMenu sottomenu = new MyMenu ("GESTIONE VISITE", SOTTOMENU_VISITE);
            
            int sceltaSottomenu = sottomenu.scegli();

            switch (sceltaSottomenu){
                case 1 -> configuratoriController.aggiungiVisita();
                case 2 -> configuratoriController.aggiungiNuovoTipoVisita();
                case 3 -> configuratoriController.mostraVisite();
                case 4 -> configuratoriController.modificaStatoVisita();
                case 5 -> configuratoriController.modificaDataVisita();
                case 6 -> configuratoriController.visualizzaVisitePerStato();
                case 7 -> configuratoriController.visualizzaArchivioStorico();
                case 8 -> configuratoriController.eliminaVisita();
                case 9 -> configuratoriController.assegnaVisitaAVolontario();
                case 10 -> configuratoriController.rimuoviVisitaDaVolontario();
                case 11 -> configuratoriController.rimuoviTipoDiVisita();



                case 0 -> tornaIndietro = true;
                default -> System.out.println("Opzione non valida.");
            }
        } while (!tornaIndietro);
    }

    /**
     * Gestisce il sottomenu per la gestione dei volontari.
     * Permette di aggiungere, eliminare volontari e gestire i loro tipi di visita.
     */
    private void sottomenuVolontari(){
        boolean tornaIndietro = false;

        do{
            MyMenu sottomenu = new MyMenu ("GESTIONE VOLONTARI", SOTTOMENU_VOLONTARI);
            
            int sceltaSottomenu = sottomenu.scegli();

            switch (sceltaSottomenu){
                case 1 -> configuratoriController.aggiungiVolontario();
                case 2 -> configuratoriController.aggiungiVolontariATipoVisita();
                case 3 -> configuratoriController.rimuoviTipoVisitaDaVolontari();
                case 4 -> configuratoriController.mostraVolontari();
                case 5 -> configuratoriController.visualizzaVolontariPerTipoVisita();
                case 6 -> configuratoriController.eliminaVolontario();

                case 0 -> tornaIndietro = true;
                default -> System.out.println("Opzione non valida.");
            }
        } while (!tornaIndietro);
    }

}
