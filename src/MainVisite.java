package src;

import src.controller.MasterController;

/**
 * Classe principale del sistema di gestione visite.
 * Rappresenta il punto di ingresso dell'applicazione e coordina l'avvio
 * e la chiusura del sistema attraverso il MasterController.
 * 
 * Il sistema gestisce:
 * - Autenticazione di utenti (Volontari, Configuratori, Fruitori)
 * - Pianificazione e gestione delle visite guidate
 * - Gestione delle disponibilità dei volontari
 * - Prenotazioni e configurazioni del sistema
 * - Gestione di luoghi, date precluse e parametri operativi
 * 
 * L'applicazione utilizza un'architettura MVC con pattern Factory e Singleton
 * per garantire modularità, scalabilità e gestione efficiente delle risorse.
 * 
 * @author Benedetta Anglani    Matr. 742087
 * @author Anna Cavazzini       Matr. 740433
 * @author Ruggero Lombardi     Matr. 743179
 * 
 * @version 4.0
 * @since 1.0
 */
public class MainVisite {
    public static void main(String[] args) {
        MasterController masterController = new MasterController().createApp();

        try {
            masterController.startApp();
        } finally {
            masterController.stopExecutorService();
        }
    }
}
