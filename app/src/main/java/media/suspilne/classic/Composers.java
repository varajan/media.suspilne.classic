package media.suspilne.classic;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

public class Composers {
    public static ArrayList<Composer> getComposers(){
        ArrayList<Composer> result = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        for (TrackEntry track:new Tracks().getTracks()) {
            names.add(track.getAuthor());
        }

        names = new ArrayList<>(new HashSet<>(names));

        for (String name: new HashSet<>(names)) {
            result.add(new Composer(name));
        }

        return result;
    }
}

class Composer{
    public String name;
    public Integer photo;
    public Integer tracksCount;
    public boolean favorite;

    public Composer(String name){
        this.name = name;
        this.photo = findPhoto(name);
        this.tracksCount = countTracks(name);

        favorite = false;
    }

    private Integer findPhoto(String name){
        for (TrackEntry track:new Tracks().getTracks()) {
            if (track.getAuthor().equals(name))
                return track.getAuthorPhoto();
        }

        return null;
    }

    private Integer countTracks(String name){
        int result = 0;

        for (TrackEntry track:new Tracks().getTracks()) {
            if (track.getAuthor().equals(name)) result++;
        }

        return result;
    }

    private View getTrackView(){
        return  ActivityTracks.getActivity().findViewById(R.id.list).findViewWithTag(name);
    }

    public void setViewDetails(){
        try
        {
            Bitmap author = ImageHelper.getBitmapFromResource(ActivityMain.getContext().getResources(), photo, 100, 100);
            author = ImageHelper.getCircularDrawable(author);
            View trackView = getTrackView();

            ((ImageView)trackView.findViewById(R.id.favorite)).setImageResource(favorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);
            ((ImageView)trackView.findViewById(R.id.photo)).setImageBitmap(author);
            ((TextView) trackView.findViewById(R.id.title)).setText(name);
            ((TextView) trackView.findViewById(R.id.author)).setText("TRACKS: " + tracksCount);
        }catch (Exception e){
            Log.e(SettingsHelper.application, e.getMessage());
            e.printStackTrace();
        }
    }
}
