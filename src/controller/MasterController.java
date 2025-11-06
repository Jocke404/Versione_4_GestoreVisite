package src.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import src.factory.MenuFactory;
import src.model.*;
import src.model.db.*;
import src.view.*;

/**
 * Controller principale dell'applicazione che coordina tutti i componenti.
 * Gestisce l'inizializzazione del sistema, l'autenticazione, la creazione dei menu
 * e l'orchestrazione dei task automatici (validazione visite, sincronizzazione database).
 * 
 *  
 *  
 */
public class MasterController {

    /** Il volontario attualmente autenticato */
    public Volontario volontarioCorrente;
    
    /** Il configuratore attualmente autenticato */
    public Configuratore configuratoreCorrente;
    
    /** Il fruitore attualmente autenticato */
    public Fruitore fruitoreCorrente;
    
    /** L'utente generico attualmente autenticato */
    public Utente utenteCorrente;
    
    /** Controller per la gestione dei thread pool */
    private ThreadPoolController threadPoolController;
    
    /** Manager dei volontari */
    private VolontariManager volontariManager;
    
    /** Manager dei configuratori */
    private ConfiguratoriManager configuratoriManager;
    
    /** Manager dei fruitori */
    private FruitoreManager fruitoreManager;
    
    /** Manager dei luoghi */
    private LuoghiManager luoghiManager;
    
    /** Manager delle visite */
    private VisiteManagerDB visiteManager;
    
    /** Manager delle prenotazioni */
    private PrenotazioneManager prenotazioneManager;
    
    /** Manager generico del database */
    private DatabaseManager databaseManager;
    
    /** Gestore degli aggiornamenti del database */
    private DatabaseUpdater databaseUpdater;
    
    /** Utility per l'aggiunta di elementi */
    private AggiuntaUtilita aggiuntaUtilita;
    
    /** Utility per la modifica di elementi */
    private ModificaUtilita modificaUtilita;
    
    /** Controller dell'autenticazione */
    private AuthenticationController authenticationController;
    
    /** Controller dei volontari */
    private VolontariController volontariController;
    
    /** Controller dei configuratori */
    private ConfiguratoriController configuratoriController;
    
    /** Controller dei fruitori */
    private FruitoreController fruitoreController;
    
    /** Controller dei luoghi */
    private LuoghiController luoghiController;
    
    /** Controller delle visite */
    private VisiteController visiteController;
    
    /** Validatore delle visite */
    private ValidatoreVisite validatore;
    
    /** Gestione dell'ambito territoriale */
    private AmbitoTerritoriale ambitoTerritoriale = new AmbitoTerritoriale();
    
    /** Factory per la creazione dei menu */
    private MenuFactory menuFactory = new MenuFactory();
    
    /** Interfaccia per l'I/O console */
    private ConsoleIO consoleIO = new ConsoleIO();
    
    /** Executor per task schedulati periodici */
    private ScheduledExecutorService scheduledExecutor;
    
    /** Utility per la visualizzazione */
    private ViewUtilita viewUtilita;
    
    /** Gestione disponibilità volontari */
    private Disponibilita disponibilita = new Disponibilita();

    /** Flag di autenticazione avvenuta */
    private Boolean isAuth = false;

    /**
     * Costruttore vuoto.
     */
    public MasterController(){}

    /**
     * Crea e inizializza l'applicazione con tutti i componenti necessari.
     * Istanzia manager, controller, utility e configura le dipendenze.
     * 
     * @return l'istanza configurata di MasterController
     */
    public MasterController createApp() {

        threadPoolController = ThreadPoolController.getInstance();
        volontariManager = new VolontariManager(threadPoolController);
        configuratoriManager = new ConfiguratoriManager(threadPoolController);
        fruitoreManager = new FruitoreManager(threadPoolController);
        luoghiManager = new LuoghiManager(threadPoolController);
        visiteManager = new VisiteManagerDB(threadPoolController);
        prenotazioneManager = new PrenotazioneManager(threadPoolController, visiteManager);
        databaseUpdater = new DatabaseUpdater(volontariManager, configuratoriManager, luoghiManager, visiteManager);
        aggiuntaUtilita = new AggiuntaUtilita(volontariManager, luoghiManager, visiteManager, prenotazioneManager);
        modificaUtilita = new ModificaUtilita(visiteManager);
        viewUtilita = ViewUtilita.getInstance();
        visiteController = new VisiteController(visiteManager);
        luoghiController = new LuoghiController(luoghiManager, viewUtilita);
        validatore = new ValidatoreVisite(visiteManager);


        

        ConsoleIO consoleIO = new ConsoleIO();
        CredentialManager credentialManager = new CredentialManager(
                                databaseUpdater, volontariManager, configuratoriManager, fruitoreManager, databaseManager);
        
        AuthenticationController authenticationController = new AuthenticationController(
            credentialManager, consoleIO);

        MasterController masterController = this;
        masterController.authenticationController = authenticationController;
        masterController.volontariController = volontariController;
        masterController.configuratoriController = configuratoriController;
        masterController.validatore = validatore;
        return masterController;
    }

    /**
     * Avvia l'applicazione dopo l'autenticazione.
     * Esegue task automatici di validazione visite e sincronizzazione disponibilità,
     * avvia task schedulati periodici e mostra il menu appropriato per l'utente.
     */
    public void startApp() {
        if (autentica()) {
            threadPoolController.createThreadPool(1).submit(() -> {
                try {
                    validatore.gestioneVisiteAuto();
                    validatore.gestioneDatePrecluseAuto();
                    disponibilita.sincronizzaDisponibilitaVolontari(volontariManager);
                } catch (Throwable t) {
                    System.err.println("Errore gestioneVisiteAuto (immediato): " + t.getMessage());
                }
            });

             
            if (scheduledExecutor == null || scheduledExecutor.isShutdown()) {
                scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("validatore-visite-scheduler");
                    return t;
                });
                scheduledExecutor.scheduleAtFixedRate(() -> {
                    try {
                        validatore.gestioneVisiteAuto();
                    } catch (Throwable t) {
                        System.err.println("Errore gestioneVisiteAuto (scheduler): " + t.getMessage());
                    }
                }, 5, 5, TimeUnit.SECONDS); 
            }
            aggiornaDatabaseAsync();            
            showMenu();
        }
    }

    /**
     * Aggiorna i dati dal database in modo asincrono.
     * Esegue la sincronizzazione in un thread separato.
     */
    private void aggiornaDatabaseAsync() {
        ExecutorService executor = threadPoolController.createThreadPool(4);
        executor.submit(()->{
            databaseUpdater.sincronizzaDalDatabase();
        });
    }

    /**
     * Arresta tutti gli ExecutorService gestiti dal ThreadPoolController.
     */
    public void stopExecutorService() {
        threadPoolController.shutdownAll();
    }

    /**
     * Gestisce il processo di autenticazione con un massimo di 3 tentativi.
     * In caso di successo, inizializza i controller specifici per il tipo di utente.
     * 
     * @return true se l'autenticazione ha successo, false altrimenti
     */
    private boolean autentica() {
        final int maxAttempts = 3;
        int attempt = 0;
        isAuth = false;
        while (attempt < maxAttempts && !isAuth) {
            attempt++;
            isAuth = authenticationController.autentica();
            if (!isAuth) {
                consoleIO.mostraMessaggio("Autenticazione fallita (" + attempt + "/" + maxAttempts + ").");
                if (attempt < maxAttempts) {
                    consoleIO.mostraMessaggio("Riprova.");
                }
            }
        }
        if (isAuth) {
            utenteCorrente = authenticationController.getUtenteCorrente();
            volontariController = new VolontariController(volontariManager, aggiuntaUtilita, consoleIO, volontarioCorrente, validatore, viewUtilita);
            configuratoriController = new ConfiguratoriController(aggiuntaUtilita, modificaUtilita, viewUtilita, volontariController, luoghiController, visiteController, visiteManager, volontariManager, luoghiManager);
        } else {
            utenteCorrente = null;
            consoleIO.mostraMessaggio("Numero massimo di tentativi superato. Accesso negato.");
        }
        return isAuth;
    }

    /**
     * Mostra il menu appropriato in base al tipo di utente autenticato.
     * Crea menu specifici per Configuratore, Volontario o Fruitore.
     */
    private void showMenu() {
        Menu menu = null;
        if (isAuth) {
            System.out.println("Buongiorno " + utenteCorrente.getNome() + "!");
            if (utenteCorrente instanceof Configuratore){
                ambitoTerritoriale.verificaAggiornaAmbitoTerritoriale();
                menu = menuFactory.creaMenuConfiguratore(configuratoriController);
            } else if (utenteCorrente instanceof Volontario){
                volontariController.volontarioCorrente = (Volontario) utenteCorrente;  
                menu = menuFactory.creaMenuVolontario(volontariController);
            }   else if (utenteCorrente instanceof Fruitore){
                fruitoreController.fruitoreCorrente = (Fruitore) utenteCorrente;
                menu = menuFactory.creaMenuFruitore(fruitoreController);
            } else {
                consoleIO.mostraMessaggio("Errore: tipo di utente non riconosciuto.");
            }
        } else {
            consoleIO.mostraMessaggio("Accesso negato. Effettua prima l'autenticazione.");
        }
        if (menu != null) {
            menu.mostraMenu();
        } else {
            consoleIO.mostraMessaggio("Errore nella creazione del menu.");
        }
    }


}
