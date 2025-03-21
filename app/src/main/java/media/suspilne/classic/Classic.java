package media.suspilne.classic;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class Classic extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = getSharedPreferences(SettingsHelper.application, 0);
        SharedPreferences.Editor editor = getSharedPreferences(SettingsHelper.application, 0).edit();

        editor.putString("sortAsc", sharedPreferences.getString("sortAsc", "true"));
        editor.putString("tracks.paused", String.valueOf(false));
        editor.putString("readSettingsFromGit", String.valueOf(true));
        editor.putString("useGitTracks", String.valueOf(true));
        editor.putString("tracks.lastPlaying", String.valueOf(-1));
        editor.putString("tracks.nowPlaying", String.valueOf(-1));
        editor.putString("tracksFilter", "");
        editor.putString("errorMessage", "");
        editor.putString("tracksList", "");
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
        StringBuilder stackTrace = new StringBuilder(message + "\r\n");

        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            stackTrace.append(ste).append("\r\n");
        }

        Log.e(SettingsHelper.application, stackTrace.toString());
    }
}
