package media.suspilne.classic;

import android.app.Service;
import java.util.ArrayList;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

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

    private ArrayList<MediaIsEndedListener> mediaIsEndedListeners = new ArrayList<>();
    private ArrayList<SourceIsNotAccessibleListener> sourceIsNotAccessibleListeners = new ArrayList<>();

    public void addListener(MediaIsEndedListener listener) {
        if (!mediaIsEndedListeners.contains(listener)){
            mediaIsEndedListeners.add(listener);
        }
    }

    public void addListener(SourceIsNotAccessibleListener listener) {
        if (!sourceIsNotAccessibleListeners.contains(listener)){
            sourceIsNotAccessibleListeners.add(listener);
        }
    }

    public PlayerService(){}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        playStream(intent.getStringExtra("stream"), intent.getLongExtra("position", 0));

        return START_NOT_STICKY;
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
                        for (SourceIsNotAccessibleListener l : sourceIsNotAccessibleListeners)
                            l.sourceIsNotAccessible();
                        break;

                    case ExoPlayer.DISCONTINUITY_REASON_INTERNAL:
                        for (MediaIsEndedListener l : mediaIsEndedListeners)
                            l.mediaIsEnded();
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
    }

    interface SourceIsNotAccessibleListener {
        void sourceIsNotAccessible();
    }

    interface MediaIsEndedListener {
        void mediaIsEnded();
    }
}
