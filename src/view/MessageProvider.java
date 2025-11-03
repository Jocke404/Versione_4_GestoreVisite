package src.view;

import java.util.Locale;
import java.util.ResourceBundle;

public class MessageProvider {

    private static ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());

    // Metodo per cambiare il Locale
    public static void setLocale(Locale locale) {
        messages = ResourceBundle.getBundle("messages", locale);
    }

    // Metodo per ottenere un messaggio
    public static String getMessage(String key) {
        return messages.getString(key);
    }
}