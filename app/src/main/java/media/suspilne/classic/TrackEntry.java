package media.suspilne.classic;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.util.IOUtils;

import java.io.InputStream;
import java.net.URL;

public class TrackEntry{
    public int id;
    private int titleId;
    private int authorNameId;
    boolean favorite;
    boolean isDownloaded;
    String stream;
    String fileName;

    TrackEntry(){ id = -1; }

    TrackEntry(int id, int title, int name){
        this.id = id;
        this.titleId = title;
        this.authorNameId = name;
        this.favorite = SettingsHelper.getBoolean("isFavorite_" + id);
        this.isDownloaded = isDownloaded(this.id);
        this.stream = stream(id);
        this.fileName = fileName(id);
    }

    private int getAuthorPhoto(){
        switch (authorNameId){
            case R.string.beethoven: return R.mipmap.beethoven;
            case R.string.rachmaninov: return R.mipmap.rachmaninov;
            case R.string.chaikovsky: return R.mipmap.chaikovsky;
            case R.string.mendelson: return R.mipmap.mendelson;
            case R.string.bach: return R.mipmap.bach;
            case R.string.musorgsky: return R.mipmap.musorgsky;
            case R.string.elgar: return R.mipmap.elgar;
            case R.string.leontovych: return R.mipmap.leontovych;
            case R.string.bilash: return R.mipmap.bilash;
            case R.string.bellini: return R.mipmap.bellini;
            case R.string.lysenko: return R.mipmap.lysenko;
            case R.string.khachaturian: return R.mipmap.khachaturian;
            case R.string.shostakovich: return R.mipmap.shostakovich;
            case R.string.chopin: return R.mipmap.chopin;
            case R.string.haydn: return R.mipmap.haydn;
            case R.string.list: return R.mipmap.list;
            case R.string.debussy: return R.mipmap.debussy;
            case R.string.orff: return R.mipmap.orff;
            case R.string.ravel: return R.mipmap.ravel;
            case R.string.borodin: return R.mipmap.borodin;
            case R.string.rossini: return R.mipmap.rossini;
            case R.string.saint_saens: return R.mipmap.saint_saens;
            case R.string.wagner: return R.mipmap.wagner;
            case R.string.mozart: return R.mipmap.mozart;
            case R.string.strauss_i: return R.mipmap.strauss_i;
            case R.string.strauss_ii: return R.mipmap.strauss_ii;
            case R.string.strauss_eduard: return R.mipmap.strauss_eduard;
            case R.string.vivaldi: return R.mipmap.vivaldi;
            case R.string.piazzolla: return R.mipmap.piazzolla;
            case R.string.bizet: return R.mipmap.bizet;
            case R.string.grieg: return R.mipmap.grieg;
            case R.string.offenbach: return R.mipmap.offenbach;
            case R.string.boccherini: return R.mipmap.boccherini;
            case R.string.ponchielli: return R.mipmap.ponchielli;
            case R.string.dukas: return R.mipmap.dukas;
            case R.string.barber: return R.mipmap.barber;
            case R.string.rimsky_korsakov: return R.mipmap.rimsky_korsakov;
            case R.string.verdi: return R.mipmap.verdi;
            case R.string.brahms: return R.mipmap.brahms;
            case R.string.handel: return R.mipmap.handel;
            case R.string.prokofiev: return R.mipmap.prokofiev;
            case R.string.puccini: return R.mipmap.puccini;
            case R.string.donizetti: return R.mipmap.donizetti;
            case R.string.gounod: return R.mipmap.gounod;

            default: return 0;
        }
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

        if (SettingsHelper.getBoolean("downloadFavoriteTracks") && !favorite){
            MainActivity.getContext().deleteFile(fileName);
        }

        if (SettingsHelper.getBoolean("downloadFavoriteTracks") && favorite){
            new DownloadTrack().execute(this);
        }
    }

    void remove(){
        getTrackView().setVisibility(View.GONE);
    }

    void setViewDetails(){
        try
        {
            Bitmap author = ImageHelper.getBitmapFromResource(MainActivity.getContext().getResources(), getAuthorPhoto(), 100, 100);
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

    @SuppressLint("DefaultLocale")
    private String fileName(int track){
        return String.format("%d.mp3", track);
    }

    private boolean isDownloaded(int track){
        return MainActivity.getContext().getFileStreamPath(fileName(track)).exists();
    }

    private String stream(int track){
        return isDownloaded(track)
            ? MainActivity.getContext().getFilesDir() + "/" + fileName(track)
            : TracksActivity.getActivity().getResources().getString(R.string.trackUrl, track);
    }

    static class DownloadTrack extends AsyncTask<TrackEntry, Void, Void> {
        @Override
        protected Void doInBackground(TrackEntry... tracks) {
            try {
                TrackEntry track = tracks[0];
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