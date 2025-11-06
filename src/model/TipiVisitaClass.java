package src.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rappresenta un tipo di visita guidata.
 * Sostituisce l'ex enum per permettere l'aggiunta dinamica di nuovi tipi.
 * Include tipi predefiniti (STORICA, SCIENTIFICA, ENOGASTRONOMICA, LABBAMBINI)
 * e permette la registrazione di tipi personalizzati.
 *   
 */
public class TipiVisitaClass {
    /** Nome del tipo di visita */
    private final String nome;
    
    /** Descrizione dettagliata del tipo di visita */
    private final String descrizione;  

    /**
     * Costruttore per creare un nuovo tipo di visita.
     * 
     * @param nome il nome del tipo di visita
     * @param descrizione la descrizione del tipo di visita
     */
    public TipiVisitaClass(String nome, String descrizione) {
        this.nome = nome != null ? nome.trim() : "";
        this.descrizione = descrizione != null ? descrizione : "";
    }

    /** Tipo di visita predefinito: Visita storica */
    public static final TipiVisitaClass STORICA = new TipiVisitaClass("STORICA", "Un percorso guidato alla scoperta della storia e dei monumenti principali della città.");
    
    /** Tipo di visita predefinito: Visita scientifica */
    public static final TipiVisitaClass SCIENTIFICA = new TipiVisitaClass("SCIENTIFICA", "Un'esperienza educativa dedicata alle scienze e alle innovazioni tecnologiche.");
    
    /** Tipo di visita predefinito: Visita enogastronomica */
    public static final TipiVisitaClass ENOGASTRONOMICA = new TipiVisitaClass("ENOGASTRONOMICA", "Un viaggio tra i sapori tipici locali con degustazioni di prodotti tradizionali.");
    
    /** Tipo di visita predefinito: Laboratorio per bambini */
    public static final TipiVisitaClass LABBAMBINI = new TipiVisitaClass("LABBAMBINI", "Attività ludico-didattiche pensate per i più piccoli, con laboratori creativi e giochi.");

    /** Registro concorrente di tutti i tipi di visita (built-in e custom) */
    private static final ConcurrentHashMap<String, TipiVisitaClass> registry = new ConcurrentHashMap<>();

    static {
        // Registra i tipi predefiniti
        registry.put(normalizeKey(STORICA.getNome()), STORICA);
        registry.put(normalizeKey(SCIENTIFICA.getNome()), SCIENTIFICA);
        registry.put(normalizeKey(ENOGASTRONOMICA.getNome()), ENOGASTRONOMICA);
        registry.put(normalizeKey(LABBAMBINI.getNome()), LABBAMBINI);
    }

    /**
     * Normalizza una chiave per la ricerca case-insensitive.
     * 
     * @param nome il nome da normalizzare
     * @return il nome in minuscolo e senza spazi extra
     */
    private static String normalizeKey(String nome) {
        return nome == null ? "" : nome.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Restituisce il nome del tipo di visita.
     * 
     * @return il nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce la descrizione del tipo di visita.
     * 
     * @return la descrizione
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Restituisce una rappresentazione in formato stringa del tipo di visita.
     * 
     * @return una stringa con nome e descrizione
     */
    @Override
    public String toString() {
        return nome + (descrizione.isEmpty() ? "" : " - " + descrizione);
    }

    /**
     * Confronta questo tipo di visita con un altro oggetto.
     * Due tipi sono uguali se hanno lo stesso nome (case-insensitive).
     * 
     * @param o l'oggetto da confrontare
     * @return true se i tipi hanno lo stesso nome
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipiVisitaClass)) return false;
        TipiVisitaClass that = (TipiVisitaClass) o;
        return nome.equalsIgnoreCase(that.nome);
    }

    /**
     * Factory method: restituisce un'istanza built-in se il nome corrisponde (case-insensitive),
     * altrimenti crea una nuova istanza dinamica con descrizione vuota.
     * 
     * @param nome il nome del tipo di visita
     * @return un'istanza di TipiVisitaClass
     */
    public static TipiVisitaClass fromName(String nome) {
        if (nome == null) return null;
        String n = nome.trim();
        if (n.equalsIgnoreCase(STORICA.getNome())) return STORICA;
        if (n.equalsIgnoreCase(SCIENTIFICA.getNome())) return SCIENTIFICA;
        if (n.equalsIgnoreCase(ENOGASTRONOMICA.getNome())) return ENOGASTRONOMICA;
        if (n.equalsIgnoreCase(LABBAMBINI.getNome())) return LABBAMBINI;
        return new TipiVisitaClass(n, "");
    }

    /**
     * Crea una lista di tipi di visita da una stringa separata da virgole.
     * 
     * @param string la stringa con i nomi separati da virgole
     * @return una lista di TipiVisitaClass
     */
    public static List<TipiVisitaClass> fromString(String string) {
        String[] parts = string.split(",");
        return java.util.Arrays.stream(parts)
                .map(String::trim)
                .map(TipiVisitaClass::fromName)
                .toList();
    }

    /**
     * Metodo compatibile con enum per ottenere un tipo dal nome.
     * 
     * @param trim il nome del tipo
     * @return un'istanza di TipiVisitaClass
     */
    public static TipiVisitaClass valueOf(String trim) {
        return fromName(trim);
    }

    /**
     * Restituisce i nomi di tutti i tipi disponibili (built-in + custom registrati).
     * 
     * @return lista dei nomi dei tipi
     */
    public static List<String> getAllTypeNames() {
        List<String> out = new ArrayList<>();
        for (TipiVisitaClass v : registry.values()) out.add(v.getNome());
        return out;
    }

    /**
     * Espone la mappa concorrente per accesso avanzato.
     * Le modifiche dovrebbero essere effettuate attraverso i metodi dedicati.
     * 
     * @return la mappa dei tipi di visita
     */
    public static ConcurrentHashMap<String, TipiVisitaClass> getTipiVisitaClassMap() {
        return registry;
    }

    /**
     * Registra un nuovo tipo personalizzato.
     * Non sovrascrive i tipi esistenti.
     * 
     * @param nome il nome del nuovo tipo
     * @param descrizione la descrizione del nuovo tipo
     * @return true se il tipo è stato inserito, false se già presente o nome non valido
     */
    public static boolean registerCustomType(String nome, String descrizione) {
        if (nome == null || nome.trim().isEmpty()) return false;
        String key = normalizeKey(nome);
        TipiVisitaClass toInsert = new TipiVisitaClass(nome.trim(), descrizione == null ? "" : descrizione);
        return registry.putIfAbsent(key, toInsert) == null;
    }

    /**
     * Rimuove un tipo personalizzato.
     * Non permette la rimozione dei tipi built-in.
     * 
     * @param nome il nome del tipo da rimuovere
     * @return true se rimosso, false se non presente o built-in (non rimovibile)
     */
    public static boolean removeCustomType(String nome) {
        if (nome == null || nome.trim().isEmpty()) return false;
        String key = normalizeKey(nome);
        // Non permettere rimozione dei built-in
        if (isBuiltInKey(key)) return false;
        return registry.remove(key) != null;
    }

    /**
     * Verifica se una chiave corrisponde a un tipo built-in.
     * 
     * @param key la chiave normalizzata
     * @return true se è un tipo predefinito
     */
    private static boolean isBuiltInKey(String key) {
        return key.equals(normalizeKey(STORICA.getNome()))
                || key.equals(normalizeKey(SCIENTIFICA.getNome()))
                || key.equals(normalizeKey(ENOGASTRONOMICA.getNome()))
                || key.equals(normalizeKey(LABBAMBINI.getNome()));
    }

    /**
     * Restituisce tutti i tipi di visita disponibili come lista.
     * 
     * @return una lista di tutti i tipi di visita
     */
    public static List<TipiVisitaClass> values() {
        return new ArrayList<>(registry.values());
    }
        
}
