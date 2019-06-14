package media.suspilne.classic;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TracksActivity extends MainActivity {
    private Tracks tracks;
    private ImageView favoriteIcon;
    private ImageView searchIcon;
    private EditText searchField;
    private TextView toolbarTitle;

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
        continueTrack(savedInstanceState);
    }

    private void continueTrack(Bundle bundle){
        if (bundle == null) return;

        tracks.nowPlaying = bundle.getInt("nowPlaying");
        tracks.lastPlaying = bundle.getInt("lastPlaying");
        tracks.position = bundle.getLong("position");

        if (tracks.nowPlaying > 0){
            playTrack(tracks.getById(tracks.nowPlaying));
            TracksActivity.this.setQuiteTimeout();
        }
    }

    private void hideSearch(){
        toolbarTitle.setVisibility(View.VISIBLE);
        searchIcon.setVisibility(View.VISIBLE);
        favoriteIcon.setVisibility(View.VISIBLE);
        searchField.setVisibility(View.GONE);

        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(searchField.getWindowToken(), 0);
    }

    private void addSearchField() {
        favoriteIcon = findViewById(R.id.showFavorite);
        searchIcon = findViewById(R.id.searchIcon);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        searchField = findViewById(R.id.searchField);

        searchIcon.setOnClickListener(v -> {
            toolbarTitle.setVisibility(View.GONE);
            searchIcon.setVisibility(View.GONE);
            favoriteIcon.setVisibility(View.GONE);
            searchField.setVisibility(View.VISIBLE);
            searchField.requestFocus();

            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        });

        favoriteIcon.setOnClickListener(v -> {
            tracks.showOnlyFavorite = !tracks.showOnlyFavorite;
            SettingsHelper.setBoolean("showOnlyFavorite", tracks.showOnlyFavorite);

            showTracks();
        });

        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                tracks.filter = v.getText().toString();

                hideSearch();
                showTracks();
                return true;
            }
            return false;
        });

        searchField.setOnTouchListener((view, event) -> {
            int actionX = (int) event.getX();
            int viewWidth = view.getWidth();
            int buttonWidth = SettingsHelper.dpToPx(30);

            if (viewWidth - buttonWidth <= actionX){
                searchField.setText("");
                tracks.filter = "";

                hideSearch();
                showTracks();
                return true;
            }

            return false;
        });
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if (searchField.getVisibility() == View.VISIBLE && (event.getAction() == KeyEvent.ACTION_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_BACK)){
            hideSearch();
            return false;
        }

        return super.onKeyDown(keycode, event);
    }

    private void showTracks(){
        favoriteIcon.setImageResource(tracks.showOnlyFavorite ? R.drawable.ic_favorite : R.drawable.ic_all);

        LinearLayout list = findViewById(R.id.list);
        list.removeViews(1, list.getChildCount()-1);

        View nothing = findViewById(R.id.nothingToShow);
        nothing.setVisibility(View.VISIBLE);

        for (final TrackEntry track:tracks.getTracks()) {
            nothing.setVisibility(View.GONE);

            View trackView = LayoutInflater.from(TracksActivity.this).inflate(R.layout.track_item, list, false);
            trackView.setTag(track.id);
            list.addView(trackView);
            track.setViewDetails();

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

            trackView.findViewById(R.id.favorite).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    track.resetFavorite();

                    if (tracks.showOnlyFavorite){

                        if (player.isPlaying() && tracks.tracksPlayNext && tracks.nowPlaying == track.id){
                            playTrack(tracks.getNext());
                        }

                        if (tracks.nowPlaying == track.id){
                            player.releasePlayer();
                        }

                        track.remove();

                        if (tracks.getTracks().size() == 0){
                            findViewById(R.id.nothingToShow).setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }

        TrackEntry current = tracks.getById(tracks.nowPlaying);
        if (current != null) {
            setPlayBtnIcon(current);
        } else {
            player.releasePlayer();
        }
    }

    private void setPlayerListeners(){
        player.addListener((Player.MediaIsEndedListener) () -> {
            if (tracks.tracksPlayNext){
                playTrack(tracks.getNext());
            }else{
                tracks.nowPlaying = -1;
                setPlayBtnIcon(new TrackEntry());
            }
        });

        player.addListener((Player.SourceIsNotAccessibleListener) () -> {
            tracks.nowPlaying = -1;
            setPlayBtnIcon(new TrackEntry());
            player.releasePlayer();

            Toast.makeText(TracksActivity.this, R.string.no_internet, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.tracks_menu;
        super.onCreate(savedInstanceState);

        tracks = new Tracks();

        addSearchField();
        showTracks();
        setPlayerListeners();
        continueTrack(savedInstanceState);
        askToContinueDownloadTracks();
    }

    private void playTrack(TrackEntry track){
        player.releasePlayer();
        player.initializePlayer(track.stream);
        if (track.id == tracks.lastPlaying){
            player.setPosition(tracks.position);
        }
        setPlayBtnIcon(track);
        tracks.nowPlaying = track.id;
        tracks.lastPlaying = track.id;
    }

    private void setPlayBtnIcon(TrackEntry track){
        LinearLayout list = findViewById(R.id.list);

        for (TrackEntry item:tracks.getTracks()){
            ImageView btn = list.findViewWithTag(item.id).findViewById(R.id.play);
            btn.setImageResource(item.id == track.id ? R.mipmap.track_pause : R.mipmap.track_play);
            btn.setTag(item.id == track.id ? R.mipmap.track_pause : R.mipmap.track_play);
        }
    }
}