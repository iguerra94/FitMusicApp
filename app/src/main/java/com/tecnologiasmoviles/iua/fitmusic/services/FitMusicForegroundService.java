package com.tecnologiasmoviles.iua.fitmusic.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.utils.Constants;
import com.tecnologiasmoviles.iua.fitmusic.utils.MediaPlayerManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class FitMusicForegroundService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE";
    private final static String FOREGROUND_CHANNEL_ID = "foreground_channel_id";

    private NotificationManager mNotificationManager;
    static private int mStateService = Constants.STATE_SERVICE.NOT_INIT;

    private static boolean supportSmallNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public FitMusicForegroundService() {
    }

    public static int getState() {
        return mStateService;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStateService = Constants.STATE_SERVICE.NOT_INIT;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d(TAG_FOREGROUND_SERVICE, "My foreground service onCreate().");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopForegroundService();
            return START_NOT_STICKY;
        }

        assert intent.getAction() != null;

        switch (intent.getAction()) {
            case Constants.ACTION_START_FOREGROUND_SERVICE:
                mStateService = Constants.STATE_SERVICE.STARTED;
                startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
                break;

            case Constants.ACTION_STOP_FOREGROUND_SERVICE:
                stopForegroundService();
                break;

            case Constants.ACTION.ACTION_PLAY:
                mStateService = Constants.STATE_SERVICE.PLAYING;
                mNotificationManager.notify(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
                break;

            case Constants.ACTION.ACTION_PAUSE:
                mStateService = Constants.STATE_SERVICE.PAUSED;
                mNotificationManager.notify(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
                break;

            case Constants.ACTION.ACTION_STEP_BACKWARD:
                mStateService = Constants.STATE_SERVICE.STEP_TO_PREV_SONG;
                mNotificationManager.notify(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
                break;

            case Constants.ACTION.ACTION_STEP_FORWARD:
                mStateService = Constants.STATE_SERVICE.STEP_TO_NEXT_SONG;
                mNotificationManager.notify(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
                break;

            default:
                stopForegroundService();
        }
        return START_NOT_STICKY;
    }

    public static void startForegroundServiceIntent(Context context) {
        Intent intent = new Intent(context, FitMusicForegroundService.class);
        intent.setAction(Constants.ACTION_START_FOREGROUND_SERVICE);
        context.startService(intent);
    }

    public static void stopForegroundServiceIntent(Context context) {
        Intent intent = new Intent(context, FitMusicForegroundService.class);
        intent.setAction(Constants.ACTION_STOP_FOREGROUND_SERVICE);
        context.startService(intent);
    }

    private void stopForegroundService() {
        Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager mNotificationManager){
        // The user-visible name of the channel.
        CharSequence name = getString(R.string.default_notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance);

        mChannel.setDescription(getString(R.string.default_notification_channel_description));
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);

        mChannel.setVibrationPattern(new long[]{100,200,300,400,500,400,300,200,400});

        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @SuppressLint("WrongConstant")
    private Notification prepareNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
            setupChannels(mNotificationManager);
        }

        NotificationCompat.Builder mBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID);
        } else {
            mBuilder = new NotificationCompat.Builder(this);
        }

        mBuilder
            .setSmallIcon(R.drawable.ic_stat_fit_music_icon_round)
            .setColor(getResources().getColor(R.color.colorPrimary))
            .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true);


        RemoteViews normalLayout = new RemoteViews(getPackageName(), R.layout.notification_normal_layout);

        switch (mStateService) {
            case Constants.STATE_SERVICE.STARTED:
                updateCurrentSongNotificationUI();
                break;

            case Constants.STATE_SERVICE.PAUSED:
                SharedPrefsManager.getInstance(this).saveBoolean(SharedPrefsKeys.MEDIA_PLAYER_PLAYING_KEY, false);
                updateCurrentSongNotificationUI();
                break;

            case Constants.STATE_SERVICE.PLAYING:
                SharedPrefsManager.getInstance(this).saveBoolean(SharedPrefsKeys.MEDIA_PLAYER_PLAYING_KEY, true);
                break;

            case Constants.STATE_SERVICE.STEP_TO_PREV_SONG:
                break;

            case Constants.STATE_SERVICE.STEP_TO_NEXT_SONG:
                break;

        }

        // Assign remoteview to notification
        if (supportSmallNotifications) {
            mBuilder.setCustomContentView(normalLayout);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return mBuilder.build();
    }

    private void updateCurrentSongNotificationUI() {
//        int id = SharedPrefsManager.getInstance(this).readInt(SharedPrefsKeys.ID_SONG_KEY);
//
//        if (id == -1) {
//            remoteViews.setImageViewUri(R.id.notificationSongCover, Uri.parse(songList.get(0).getSongCoverUri()));
//            remoteViews.setTextViewText(R.id.notificationSongTitle, songList.get(0).getSongTitle());
//            remoteViews.setTextViewText(R.id.notificationSongArtist, songList.get(0).getArtist());
//        } else {
//            remoteViews.setImageViewUri(R.id.notificationSongCover, Uri.parse(songList.get(id - 1).getSongCoverUri()));
//            remoteViews.setTextViewText(R.id.notificationSongTitle, songList.get(id - 1).getSongTitle());
//            remoteViews.setTextViewText(R.id.notificationSongArtist, songList.get(id - 1).getArtist());
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPrefsManager.getInstance(this).getSharedPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPrefsKeys.ID_SONG_KEY)) {
            if (mStateService != Constants.STATE_SERVICE.STEP_TO_PREV_SONG && mStateService != Constants.STATE_SERVICE.STEP_TO_NEXT_SONG) {
//                if (songList.size() > 0) {
                    boolean isPlaying = MediaPlayerManager.getInstance().getMediaPlayer().isPlaying();
                    if (isPlaying) {
                        mStateService = Constants.STATE_SERVICE.PLAYING;
                    } else {
                        mStateService = Constants.STATE_SERVICE.PAUSED;
                    }
                    mNotificationManager.notify(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
//                }
            }
        }

        if (key.equals(SharedPrefsKeys.MEDIA_PLAYER_PLAYING_KEY)) {
            boolean isPlaying = SharedPrefsManager.getInstance(this).readBoolean(SharedPrefsKeys.MEDIA_PLAYER_PLAYING_KEY);
            Toast.makeText(this, "isPlaying: " + isPlaying, Toast.LENGTH_SHORT).show();
            if (isPlaying) {
                mStateService = Constants.STATE_SERVICE.PLAYING;
            } else {
                mStateService = Constants.STATE_SERVICE.PAUSED;
            }
            mNotificationManager.notify(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
        }
    }

}