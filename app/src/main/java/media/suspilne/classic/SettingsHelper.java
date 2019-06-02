package media.suspilne.classic;

import android.app.Activity;
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

    public static String getString(Activity activity, String setting){
        return getString(activity, setting, "");
    }

    public static String getString(Activity activity, String setting, String defaultValue){
        return activity.getSharedPreferences(application,0).getString(setting, defaultValue);
    }

    public static void setString(Activity activity, String setting, String value){
        SharedPreferences.Editor editor = activity.getSharedPreferences(application, 0).edit();
        editor.putString(setting, value);
        editor.commit();
    }

    public static ArrayList<Integer> getSavedTaleIds(Activity activity){
        ArrayList<Integer> readers = new ArrayList<>();
        ArrayList<Integer> titles  = new ArrayList<>();

        for (String reader:getAllSettings(activity, "reader-")){
            readers.add(Integer.parseInt(reader.split("-")[1]));
        }

        for (String title:getAllSettings(activity, "title-")){
            titles.add(Integer.parseInt(title.split("-")[1]));
        }

        return ListHelper.intersect(readers, titles);
    }

    public static ArrayList<String> getAllSettings(Activity activity, String setting){
        ArrayList<String> result = new ArrayList<>();

        Map<String, ?> allEntries = activity.getSharedPreferences(application, 0).getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains(setting)) {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    public static boolean getBoolean(Activity activity, String setting){
        return getString(activity, setting).toLowerCase().equals("true");
    }

    public static void setBoolean(Activity activity, String setting, boolean value){
        setString(activity, setting, String.valueOf(value));
    }

    public static int getInt(Activity activity, String setting, int defaultValue){
        return Integer.parseInt(getString(activity, setting, String.valueOf(defaultValue)));
    }

    public static int getInt(Activity activity, String setting){
        return Integer.parseInt(getString(activity, setting, "0"));
    }

    public static void setInt(Activity activity, String setting, int value){
        setString(activity, setting, String.valueOf(value));
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static Boolean fileExists(Context context, String name){
        return context.getFileStreamPath(name).exists();
    }

    public static void saveImage(Context context, String name, Drawable drawable){
        try {
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();

            FileOutputStream outputStream;
            outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Drawable getImage(Context context, String name){
        try {
            FileInputStream stream = context.openFileInput(name);
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