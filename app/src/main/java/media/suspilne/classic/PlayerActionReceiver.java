package media.suspilne.classic;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.exoplayer2.ui.PlayerNotificationManager.ACTION_NEXT;
import static com.google.android.exoplayer2.ui.PlayerNotificationManager.ACTION_PAUSE;
import static com.google.android.exoplayer2.ui.PlayerNotificationManager.ACTION_PLAY;
import static com.google.android.exoplayer2.ui.PlayerNotificationManager.ACTION_PREVIOUS;

public class PlayerActionReceiver implements PlayerNotificationManager.CustomActionReceiver {
    @Override
    public Map<String, NotificationCompat.Action> createCustomActions(Context context, int instanceId) {
        Log.e(SettingsHelper.application, "createCustomActions");

        Intent notificationIntent = new Intent(context, ActivityTracks.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

//        Intent playPrevIntent = new Intent();
//        playPrevIntent.setAction(SettingsHelper.application + "previous");
//        playPrevIntent.putExtra("code", "PlayPrevious");
//        PendingIntent playPrevPendingIntent = PendingIntent.getBroadcast(this, 0, playPrevIntent, 0);
//        notificationBuilder.addAction(0, getResources().getString(R.string.prev), playPrevPendingIntent);


        NotificationCompat.Action prev = new NotificationCompat.Action(
                R.drawable.ic_track,
                ACTION_PREVIOUS,
                pendingIntent
        );

        NotificationCompat.Action play = new NotificationCompat.Action(
                R.drawable.ic_notfavorite,
//                R.mipmap.track_play,
                ACTION_PLAY,
                pendingIntent
        );

        NotificationCompat.Action pause = new NotificationCompat.Action(
                R.drawable.ic_queue_music,
//                R.mipmap.track_pause,
                ACTION_PAUSE,
                pendingIntent
        );

        NotificationCompat.Action next = new NotificationCompat.Action(
                R.drawable.ic_search,
//                R.mipmap.track_next,
                ACTION_NEXT,
                pendingIntent
        );

        Map<String, NotificationCompat.Action> actionMap = new HashMap<>();
        actionMap.put(ACTION_PREVIOUS, prev);
        actionMap.put(ACTION_PLAY, play);
        actionMap.put(ACTION_PAUSE, pause);
        actionMap.put(ACTION_NEXT, next);
        return actionMap;
    }

    @Override
    public List<String> getCustomActions(Player player) {
        Log.e(SettingsHelper.application, "getCustomActions");

        List<String> customActions = new ArrayList<>();
        customActions.add(ACTION_PREVIOUS);

        if(player.getPlayWhenReady()) {
            customActions.add(ACTION_PAUSE);
        }else{
            customActions.add(ACTION_PLAY);
        }
        customActions.add(ACTION_NEXT);

        return customActions;
    }

    @Override
    public void onCustomAction(Player player, String action, Intent intent) {
        Log.e(SettingsHelper.application, "ACTION " + action);

        switch (action) {
            case  ACTION_PLAY :
                break;

            case  ACTION_PAUSE :
                break;

            case ACTION_NEXT :
                break;

            case ACTION_PREVIOUS :
                break;
        }
    }
}
