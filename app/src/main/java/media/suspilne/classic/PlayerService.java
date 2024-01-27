package media.suspilne.classic;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class PlayerService extends IntentService {
    private ExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;

    public static String NOTIFICATION_CHANNEL = SettingsHelper.application;
    public static int NOTIFICATION_ID = 21;

    public PlayerService() {
        super(NOTIFICATION_CHANNEL);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // not implemented
    }

    @Override
    public void onCreate(){
        registerReceiver();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(SettingsHelper.application);

            if (channel == null){
                NotificationChannel notificationChannel = new NotificationChannel(SettingsHelper.application, SettingsHelper.application, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setSound(null, null);
                notificationChannel.setShowBadge(false);

                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        int trackId = intent != null ? intent.getIntExtra("track.id", -1) : -1;
        TrackEntry track = new Tracks().getById(trackId);
        playTrack(track);

        return START_NOT_STICKY;
    }

    private void playStream(String stream, long position) {
        releasePlayer();

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(stream));
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();

        player = new ExoPlayer.Builder(ActivityMain.getActivity()).build();
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setAudioAttributes(audioAttributes, true);
        player.setPlayWhenReady(true);
        player.seekTo(position);

        PlayerNotificationManager.NotificationListener listener = new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(notificationId, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
                } else {
                    startForeground(notificationId, notification);
                }
            }

            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                stopSelf();
            }
        };

        playerNotificationManager = new PlayerNotificationManager
                .Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL)
                .setNotificationListener(listener)
                .setMediaDescriptionAdapter(new PlayerAdapter(this))
                .build();

        playerNotificationManager.setUseStopAction(true);

        playerNotificationManager.setUseFastForwardAction(true);
        playerNotificationManager.setUseRewindAction     (true);
        playerNotificationManager.setUseNextAction       (false);
        playerNotificationManager.setUsePreviousAction   (false);

        playerNotificationManager.setUseFastForwardActionInCompactView(true);
        playerNotificationManager.setUseRewindActionInCompactView     (true);
        playerNotificationManager.setUseNextActionInCompactView       (false);
        playerNotificationManager.setUsePreviousActionInCompactView   (false);

        playerNotificationManager.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                stopSelf();
                sendMessage("SourceIsNotAccessible");
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                Tracks.setPause(!playWhenReady);
                sendMessage("SetPlayBtnIcon");
            }

            @Override
            public void onPlaybackStateChanged(@Player.State int playbackState) {
                sendMessage("SetPlayBtnIcon");

                if (playbackState == ExoPlayer.STATE_IDLE) {
                    Tracks.setNowPlaying(-1);
                }

                if (playbackState == ExoPlayer.STATE_ENDED) {
                    Tracks.setLastPosition(0);
                    playTrack(new Tracks().getNext());
                }
            }

            @Override
            public void onPositionDiscontinuity(
                    @NonNull Player.PositionInfo oldPosition,
                    @NonNull Player.PositionInfo newPosition,
                    int reason) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    if (oldPosition.positionMs > newPosition.positionMs) {
                        playTrack(new Tracks().getPrevious());
                    } else {
                        playTrack(new Tracks().getNext());
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            Tracks.setLastPosition(player.getCurrentPosition());
        }

        playerNotificationManager.setPlayer(null);
        releasePlayer();
        unregisterReceiver();
    }

    private void releasePlayer(){
        while (player != null){
            player.release();
            player = null;
        }
    }

    private void sendMessage(String code){
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_CHANNEL);
        intent.putExtra("code", code);
        sendBroadcast(intent);
    }

    private void playTrack(TrackEntry track){
        if (track.id != -1 && !SettingsHelper.getBoolean(("stopPlaybackOnTimeout"))){
            long position = track.id == Tracks.getLastPlaying() ? Tracks.getLastPosition() : 0;

            SettingsHelper.setInt("tracks.nowPlaying", track.id);
            SettingsHelper.setInt("tracks.lastPlaying", track.id);

            playStream(track.stream, position);
        } else {
            SettingsHelper.setInt("tracks.nowPlaying", -1);

            playerNotificationManager.setPlayer(null);
            releasePlayer();
        }

        sendMessage("SetPlayBtnIcon");
    }

    private void registerReceiver(){
        try{
            IntentFilter filter = new IntentFilter();
            filter.addAction(NOTIFICATION_CHANNEL);
            this.registerReceiver(receiver, filter);
        }catch (Exception e){ /*nothing*/ }
    }

    private void unregisterReceiver(){
        try{
            this.unregisterReceiver(receiver);
        }catch (Exception e){ /*nothing*/ }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("StopPlay".equals(intent.getStringExtra("code"))) {
                Tracks.setNowPlaying(-1);
                Tracks.setLastPosition(player.getCurrentPosition());
                stopSelf();
                sendMessage("SetPlayBtnIcon");
            }
        }
    };
}