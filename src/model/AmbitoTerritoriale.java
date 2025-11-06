package src.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.model.db.ApplicationSettingsDAO;
import src.view.ConsoleIO;
import lib.InputDati;

/**
 * Gestisce l'ambito territoriale del sistema di gestione visite.
 * Questa classe si occupa della configurazione, salvataggio e caricamento
 * dell'ambito territoriale che definisce i comuni in cui il sistema opera.
 * 
 * L'ambito territoriale può essere salvato sia nel database tramite ApplicationSettingsDAO
 * che in un file di configurazione come fallback.
 * 
 *  
 *  
 */
public class AmbitoTerritoriale {

    /** Percorso del file di configurazione per l'ambito territoriale */
    private static final String AMBITO_FILE = "src/utility/ambito_territoriale.config";
    
    /** Set dei comuni che compongono l'ambito territoriale */
    private Set<String> ambitoTerritoriale = new HashSet<>();
    
    /** Interfaccia per l'input/output con la console */
    private final ConsoleIO consoleIO = new ConsoleIO();

    /**
     * Verifica se l'ambito territoriale è già configurato e, in caso negativo,
     * avvia la procedura di configurazione e salvataggio.
     * Se è già configurato, carica i dati esistenti.
     */
    public void verificaAggiornaAmbitoTerritoriale() {
        if (!isAmbitoConfigurato()) {
            scegliAmbitoTerritoriale();
            salvaAmbitoTerritoriale();
        } else {
            caricaAmbitoTerritoriale();
        }
    }

    /**
     * Verifica se l'ambito territoriale è già stato configurato.
     * Controlla prima nel database tramite ApplicationSettingsDAO,
     * poi verifica l'esistenza del file di configurazione.
     * 
     * @return true se l'ambito territoriale è configurato, false altrimenti
     */
    public boolean isAmbitoConfigurato() {
        try {
            if (ApplicationSettingsDAO.hasTerritorialScope()) return true;
        } catch (Throwable t) {}
         
        File file = new File(AMBITO_FILE);
        return file.exists();
    }

    /**
     * Avvia la procedura interattiva per la configurazione dell'ambito territoriale.
     * Permette all'utente di inserire uno o più comuni che definiranno l'ambito
     * territoriale del sistema. I dati vengono salvati automaticamente.
     */
    public void scegliAmbitoTerritoriale() {
        consoleIO.mostraMessaggio("Configurazione ambito territoriale (inserisci uno o più comuni).");
        ambitoTerritoriale.clear();
        do {
            String comune = InputDati.leggiStringaNonVuota("Inserisci il nome del comune: ");
            ambitoTerritoriale.add(comune);
        } while (InputDati.yesOrNo("Vuoi aggiungere un altro comune? (s/n): "));
        salvaAmbitoTerritoriale();
        consoleIO.mostraMessaggio("Ambito territoriale configurato: " + ambitoTerritoriale);
    }

    /**
     * Salva l'ambito territoriale configurato.
     * Tenta prima di salvare nel database tramite ApplicationSettingsDAO,
     * in caso di fallimento utilizza il file di configurazione come fallback.
     */
    private void salvaAmbitoTerritoriale() {
        List<String> lista = new ArrayList<>(ambitoTerritoriale);
        boolean ok = false;
        try {
            ok = ApplicationSettingsDAO.setTerritorialScope(lista);
        } catch (Throwable t) {
             
        }
        if (!ok) {
             
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(AMBITO_FILE))) {
                for (String comune : ambitoTerritoriale) {
                    writer.write(comune);
                    writer.newLine();
                }
                ok = true;
            } catch (IOException e) {
                consoleIO.mostraMessaggio("Errore nel salvataggio dell'ambito territoriale.");
            }
        }
        if (ok) consoleIO.mostraMessaggio("Ambito salvato.");
    }

    /**
     * Carica l'ambito territoriale dai dati salvati.
     * Tenta prima di caricare dal database tramite ApplicationSettingsDAO,
     * in caso di fallimento legge dal file di configurazione.
     * Se i dati vengono caricati dal file, tenta di sincronizzarli con il database.
     */
    public void caricaAmbitoTerritoriale() {
        ambitoTerritoriale.clear();
        List<String> fromDb = null;
        try {
            fromDb = ApplicationSettingsDAO.getTerritorialScope();
        } catch (Throwable t) {
            fromDb = null;
        }
        if (fromDb != null && !fromDb.isEmpty()) {
            ambitoTerritoriale.addAll(fromDb);
            return;
        }

         
        try (BufferedReader reader = new BufferedReader(new FileReader(AMBITO_FILE))) {
            String line;
            List<String> tmp = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    ambitoTerritoriale.add(trimmed);
                    tmp.add(trimmed);
                }
            }
            if (!tmp.isEmpty()) {
                try {
                    ApplicationSettingsDAO.setTerritorialScope(tmp);
                } catch (Throwable t) { /* ignore */ }
            }
        } catch (IOException e) {
            consoleIO.mostraMessaggio("Errore nel caricamento dell'ambito territoriale.");
        }
    }

    /**
     * Restituisce la lista dei comuni che compongono l'ambito territoriale.
     * Se l'ambito non è ancora stato caricato in memoria, lo carica automaticamente.
     * 
     * @return Lista dei comuni dell'ambito territoriale
     */
    public List<String> getAmbitoTerritoriale() {
        if (ambitoTerritoriale.isEmpty()) {
            caricaAmbitoTerritoriale();
        }
        return new ArrayList<>(ambitoTerritoriale);
    }

}