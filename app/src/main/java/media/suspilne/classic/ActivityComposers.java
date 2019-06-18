package media.suspilne.classic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class ActivityComposers extends ActivityMain {
    LinearLayout composersList;
    ArrayList<Composer> composers = Composers.getComposers();

    protected void onCreate(Bundle savedInstanceState) {
        currentView = R.id.composers_menu;
        super.onCreate(savedInstanceState);

        composersList = findViewById(R.id.list);

        for (final Composer composer:composers) {
            View composerView = LayoutInflater.from(this).inflate(R.layout.track_item, composersList, false);
            composerView.setTag(composer.name);
            composersList.addView(composerView);
            composer.setViewDetails();

            composerView.findViewById(R.id.play).setOnClickListener(v -> {
                // open TracksView with filter by author and start playing
            });

            composerView.findViewById(R.id.favorite).setOnClickListener(v -> {
                // add all to favorite/remove all
            });
        }
    }
}
