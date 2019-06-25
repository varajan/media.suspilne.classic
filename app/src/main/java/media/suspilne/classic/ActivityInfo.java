package media.suspilne.classic;

import android.os.Bundle;
import android.widget.TextView;

public class ActivityInfo extends ActivityMain {
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_info);
        currentView = R.id.info_menu;
        super.onCreate(savedInstanceState);

        ((TextView)findViewById(R.id.infoText)).setText(getString(R.string.description, SettingsHelper.getVersionName()));
    }
}