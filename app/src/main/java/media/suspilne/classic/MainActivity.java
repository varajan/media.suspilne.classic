package media.suspilne.classic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Timer quitTimer;
    protected Player player;
    protected NavigationView navigation;
    protected int currentView;

    protected void setQuiteTimeout(){
        if (SettingsHelper.getBoolean(this, "autoQuit")) {
            if (quitTimer != null) quitTimer.cancel();
            int timeout = SettingsHelper.getInt(this, "timeout");

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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
        } else {
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
        builder.setMessage("Вийти з Казок?")
                .setPositiveButton("Так", dialogClickListener)
                .setNegativeButton("Ні", dialogClickListener)
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
                    openActivity(Tracks.class);
                }
                break;


            case R.id.settings_menu:
                if (currentView != R.id.settings_menu) {
                    openActivity(Settings.class);
                }
                break;

            case R.id.exit_menu:
                showQuitDialog();
        }

        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return false;
    }
}