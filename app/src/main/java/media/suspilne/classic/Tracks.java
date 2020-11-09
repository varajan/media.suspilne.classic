package media.suspilne.classic;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class Tracks {
    String filter = SettingsHelper.getString("tracksFilter");
    boolean showOnlyFavorite = SettingsHelper.getBoolean("showOnlyFavorite");

    public static void setLastPosition(long value){
        SettingsHelper.setLong("PlayerPosition", value);
    }

    public static long getLastPosition(){
        return SettingsHelper.getLong("PlayerPosition");
    }

    public static void setLastPlaying(int value) {
        SettingsHelper.setInt("tracks.lastPlaying", value);
    }

    public static int getLastPlaying(){
        return SettingsHelper.getInt("tracks.lastPlaying");
    }

    public static void setNowPlaying(int value){
        SettingsHelper.setInt("tracks.nowPlaying", value);
    }

    public static int getNowPlaying(){
        return SettingsHelper.getInt("tracks.nowPlaying");
    }

    public static void setPause(boolean value){
        SettingsHelper.setBoolean("tracks.paused", value);
    }

    public static boolean isPaused(){
        return SettingsHelper.getBoolean("tracks.paused");
    }

    TrackEntry getPrevious(){
        boolean skip = true;
        int nowPlaying = getNowPlaying();
        List<String> ids = Arrays.asList( SettingsHelper.getString("filteredTracksList").split(";") );
        Collections.reverse(ids);

        for(String id:ids){
            int trackId = Integer.parseInt(id);

            if (trackId != nowPlaying && skip) continue;
            if (trackId == nowPlaying) {skip = false; continue;}

            return getById(trackId);
        }

        return ids.size() == 0 ? new TrackEntry() : getById(ids.get(0));
    }

    TrackEntry getNext(){
        boolean skip = true;
        int nowPlaying = getNowPlaying();
        List<String> ids = ListHelper.removeBlank(SettingsHelper.getString("filteredTracksList").split(";"));

        for(String id:ids){
            int trackId = Integer.parseInt(id);

            if (trackId != nowPlaying && skip) continue;
            if (trackId == nowPlaying) {skip = false; continue;}

            return getById(trackId);
        }

        return ids.size() == 0 ? new TrackEntry() : getById(ids.get(0));
    }

    TrackEntry getById(int id){
        for (TrackEntry track:items) {
            if (track.id == id) return track;
        }

        return null;
    }

    TrackEntry getById(String id) {
        return getById(Integer.parseInt(id));
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

    int compare(String arg1, String arg2) {
        Collator collator = Collator.getInstance(new Locale(LocaleManager.getLanguage()));
        collator.setStrength(Collator.PRIMARY);

        return collator.compare(removeQuotes(arg1), removeQuotes(arg2));
    }

    String removeQuotes(String string)
    {
        return string
                .replace("\"", "")
                .replace("«", "")
                .replace("»", "")
                .replace(",", "")
                .replace(".", "")
                .replace("№", "");
    }

    public void setTracksList(){
        String sorting = SettingsHelper.getString("sorting", "shuffle");
        StringBuilder list = new StringBuilder();
        List<TrackEntry> result = new ArrayList<>(items);

        switch (sorting){
            case "shuffle":
                Collections.shuffle(result);
                break;

            case "sortAsc":
                Collections.shuffle(result);
                if (SettingsHelper.getBoolean("groupByAuthor")) {
                    Collections.sort(result, (track1, track2)
                            -> track1.getAuthor().equals(track2.getAuthor())
                            ?  compare(track1.getTitle(), track2.getTitle())
                            :  compare(track1.getAuthor(), track2.getAuthor()));
                } else {
                    Collections.sort(result, (track1, track2) -> compare(track1.getTitle(), track2.getTitle()));
                }
                break;

            case "sort19":
                Collections.sort(result, (track1, track2) -> compare(track1.duration, track2.duration));
                break;

            case "sort91":
                Collections.sort(result, (track1, track2) -> compare(track1.duration, track2.duration));
                Collections.reverse(result);
                break;
        }

        for (TrackEntry track:result) { list.append(track.id).append(";"); }

        SettingsHelper.setString("tracksList", list.toString());
    }

    public List<TrackEntry> getTracksList(){
        List<TrackEntry> result = new ArrayList<>();

        if (SettingsHelper.getString("tracksList", "").length() == 0) setTracksList();

        for(String id:SettingsHelper.getString("tracksList").split(";")){
            result.add(getById(id));
        }

        return result;
    }

    List<TrackEntry> getTracks(){
        return getTracks(false);
    }

    private List<TrackEntry> items = new ArrayList<>(Arrays.asList(
            new TrackEntry(1, "⏱ 11:11", R.string.track_001, R.string.rachmaninov),
            new TrackEntry(2, "⏱ 05:29", R.string.track_002, R.string.rachmaninov),
            new TrackEntry(3, "⏱ 08:12", R.string.track_003, R.string.rachmaninov),
            new TrackEntry(4, "⏱ 03:40", R.string.track_004, R.string.rachmaninov),
            new TrackEntry(5, "⏱ 05:41", R.string.track_005, R.string.rachmaninov),
            new TrackEntry(6, "⏱ 09:37", R.string.track_006, R.string.beethoven),
            new TrackEntry(7, "⏱ 02:56", R.string.track_007, R.string.beethoven),
            new TrackEntry(8, "⏱ 05:55", R.string.track_008, R.string.beethoven),
            new TrackEntry(9, "⏱ 04:38", R.string.track_009, R.string.beethoven),
            new TrackEntry(10, "⏱ 10:24", R.string.track_010, R.string.beethoven),
            new TrackEntry(11, "⏱ 07:15", R.string.track_011, R.string.beethoven),
            new TrackEntry(12, "⏱ 06:09", R.string.track_012, R.string.beethoven),
            new TrackEntry(13, "⏱ 08:17", R.string.track_013, R.string.beethoven),
            new TrackEntry(14, "⏱ 02:03", R.string.track_014, R.string.beethoven),
            new TrackEntry(15, "⏱ 07:17", R.string.track_015, R.string.beethoven),
            new TrackEntry(16, "⏱ 05:11", R.string.track_016, R.string.chaikovsky),
            new TrackEntry(17, "⏱ 04:48", R.string.track_017, R.string.chaikovsky),
            new TrackEntry(18, "⏱ 06:54", R.string.track_018, R.string.chaikovsky),
            new TrackEntry(19, "⏱ 22:02", R.string.track_019, R.string.chaikovsky),
            new TrackEntry(20, "⏱ 06:18", R.string.track_020, R.string.chaikovsky),
            new TrackEntry(21, "⏱ 01:42", R.string.track_021, R.string.chaikovsky),
            new TrackEntry(22, "⏱ 12:31", R.string.track_022, R.string.mendelson),
            new TrackEntry(23, "⏱ 04:52", R.string.track_023, R.string.mendelson),
            new TrackEntry(24, "⏱ 02:33", R.string.track_024, R.string.orff),
            new TrackEntry(25, "⏱ 08:49", R.string.track_025, R.string.bach),
            new TrackEntry(26, "⏱ 05:43", R.string.track_026, R.string.bach),
            new TrackEntry(27, "⏱ 06:03", R.string.track_027, R.string.bach),
            new TrackEntry(28, "⏱ 08:56", R.string.track_028, R.string.debussy),
            new TrackEntry(29, "⏱ 14:53", R.string.track_029, R.string.ravel),
            new TrackEntry(30, "⏱ 11:19", R.string.track_030, R.string.borodin),
            new TrackEntry(31, "⏱ 09:17", R.string.track_031, R.string.ravel),
            new TrackEntry(32, "⏱ 07:15", R.string.track_032, R.string.rossini),
            new TrackEntry(33, "⏱ 11:54", R.string.track_033, R.string.rossini),
            new TrackEntry(34, "⏱ 10:51", R.string.track_034, R.string.rossini),
            new TrackEntry(35, "⏱ 08:37", R.string.track_035, R.string.rossini),
            new TrackEntry(36, "⏱ 09:18", R.string.track_036, R.string.saint_saens),
            new TrackEntry(37, "⏱ 10:59", R.string.track_037, R.string.wagner),
            new TrackEntry(38, "⏱ 04:08", R.string.track_038, R.string.mozart),
            new TrackEntry(39, "⏱ 02:36", R.string.track_039, R.string.strauss_i),
            new TrackEntry(40, "⏱ 02:37", R.string.track_040, R.string.strauss_ii),
            new TrackEntry(41, "⏱ 03:30", R.string.track_041, R.string.strauss_ii),
            new TrackEntry(42, "⏱ 03:10", R.string.track_042, R.string.strauss_eduard),
            new TrackEntry(43, "⏱ 10:11", R.string.track_043, R.string.strauss_ii),
            new TrackEntry(44, "⏱ 12:27", R.string.track_044, R.string.vivaldi),
            new TrackEntry(45, "⏱ 11:25", R.string.track_045, R.string.vivaldi),
            new TrackEntry(46, "⏱ 11:26", R.string.track_046, R.string.vivaldi),
            new TrackEntry(47, "⏱ 09:55", R.string.track_047, R.string.vivaldi),
            new TrackEntry(48, "⏱ 05:23", R.string.track_048, R.string.mozart),
            new TrackEntry(49, "⏱ 04:59", R.string.track_049, R.string.mozart),
            new TrackEntry(50, "⏱ 06:48", R.string.track_050, R.string.mozart),
            new TrackEntry(51, "⏱ 06:21", R.string.track_051, R.string.mozart),
            new TrackEntry(52, "⏱ 04:56", R.string.track_052, R.string.piazzolla),
            new TrackEntry(53, "⏱ 04:39", R.string.track_053, R.string.piazzolla),
            new TrackEntry(54, "⏱ 06:14", R.string.track_054, R.string.piazzolla),
            new TrackEntry(55, "⏱ 02:25", R.string.track_055, R.string.bizet),
            new TrackEntry(56, "⏱ 04:11", R.string.track_056, R.string.bizet),
            new TrackEntry(57, "⏱ 12:38", R.string.track_057, R.string.grieg),
            new TrackEntry(58, "⏱ 04:37", R.string.track_058, R.string.grieg),
            new TrackEntry(59, "⏱ 03:34", R.string.track_059, R.string.grieg),
            new TrackEntry(60, "⏱ 02:13", R.string.track_060, R.string.grieg),
            new TrackEntry(61, "⏱ 02:10", R.string.track_061, R.string.khachaturian),
            new TrackEntry(62, "⏱ 08:51", R.string.track_062, R.string.offenbach),
            new TrackEntry(63, "⏱ 03:02", R.string.track_063, R.string.offenbach),
            new TrackEntry(64, "⏱ 03:30", R.string.track_064, R.string.boccherini),
            new TrackEntry(65, "⏱ 09:29", R.string.track_065, R.string.ponchielli),
            new TrackEntry(66, "⏱ 04:30", R.string.track_066, R.string.debussy),
            new TrackEntry(67, "⏱ 11:26", R.string.track_067, R.string.musorgsky),
            new TrackEntry(68, "⏱ 12:05", R.string.track_068, R.string.dukas),
            new TrackEntry(69, "⏱ 07:23", R.string.track_069, R.string.barber),
            new TrackEntry(70, "⏱ 01:27", R.string.track_070, R.string.rimsky_korsakov),
            new TrackEntry(71, "⏱ 08:20", R.string.track_071, R.string.verdi),
            new TrackEntry(72, "⏱ 03:23", R.string.track_072, R.string.saint_saens),
            new TrackEntry(73, "⏱ 03:41", R.string.track_073, R.string.wagner),
            new TrackEntry(74, "⏱ 04:12", R.string.track_074, R.string.list),
            new TrackEntry(75, "⏱ 03:18", R.string.track_075, R.string.brahms),
            new TrackEntry(76, "⏱ 02:50", R.string.track_076, R.string.brahms),
            new TrackEntry(77, "⏱ 03:36", R.string.track_077, R.string.handel),
            new TrackEntry(78, "⏱ 03:59", R.string.track_078, R.string.prokofiev),
            new TrackEntry(79, "⏱ 04:06", R.string.track_079, R.string.prokofiev),
            new TrackEntry(80, "⏱ 03:33", R.string.track_080, R.string.puccini),

            new TrackEntry(83, "⏱ 02:57", R.string.track_083, R.string.verdi),

            new TrackEntry(85, "⏱ 04:52", R.string.track_085, R.string.rossini),
            new TrackEntry(86, "⏱ 03:12", R.string.track_086, R.string.puccini),
            new TrackEntry(87, "⏱ 04:49", R.string.track_087, R.string.puccini),
            new TrackEntry(88, "⏱ 03:04", R.string.track_088, R.string.puccini),
            new TrackEntry(89, "⏱ 03:45", R.string.track_089, R.string.verdi),
            new TrackEntry(90, "⏱ 02:23", R.string.track_090, R.string.verdi),
            new TrackEntry(91, "⏱ 04:46", R.string.track_091, R.string.donizetti),
            new TrackEntry(92, "⏱ 02:17", R.string.track_092, R.string.gounod),
            new TrackEntry(93, "⏱ 05:34", R.string.track_093, R.string.list),
            new TrackEntry(94, "⏱ 04:50", R.string.track_094, R.string.list),
            new TrackEntry(95, "⏱ 04:17", R.string.track_095, R.string.list),
            new TrackEntry(96, "⏱ 07:16", R.string.track_096, R.string.haydn),
            new TrackEntry(97, "⏱ 05:01", R.string.track_097, R.string.haydn),
            new TrackEntry(98, "⏱ 03:27", R.string.track_098, R.string.chopin),
            new TrackEntry(99, "⏱ 01:43", R.string.track_099, R.string.chopin),
            new TrackEntry(100, "⏱ 04:26", R.string.track_100, R.string.chopin),
            new TrackEntry(101, "⏱ 04:33", R.string.track_101, R.string.chopin),
            new TrackEntry(102, "⏱ 07:49", R.string.track_102, R.string.chopin),
            new TrackEntry(103, "⏱ 02:40", R.string.track_103, R.string.chopin),
            new TrackEntry(104, "⏱ 04:00", R.string.track_104, R.string.shostakovich),
            new TrackEntry(105, "⏱ 03:56", R.string.track_105, R.string.khachaturian),
            new TrackEntry(106, "⏱ 05:13", R.string.track_106, R.string.lysenko),
            new TrackEntry(107, "⏱ 02:25", R.string.track_107, R.string.chaikovsky),
            new TrackEntry(108, "⏱ 02:36", R.string.track_108, R.string.chaikovsky),
            new TrackEntry(109, "⏱ 04:55", R.string.track_109, R.string.chaikovsky),
            new TrackEntry(110, "⏱ 02:55", R.string.track_110, R.string.chaikovsky),
            new TrackEntry(111, "⏱ 01:33", R.string.track_111, R.string.musorgsky),
            new TrackEntry(112, "⏱ 02:41", R.string.track_112, R.string.musorgsky),
            new TrackEntry(113, "⏱ 03:30", R.string.track_113, R.string.musorgsky),
            new TrackEntry(114, "⏱ 05:12", R.string.track_114, R.string.musorgsky),
            new TrackEntry(115, "⏱ 02:37", R.string.track_115, R.string.lysenko),
            new TrackEntry(116, "⏱ 06:00", R.string.track_116, R.string.bellini),

            new TrackEntry(118, "⏱ 03:46", R.string.track_118, R.string.bilash),
            new TrackEntry(119, "⏱ 02:00", R.string.track_119, R.string.leontovych),
            new TrackEntry(120, "⏱ 01:45", R.string.track_120, R.string.leontovych),
            new TrackEntry(121, "⏱ 03:07", R.string.track_121, R.string.leontovych),
            new TrackEntry(122, "⏱ 04:32", R.string.track_122, R.string.leontovych),
            new TrackEntry(123, "⏱ 01:36", R.string.track_123, R.string.leontovych),
            new TrackEntry(124, "⏱ 03:28", R.string.track_124, R.string.elgar),
            new TrackEntry(125, "⏱ 02:30", R.string.track_125, R.string.chaikovsky)
    ));
}