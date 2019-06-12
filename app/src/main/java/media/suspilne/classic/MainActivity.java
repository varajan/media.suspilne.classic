package media.suspilne.classic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.util.stream.Collectors.toList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ProgressDialog progress;
    private Timer quitTimer;
    protected Player player;
    protected NavigationView navigation;
    protected int currentView;

    private static Context context;
    public static Context getContext(){ return context; }

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

        String language = SettingsHelper.getString("Language", LacaleManager.getLanguage());
        LacaleManager.setLanguage(this, language);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity.context = getApplicationContext();
        MainActivity.activity = this;

        String language = SettingsHelper.getString("Language", LacaleManager.getLanguage());
        LacaleManager.setLanguage(this, language);

        setContentView(currentView == R.id.tracks_menu ? R.layout.activity_tracks : R.layout.activity_settings);

        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigation = findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(this);
        navigation.setCheckedItem(currentView);

        setTitle();
        setQuiteTimeout();

        player = new Player(this);
        player.UpdateSslProvider();
        startService(new Intent(this, Player.class));
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
            openActivity(TracksActivity.class);
        }
        else {
            showQuitDialog();
        }
    }

    private void showQuitDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        exit();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //'No' button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_exit)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
    }

    private void setTitle() {
        String title = navigation.getMenu().findItem(currentView).getTitle().toString();
        getSupportActionBar().setTitle(title);
    }

    private void openActivity(Class view){
        if (player != null) player.releasePlayer();

        Intent intent = new Intent(this, view);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tracks_menu:
                if (currentView != R.id.tracks_menu) {
                    openActivity(TracksActivity.class);
                }
                break;


            case R.id.settings_menu:
                if (currentView != R.id.settings_menu) {
                    openActivity(SettingsActivity.class);
                }
                break;

            case R.id.exit_menu:
                showQuitDialog();
        }

        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return false;
    }

    void dropDownloads(String extension){
        for (String file:context.fileList()) {
            if (file.toLowerCase().contains(extension.toLowerCase())){
                context.deleteFile(file);
            }
        }
    }

    void download(){
        if (!this.isNetworkAvailable()){
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        } else {
            Tracks tracks = new Tracks();
            tracks.showOnlyFavorite = !SettingsHelper.getBoolean("downloadAllTracks");
            new DownloadAll().execute(tracks.getTracks().toArray(new TrackEntry[0]));
        }
    }

    class DownloadAll extends AsyncTask<TrackEntry, Void, Boolean> {
        protected void onPreExecute() {
            progress = new ProgressDialog(MainActivity.this);
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
        protected void onPostExecute(final Boolean success) {
            if (progress.isShowing()) {
                progress.dismiss();
            }

            Toast.makeText(MainActivity.this, success ? R.string.done : R.string.downloadFailed, Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(TrackEntry... integers) {
            try {
                progress.setMax(integers.length);

                for (TrackEntry track:integers) {
                    String name = String.format("%d.mp3", track.id);

                    if (context.getFileStreamPath(name).exists()) continue;

                    InputStream is = (InputStream) new URL(track.stream()).getContent();
                    SettingsHelper.saveFile(name, IOUtils.toByteArray(is));
                    publishProgress();
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }
}