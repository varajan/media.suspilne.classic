package media.suspilne.classic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.google.android.material.navigation.NavigationView;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NotificationManager notificationManager;
    private Timer quitTimer;

    protected NavigationView navigation;
    protected TextView activityTitle;
    protected int currentView;

    private static Activity activity;
    public static Activity getActivity(){ return activity; }

    private  void resetTimer(){
        if (quitTimer != null) quitTimer.cancel();
        SettingsHelper.setBoolean("stopPlaybackOnTimeout", false);
    }

    protected void setQuiteTimeout(){
        if (SettingsHelper.getBoolean("autoQuit")) {
            int timeout = SettingsHelper.getInt("timeout");
            timeout = timeout==0 ? 5 : timeout;

            resetTimer();
            quitTimer = new Timer();
            quitTimer.schedule(new stopPlaybackOnTimeout(), timeout * 60 * 1000);
        } else {
            resetTimer();
        }
    }

    class stopPlaybackOnTimeout extends TimerTask {
        @Override
        public void run() {
            SettingsHelper.setBoolean("stopPlaybackOnTimeout", true);
        }
    }

    protected boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && isNetworkSpeedOk();
    }

    private boolean isNetworkSpeedOk() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return true;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager != null
                ? connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork())
                : null;

        int downSpeed = networkCapabilities != null ? networkCapabilities.getLinkDownstreamBandwidthKbps() : 0;
        int upSpeed   = networkCapabilities != null ? networkCapabilities.getLinkUpstreamBandwidthKbps() : 0;

        return downSpeed > 20_000 && upSpeed > 2_000;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String language = SettingsHelper.getString("Language", LocaleManager.getLanguage());
        LocaleManager.setLanguage(this, language);
    }

    private void readSettingsFromGit(){
        if (!SettingsHelper.getBoolean("readSettingsFromGit")) return;

        new Thread(() -> {
            ArrayList<String> settings = new ArrayList<>();

            try {
                String url = "https://raw.githubusercontent.com/varajan/media.suspilne.classic/master/settings";
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(15000);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String str;
                while ((str = in.readLine()) != null) {
                    settings.add(str);
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            SettingsHelper.setBoolean("useGitTracks", settings.contains("useGitTracks:true"));
            SettingsHelper.setBoolean("readSettingsFromGit", false);
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String language = SettingsHelper.getString(this, "Language", LocaleManager.getLanguage());
        LocaleManager.setLanguage(this, language);

        ActivityMain.activity = this;

        readSettingsFromGit();

        switch (currentView){
            case R.id.tracks_menu:
                setContentView(R.layout.activity_tracks);
                break;

            case R.id.composers_menu:
                setContentView(R.layout.activity_composers);
                break;

            case R.id.settings_menu:
                setContentView(R.layout.activity_settings);
                break;

            case R.id.info_menu:
                setContentView(R.layout.activity_info);
                break;
        }

        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activityTitle = findViewById(R.id.title);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigation = findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(this);
        navigation.setCheckedItem(currentView);

        setTitle();
        setQuiteTimeout();
        showErrorMessage();
    }

    @Override
    protected void onResume(){
        super.onResume();
        showErrorMessage();

        ActivityMain.activity = this;
    }

    private void showErrorMessage(){
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        String errorMessage = SettingsHelper.getString("errorMessage");

        if (!errorMessage.isEmpty()){
            showAlert(getString(R.string.an_error_occurred), errorMessage);
            notificationManager.cancel(DownloadTask.WITH_ERROR);
            SettingsHelper.setString("errorMessage", "");
        }
    }

    private void exit(){
        moveTaskToBack(true);
        stopPlayerService();
        System.exit(1);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(currentView == R.id.settings_menu){
            openActivity(ActivityTracks.class);
        }
        else {
            showQuitDialog();
        }
    }

    private void showQuitDialog(){
        new AlertDialog.Builder(this)
            .setIcon(R.mipmap.icon_classic)
            .setTitle(R.string.confirm_exit)
            .setPositiveButton(R.string.yes, (dialog, which) -> exit())
            .setNegativeButton(R.string.no, null)
            .show();
    }

    protected void setTitle() {
        String title = navigation.getMenu().findItem(currentView).getTitle().toString();
        activityTitle.setText(title);
    }

    protected void stopPlayerService(){
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        stopService(new Intent(this, PlayerService.class));
        try {
            notificationManager.cancelAll();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void openActivity(Class view){
        Intent intent = new Intent(this, view);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tracks_menu:
                if (currentView != R.id.tracks_menu) {
                    openActivity(ActivityTracks.class);
                }
                break;

            case R.id.composers_menu:
                if (currentView != R.id.composers_menu) {
                    openActivity(ActivityComposers.class);
                }
                break;

            case R.id.settings_menu:
                if (currentView != R.id.settings_menu) {
                    openActivity(ActivitySettings.class);
                }
                break;

            case R.id.info_menu:
                if (currentView != R.id.info_menu) {
                    openActivity(ActivityInfo.class);
                }
                break;

            case R.id.rate_menu:
                rateApp();
                break;

            case R.id.exit_menu:
                showQuitDialog();
                break;
        }

        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return false;
    }

    private void rateApp(){
        try {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    void download(){
        if (!this.isNetworkAvailable()){
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        } else {
            boolean onlyFavorite = SettingsHelper.getBoolean("downloadFavoriteTracks") && !SettingsHelper.getBoolean("downloadAllTracks");
            TrackEntry[] download = new Tracks().getTracks(onlyFavorite).toArray(new TrackEntry[0]);

            new DownloadTask().execute(download);
        }
    }

    public void showAlert(String title, String message){
        new AlertDialog.Builder(this)
            .setIcon(R.mipmap.icon_classic)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(R.string.ok, null)
            .show();
    }

    protected void continueDownloadTracks(){
        if (!SettingsHelper.getBoolean("downloadAllTracks") && !SettingsHelper.getBoolean("downloadFavoriteTracks")) return;
        if (SettingsHelper.freeSpace() < 150 || !isNetworkAvailable()) return;

        boolean allAreDownloaded = true;
        boolean onlyFavorite = SettingsHelper.getBoolean("downloadFavoriteTracks") && !SettingsHelper.getBoolean("downloadAllTracks");

        for (TrackEntry track : new Tracks().getTracks()){
            if ((!onlyFavorite || track.isFavorite) && !track.isDownloaded){
                allAreDownloaded = false;
                break;
            }
        }

        if (!allAreDownloaded) download();
    }

    protected void suggestToDownloadFavoriteTracks(){
        if (SettingsHelper.getBoolean("suggestToDownloadFavoriteTracks")) return;
        if (SettingsHelper.getBoolean("downloadAllTracks") || SettingsHelper.getBoolean("downloadFavoriteTracks")) return;
        if (SettingsHelper.freeSpace() < 150 || !isNetworkAvailable()) return;

        int favorites = new Tracks().getTracks(true).size();
        if (favorites < 5) return;

        SettingsHelper.setBoolean("suggestToDownloadFavoriteTracks", true);

        new AlertDialog.Builder(ActivityMain.this)
            .setIcon(R.mipmap.icon_classic)
            .setTitle(R.string.download)
            .setMessage(getString(R.string.suggestToDownloadFavorite, favorites))
            .setPositiveButton(R.string.download, (dialog, which) -> {SettingsHelper.setBoolean("downloadFavoriteTracks", true); download();})
            .setNegativeButton(R.string.no, null)
            .show();
    }
}