package media.suspilne.classic;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TrackEntry{
    public int id;
    private int titleId;
    private int authorNameId;
    private int authorPhotoId;
    boolean favorite;

    TrackEntry(){ id = -1; }

    TrackEntry(int id, int title, int name, int photo){
        this.id = id;
        this.titleId = title;
        this.authorNameId = name;
        this.authorPhotoId = photo;
        this.favorite = SettingsHelper.getBoolean("isFavorite_" + id);
    }

    String getAuthor(){
        return TracksActivity.getActivity().getResources().getString(authorNameId);
    }

    String getTitle(){
        return TracksActivity.getActivity().getResources().getString(titleId);
    }

    private View getTrackView(){
        return  TracksActivity.getActivity().findViewById(R.id.list).findViewWithTag(id);
    }

    void resetFavorite(){
        favorite = !favorite;
        SettingsHelper.setBoolean("isFavorite_" + id, favorite);

        ((ImageView)getTrackView().findViewById(R.id.favorite)).setImageResource(favorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);
    }

    void remove(){
        getTrackView().setVisibility(View.GONE);
    }

    void setViewDetails(){
        try
        {
            Bitmap author = ImageHelper.getBitmapFromResource(MainActivity.getContext().getResources(), authorPhotoId, 100, 100);
            author = ImageHelper.getCircularDrawable(author);
            View trackView = getTrackView();

            ((ImageView)trackView.findViewById(R.id.favorite)).setImageResource(favorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);
            ((ImageView)trackView.findViewById(R.id.photo)).setImageBitmap(author);
            ((TextView) trackView.findViewById(R.id.title)).setText(titleId);
            ((TextView) trackView.findViewById(R.id.author)).setText(authorNameId);
        }catch (Exception e){
            Log.e(SettingsHelper.application, "Failed to load track #" + id);
            Log.e(SettingsHelper.application, e.getMessage());
            e.printStackTrace();
        }
    }

    String stream(){
        return stream(id);
    }

    String stream(int track){
        return TracksActivity.getActivity().getResources().getString(R.string.trackUrl, track);
    }
}