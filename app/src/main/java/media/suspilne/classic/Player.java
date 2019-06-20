package media.suspilne.classic;

import android.app.Service;
import android.content.Context;
import java.util.ArrayList;
import javax.net.ssl.SSLContext;

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
import com.google.android.gms.security.ProviderInstaller;

public class Player extends Service {
    private ExoPlayer player;
    private Context context;

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

    public Player(){
        this.context = getBaseContext();
    }

    public Player(Context context){
        this.context = context;
    }

    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    public void setPosition(long position){
        player.seekTo(position);
    }

    public long position(){
        return isPlaying() ? player.getCurrentPosition() : 0;
    }

    public Boolean isPlaying(){
        return player != null;
    }

    public void initializePlayer(String stream) {
        Uri uri = Uri.parse(stream);

        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(context),
                new DefaultTrackSelector(), new DefaultLoadControl());

        MediaSource mediaSource = new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(ActivityMain.getActivity(),"exoplayer-codelab"))
                .createMediaSource(uri);
        player.prepare(mediaSource, true, false);
        player.setPlayWhenReady(true);

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

    public void UpdateSslProvider(){
        try {
            ProviderInstaller.installIfNeeded(context.getApplicationContext());

            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    interface SourceIsNotAccessibleListener {
        void sourceIsNotAccessible();
    }

    interface MediaIsEndedListener {
        void mediaIsEnded();
    }
}
