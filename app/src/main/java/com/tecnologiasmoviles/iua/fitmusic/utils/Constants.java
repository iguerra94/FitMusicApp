package com.tecnologiasmoviles.iua.fitmusic.utils;

public class Constants {

    public static final int NOTIFICATION_ID_FOREGROUND_SERVICE = 8466503;
    public static final long DELAY_SHUTDOWN_FOREGROUND_SERVICE = 20000;
    public static final long DELAY_UPDATE_NOTIFICATION_FOREGROUND_SERVICE = 10000;

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    public static class ACTION {
        public static final String ACTION_PLAY = "ACTION_PLAY";
        public static final String ACTION_PAUSE = "ACTION_PAUSE";
        public static final String ACTION_STEP_BACKWARD = "ACTION_STEP_BACKWARD";
        public static final String ACTION_STEP_FORWARD = "ACTION_STEP_FORWARD";
    }

    public static class STATE_SERVICE {
        public static final int NOT_INIT = 0;
        public static final int STARTED = 10;
        public static final int PAUSED = 20;
        public static final int PLAYING = 30;
        public static final int STEP_TO_NEXT_SONG = 40;
        public static final int STEP_TO_PREV_SONG = 50;
    }

    public static class URL {
        public static final String URL_BASE = "https://fitmusic-af1fe.firebaseapp.com";
        public static final String URL_TERMS = URL_BASE + "/terms.html";
        public static final String URL_PRIVACY = URL_BASE + "/privacy.html";
    }

    public static final String REGISTRATION_TOKEN_FILENAME = "registration_token";
}