package com.tecnologiasmoviles.iua.fitmusic.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.tecnologiasmoviles.iua.fitmusic.BuildConfig;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RaceRegisteredActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = RaceRegisteredActivity.class.getSimpleName();

    private static final String RACE_DISTANCE_KEY = "race_distance";
    private static final String RACE_DURATION_KEY = "race_duration";
    private static final int TWEET_COMPOSER_REQUEST_CODE = 100;

    ShareDialog shareDialog;
    LoginManager loginManager;
    CallbackManager callbackManager;

    TextView raceDistanceTextViewRaceDetail;
    TextView raceDurationTextViewRaceDetail;

    ImageButton btnTweetShare;
    ImageButton btnFacebookShare;

    String raceDistance;
    String raceDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_registered);

        Toolbar toolbarRaceRegistered = findViewById(R.id.toolbar_race_registered);
        setSupportActionBar(toolbarRaceRegistered);

        assert getSupportActionBar() != null;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Twitter.initialize(this);
        FacebookSdk.sdkInitialize(this);

        List<String> permissionNeeds = Arrays.asList("publish_actions"); // permission.
        loginManager = LoginManager.getInstance();

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Toast.makeText(RaceRegisteredActivity.this, "Post shared!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(LOG_TAG, "ERROR: " + error.getMessage());
            }
        });

        String[] raceData = readRaceDistanceAndDurationFromSharedPrefs();
        raceDistance = raceData[0];
        raceDuration = raceData[1];

        raceDistanceTextViewRaceDetail = findViewById(R.id.raceDistanceTextViewRaceDetail);
        raceDurationTextViewRaceDetail = findViewById(R.id.raceDurationTextViewRaceDetail);

        raceDistanceTextViewRaceDetail.setText(raceDistance);
        raceDurationTextViewRaceDetail.setText(raceDuration);

        btnTweetShare = findViewById(R.id.tw_post_tweet);
        btnTweetShare.setOnClickListener(this);

        btnFacebookShare = findViewById(R.id.fb_share_button);
        btnFacebookShare.setOnClickListener(this);
    }

    private String[] readRaceDistanceAndDurationFromSharedPrefs() {
        SharedPreferences sharedPref = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        String[] data = new String[2];

        data[0] = sharedPref.getString(RACE_DISTANCE_KEY, "");
        data[1] = sharedPref.getString(RACE_DURATION_KEY, "");

        return data;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TWEET_COMPOSER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "La carrera se ha compartido con exito en Twitter.", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        String durationNormalized = normalizeDuration(raceDuration);

        StringBuilder shareUrl = new StringBuilder()
                .append("https://fitmusic-af1fe.firebaseapp.com/shareRace?distance=")
                .append(raceDistance)
                .append("&duration=")
                .append(durationNormalized);
        if (v.getId() == R.id.tw_post_tweet) {

//            TweetComposer.Builder builder;
            try {
                String durationAsSentence = parseDurationAsSentence(raceDuration);
//                builder = new TweetComposer.Builder(this)
//                        .text("I just finished an amazing running session. I managed to do " +
//                                raceDistance + " km. in " + durationAsSentence + ". Via @FitMusicApp")
//                        .url(new URL(shareUrl.toString()));
//                builder.show();

                Intent intent = new TweetComposer.Builder(this)
                        .text("I just finished an amazing running session. I managed to do " +
                                raceDistance + " km. in " + durationAsSentence + ". Via @FitMusicApp")
                        .url(new URL(shareUrl.toString()))
                        .createIntent();

                startActivityForResult(intent, TWEET_COMPOSER_REQUEST_CODE);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        if (v.getId() == R.id.fb_share_button) {
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                // Sharing the content to facebook
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(shareUrl.toString()))
                        .build();

                shareDialog.show(content);
            }
        }
    }

    private String normalizeDuration(String raceDuration) {
        String durationNormalized = "";

        if (raceDuration.contains("h")) {
            durationNormalized = raceDuration.replace("h", ":");

            if (raceDuration.contains("''")) {
                durationNormalized = durationNormalized.replace("''", "");
            }

            if (raceDuration.contains("'")) {
                durationNormalized = durationNormalized.replace("'", ":");
            }
        } else {
            if (raceDuration.contains("''")) {
                durationNormalized = raceDuration.replace("''", "");
            }

            if (raceDuration.contains("'")) {
                durationNormalized = durationNormalized.replace("'", ":");
            }
        }

        return durationNormalized;
    }

    private String parseDurationAsSentence(String raceDuration) {
        List<String> durationSplitted = new ArrayList<>();

        if (raceDuration.contains("h")) {
            durationSplitted.add(raceDuration.substring(0,3));
            durationSplitted.add(raceDuration.substring(3,6));
            durationSplitted.add(raceDuration.substring(6));
        } else {
            durationSplitted.add(raceDuration.substring(0,3));
            durationSplitted.add(raceDuration.substring(3));
        }

        String hours = "";
        String minutes = "";
        String seconds = "";

        if (durationSplitted.size() == 3) { // duration is at least is more than one hour
            if (durationSplitted.get(0).charAt(0) == '0') {
                if (durationSplitted.get(0).charAt(1) == '1') {
                    hours = durationSplitted.get(0).charAt(1) + " hour";
                } else {
                    hours = durationSplitted.get(0).charAt(1) + " hours";
                }
            } else {
                hours = durationSplitted.get(0).substring(0,2) + " hours";
            }

            if (!durationSplitted.get(1).substring(0,2).equals("00")) { // duration have minutes
                if (!durationSplitted.get(2).substring(0,2).equals("00")) { // duration have seconds
                    if (durationSplitted.get(1).charAt(0) == '0') {
                        if (durationSplitted.get(1).charAt(1) == '1') {
                            minutes = ", " + durationSplitted.get(1).charAt(1) + " minute";
                        } else {
                            minutes = ", " + durationSplitted.get(1).charAt(1) + " minutes";
                        }
                    } else {
                        minutes = ", " + durationSplitted.get(1).substring(0,2) + " minutes";
                    }

                    if (durationSplitted.get(2).charAt(0) == '0') {
                        if (durationSplitted.get(2).charAt(1) == '1') {
                            seconds = " and " + durationSplitted.get(2).charAt(1) + " second";
                        } else {
                            seconds = " and " + durationSplitted.get(2).charAt(1) + " seconds";
                        }
                    } else {
                        seconds = " and " + durationSplitted.get(2).substring(0,2) + " seconds";
                    }
                } else {
                    if (durationSplitted.get(1).charAt(0) == '0') {
                        if (durationSplitted.get(1).charAt(1) == '1') {
                            minutes = " and " + durationSplitted.get(1).charAt(1) + " minute";
                        } else {
                            minutes = " and " + durationSplitted.get(1).charAt(1) + " minutes";
                        }
                    } else {
                        minutes = " and " + durationSplitted.get(1).substring(0,2) + " minutes";
                    }
                }
            } else {
                if (durationSplitted.get(2).charAt(0) == '0') {
                    if (durationSplitted.get(2).charAt(1) == '1') {
                        seconds = " and " + durationSplitted.get(2).charAt(1) + " second";
                    } else {
                        seconds = " and " + durationSplitted.get(2).charAt(1) + " seconds";
                    }
                } else {
                    seconds = " and " + durationSplitted.get(2).substring(0,2) + " seconds";
                }
            }
        } else {
            if (!durationSplitted.get(0).substring(0,2).equals("00")) { // duration have minutes
                if (!durationSplitted.get(1).substring(0,2).equals("00")) { // duration have seconds
                    if (durationSplitted.get(0).charAt(0) == '0') {
                        if (durationSplitted.get(0).charAt(1) == '1') {
                            minutes = durationSplitted.get(0).charAt(1) + " minute";
                        } else {
                            minutes = durationSplitted.get(0).charAt(1) + " minutes";
                        }
                    } else {
                        minutes = durationSplitted.get(0).substring(0,2) + " minutes";
                    }

                    if (durationSplitted.get(1).charAt(0) == '0') {
                        if (durationSplitted.get(1).charAt(1) == '1') {
                            seconds = " and " + durationSplitted.get(1).charAt(1) + " second";
                        } else {
                            seconds = " and " + durationSplitted.get(1).charAt(1) + " seconds";
                        }
                    } else {
                        seconds = " and " + durationSplitted.get(1).substring(0,2) + " seconds";
                    }
                } else {
                    if (durationSplitted.get(0).charAt(0) == '0') {
                        if (durationSplitted.get(0).charAt(1) == '1') {
                            minutes = durationSplitted.get(0).charAt(1) + " minute";
                        } else {
                            minutes = durationSplitted.get(0).charAt(1) + " minutes";
                        }
                    } else {
                        minutes = durationSplitted.get(0).substring(0,2) + " minutes";
                    }
                }
            } else {
                if (durationSplitted.get(1).charAt(0) == '0') {
                    if (durationSplitted.get(1).charAt(1) == '1') {
                        seconds = durationSplitted.get(1).charAt(1) + " second";
                    } else {
                        seconds = durationSplitted.get(1).charAt(1) + " seconds";
                    }
                } else {
                    seconds = durationSplitted.get(1).substring(0,2) + " seconds";
                }
            }
        }

        return hours + minutes + seconds;
    }

}