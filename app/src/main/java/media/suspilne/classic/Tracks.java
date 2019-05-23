package media.suspilne.classic;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Tracks extends MainActivity {
    int nowPlaying;
    int lastPlaying;
    long position;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player.isPlaying())
            player.releasePlayer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("nowPlaying", player.isPlaying() ? nowPlaying : -1);
        outState.putInt("lastPlaying", lastPlaying);
        outState.putLong("position", player.isPlaying() ? player.position() : position);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        nowPlaying = savedInstanceState.getInt("nowPlaying");
        lastPlaying = savedInstanceState.getInt("lastPlaying");
        position = savedInstanceState.getLong("position");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_tracks);
        currentView = R.id.tracks_menu;
        super.onCreate(savedInstanceState);

        if (!isNetworkAvailable()){
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }

        new GetTracks().execute("https://classical.suspilne.media/list");
    }

    class GetTracks extends AsyncTask<String, Void, ArrayList<Integer>> {
        @Override
        protected ArrayList<Integer> doInBackground(String... arg) {
            ArrayList<Integer> result = new ArrayList<>();

            try {
                Document document = Jsoup.connect(arg[0]).get();
                Elements Tracks = document.select("div.tales-list a");
                for (Element tale : Tracks) {
                    String href = tale.attr("href");
                    String id = href.split("\\?")[0].split("/")[2];
                    result.add(Integer.valueOf(id));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            result = ListHelper.union(result, SettingsHelper.getSavedTaleIds(Tracks.this));
            Collections.sort(result);

            return result;
        }

        @Override
        protected void onPostExecute(final ArrayList<Integer> ids) {
            super.onPostExecute(ids);
            final LinearLayout list = findViewById(R.id.list);

            for (final int id:ids) {
                View item = LayoutInflater.from(Tracks.this).inflate(R.layout.track_item, list, false);
                item.setTag(id);
                list.addView(item);

                new SetTaleTitle().execute(id);

                final ImageView playBtn = item.findViewById(R.id.play);

                playBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (player.isPlaying() && playBtn.getTag().equals(R.mipmap.track_pause)){
                            position = player.position();
                            lastPlaying = id;

                            player.releasePlayer();
                            playBtn.setImageResource(R.mipmap.track_play);
                            playBtn.setTag(R.mipmap.track_play);
                        }else{
                            playTale(ids, id);
                            Tracks.this.setQuiteTimeout();
                        }
                    }
                });

                if (nowPlaying == id){
                    playBtn.setImageResource(R.mipmap.track_pause);
                    playBtn.setTag(R.mipmap.track_pause);

                    player.initializePlayer("https://classical.suspilne.media/inc/audio/" + String.format("%02d", nowPlaying) + ".mp3");
                    player.setPosition(position);

                    Tracks.this.setQuiteTimeout();
                }
            }

            player.addListener(new Player.MediaIsEndedListener(){
                @Override
                public void mediaIsEnded(){
                    if (SettingsHelper.getBoolean(Tracks.this, "TracksPlayNext")){
                        int next = ids.get(0);

                        for(int i:ids){
                            if (i > nowPlaying) {
                                next = i;
                                break;
                            }
                        }

                        playTale(ids, next);
                    }else{
                        nowPlaying = -1;
                        setPlayBtnIcon(ids, -1);
                    }
                }
            });

            player.addListener(new Player.SourceIsNotAccessibleListener(){
                @Override
                public void sourceIsNotAccessible(){
                    nowPlaying = -1;
                    setPlayBtnIcon(ids, -1);
                    player.releasePlayer();

                    Toast.makeText(Tracks.this, R.string.no_internet, Toast.LENGTH_LONG).show();
                }
            });
        }

        private void playTale(ArrayList<Integer> ids, int playId){
            player.releasePlayer();
            player.initializePlayer("https://classical.suspilne.media/inc/audio/" + String.format("%02d", playId) + ".mp3");
            if (playId == lastPlaying){
                player.setPosition(position);
            }
            setPlayBtnIcon(ids, playId);
            nowPlaying = playId;
            lastPlaying = playId;
        }

        private void setPlayBtnIcon(ArrayList<Integer> ids, int id){
            LinearLayout list = findViewById(R.id.list);

            for (int x:ids){
                ImageView btn = list.findViewWithTag(x).findViewById(R.id.play);
                btn.setImageResource(x == id ? R.mipmap.track_pause : R.mipmap.track_play);
                btn.setTag(x == id ? R.mipmap.track_pause : R.mipmap.track_play);
            }
        }
    }

    class SetTaleTitle extends AsyncTask<Integer, Void, String[]> {
        private int id;

        @Override
        protected String[] doInBackground(Integer... arg) {
            try {
                id = arg[0];
                String title = SettingsHelper.getString(Tracks.this, "title-" + id);
                String reader = SettingsHelper.getString(Tracks.this, "reader-" + id);

                if (title.equals("") || reader.equals("")){
                    Document document = Jsoup.connect("https://classical.suspilne.media/list").get();
                    title = document.select("div.tales-list a[href*='/" + id + "?'] div[class$='caption']").text().trim();
                    reader = document.select("div.tales-list a[href*='/" + id + "?'] div[class$='tale-time']").text().trim();

                    SettingsHelper.setString(Tracks.this, "title-" + id, title);
                    SettingsHelper.setString(Tracks.this, "reader-" + id, reader);
                }

                return new String[] {title, reader};
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] titles) {
            super.onPostExecute(titles);

            if (titles == null){
                Toast.makeText(Tracks.this, R.string.no_internet, Toast.LENGTH_LONG).show();
                return;
            }

            View item = findViewById(R.id.list).findViewWithTag(id);
            TextView title = item.findViewById(R.id.title);
            TextView reader = item.findViewById(R.id.reader);
            ImageView preview = item.findViewById(R.id.preview);
            int margin = ((ConstraintLayout.LayoutParams)preview.getLayoutParams()).leftMargin;
            int imageWidth = preview.getWidth();
            int maxWidth =  item.getWidth() - imageWidth - 3 * margin;

            title.setText(titles[0]);
            reader.setText(titles[1]);

            title.setWidth(maxWidth);
            reader.setWidth(maxWidth);
        }
    }
}