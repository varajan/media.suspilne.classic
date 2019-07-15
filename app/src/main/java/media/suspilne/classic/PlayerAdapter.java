package media.suspilne.classic;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class PlayerAdapter implements PlayerNotificationManager.MediaDescriptionAdapter{
    @Override
    public String getCurrentContentTitle(Player player) {
        int window = player.getCurrentWindowIndex();
//        return getTitle(window);
        return "getCurrentContentTitle";
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        int window = player.getCurrentWindowIndex();
        return "getCurrentContentText";
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
//        int window = player.getCurrentWindowIndex();
//        Bitmap largeIcon = getLargeIcon(window);
//        if (largeIcon == null && getLargeIconUri(window) != null) {
//            // load bitmap async
//            loadBitmap(getLargeIconUri(window), callback);
//            return getPlaceholderBitmap();
//        }
//        return largeIcon;

        Bitmap authorPhoto = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), new Composer(5).photo, 100, 100);
        authorPhoto = ImageHelper.getCircularDrawable(authorPhoto);

        return authorPhoto;
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        int window = player.getCurrentWindowIndex();
//        return createPendingIntent(window);
        return null;

//        Intent notificationIntent = new Intent(this, ActivityTracks.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent openTracksIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//        return openTracksIntent;
    }
}
