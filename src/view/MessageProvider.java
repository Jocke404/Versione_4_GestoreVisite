package src.view;

import java.util.Locale;
import java.util.ResourceBundle;

public class MessageProvider {

    private static ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());

     
    public static void setLocale(Locale locale) {
        messages = ResourceBundle.getBundle("messages", locale);
    }

     
    public static String getMessage(String key) {
        return messages.getString(key);
    }
}