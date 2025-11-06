package src.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller singleton per la gestione centralizzata dei thread pool.
 * Fornisce metodi per creare thread pool e garantisce la corretta
 * terminazione di tutti i pool alla chiusura dell'applicazione.
 * 
 *  
 *  
 */
public class ThreadPoolController {

    /** Istanza singleton del controller */
    private static ThreadPoolController instance;  
    
    /** Lista di tutti i thread pool creati per gestione centralizzata */
    private static final List<ExecutorService> threadPools = new ArrayList<>();

    /**
     * Costruttore privato per pattern singleton.
     */
    private ThreadPoolController() {}

    /**
     * Crea un nuovo thread pool con un numero fisso di thread.
     * Il pool viene registrato per la gestione centralizzata.
     * 
     * @param poolSize il numero di thread nel pool
     * @return l'ExecutorService creato
     */
    public ExecutorService createThreadPool(int poolSize) {
        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
        threadPools.add(executorService);
        return executorService;
    }

    /**
     * Crea un nuovo executor con un singolo thread.
     * L'executor viene registrato per la gestione centralizzata.
     * 
     * @return l'ExecutorService a singolo thread creato
     */
    public ExecutorService createSingleThreadExecutor() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        threadPools.add(executorService);
        return executorService;
    }

    /**
     * Arresta tutti i thread pool creati e li rimuove dalla lista.
     * Questo metodo dovrebbe essere chiamato alla chiusura dell'applicazione.
     */
    public void shutdownAll() {
        for (ExecutorService executorService : threadPools) {
            executorService.shutdown();
        }
        threadPools.clear();
    }
    
    /**
     * Restituisce l'istanza singleton del controller.
     * Crea l'istanza al primo accesso (lazy initialization).
     * 
     * @return l'istanza singleton di ThreadPoolController
     */
    public static synchronized ThreadPoolController getInstance() {
        if (instance == null) {
            instance = new ThreadPoolController();
        }
        return instance;
    }
}
