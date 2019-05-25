package media.suspilne.classic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TracksActivity extends MainActivity {
    private List<TrackEntry> tracks = new ArrayList<>(Arrays.asList(
        new TrackEntry(1, R.string.track_001, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
        new TrackEntry(2, R.string.track_002, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
        new TrackEntry(3, R.string.track_003, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
        new TrackEntry(4, R.string.track_004, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
        new TrackEntry(5, R.string.track_005, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
        new TrackEntry(6, R.string.track_006, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
        new TrackEntry(7, R.string.track_007, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
        new TrackEntry(8, R.string.track_008, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
        new TrackEntry(9, R.string.track_009, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
        new TrackEntry(10, R.string.track_010, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
        new TrackEntry(11, R.string.track_011, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
        new TrackEntry(12, R.string.track_012, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
        new TrackEntry(13, R.string.track_013, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
        new TrackEntry(14, R.string.track_014, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
        new TrackEntry(15, R.string.track_015, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
        new TrackEntry(16, R.string.track_016, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
        new TrackEntry(17, R.string.track_017, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
        new TrackEntry(18, R.string.track_018, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
        new TrackEntry(19, R.string.track_019, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
        new TrackEntry(20, R.string.track_020, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
        new TrackEntry(21, R.string.track_021, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
        new TrackEntry(22, R.string.track_022, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
        new TrackEntry(23, R.string.track_023, R.string.medelson, R.mipmap.mendelson),
        new TrackEntry(24, R.string.track_024, R.string.carl_orff, R.mipmap.carl_orff),
        new TrackEntry(25, R.string.track_025, R.string.sebastian_bach, R.mipmap.bach)
            ));

    int nowPlaying;
    int lastPlaying;
    long position;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player.isPlaying())
            player.releasePlayer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("nowPlaying", player.isPlaying() ? nowPlaying : -1);
        outState.putInt("lastPlaying", lastPlaying);
        outState.putLong("position", player.isPlaying() ? player.position() : position);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        nowPlaying = savedInstanceState.getInt("nowPlaying");
        lastPlaying = savedInstanceState.getInt("lastPlaying");
        position = savedInstanceState.getLong("position");
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
        for (final TrackEntry track:tracks) {
            View item = LayoutInflater.from(TracksActivity.this).inflate(R.layout.track_item, list, false);
            item.setTag(track.id);
            list.addView(item);

            // -- set titles/photos
            ((ImageView)item.findViewById(R.id.photo)).setImageResource(track.authorPhotoId);
            ((TextView) item.findViewById(R.id.title)).setText(track.titleId);
            ((TextView) item.findViewById(R.id.author)).setText(track.authorNameId);
            // -- set titles/photos

            final ImageView playBtn = item.findViewById(R.id.play);
            playBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (player.isPlaying() && playBtn.getTag().equals(R.mipmap.track_pause)){
                        position = player.position();
                        lastPlaying = track.id;

                        player.releasePlayer();
                        playBtn.setImageResource(R.mipmap.track_play);
                        playBtn.setTag(R.mipmap.track_play);
                    }else{
                        playTrack(track);
                        TracksActivity.this.setQuiteTimeout();
                    }
                }
            });

            if (nowPlaying == track.id){
                playBtn.setImageResource(R.mipmap.track_pause);
                playBtn.setTag(R.mipmap.track_pause);

                player.initializePlayer(track.stream(nowPlaying));
                player.setPosition(position);

                TracksActivity.this.setQuiteTimeout();
            }
        }
        // -- add items

        player.addListener(new Player.MediaIsEndedListener(){
            @Override
            public void mediaIsEnded(){
                if (SettingsHelper.getBoolean(TracksActivity.this, "TracksPlayNext")){
//                    int next = ids.get(0);
//
//                    for(int i:ids){
//                        if (i > nowPlaying) {
//                            next = i;
//                            break;
//                        }
//                    }
//
//                    playTrack(next);
                }else{
                    nowPlaying = -1;
                    setPlayBtnIcon(new TrackEntry());
                }
            }
        });

        player.addListener(new Player.SourceIsNotAccessibleListener(){
            @Override
            public void sourceIsNotAccessible(){
                nowPlaying = -1;
                setPlayBtnIcon(new TrackEntry());
                player.releasePlayer();

                Toast.makeText(TracksActivity.this, R.string.no_internet, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void playTrack(TrackEntry track){
        player.releasePlayer();
        player.initializePlayer(track.stream());
        if (track.id == lastPlaying){
            player.setPosition(position);
        }
        setPlayBtnIcon(track);
        nowPlaying = track.id;
        lastPlaying = track.id;
    }

    private void setPlayBtnIcon(TrackEntry track){
        LinearLayout list = findViewById(R.id.list);

        for (TrackEntry item:tracks){
            ImageView btn = list.findViewWithTag(item.id).findViewById(R.id.play);
            btn.setImageResource(item.id == track.id ? R.mipmap.track_pause : R.mipmap.track_play);
            btn.setTag(item.id == track.id ? R.mipmap.track_pause : R.mipmap.track_play);
        }
    }
}