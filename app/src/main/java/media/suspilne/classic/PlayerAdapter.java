package media.suspilne.classic;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class PlayerAdapter implements PlayerNotificationManager.MediaDescriptionAdapter{
    private Context context;

    public PlayerAdapter(Context context) {
        this.context = context;
    }

    private TrackEntry getTrack() {
        return new Tracks().getById(Tracks.getNowPlaying());
    }

    @Override
    public String getCurrentContentTitle(Player player) {
        try {
            return getTrack().getTitle();
        } catch (Exception ex) {
            return this.context.getString(R.string.title);
        }
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        try {
            return getTrack().getAuthor();
        } catch (Exception ex) {
            return this.context.getString(R.string.author);
        }
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        try{
            Composer composer = new Composer((getTrack().getAuthorId()));
            Bitmap authorPhoto = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), composer.photo, 100, 100);
            return ImageHelper.getCircularDrawable(authorPhoto);
        } catch (Exception ex) {
            return null;
        }
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        int flag = android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.R ? 0 : PendingIntent.FLAG_IMMUTABLE;

        Intent notificationIntent = new Intent(context, ActivityTracks.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );

        return PendingIntent.getActivity(context, 0, notificationIntent, flag);
    }
}
