package media.suspilne.classic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TracksActivity extends MainActivity {
    private Tracks tracks = new Tracks();

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player.isPlaying())
            player.releasePlayer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("nowPlaying", player.isPlaying() ? tracks.nowPlaying : -1);
        outState.putInt("lastPlaying", tracks.lastPlaying);
        outState.putLong("position", player.isPlaying() ? player.position() : tracks.position);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        tracks.nowPlaying = savedInstanceState.getInt("nowPlaying");
        tracks.lastPlaying = savedInstanceState.getInt("lastPlaying");
        tracks.position = savedInstanceState.getLong("position");

        if (tracks.nowPlaying > 0){
            playTrack(tracks.items.get(tracks.nowPlaying-1));
            TracksActivity.this.setQuiteTimeout();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_tracks);
        currentView = R.id.tracks_menu;
        super.onCreate(savedInstanceState);

        if (!isNetworkAvailable()){
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }

        LinearLayout list = findViewById(R.id.list);
        for (final TrackEntry track:tracks.items) {
            View trackView = LayoutInflater.from(TracksActivity.this).inflate(R.layout.track_item, list, false);
            trackView.setTag(track.id);
            list.addView(trackView);
            track.setViewDetails(this, trackView);

            final ImageView playBtn = trackView.findViewById(R.id.play);
            playBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (player.isPlaying() && playBtn.getTag().equals(R.mipmap.track_pause)){
                        tracks.position = player.position();
                        tracks.lastPlaying = track.id;

                        player.releasePlayer();
                        playBtn.setImageResource(R.mipmap.track_play);
                        playBtn.setTag(R.mipmap.track_play);
                    }else{
                        playTrack(track);
                        TracksActivity.this.setQuiteTimeout();
                    }
                }
            });
        }
        // -- add items

        player.addListener(new Player.MediaIsEndedListener(){
            @Override
            public void mediaIsEnded(){
                if (SettingsHelper.getBoolean(TracksActivity.this, "tracksPlayNext")){
                    playTrack(tracks.next());
                }else{
                    tracks.nowPlaying = -1;
                    setPlayBtnIcon(new TrackEntry());
                }
            }
        });

        player.addListener(new Player.SourceIsNotAccessibleListener(){
            @Override
            public void sourceIsNotAccessible(){
                tracks.nowPlaying = -1;
                setPlayBtnIcon(new TrackEntry());
                player.releasePlayer();

                Toast.makeText(TracksActivity.this, R.string.no_internet, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void playTrack(TrackEntry track){
        player.releasePlayer();
        player.initializePlayer(track.stream());
        if (track.id == tracks.lastPlaying){
            player.setPosition(tracks.position);
        }
        setPlayBtnIcon(track);
        tracks.nowPlaying = track.id;
        tracks.lastPlaying = track.id;
    }

    private void setPlayBtnIcon(TrackEntry track){
        LinearLayout list = findViewById(R.id.list);

        for (TrackEntry item:tracks.items){
            ImageView btn = list.findViewWithTag(item.id).findViewById(R.id.play);
            btn.setImageResource(item.id == track.id ? R.mipmap.track_pause : R.mipmap.track_play);
            btn.setTag(item.id == track.id ? R.mipmap.track_pause : R.mipmap.track_play);
        }
    }
}