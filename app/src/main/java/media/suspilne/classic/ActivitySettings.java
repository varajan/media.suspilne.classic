package media.suspilne.classic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ActivitySettings extends ActivityMain {
    private Switch batteryOptimization;
    private Switch downloadAllTracks;
    private Switch downloadFavoriteTracks;
    private Switch tracksPlayNext;
    private Switch showOnlyFavorite;
    private Switch autoQuit;
    private SeekBar timeout;
    private TextView timeoutText;
    private Spinner languages;
    private int step = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.settings_menu;
        super.onCreate(savedInstanceState);

        batteryOptimization = this.findViewById(R.id.batteryOptimization);
        downloadAllTracks = this.findViewById(R.id.downloadAllTracks);
        downloadFavoriteTracks = this.findViewById(R.id.downloadFavoriteTracks);
        tracksPlayNext = this.findViewById(R.id.tracksPlayNext);
        showOnlyFavorite = this.findViewById(R.id.showOnlyFavorite);
        autoQuit = this.findViewById(R.id.autoQuit);
        timeout = this.findViewById(R.id.timeout);
        timeoutText = this.findViewById(R.id.timeoutText);
        languages = this.findViewById(R.id.languages);

        setLanguages();
        setColorsAndState();

        tracksPlayNext.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("tracksPlayNext", isChecked));
        showOnlyFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("showOnlyFavorite", isChecked));
        autoQuit.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitch("autoQuit", isChecked));

        batteryOptimization.setOnCheckedChangeListener(onIgnoreBatteryChangeListener);
        timeout.setOnSeekBarChangeListener(onTimeoutChange);
        languages.setOnItemSelectedListener(omLanguageSelect);

        askToContinueDownloadTracks();
    }

    void setSwitch(String title, boolean isChecked){
        SettingsHelper.setBoolean(title, isChecked);
        setColorsAndState();
    }

    private void setLanguages(){
        ArrayList<Country> countries = new ArrayList<>();
        countries.add( new Country("en", getString(R.string.language_en), R.mipmap.uk));
        countries.add( new Country("uk", getString(R.string.language_ua), R.mipmap.ua));

        LanguageArrayAdapter arrayAdapter = new LanguageArrayAdapter(this, R.layout.language, countries);
        String currentLanguage = getResources().getConfiguration().locale.getLanguage();

        languages.setAdapter(arrayAdapter);

        for (int i = 0; i < countries.size(); i++) {
            if (countries.get(i).code.equals(currentLanguage)){
                languages.setSelection(i);
                break;
            }
        }
    }

    private void doDownloadAll(){
        long free = SettingsHelper.freeSpace();
        if (free < 1500){
            new AlertDialog.Builder(this)
                .setIcon(R.mipmap.icon_classic)
                .setTitle(R.string.an_error_occurred)
                .setMessage(getString(R.string.not_enough_space, free + "MB"))
                .setNeutralButton(R.string.ok, null)
                .show();

            return;
        }

        SettingsHelper.setBoolean("downloadAllTracks", true);
        SettingsHelper.setBoolean("downloadFavoriteTracks", true);
        download();
        setColorsAndState();
    }

    private void doDownloadFavorite(){
        SettingsHelper.setBoolean("downloadFavoriteTracks", true);
        download();
        setColorsAndState();
    }

    private void doCleanup(boolean includeFavorite){
        List<TrackEntry> tracks = new Tracks().getTracks();

        for (TrackEntry track : tracks) {
            if (includeFavorite || !track.isFavorite){
                track.deleteFile();
            }
        }

        SettingsHelper.setBoolean(includeFavorite ? "downloadFavoriteTracks" : "downloadAllTracks", false);
        setColorsAndState();
    }

    private CompoundButton.OnCheckedChangeListener onDownloadAllSelect = (buttonView, isChecked) -> {
        new AlertDialog.Builder(ActivitySettings.this)
            .setIcon(R.mipmap.icon_classic)
            .setTitle(isChecked ? R.string.download : R.string.clear)
            .setMessage(isChecked ? R.string.downloadAllTracksQuestion : R.string.clearAllTracksQuestion)
            .setPositiveButton(isChecked ? R.string.download : R.string.clear, (dialog, which) -> {if (isChecked) doDownloadAll(); else doCleanup(false);})
            .setNegativeButton(R.string.no, (dialog, which) -> setColorsAndState())
            .setOnDismissListener(dialog -> setColorsAndState())
            .show();
    };

    private CompoundButton.OnCheckedChangeListener onDownloadFavoriteSelect = (buttonView, isChecked) -> {
        new AlertDialog.Builder(ActivitySettings.this)
            .setIcon(R.mipmap.icon_classic)
            .setTitle(isChecked ? R.string.download : R.string.clear)
            .setMessage(isChecked ? R.string.downloadFavoriteTracksQuestion : R.string.clearAllTracksQuestion)
            .setPositiveButton(isChecked ? R.string.download : R.string.clear, (dialog, which) -> {if (isChecked) doDownloadFavorite(); else doCleanup(true);})
            .setNegativeButton(R.string.no, (dialog, which) -> setColorsAndState())
            .setOnDismissListener(dialog -> setColorsAndState())
            .show();
    };

    private CompoundButton.OnCheckedChangeListener onIgnoreBatteryChangeListener = (buttonView, isChecked) -> {
        requestIgnoreBatteryOptimization();
        setColorsAndState();
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations(){
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        return pm.isIgnoringBatteryOptimizations(this.getPackageName());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestIgnoreBatteryOptimization(){
        if (isIgnoringBatteryOptimizations()){
            startActivityForResult(new Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), 0);
        }else{
            Uri packageUri = Uri.parse("package:" + this.getPackageName());
            startActivityForResult(new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setColorsAndState();
    }

    private void setColorsAndState() {
        boolean isShowOnlyFavorite = SettingsHelper.getBoolean("showOnlyFavorite");
        boolean isTracksPlayNext = SettingsHelper.getBoolean("tracksPlayNext");
        boolean isAutoQuit = SettingsHelper.getBoolean("autoQuit");
        boolean isDownloadAllTracks = SettingsHelper.getBoolean("downloadAllTracks");
        boolean isDownloadFavoriteTracks = SettingsHelper.getBoolean("downloadFavoriteTracks");

        int primaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        int primary = ContextCompat.getColor(this, R.color.colorPrimary);
        String minutes = SettingsHelper.getString("timeout", "5");

        timeoutText.setText(getString(R.string.x_minutes, minutes));
        timeout.setProgress(SettingsHelper.getInt("timeout", 1) / step);

        showOnlyFavorite.setChecked(isShowOnlyFavorite);
        tracksPlayNext.setChecked(isTracksPlayNext);
        autoQuit.setChecked(isAutoQuit);
        timeout.setEnabled(isAutoQuit);
        timeout.setEnabled(isAutoQuit);

        if (Build.VERSION.SDK_INT > 23){
            batteryOptimization.setOnCheckedChangeListener(null);
            batteryOptimization.setTextColor(isIgnoringBatteryOptimizations() ? primaryDark : primary);
            batteryOptimization.setChecked(isIgnoringBatteryOptimizations());
            batteryOptimization.setOnCheckedChangeListener(onIgnoreBatteryChangeListener);
        }else{
            batteryOptimization.setVisibility(View.GONE);
        }

        downloadAllTracks.setOnCheckedChangeListener(null);
        downloadAllTracks.setTextColor(isDownloadAllTracks ? primaryDark : primary);
        downloadAllTracks.setChecked(isDownloadAllTracks);
        downloadAllTracks.setOnCheckedChangeListener(onDownloadAllSelect);

        downloadFavoriteTracks.setEnabled(!isDownloadAllTracks);
        downloadFavoriteTracks.setOnCheckedChangeListener(null);
        downloadFavoriteTracks.setTextColor(isDownloadFavoriteTracks ? primaryDark : primary);
        downloadFavoriteTracks.setChecked(!isDownloadAllTracks && isDownloadFavoriteTracks);
        downloadFavoriteTracks.setOnCheckedChangeListener(onDownloadFavoriteSelect);

        showOnlyFavorite.setTextColor(isShowOnlyFavorite ? primaryDark : primary);
        tracksPlayNext.setTextColor(isTracksPlayNext ? primaryDark : primary);
        autoQuit.setTextColor(isAutoQuit ? primaryDark : primary);
        timeoutText.setTextColor(isAutoQuit ? primaryDark : primary);
    }

    AdapterView.OnItemSelectedListener omLanguageSelect = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String code = ((Country) languages.getSelectedItem()).code;
            String currentLanguage = LocaleManager.getLanguage();

            SettingsHelper.setString("Language", code);
            LocaleManager.setLanguage(ActivitySettings.this, code);

            if (!code.equals(currentLanguage)){
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    };

    SeekBar.OnSeekBarChangeListener onTimeoutChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            SettingsHelper.setInt("timeout", seekBar.getProgress() * step);
            String minutes = SettingsHelper.getString("timeout", "0");

            timeoutText.setText(getString(R.string.x_minutes, minutes));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
}