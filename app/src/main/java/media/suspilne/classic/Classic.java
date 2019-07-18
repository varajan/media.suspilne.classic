package media.suspilne.classic;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class Classic extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences.Editor editor = getSharedPreferences(SettingsHelper.application, 0).edit();
        editor.putString("askedToContinueDownload", String.valueOf(false));
        editor.putString("tracks.paused", String.valueOf(false));
        editor.putString("tracks.lastPlaying", String.valueOf(-1));
        editor.putString("tracks.nowPlaying", String.valueOf(-1));
        editor.putString("tracksFilter", "");
        editor.putString("errorMessage", "");
        editor.apply();
    }

    public static void logError(String message, boolean logStackTrace){
        if (logStackTrace){
            logStackTrace(message);
        } else {
            Log.e(SettingsHelper.application, message);
        }
    }

    public static void logError(String message){
        logError(message, true);
    }

    private static void logStackTrace(String message){
        String stackTrace = message + "\r\n";

        int line = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            if (line > 7 && ste.toString().contains(SettingsHelper.application)){
                stackTrace += ste + "\r\n";
            }

            line++;
        }

        Log.e(SettingsHelper.application, stackTrace);
    }
}
