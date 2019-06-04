package media.suspilne.classic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

public class SettingsHelper {
    public static String application = "media.suspilne.classic";

    public static String getString(String setting){
        return getString(setting, "");
    }

    public static String getString(String setting, String defaultValue){
        return MainActivity.getContext().getSharedPreferences(application,0).getString(setting, defaultValue);
    }

    public static void setString(String setting, String value){
        SharedPreferences.Editor editor = MainActivity.getContext().getSharedPreferences(application, 0).edit();
        editor.putString(setting, value);
        editor.commit();
    }

    public static ArrayList<String> getAllSettings(String setting){
        ArrayList<String> result = new ArrayList<>();

        Map<String, ?> allEntries = MainActivity.getContext().getSharedPreferences(application, 0).getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains(setting)) {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    public static boolean getBoolean(String setting){
        return getString(setting).toLowerCase().equals("true");
    }

    public static void setBoolean(String setting, boolean value){
        setString(setting, String.valueOf(value));
    }

    public static int getInt(String setting, int defaultValue){
        return Integer.parseInt(getString(setting, String.valueOf(defaultValue)));
    }

    public static int getInt(String setting){
        return Integer.parseInt(getString(setting, "0"));
    }

    public static void setInt(String setting, int value){
        setString(setting, String.valueOf(value));
    }

    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = MainActivity.getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(int px) {
        DisplayMetrics displayMetrics = MainActivity.getContext().getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static Boolean fileExists(String name){
        return MainActivity.getContext().getFileStreamPath(name).exists();
    }

    public static void saveImage(String name, Drawable drawable){
        try {
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();

            FileOutputStream outputStream;
            outputStream = MainActivity.getContext().openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Drawable getImage(String name){
        try {
            FileInputStream stream = MainActivity.getContext().openFileInput(name);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();

            return new BitmapDrawable(bitmap);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}