package media.suspilne.classic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import static com.google.android.exoplayer2.ExoPlayerFactory.newSimpleInstance;

public class PlayerService extends Service {
    private ExoPlayer player;
    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        registerReceiver();
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForeground(1, getNotification(-1, "", ""));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String author = intent.getStringExtra("author");
        String title = intent.getStringExtra("title");
        int icon = intent.getIntExtra("icon", 0);

        playStream(intent.getStringExtra("stream"), intent.getLongExtra("position", 0));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(SettingsHelper.application, SettingsHelper.application, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setSound(null, null);
            notificationChannel.setShowBadge(false);

            notificationManager.createNotificationChannel(notificationChannel);

            this.startForeground(1, getNotification(icon, author, title));
        } else{
            notificationManager.notify(1, getNotification(icon, author, title));
        }

        return START_NOT_STICKY;
    }

    private Notification getNotification(int icon, String author, String title){
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // open application
        Intent notificationIntent = new Intent(this, ActivityTracks.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openTracksIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        // open application

        // photo
        Bitmap authorPhoto = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), new Composer(icon).photo, 100, 100);
        authorPhoto = ImageHelper.getCircularDrawable(authorPhoto);
        // photo

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, SettingsHelper.application)
            .setSmallIcon(R.drawable.ic_track)
            .setContentTitle(author)
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLargeIcon(authorPhoto)
            .setUsesChronometer(true)
            .setSound(null)
            .setContentIntent(openTracksIntent);

        // playNext
        Intent playNextIntent = new Intent();
        playNextIntent.setAction(SettingsHelper.application + "next");
        playNextIntent.putExtra("code", "PlayNext");
        PendingIntent playNextPendingIntent = PendingIntent.getBroadcast(this, 0, playNextIntent, 0);
        notificationBuilder.addAction(R.drawable.exo_controls_next, getString(R.string.next), playNextPendingIntent);
        // playNext

        // stop
        Intent stopIntent = new Intent();
        stopIntent.setAction(SettingsHelper.application + "stop");
        stopIntent.putExtra("code", "StopPlay");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        notificationBuilder.addAction(R.drawable.exo_controls_pause, getString(R.string.stop), stopPendingIntent);
        // stop

        return notificationBuilder.build();
    }

    private void playStream(String stream, long position) {
        Uri uri = Uri.parse(stream);
        player = newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());

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

        releasePlayer();
        clearNotifications();
        unregisterReceiver();
    }

    private void releasePlayer(){
        while (player != null){
            player.release();
            player = null;
        }
    }

    private void clearNotifications(){
        if (notificationManager != null){
            notificationManager.cancelAll();
        }
    }

    private void sendMessage(String code){
        Intent intent = new Intent();
        intent.setAction(SettingsHelper.application);
        intent.putExtra("code", code);
        sendBroadcast(intent);
    }

    private void registerReceiver(){
        try{
            IntentFilter filter = new IntentFilter();

            filter.addAction(SettingsHelper.application);
            filter.addAction(SettingsHelper.application + "next");
            filter.addAction(SettingsHelper.application + "stop");

            this.registerReceiver(receiver, filter);
        }catch (Exception e){ /*nothing*/ }
    }

    private void unregisterReceiver(){
        try{
            this.unregisterReceiver(receiver);
        }catch (Exception e){ /*nothing*/ }
    }

    private void playTrack(TrackEntry track){
        if (track.id != -1){
            playStream(track.stream, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(SettingsHelper.application, SettingsHelper.application, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setSound(null, null);
                notificationChannel.setShowBadge(false);

                this.startForeground(1, getNotification(track.getAuthorId(), track.getAuthor(), track.getTitle()));
            } else{
                notificationManager.notify(1, getNotification(track.getAuthorId(), track.getAuthor(), track.getTitle()));
            }
        } else {
            releasePlayer();
            clearNotifications();
        }

        SettingsHelper.setInt("tracks.nowPlaying", track.id);
        SettingsHelper.setInt("tracks.lastPlaying", track.id);
        sendMessage("SetPlayBtnIcon");
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Tracks tracks = new Tracks();

            switch (intent.getStringExtra("code")){
                case "SourceIsNotAccessible":
                    stopService(new Intent(PlayerService.this, PlayerService.class));
                    break;

                case "MediaIsEnded":
                    releasePlayer();
                    playTrack(tracks.getNext());
                    break;

                case "PlayNext":
                    releasePlayer();
                    playTrack(tracks.getNext());
                    break;

                case "StopPlay":
                    tracks.setNowPlaying(-1);
                    tracks.setLastPlaying(-1);
                    stopService(new Intent(PlayerService.this, PlayerService.class));
                    sendMessage("SetPlayBtnIcon");
                    break;
            }
        }
    };
}