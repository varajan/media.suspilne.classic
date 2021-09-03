package media.suspilne.classic;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.util.IOUtils;
import java.io.InputStream;
import java.net.URL;

import static android.content.Context.NOTIFICATION_SERVICE;

public class DownloadTask extends AsyncTask<TrackEntry, String, String> {
    private NotificationManager notificationManager;
    private PendingIntent openApplication;

    private Drawable image = ContextCompat.getDrawable(ActivityMain.getActivity(), R.mipmap.icon_classic);
    private int count;
    private int current;
    static int IN_PROGRESS = 22;
    static int COMPLETED = 23;
    static int WITH_ERROR = 24;

    public static void cancelAllNotifications(){
        NotificationManager notificationManager = (NotificationManager) ActivityMain.getActivity().getSystemService(NOTIFICATION_SERVICE);

        notificationManager.cancel(IN_PROGRESS);
        notificationManager.cancel(COMPLETED);
        notificationManager.cancel(WITH_ERROR);
    }

    private void showProgressNotification(String text){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityMain.getActivity(), SettingsHelper.application)
            .setSmallIcon(R.drawable.ic_cloud_download)
            .setContentTitle(ActivityMain.getActivity().getString(R.string.downloading))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLargeIcon(ImageHelper.getBitmap(image))
            .setProgress(count, current, false)
            .setContentIntent(openApplication)
            .setSound(null);

        notificationManager.notify(IN_PROGRESS, notificationBuilder.build());
    }

    private void showCompletedNotification(){
        if (count == 0){
            cancelAllNotifications();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityMain.getActivity(), SettingsHelper.application)
                    .setSmallIcon(R.drawable.ic_cloud_done)
                    .setContentTitle(ActivityMain.getActivity().getString(R.string.download_completed, count))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setLargeIcon(ImageHelper.getBitmap(image))
                    .setContentIntent(openApplication)
                    .setSound(null);

            notificationManager.notify(COMPLETED, notificationBuilder.build());
        }
    }

    private void showFailedNotification(String errorMessage){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityMain.getActivity(), SettingsHelper.application)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle(ActivityMain.getActivity().getString(R.string.an_error_occurred))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLargeIcon(ImageHelper.getBitmap(image))
            .setContentIntent(openApplication)
            .setSound(null);

        notificationManager.notify(WITH_ERROR, notificationBuilder.build());
        SettingsHelper.setString("errorMessage", errorMessage);
    }

    protected void onPreExecute() {
        notificationManager = (NotificationManager) ActivityMain.getActivity().getSystemService(NOTIFICATION_SERVICE);
        cancelAllNotifications();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(SettingsHelper.application, SettingsHelper.application, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setSound(null, null);
            notificationChannel.setShowBadge(false);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent notificationIntent = new Intent(ActivityMain.getActivity(), ActivityMain.getActivity().getClass());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openApplication = PendingIntent.getActivity(ActivityMain.getActivity(), 0, notificationIntent, 0);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        showProgressNotification(values.length > 0 ? values[0] : "");
    }

    @Override
    protected void onPostExecute(String result) {
        cancelAllNotifications();

        if (result.isEmpty()){
            showCompletedNotification();
        }else{
            showFailedNotification(result);
        }
    }

    @Override
    protected String doInBackground(TrackEntry... tracks) {
        try {
            this.count = tracks.length;
            this.current = 0;

            for (TrackEntry track:tracks) {
                if (track.isDownloaded) publishProgress();
            }

            for (TrackEntry track:tracks) {
                if (track.isDownloaded) continue;
                long freeSpace = SettingsHelper.freeSpace();
                long required = 100 * 1024 * 1024;

                if (freeSpace < required){
                    throw new Exception(ActivityMain.getActivity().getString(
                        R.string.not_enough_space, SettingsHelper.formattedSize(freeSpace), SettingsHelper.formattedSize(required)));
                }

                InputStream is = (InputStream) new URL(track.stream).getContent();
                SettingsHelper.saveFile(track.fileName, IOUtils.toByteArray(is));
                publishProgress(track.getAuthor() + ": " + track.getTitle());
                current++;
            }
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }

        return "";
    }
}