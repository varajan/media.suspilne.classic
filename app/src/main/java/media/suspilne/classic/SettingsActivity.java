package media.suspilne.classic;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

public class SettingsActivity extends MainActivity {
    private Switch batteryOptimization;
    private Switch tracksPlayNext;
    private Switch autoQuit;
    private SeekBar timeout;
    private TextView timeoutText;
    private Spinner languages;
    private int step = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);
        currentView = R.id.settings_menu;
        super.onCreate(savedInstanceState);

        batteryOptimization = this.findViewById(R.id.batteryOptimization);
        tracksPlayNext = this.findViewById(R.id.tracksPlayNext);
        autoQuit = this.findViewById(R.id.autoQuit);
        timeout = this.findViewById(R.id.timeout);
        timeoutText = this.findViewById(R.id.timeoutText);
        languages = this.findViewById(R.id.languages);

        setLanguages();
        setColorsAndState();

        tracksPlayNext.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsHelper.setBoolean(SettingsActivity.this, "tracksPlayNext", isChecked);
                setColorsAndState();
            }
        });

        autoQuit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsHelper.setBoolean(SettingsActivity.this, "autoQuit", isChecked);
                setColorsAndState();
            }
        });

        batteryOptimization.setOnCheckedChangeListener(onIgnoreBatteryChangeListener);

        timeout.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SettingsHelper.setInt(SettingsActivity.this,"timeout", seekBar.getProgress() * step);
                String minutes = SettingsHelper.getString(SettingsActivity.this, "timeout", "0");

                timeoutText.setText(getResources().getString(R.string.x_minutes, minutes));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String code = ((Country) languages.getSelectedItem()).code;

                SettingsHelper.setString(SettingsActivity.this,"Language", code);
                SettingsActivity.this.setLanguage(code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setLanguages(){
        ArrayList<Country> countries = new ArrayList<>();
        countries.add( new Country("en", getResources().getString(R.string.language_en), 1));
        countries.add( new Country("uk", getResources().getString(R.string.language_ua), 1));

        String currentLanguage = getResources().getConfiguration().locale.toString();
        LanguageArrayAdapter arrayAdapter = new LanguageArrayAdapter(this, R.layout.language, countries);

        languages.setAdapter(arrayAdapter);

        for (int i = 0; i < countries.size(); i++) {
            if (countries.get(i).code.equals(currentLanguage)){
                languages.setSelection(i);
                break;
            }
        }
    }

    private CompoundButton.OnCheckedChangeListener onIgnoreBatteryChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            requestIgnoreBatteryOptimization();
            setColorsAndState();
        }
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
        boolean isTracksPlayNext = SettingsHelper.getBoolean(this, "tracksPlayNext");
        boolean isAutoQuit = SettingsHelper.getBoolean(this, "autoQuit");
        int primaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        int primary = ContextCompat.getColor(this, R.color.colorPrimary);

        String minutes = SettingsHelper.getString(SettingsActivity.this, "timeout", "5");

        timeoutText.setText(getResources().getString(R.string.x_minutes, minutes));
        timeout.setProgress(SettingsHelper.getInt(this, "timeout", 1) / step);

        batteryOptimization.setEnabled(Build.VERSION.SDK_INT > 23);
        tracksPlayNext.setChecked(isTracksPlayNext);
        autoQuit.setChecked(isAutoQuit);
        timeout.setEnabled(isAutoQuit);
        timeout.setEnabled(isAutoQuit);

        batteryOptimization.setOnCheckedChangeListener(null);
        batteryOptimization.setTextColor(Build.VERSION.SDK_INT > 23 && isIgnoringBatteryOptimizations() ? primaryDark : primary);
        batteryOptimization.setChecked(Build.VERSION.SDK_INT > 23 && isIgnoringBatteryOptimizations());
        batteryOptimization.setOnCheckedChangeListener(onIgnoreBatteryChangeListener);

        tracksPlayNext.setTextColor(isTracksPlayNext ? primaryDark : primary);
        autoQuit.setTextColor(isAutoQuit ? primaryDark : primary);
        timeoutText.setTextColor(isAutoQuit ? primaryDark : primary);
    }
}