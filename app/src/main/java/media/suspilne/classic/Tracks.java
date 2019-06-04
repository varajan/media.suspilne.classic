package media.suspilne.classic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Tracks {
    public int nowPlaying;
    public int lastPlaying;
    public long position;

    boolean showOnlyFavorite = SettingsHelper.getBoolean("showOnlyFavorite");
    boolean tracksPlayNext = SettingsHelper.getBoolean( "tracksPlayNext");

    public TrackEntry getNext(){
        List<TrackEntry> tracks = getTracks();
        boolean skip = true;

        for (int i = 0; i < tracks.size(); i++){
            if (tracks.get(i).id != nowPlaying && skip){
                continue;
            }

            if (tracks.get(i).id == nowPlaying) {
                skip = false;
                continue;
            }

            return tracks.get(i);
        }

        return tracks.size() > 0 ? tracks.get(0) : new TrackEntry();
    }

    public TrackEntry getById(int id){
        for (TrackEntry track:items) {
            if (track.id == id) return track;
        }

        return null;
    }

    public List<TrackEntry> getTracks(){
        List<TrackEntry> tracks = showOnlyFavorite ? getFavorite() : items;

        Collections.sort(tracks, new Comparator<TrackEntry>() {
            @Override
            public int compare(TrackEntry track1, TrackEntry track2) {
                return track1.getAuthor().equals(track2.getAuthor())
                        ? track1.getTitle().compareTo(track2.getTitle())
                        : track1.getAuthor().compareTo(track2.getAuthor());
            }
        });

        return tracks;
    }

    private List<TrackEntry> getFavorite(){
        List<TrackEntry> tracks = new ArrayList<>();

        for (TrackEntry track:items) {
            if (track.favorite) tracks.add(track);
        }

        return tracks;
    }

    private List<TrackEntry> items = new ArrayList<>(Arrays.asList(
            new TrackEntry(1, R.string.track_001, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
            new TrackEntry(2, R.string.track_002, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
            new TrackEntry(3, R.string.track_003, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
            new TrackEntry(4, R.string.track_004, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
            new TrackEntry(5, R.string.track_005, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
            new TrackEntry(6, R.string.track_006, R.string.sergey_rachmaninov, R.mipmap.sergei_rachmaninoff),
            new TrackEntry(7, R.string.track_007, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
            new TrackEntry(8, R.string.track_008, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
            new TrackEntry(9, R.string.track_009, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
            new TrackEntry(10, R.string.track_010, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
            new TrackEntry(11, R.string.track_011, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
            new TrackEntry(12, R.string.track_012, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
            new TrackEntry(13, R.string.track_013, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
            new TrackEntry(14, R.string.track_014, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
            new TrackEntry(15, R.string.track_015, R.string.ludvig_van_beethoven, R.mipmap.ludvig_van_beethoven),
            new TrackEntry(16, R.string.track_016, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
            new TrackEntry(17, R.string.track_017, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
            new TrackEntry(18, R.string.track_018, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
            new TrackEntry(19, R.string.track_019, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
            new TrackEntry(20, R.string.track_020, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
            new TrackEntry(21, R.string.track_021, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
            new TrackEntry(22, R.string.track_022, R.string.petro_chaikovsky, R.mipmap.petro_chaikovsky),
            new TrackEntry(23, R.string.track_023, R.string.medelson, R.mipmap.mendelson),
            new TrackEntry(24, R.string.track_024, R.string.carl_orff, R.mipmap.carl_orff),
            new TrackEntry(25, R.string.track_025, R.string.sebastian_bach, R.mipmap.bach),
            new TrackEntry(26, R.string.track_026, R.string.sebastian_bach, R.mipmap.bach),
            new TrackEntry(27, R.string.track_027, R.string.sebastian_bach, R.mipmap.bach),
            new TrackEntry(28, R.string.track_028, R.string.debussy, R.mipmap.debussy),
            new TrackEntry(29, R.string.track_029, R.string.ravel, R.mipmap.ravel),
            new TrackEntry(30, R.string.track_030, R.string.borodin, R.mipmap.borodin),
            new TrackEntry(31, R.string.track_031, R.string.ravel, R.mipmap.ravel),
            new TrackEntry(32, R.string.track_032, R.string.rossini, R.mipmap.rossini),
            new TrackEntry(33, R.string.track_033, R.string.rossini, R.mipmap.rossini),
            new TrackEntry(34, R.string.track_034, R.string.rossini, R.mipmap.rossini),
            new TrackEntry(35, R.string.track_035, R.string.rossini, R.mipmap.rossini),
            new TrackEntry(36, R.string.track_036, R.string.kamil, R.mipmap.kamil),
            new TrackEntry(37, R.string.track_037, R.string.wagner, R.mipmap.wagner),
            new TrackEntry(38, R.string.track_038, R.string.mozart, R.mipmap.mozart),
            new TrackEntry(39, R.string.track_039, R.string.strauss_i, R.mipmap.strauss_i),
            new TrackEntry(40, R.string.track_040, R.string.strauss_ii, R.mipmap.strauss_ii),
            new TrackEntry(41, R.string.track_041, R.string.straus_eduard, R.mipmap.eduard_strauss),
            new TrackEntry(42, R.string.track_042, R.string.strauss_ii, R.mipmap.strauss_ii),
            new TrackEntry(43, R.string.track_043, R.string.strauss_ii, R.mipmap.strauss_ii),
            new TrackEntry(44, R.string.track_044, R.string.vivaldi, R.mipmap.vivaldi),
            new TrackEntry(45, R.string.track_045, R.string.vivaldi, R.mipmap.vivaldi),
            new TrackEntry(46, R.string.track_046, R.string.vivaldi, R.mipmap.vivaldi),
            new TrackEntry(47, R.string.track_047, R.string.vivaldi, R.mipmap.vivaldi),
            new TrackEntry(48, R.string.track_048, R.string.mozart, R.mipmap.mozart),
            new TrackEntry(49, R.string.track_049, R.string.mozart, R.mipmap.mozart),
            new TrackEntry(50, R.string.track_050, R.string.mozart, R.mipmap.mozart),
            new TrackEntry(51, R.string.track_051, R.string.mozart, R.mipmap.mozart),
            new TrackEntry(52, R.string.track_052, R.string.astor, R.mipmap.astor),
            new TrackEntry(53, R.string.track_053, R.string.astor, R.mipmap.astor),
            new TrackEntry(54, R.string.track_054, R.string.astor, R.mipmap.astor),
            new TrackEntry(55, R.string.track_055, R.string.bizet, R.mipmap.bizet),
            new TrackEntry(56, R.string.track_056, R.string.bizet, R.mipmap.bizet),
            new TrackEntry(57, R.string.track_057, R.string.grieg, R.mipmap.grieg),
            new TrackEntry(58, R.string.track_058, R.string.grieg, R.mipmap.grieg),
            new TrackEntry(59, R.string.track_059, R.string.grieg, R.mipmap.grieg),
            new TrackEntry(60, R.string.track_060, R.string.grieg, R.mipmap.grieg)
    ));
}
