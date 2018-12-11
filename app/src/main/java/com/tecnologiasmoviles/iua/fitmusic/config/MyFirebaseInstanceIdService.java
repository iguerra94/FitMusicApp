package com.tecnologiasmoviles.iua.fitmusic.config;

import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String LOG_TAG = MyFirebaseInstanceIdService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
//        String newToken = FirebaseInstanceId.getInstance().getToken();
//        Log.d(LOG_TAG, "New Token: " + newToken);
//
//        String lastToken = readRegistrationTokenFromExternalStorage();
//        Log.d(LOG_TAG, "Last Token: " + lastToken);
    }
}