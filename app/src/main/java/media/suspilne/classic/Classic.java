package media.suspilne.classic;

import android.app.Application;
import android.content.SharedPreferences;

public class Classic  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences.Editor editor = getSharedPreferences(SettingsHelper.application, 0).edit();
        editor.putString("askedToContinueDownload", String.valueOf(false));
        editor.apply();
    }
}
