package media.suspilne.classic;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import static android.app.Notification.VISIBILITY_PUBLIC;

public class PlayerService extends Service {
    private ExoPlayer player;
    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        playStream(intent.getStringExtra("stream"), intent.getLongExtra("position", 0));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showNotification(intent.getIntExtra("icon", 0), intent.getStringExtra("author"), intent.getStringExtra("title"));
        }

        return START_NOT_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification(int icon, String author, String title){
        String CHANNEL_ID = "classic";
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, SettingsHelper.application, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setSound(null, null);
        notificationChannel.setShowBadge(false);

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

        // open application
        Intent notificationIntent = new Intent(this, ActivityTracks.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openTracksIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // open application

        // photo
        Bitmap authorPhoto = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), new Composer(icon).photo, 100, 100);
        authorPhoto = ImageHelper.getCircularDrawable(authorPhoto);
        // photo

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_track)
                .setContentTitle(author)
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(VISIBILITY_PUBLIC)
                .setLargeIcon(authorPhoto)
                .setSound(null)
                .setUsesChronometer(true)
                .setContentIntent(openTracksIntent);

        // playNext
        Intent playNextIntent = new Intent();
        playNextIntent.setAction(SettingsHelper.application + "next");
        playNextIntent.putExtra("code", "PlayNext");
        PendingIntent playNextPendingIntent = PendingIntent.getBroadcast(this, 0, playNextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(R.drawable.exo_controls_next, getString(R.string.next), playNextPendingIntent);
        // playNext

        // stop
        Intent stopIntent = new Intent();
        stopIntent.setAction(SettingsHelper.application + "stop");
        stopIntent.putExtra("code", "StopPlay");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(R.drawable.exo_controls_pause, getString(R.string.stop), stopPendingIntent);
        // stop

        startForeground(2107, notificationBuilder.build());
    }

    private void playStream(String stream, long position) {
        Uri uri = Uri.parse(stream);
        player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());

        MediaSource mediaSource = new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(this,"exoplayer-codelab"))
                .createMediaSource(uri);
        player.prepare(mediaSource, true, false);
        player.setPlayWhenReady(true);
        player.seekTo(position);

        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {}

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}

            @Override
            public void onLoadingChanged(boolean isLoading) {}

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch(playbackState) {
                    case ExoPlayer.DISCONTINUITY_REASON_SEEK:
                        sendMessage("SourceIsNotAccessible");
                        break;

                    case ExoPlayer.DISCONTINUITY_REASON_INTERNAL:
                        sendMessage("MediaIsEnded");
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {}

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {}

            @Override
            public void onPlayerError(ExoPlaybackException error) { }

            @Override
            public void onPositionDiscontinuity(int reason) {}

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}

            @Override
            public void onSeekProcessed() {}
        });
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            SettingsHelper.setLong("PlayerPosition", player.getCurrentPosition());
        }

        while (player != null){
            player.release();
            player = null;
        }

        notificationManager.cancelAll();
    }

    private void sendMessage(String code){
        Intent intent = new Intent();
        intent.setAction(SettingsHelper.application);
        intent.putExtra("code", code);
//        intent.putExtra("duration", player.getDuration());
//        intent.putExtra("position", player.getCurrentPosition());
        sendBroadcast(intent);
    }
}