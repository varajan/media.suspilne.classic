package media.suspilne.classic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Composers {
    public ArrayList<Composer> composers;

    public Composers(){

        if (isAscSorted()){
            Collections.sort(items, (c1, c2) -> c1.getName().compareTo(c2.getName()));
        }else{
            Collections.sort(items, (c1, c2) -> c2.tracksCount.compareTo(c1.tracksCount));
        }

        composers = items;
    }

    public static boolean isAscSorted(){
        return SettingsHelper.getBoolean("isAscSorted");
    }

    private ArrayList<Composer> items = new ArrayList<>(Arrays.asList(
            new Composer(R.string.beethoven),
            new Composer(R.string.rachmaninov),
            new Composer(R.string.chaikovsky),
            new Composer(R.string.mendelson),
            new Composer(R.string.bach),
            new Composer(R.string.musorgsky),
            new Composer(R.string.elgar),
            new Composer(R.string.leontovych),
            new Composer(R.string.bilash),
            new Composer(R.string.bellini),
            new Composer(R.string.lysenko),
            new Composer(R.string.khachaturian),
            new Composer(R.string.shostakovich),
            new Composer(R.string.chopin),
            new Composer(R.string.haydn),
            new Composer(R.string.list),
            new Composer(R.string.debussy),
            new Composer(R.string.orff),
            new Composer(R.string.ravel),
            new Composer(R.string.borodin),
            new Composer(R.string.rossini),
            new Composer(R.string.saint_saens),
            new Composer(R.string.wagner),
            new Composer(R.string.mozart),
            new Composer(R.string.strauss_i),
            new Composer(R.string.strauss_ii),
            new Composer(R.string.strauss_eduard),
            new Composer(R.string.vivaldi),
            new Composer(R.string.piazzolla),
            new Composer(R.string.bizet),
            new Composer(R.string.grieg),
            new Composer(R.string.offenbach),
            new Composer(R.string.boccherini),
            new Composer(R.string.ponchielli),
            new Composer(R.string.dukas),
            new Composer(R.string.barber),
            new Composer(R.string.rimsky_korsakov),
            new Composer(R.string.verdi),
            new Composer(R.string.brahms),
            new Composer(R.string.handel),
            new Composer(R.string.prokofiev),
            new Composer(R.string.puccini),
            new Composer(R.string.donizetti),
            new Composer(R.string.gounod)
    ));
}