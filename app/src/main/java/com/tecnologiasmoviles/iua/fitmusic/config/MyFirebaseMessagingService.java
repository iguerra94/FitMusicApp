package com.tecnologiasmoviles.iua.fitmusic.config;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String LOG_TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Uri imageUriData = Uri.parse(remoteMessage.getData().get("image"));
        String titleData = remoteMessage.getData().get("title");
        String bodyData = remoteMessage.getData().get("body");

        Log.d(LOG_TAG, "Image Uri Data: " + imageUriData);
        Log.d(LOG_TAG, "Title Data: " + titleData);
        Log.d(LOG_TAG, "Body Data: " + bodyData);

        MyNotificationManager.getInstance(getApplicationContext())
                .displayNotification(titleData, bodyData, imageUriData);
    }

}