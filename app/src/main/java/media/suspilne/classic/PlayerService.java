package media.suspilne.classic;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

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

public class PlayerService extends Service {
    private ExoPlayer player;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.e("SERVICE:" + SettingsHelper.application,intent == null ? "Intent NULL" : "Intent is not null");

        playStream(intent.getStringExtra("stream"), intent.getLongExtra("position", 0));

        return START_STICKY;
    }

    private void playStream(String stream, long position) {
        Uri uri = Uri.parse(stream);
        player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());

        MediaSource mediaSource = new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(ActivityMain.getActivity(),"exoplayer-codelab"))
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
        Log.e("SERVICE:" + SettingsHelper.application,"SERVICE IS DESTROYED");

        if (player != null) {
            SettingsHelper.setLong("PlayerPosition", player.getCurrentPosition());
        }

        while (player != null){
            player.release();
            player = null;
        }
    }

    private void sendMessage(String code){
        Intent intent = new Intent();
        intent.setAction(SettingsHelper.application);
        intent.putExtra("code", code);
        intent.putExtra("duration", player.getDuration());
        intent.putExtra("position", player.getCurrentPosition());
        sendBroadcast(intent);
    }
}