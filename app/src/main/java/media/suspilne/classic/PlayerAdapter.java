package media.suspilne.classic;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class PlayerAdapter implements PlayerNotificationManager.MediaDescriptionAdapter{
    private Context context;

    public PlayerAdapter(Context context) {
        this.context = context;
    }

    private TrackEntry track(){
        return new Tracks().getById(Tracks.getNowPlaying());
    }

    @Override
    public String getCurrentContentTitle(Player player) {
        return track().getTitle();
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        return track().getAuthor();
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        if (!SettingsHelper.getBoolean("show_composer_photo")) return null;

        Composer composer = new Composer((track().getAuthorId()));
        Bitmap authorPhoto = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), composer.photo, 100, 100);
        authorPhoto = ImageHelper.getCircularDrawable(authorPhoto);

        return authorPhoto;
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        Intent notificationIntent = new Intent(context, ActivityTracks.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openTracksIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        return openTracksIntent;
    }
}
