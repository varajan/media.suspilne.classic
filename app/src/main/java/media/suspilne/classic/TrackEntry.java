package media.suspilne.classic;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TrackEntry{
    public int id;
    public int titleId;
    public int authorNameId;
    public int authorPhotoId;

    public TrackEntry(){ id = -1; }

    public TrackEntry(int id, int title, int name, int photo){
        this.id = id;
        this.titleId = title;
        this.authorNameId = name;
        this.authorPhotoId = photo;
    }

    public void setViewDetails(Context context, View trackView){
        try
        {
            Bitmap author = ImageHelper.getBitmapFromResource(context.getResources(), authorPhotoId, 100, 100);
            author = ImageHelper.getCircularDrawable(author);

            ((ImageView)trackView.findViewById(R.id.photo)).setImageBitmap(author);
            ((TextView) trackView.findViewById(R.id.title)).setText(titleId);
            ((TextView) trackView.findViewById(R.id.author)).setText(authorNameId);
        }catch (Exception e){
            Log.e(SettingsHelper.application, "Failed to load track #" + id);
            Log.e(SettingsHelper.application, e.getMessage());
            e.printStackTrace();
        }
    }

    public String stream(){
        return stream(id);
    }

    public String stream(int track){
        return String.format("https://classical.suspilne.media/inc/audio/%02d.mp3", track);
    }
}