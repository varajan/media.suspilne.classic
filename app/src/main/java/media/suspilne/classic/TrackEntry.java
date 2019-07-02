package media.suspilne.classic;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.util.IOUtils;

import java.io.InputStream;
import java.net.URL;

public class TrackEntry{
    public int id;
    private int titleId;
    private int authorNameId;
    boolean isFavorite;
    boolean isDownloaded;
    String stream;
    String fileName;

    TrackEntry(){ id = -1; }

    TrackEntry(int id, int title, int name){
        this.id = id;
        this.titleId = title;
        this.authorNameId = name;
        this.isFavorite = SettingsHelper.getBoolean("isFavorite_" + id);
        this.isDownloaded = isDownloaded(this.id);
        this.stream = stream(id);
        this.fileName = fileName(id);
    }

    int getAuthorId(){
        return authorNameId;
    }

    String getAuthor(){
        return ActivityTracks.getActivity().getResources().getString(authorNameId);
    }

    String getTitle(){
        return ActivityTracks.getActivity().getResources().getString(titleId);
    }

    private View getTrackView(){
        return ActivityTracks.getActivity().findViewById(R.id.list).findViewWithTag(id);
    }

    void resetFavorite(){
        boolean downloadAll = SettingsHelper.getBoolean("downloadAllTracks");
        boolean downloadFavorite = SettingsHelper.getBoolean("downloadFavoriteTracks");

        isFavorite = !isFavorite;
        SettingsHelper.setBoolean("isFavorite_" + id, isFavorite);

        ((ImageView)getTrackView().findViewById(R.id.favorite)).setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);

        if ( isFavorite && downloadFavorite && !downloadAll) this.download();
        if (!isFavorite && downloadFavorite && !downloadAll) this.deleteFile();
    }

    void setDownloadedIcon(){
        View trackView = getTrackView();

        if (trackView != null){
            isDownloaded = isDownloaded(id);
            getTrackView().findViewById(R.id.downloaded).setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
        }
    }

    boolean shouldBeShown(boolean showOnlyFavorite, String filter){
        return (!showOnlyFavorite || isFavorite) && matchesFilter(filter);
    }

    boolean matchesFilter(String filter){
        filter = filter.toLowerCase();
        return getTitle().toLowerCase().contains(filter) || getAuthor().toLowerCase().contains(filter);
    }

    void scrollIntoView(){
        try
        {
            ScrollView scrollView = ActivityTracks.getActivity().findViewById(R.id.scrollView);
            scrollView.postDelayed(() -> scrollView.scrollTo(0, (int)getTrackView().getY()), 100);
        }
        catch (Exception e){
            Log.e(SettingsHelper.application, e.getMessage());
        }
    }

    void hide(){
        getTrackView().setVisibility(View.GONE);
    }

    void show(){
        getTrackView().setVisibility(View.VISIBLE);
    }

    void setViewDetails(){
        try
        {
            Bitmap author = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), new Composer(authorNameId).photo, 100, 100);
            author = ImageHelper.getCircularDrawable(author);
            View trackView = getTrackView();

            ((ImageView)trackView.findViewById(R.id.favorite)).setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_notfavorite);
            ((ImageView)trackView.findViewById(R.id.photo)).setImageBitmap(author);
            ((TextView) trackView.findViewById(R.id.title)).setText(titleId);
            ((TextView) trackView.findViewById(R.id.author)).setText(authorNameId);
            setDownloadedIcon();
        }catch (Exception e){
            Log.e(SettingsHelper.application, "Failed to load track #" + id);
            Log.e(SettingsHelper.application, e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    private String fileName(int track){
        return String.format("%d.mp3", track);
    }

    private boolean isDownloaded(int track){
        return ActivityMain.getActivity().getFileStreamPath(fileName(track)).exists();
    }

    private String stream(int track){
        return isDownloaded(track)
            ? ActivityMain.getActivity().getFilesDir() + "/" + fileName(track)
            : ActivityTracks.getActivity().getResources().getString(R.string.trackUrl, track);
    }

    public void download(){
        new DownloadTrack().execute(this);
    }

    public void deleteFile(){
        ActivityMain.getActivity().deleteFile(fileName);
        setDownloadedIcon();
    }

    static class DownloadTrack extends AsyncTask<TrackEntry, Void, Void> {
        private TrackEntry track;

        @Override
        protected void onPostExecute(Void result) {
            track.isDownloaded = true;
            track.setDownloadedIcon();
        }

        @Override
        protected Void doInBackground(TrackEntry... tracks) {
            try {
                track = tracks[0];
                if (!track.isDownloaded)
                {
                    InputStream is = (InputStream) new URL(track.stream).getContent();
                    SettingsHelper.saveFile(track.fileName, IOUtils.toByteArray(is));
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }
}