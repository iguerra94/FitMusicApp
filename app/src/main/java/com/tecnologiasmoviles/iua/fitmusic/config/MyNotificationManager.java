package com.tecnologiasmoviles.iua.fitmusic.config;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.view.SplashActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MyNotificationManager {

    private Context mCtx;
    private static MyNotificationManager mInstance;

    private MyNotificationManager(Context context) {
        mCtx = context;
    }

    public static synchronized MyNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyNotificationManager(context);
        }
        return mInstance;
    }

    public void displayNotification(String title, String body, Uri imageUri) {
        NotificationManager mNotificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = new Random().nextInt(60000);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //Setting up Notification channels for android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels(mNotificationManager);
        }

        Bitmap bitmap = getBitmapFromUrl(imageUri.toString());

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
        style.bigPicture(bitmap);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx, mCtx.getString(R.string.default_notification_channel_id))
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_stat_fit_music_icon_round)
                .setStyle(style)
                .setColor(mCtx.getResources().getColor(R.color.colorPrimary))
                .setSound(defaultSoundUri)
                .setAutoCancel(true);

        Intent intent = new Intent(mCtx, SplashActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(mCtx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        if (mNotificationManager != null) {
            mNotificationManager.notify(notificationId, mBuilder.build());
        }
    }

    private Bitmap getBitmapFromUrl(String imageUri) {
        try {
            URL url = new URL(imageUri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream is = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager mNotificationManager){
        NotificationChannel mChannel = new NotificationChannel(mCtx.getString(R.string.default_notification_channel_id),
                mCtx.getString(R.string.default_notification_channel_name), NotificationManager.IMPORTANCE_HIGH);

        mChannel.setDescription(mCtx.getString(R.string.default_notification_channel_description));
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);

        mChannel.setVibrationPattern(new long[]{100,200,300,400,500,400,300,200,400});

        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

}