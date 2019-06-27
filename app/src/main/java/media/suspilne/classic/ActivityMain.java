package media.suspilne.classic;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.util.IOUtils;
import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ProgressDialog progress;
    private Timer quitTimer;
//    protected PlayerService player;
    protected NavigationView navigation;
    protected TextView activityTitle;
    protected int currentView;

    private static Activity activity;
    public static Activity getActivity(){ return activity; }

    protected void setQuiteTimeout(){
        if (SettingsHelper.getBoolean("autoQuit")) {
            if (quitTimer != null) quitTimer.cancel();
            int timeout = SettingsHelper.getInt("timeout");

            quitTimer = new Timer();
            quitTimer.schedule(new stopRadioOnTimeout(), timeout * 60 * 1000);
        } else {
            if (quitTimer != null) quitTimer.cancel();
        }
    }

    class stopRadioOnTimeout extends TimerTask {
        @Override
        public void run() {
            exit();
        }
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String language = SettingsHelper.getString("Language", LocaleManager.getLanguage());
        LocaleManager.setLanguage(this, language);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String language = SettingsHelper.getString(this, "Language", LocaleManager.getLanguage());
        LocaleManager.setLanguage(this, language);

        ActivityMain.activity = this;

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

//        player = new PlayerService();
//        startService(new Intent(activity, PlayerService.class));
    }

    private void exit(){
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
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

    protected boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void stopPlayerService(){
        stopService(new Intent(this, PlayerService.class));
    }

    protected void openActivity(Class view){
        stopPlayerService();
//        if (player != null) player.stopService(new Intent(this, PlayerService.class));

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

            new DownloadAll().execute(download);
        }
    }

    class DownloadAll extends AsyncTask<TrackEntry, Void, String> {
        protected void onPreExecute() {
            progress = new ProgressDialog(ActivityMain.this);
            progress.setIcon(R.mipmap.icon_classic);
            progress.setTitle(R.string.downloading);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progress.incrementProgressBy(1);
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }

            if (result.isEmpty()){
                Toast.makeText(ActivityMain.this, R.string.done, Toast.LENGTH_LONG).show();
            }else{
                new AlertDialog.Builder(ActivityMain.this)
                    .setIcon(R.mipmap.icon_classic)
                    .setTitle(R.string.an_error_occurred)
                    .setMessage(result)
                    .setNeutralButton(R.string.ok, null)
                    .show();
            }
        }

        @Override
        protected String doInBackground(TrackEntry... tracks) {
            try {
                progress.setMax(tracks.length);

                for (TrackEntry track:tracks) {
                    if (track.isDownloaded) publishProgress();
                }

                for (TrackEntry track:tracks) {
                    if (track.isDownloaded) continue;
                    long freeSpace = SettingsHelper.freeSpace();
                    long required = 100 * 1024 * 1024;

                    if (freeSpace < required){
                        throw new Exception(getString(R.string.not_enough_space, SettingsHelper.formattedSize(freeSpace), SettingsHelper.formattedSize(required)));
                    }

                    InputStream is = (InputStream) new URL(track.stream).getContent();
                    SettingsHelper.saveFile(track.fileName, IOUtils.toByteArray(is));
                    publishProgress();
                }
            }catch (Exception e){
                e.printStackTrace();
                return e.getMessage();
            }

            return "";
        }
    }

    protected void askToContinueDownloadTracks(){
        if (SettingsHelper.getBoolean("askedToContinueDownload")) return;
        if (!SettingsHelper.getBoolean("downloadAllTracks") && !SettingsHelper.getBoolean("downloadFavoriteTracks")) return;
        if (SettingsHelper.freeSpace() < 150 || !isNetworkAvailable()) return;

        SettingsHelper.setBoolean("askedToContinueDownload", true);

        boolean allAreDownloaded = true;
        boolean onlyFavorite = SettingsHelper.getBoolean("downloadFavoriteTracks") && !SettingsHelper.getBoolean("downloadAllTracks");

        for (TrackEntry track : new Tracks().getTracks()){
            if ((!onlyFavorite || track.isFavorite) && !track.isDownloaded){
                allAreDownloaded = false;
                break;
            }
        }

        if (allAreDownloaded) return;

        new AlertDialog.Builder(ActivityMain.this)
            .setIcon(R.mipmap.icon_classic)
            .setTitle(R.string.continueDownload)
            .setMessage(R.string.not_all_tracks)
            .setPositiveButton(R.string.download, (dialog, which) -> download())
            .setNegativeButton(R.string.no, null)
            .show();
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