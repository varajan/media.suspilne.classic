package media.suspilne.classic;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class Composer{
    public Integer name;
    public Integer photo;
    public Integer tracksCount;

    public Composer(int name){
        this.name = name;
        this.photo = getPhoto();
        this.tracksCount = getTracksCount();
    }

    public String getName(){
        return ActivityTracks.getActivity().getResources().getString(name);
    }

    private View getComposerView(){
        return  ActivityTracks.getActivity().findViewById(R.id.list).findViewWithTag(getName());
    }

    public void setViewDetails(Context context){
        try
        {
            Bitmap author = ImageHelper.getBitmapFromResource(ActivityMain.getActivity().getResources(), photo, 100, 100);
            author = ImageHelper.getCircularDrawable(author);
            View composerView = getComposerView();

            composerView.findViewById(R.id.favorite).setVisibility(View.GONE);
            ((ImageView)composerView.findViewById(R.id.photo)).setImageBitmap(author);
            ((TextView) composerView.findViewById(R.id.title)).setText(name);
            ((TextView) composerView.findViewById(R.id.author)).setText(context.getString(R.string.composer_tracks, tracksCount));
        }catch (Exception e){
            Log.e(SettingsHelper.application, e.getMessage());
            e.printStackTrace();
        }
    }

    private int getPhoto(){
        switch (name){
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

    private int getTracksCount(){
        switch (name){
            case R.string.bach: return 3;
            case R.string.barber: return 1;
            case R.string.beethoven: return 10;
            case R.string.bellini: return 1;
            case R.string.bilash: return 1;
            case R.string.bizet: return 2;
            case R.string.boccherini: return 1;
            case R.string.brahms: return 2;
            case R.string.borodin: return 1;
            case R.string.chaikovsky: return 11;
            case R.string.chopin: return 6;
            case R.string.debussy: return 2;
            case R.string.donizetti: return 1;
            case R.string.dukas: return 1;
            case R.string.elgar: return 1;
            case R.string.gounod: return 1;
            case R.string.grieg: return 4;
            case R.string.handel: return 1;
            case R.string.haydn: return 2;
            case R.string.khachaturian: return 2;
            case R.string.leontovych: return 5;
            case R.string.list: return 4;
            case R.string.lysenko: return 2;
            case R.string.mendelson: return 2;
            case R.string.mozart: return 5;
            case R.string.musorgsky: return 5;
            case R.string.offenbach: return 2;
            case R.string.orff: return 1;
            case R.string.prokofiev: return 2;
            case R.string.puccini: return 4;
            case R.string.piazzolla: return 3;
            case R.string.ponchielli: return 1;
            case R.string.rachmaninov: return 5;
            case R.string.ravel: return 2;
            case R.string.rimsky_korsakov: return 1;
            case R.string.rossini: return 5;
            case R.string.shostakovich: return 1;
            case R.string.saint_saens: return 2;
            case R.string.strauss_i: return 1;
            case R.string.strauss_ii: return 3;
            case R.string.strauss_eduard: return 1;
            case R.string.verdi: return 4;
            case R.string.vivaldi: return 4;
            case R.string.wagner: return 2;

            default: return 0;
        }
    }
}
