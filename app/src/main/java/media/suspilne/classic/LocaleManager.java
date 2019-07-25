package media.suspilne.classic;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleManager {
    public static void setLanguage(Context context, String language){
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale myLocale = new Locale(language);
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        Locale.setDefault(myLocale);
        configuration.setLocale(myLocale);
        resources.updateConfiguration(configuration, displayMetrics);
    }

    public static String getLanguage(){
        String systemLanguage = Locale.getDefault().getLanguage();
        Classic.logError("System Language: " + Locale.getDefault().getCountry() + "(" + systemLanguage + ")");

        switch (systemLanguage){
            case "uk": return "uk";
            case "en": return "en";
            case "de": return "de";

            default: return "en";
        }
    }
}
