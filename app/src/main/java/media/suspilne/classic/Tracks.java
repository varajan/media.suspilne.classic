package media.suspilne.classic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Tracks {
    int nowPlaying;
    int lastPlaying;
    String filter = "";
    boolean showOnlyFavorite = SettingsHelper.getBoolean("showOnlyFavorite");

    TrackEntry getNext(){
        List<TrackEntry> tracks = getTracks();
        boolean skip = (nowPlaying > 0 && tracks.get(nowPlaying).isVisible());

        for (int i = 0; i < tracks.size(); i++){
            TrackEntry track = tracks.get(i);

            if (!track.isVisible()) { continue; }
            if (track.id != nowPlaying && skip) { continue; }
            if (track.id == nowPlaying) { skip = false; continue; }

            return tracks.get(i);
        }

        return new TrackEntry();
    }

    TrackEntry getById(int id){
        for (TrackEntry track:getTracks()) {
            if (track.id == id) return track;
        }

        return null;
    }

    List<TrackEntry> getTracks(boolean onlyFavorite){
        List<TrackEntry> result = new ArrayList<>();

        for (TrackEntry track:items) {
            if (!onlyFavorite || track.isFavorite) result.add(track);
        }

        Collections.sort(result, (track1, track2)
                -> track1.getAuthor().equals(track2.getAuthor())
                ?  track1.getTitle().compareTo(track2.getTitle())
                :  track1.getAuthor().compareTo(track2.getAuthor()));

        return result;
    }

    List<TrackEntry> getTracks(){
        return getTracks(false);
    }

    private List<TrackEntry> items = new ArrayList<>(Arrays.asList(
            new TrackEntry(1, R.string.track_001, R.string.rachmaninov),
            new TrackEntry(2, R.string.track_002, R.string.rachmaninov),
            new TrackEntry(3, R.string.track_003, R.string.rachmaninov),
            new TrackEntry(4, R.string.track_004, R.string.rachmaninov),
            new TrackEntry(5, R.string.track_005, R.string.rachmaninov),
            new TrackEntry(6, R.string.track_006, R.string.beethoven),
            new TrackEntry(7, R.string.track_007, R.string.beethoven),
            new TrackEntry(8, R.string.track_008, R.string.beethoven),
            new TrackEntry(9, R.string.track_009, R.string.beethoven),
            new TrackEntry(10, R.string.track_010, R.string.beethoven),
            new TrackEntry(11, R.string.track_011, R.string.beethoven),
            new TrackEntry(12, R.string.track_012, R.string.beethoven),
            new TrackEntry(13, R.string.track_013, R.string.beethoven),
            new TrackEntry(14, R.string.track_014, R.string.beethoven),
            new TrackEntry(15, R.string.track_015, R.string.beethoven),
            new TrackEntry(16, R.string.track_016, R.string.chaikovsky),
            new TrackEntry(17, R.string.track_017, R.string.chaikovsky),
            new TrackEntry(18, R.string.track_018, R.string.chaikovsky),
            new TrackEntry(19, R.string.track_019, R.string.chaikovsky),
            new TrackEntry(20, R.string.track_020, R.string.chaikovsky),
            new TrackEntry(21, R.string.track_021, R.string.chaikovsky),
            new TrackEntry(22, R.string.track_022, R.string.mendelson),
            new TrackEntry(23, R.string.track_023, R.string.mendelson),
            new TrackEntry(24, R.string.track_024, R.string.orff),
            new TrackEntry(25, R.string.track_025, R.string.bach),
            new TrackEntry(26, R.string.track_026, R.string.bach),
            new TrackEntry(27, R.string.track_027, R.string.bach),
            new TrackEntry(28, R.string.track_028, R.string.debussy),
            new TrackEntry(29, R.string.track_029, R.string.ravel),
            new TrackEntry(30, R.string.track_030, R.string.borodin),
            new TrackEntry(31, R.string.track_031, R.string.ravel),
            new TrackEntry(32, R.string.track_032, R.string.rossini),
            new TrackEntry(33, R.string.track_033, R.string.rossini),
            new TrackEntry(34, R.string.track_034, R.string.rossini),
            new TrackEntry(35, R.string.track_035, R.string.rossini),
            new TrackEntry(36, R.string.track_036, R.string.saint_saens),
            new TrackEntry(37, R.string.track_037, R.string.wagner),
            new TrackEntry(38, R.string.track_038, R.string.mozart),
            new TrackEntry(39, R.string.track_039, R.string.strauss_i),
            new TrackEntry(40, R.string.track_040, R.string.strauss_ii),
            new TrackEntry(41, R.string.track_041, R.string.strauss_ii),
            new TrackEntry(42, R.string.track_042, R.string.strauss_eduard),
            new TrackEntry(43, R.string.track_043, R.string.strauss_ii),
            new TrackEntry(44, R.string.track_044, R.string.vivaldi),
            new TrackEntry(45, R.string.track_045, R.string.vivaldi),
            new TrackEntry(46, R.string.track_046, R.string.vivaldi),
            new TrackEntry(47, R.string.track_047, R.string.vivaldi),
            new TrackEntry(48, R.string.track_048, R.string.mozart),
            new TrackEntry(49, R.string.track_049, R.string.mozart),
            new TrackEntry(50, R.string.track_050, R.string.mozart),
            new TrackEntry(51, R.string.track_051, R.string.mozart),
            new TrackEntry(52, R.string.track_052, R.string.piazzolla),
            new TrackEntry(53, R.string.track_053, R.string.piazzolla),
            new TrackEntry(54, R.string.track_054, R.string.piazzolla),
            new TrackEntry(55, R.string.track_055, R.string.bizet),
            new TrackEntry(56, R.string.track_056, R.string.bizet),
            new TrackEntry(57, R.string.track_057, R.string.grieg),
            new TrackEntry(58, R.string.track_058, R.string.grieg),
            new TrackEntry(59, R.string.track_059, R.string.grieg),
            new TrackEntry(60, R.string.track_060, R.string.grieg),
            new TrackEntry(61, R.string.track_061, R.string.khachaturian),
            new TrackEntry(62, R.string.track_062, R.string.offenbach),
            new TrackEntry(63, R.string.track_063, R.string.offenbach),
            new TrackEntry(64, R.string.track_064, R.string.boccherini),
            new TrackEntry(65, R.string.track_065, R.string.ponchielli),
            new TrackEntry(66, R.string.track_066, R.string.debussy),
            new TrackEntry(67, R.string.track_067, R.string.musorgsky),
            new TrackEntry(68, R.string.track_068, R.string.dukas),
            new TrackEntry(69, R.string.track_069, R.string.barber),
            new TrackEntry(70, R.string.track_070, R.string.rimsky_korsakov),
            new TrackEntry(71, R.string.track_071, R.string.verdi),
            new TrackEntry(72, R.string.track_072, R.string.saint_saens),
            new TrackEntry(73, R.string.track_073, R.string.wagner),
            new TrackEntry(74, R.string.track_074, R.string.list),
            new TrackEntry(75, R.string.track_075, R.string.brahms),
            new TrackEntry(76, R.string.track_076, R.string.brahms),
            new TrackEntry(77, R.string.track_077, R.string.handel),
            new TrackEntry(78, R.string.track_078, R.string.prokofiev),
            new TrackEntry(79, R.string.track_079, R.string.prokofiev),
            new TrackEntry(80, R.string.track_080, R.string.puccini),
            new TrackEntry(83, R.string.track_083, R.string.verdi),
            new TrackEntry(85, R.string.track_085, R.string.rossini),
            new TrackEntry(86, R.string.track_086, R.string.puccini),
            new TrackEntry(87, R.string.track_087, R.string.puccini),
            new TrackEntry(88, R.string.track_088, R.string.puccini),
            new TrackEntry(89, R.string.track_089, R.string.verdi),
            new TrackEntry(90, R.string.track_090, R.string.verdi),
            new TrackEntry(91, R.string.track_091, R.string.donizetti),
            new TrackEntry(92, R.string.track_092, R.string.gounod),
            new TrackEntry(93, R.string.track_093, R.string.list),
            new TrackEntry(94, R.string.track_094, R.string.list),
            new TrackEntry(95, R.string.track_095, R.string.list),
            new TrackEntry(96, R.string.track_096, R.string.haydn),
            new TrackEntry(97, R.string.track_097, R.string.haydn),
            new TrackEntry(98, R.string.track_098, R.string.chopin),
            new TrackEntry(99, R.string.track_099, R.string.chopin),
            new TrackEntry(100, R.string.track_100, R.string.chopin),
            new TrackEntry(101, R.string.track_101, R.string.chopin),
            new TrackEntry(102, R.string.track_102, R.string.chopin),
            new TrackEntry(103, R.string.track_103, R.string.chopin),
            new TrackEntry(104, R.string.track_104, R.string.shostakovich),
            new TrackEntry(105, R.string.track_105, R.string.khachaturian),
            new TrackEntry(106, R.string.track_106, R.string.lysenko),
            new TrackEntry(107, R.string.track_107, R.string.chaikovsky),
            new TrackEntry(108, R.string.track_108, R.string.chaikovsky),
            new TrackEntry(109, R.string.track_109, R.string.chaikovsky),
            new TrackEntry(110, R.string.track_110, R.string.chaikovsky),
            new TrackEntry(111, R.string.track_111, R.string.musorgsky),
            new TrackEntry(112, R.string.track_112, R.string.musorgsky),
            new TrackEntry(113, R.string.track_113, R.string.musorgsky),
            new TrackEntry(114, R.string.track_114, R.string.musorgsky),
            new TrackEntry(115, R.string.track_115, R.string.lysenko),
            new TrackEntry(116, R.string.track_116, R.string.bellini),
            new TrackEntry(118, R.string.track_118, R.string.bilash),
            new TrackEntry(119, R.string.track_119, R.string.leontovych),
            new TrackEntry(120, R.string.track_120, R.string.leontovych),
            new TrackEntry(121, R.string.track_121, R.string.leontovych),
            new TrackEntry(122, R.string.track_122, R.string.leontovych),
            new TrackEntry(123, R.string.track_123, R.string.leontovych),
            new TrackEntry(124, R.string.track_124, R.string.elgar),
            new TrackEntry(125, R.string.track_125, R.string.chaikovsky)
    ));
}