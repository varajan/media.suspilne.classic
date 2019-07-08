package media.suspilne.classic;

import android.app.NotificationManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import com.google.android.gms.common.util.IOUtils;
import java.io.InputStream;
import java.net.URL;

import static android.content.Context.NOTIFICATION_SERVICE;

public class DownloadTask extends AsyncTask<TrackEntry, String, String> {
    private NotificationManager notificationManager;

    private Drawable image = ContextCompat.getDrawable(ActivityMain.getActivity(), R.mipmap.icon_classic);
    private int notificationId = 2;
    private int count;
    private int current;

    private void showProgressNotification(String text){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityMain.getActivity(), SettingsHelper.application)
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle(ActivityMain.getActivity().getString(R.string.downloading))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(ImageHelper.getBitmap(image))
                .setProgress(count, current, false)
                .setSound(null);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void showCompletedNotification(){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityMain.getActivity(), SettingsHelper.application)
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle(ActivityMain.getActivity().getString(R.string.download_completed, count))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(ImageHelper.getBitmap(image))
                .setSound(null);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void showFailedNotification(String text){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ActivityMain.getActivity(), SettingsHelper.application)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle(ActivityMain.getActivity().getString(R.string.an_error_occurred))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLargeIcon(ImageHelper.getBitmap(image))
            .setSound(null);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    protected void onPreExecute() {
        notificationManager = (NotificationManager) ActivityMain.getActivity().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        showProgressNotification(values.length > 0 ? values[0] : "");
    }

    @Override
    protected void onPostExecute(String result) {
        notificationManager.cancel(2);

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
