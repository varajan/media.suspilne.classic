package media.suspilne.classic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActivityTracks extends ActivityMain {
    private Tracks tracks;
    private ImageView favoriteIcon;
    private ImageView searchIcon;
    private EditText searchField;
    private LinearLayout tracksList;
    private boolean returnToComposers = false;

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
        outState.putBoolean("returnToComposers", returnToComposers);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        continueTrack(savedInstanceState);
    }

    private void continueTrack(Bundle bundle){
        if (bundle == null) return;

        returnToComposers = bundle.getBoolean("returnToComposers");
        tracks.nowPlaying = bundle.getInt("nowPlaying");
        tracks.lastPlaying = bundle.getInt("lastPlaying");
        tracks.position = bundle.getLong("position");

        if (tracks.nowPlaying > 0){
            playTrack(tracks.getById(tracks.nowPlaying));
            ActivityTracks.this.setQuiteTimeout();
        }
    }

    private void hideSearch(){
        searchIcon.setVisibility(View.VISIBLE);
        favoriteIcon.setVisibility(View.VISIBLE);
        searchField.setVisibility(View.GONE);

        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(searchField.getWindowToken(), 0);
    }

    private View.OnClickListener search = v -> {
        searchIcon.setVisibility(View.GONE);
        favoriteIcon.setVisibility(View.GONE);
        searchField.setVisibility(View.VISIBLE);
        searchField.requestFocus();

        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    };

    private void addSearchField() {
        favoriteIcon = findViewById(R.id.showFavorite);
        searchIcon = findViewById(R.id.searchIcon);
        searchField = findViewById(R.id.searchField);

        findViewById(R.id.toolbar).setOnClickListener(search);
        searchIcon.setOnClickListener(search);

        favoriteIcon.setOnClickListener(v -> {
            tracks.showOnlyFavorite = !tracks.showOnlyFavorite;
            SettingsHelper.setBoolean("showOnlyFavorite", tracks.showOnlyFavorite);

            filterTracks();
        });

        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                tracks.filter = v.getText().toString();
                returnToComposers = false;

                hideSearch();
                filterTracks();
                return true;
            }
            return false;
        });

        searchField.setOnTouchListener((view, event) -> {
            int actionX = (int) event.getX();
            int viewWidth = view.getWidth();
            int buttonWidth = SettingsHelper.dpToPx(50);

            if (viewWidth - buttonWidth <= actionX){
                searchField.setText("");
                tracks.filter = "";
                returnToComposers = false;

                hideSearch();
                filterTracks();
                return true;
            }

            return false;
        });
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if (searchField.getVisibility() == View.VISIBLE && (event.getAction() == KeyEvent.ACTION_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_BACK)){
            tracks.filter = searchField.getText().toString();
            hideSearch();
            filterTracks();
            return false;
        }

        return super.onKeyDown(keycode, event);
    }

    private void filterTracks(){
        favoriteIcon.setImageResource(tracks.showOnlyFavorite ? R.drawable.ic_favorite : R.drawable.ic_all);
        getSupportActionBar().setTitle(tracks.filter.equals("") ? getString(R.string.tracks) : "\u2315 " + tracks.filter);
        View nothing = findViewById(R.id.nothingToShow);
        int visibility = View.VISIBLE;

        for (final TrackEntry track:tracks.getTracks()) {
            if (tracks.showOnlyFavorite && !track.favorite || !track.matchesFilter(tracks.filter)){
                track.hide();
            }else{
                track.show();
                visibility = View.GONE;
            }
        }

        nothing.setVisibility(visibility);

        TrackEntry currentTrack = tracks.getById(tracks.nowPlaying);
        if (player.isPlaying() && !currentTrack.isVisible()) {
            player.releasePlayer();

            if (tracks.tracksPlayNext) playTrack(tracks.getNext());
        }
    }

    private void showTracks(){
        for (final TrackEntry track:tracks.getTracks()) {
            View trackView = LayoutInflater.from(this).inflate(R.layout.track_item, tracksList, false);
            trackView.setTag(track.id);
            tracksList.addView(trackView);
            track.setViewDetails();

            final ImageView playBtn = trackView.findViewById(R.id.play);
            playBtn.setOnClickListener(v -> {
                if (player.isPlaying() && playBtn.getTag().equals(R.mipmap.track_pause)){
                    tracks.position = player.position();
                    tracks.lastPlaying = track.id;

                    player.releasePlayer();
                    playBtn.setImageResource(R.mipmap.track_play);
                    playBtn.setTag(R.mipmap.track_play);
                }else{
                    playTrack(track);
                    setQuiteTimeout();
                }
            });

            trackView.findViewById(R.id.favorite).setOnClickListener(v -> {
                track.resetFavorite();
                filterTracks();
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

            Toast.makeText(ActivityTracks.this, R.string.no_internet, Toast.LENGTH_LONG).show();
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (returnToComposers && !drawer.isDrawerOpen(GravityCompat.START)) {
            openActivity(ActivityComposers.class);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.tracks_menu;
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String filter = intent.getStringExtra("filter");
        boolean play = intent.getBooleanExtra("play", false);

        tracksList = findViewById(R.id.list);
        tracks = new Tracks();
        tracks.filter = filter == null ? "" : filter;

        addSearchField();
        showTracks();
        filterTracks();
        setPlayerListeners();
        continueTrack(savedInstanceState);
        askToContinueDownloadTracks();

        if (play){
            returnToComposers = true;
            playTrack(tracks.getNext());
            ActivityTracks.this.setQuiteTimeout();
        }
    }

    private void playTrack(TrackEntry track){
        player.releasePlayer();
        setPlayBtnIcon(track);

        if (track.id != -1){
            player.initializePlayer(track.stream);
            if (track.id == tracks.lastPlaying){
                player.setPosition(tracks.position);
            }
        }

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