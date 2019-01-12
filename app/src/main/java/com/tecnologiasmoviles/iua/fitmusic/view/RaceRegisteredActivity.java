package com.tecnologiasmoviles.iua.fitmusic.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RaceRegisteredActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = RaceRegisteredActivity.class.getSimpleName();

    private static final int TWEET_COMPOSER_REQUEST_CODE = 100;

    TextView raceDistanceTextViewRaceDetail;
    TextView raceDurationTextViewRaceDetail;

    ImageButton btnTweetShare;

    long raceDistance;
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

        raceDistance = SharedPrefsManager.getInstance(this).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);
        raceDuration = SharedPrefsManager.getInstance(this).readString(SharedPrefsKeys.RACE_DURATION_KEY);

        raceDistanceTextViewRaceDetail = findViewById(R.id.raceDistanceTextViewRaceDetail);
        raceDurationTextViewRaceDetail = findViewById(R.id.raceDurationTextViewRaceDetail);

        float distanceToKms = (raceDistance/1000f);
        raceDistanceTextViewRaceDetail.setText(String.format("%.2f", distanceToKms));

        raceDurationTextViewRaceDetail.setText(raceDuration);

        btnTweetShare = findViewById(R.id.tw_post_tweet);
        btnTweetShare.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
        String durationNormalized = TimeUtils.normalizeDuration(raceDuration);

        float raceDistanceToKms = (raceDistance/1000f);
        String raceDistanceWithFixedDecimals = String.format("%.2f", raceDistanceToKms);

        StringBuilder shareUrl = new StringBuilder()
                .append("https://fitmusic-af1fe.firebaseapp.com/shareRace")
                .append("?distance=")
                .append(raceDistanceWithFixedDecimals)
                .append("&duration=")
                .append(durationNormalized);
        if (v.getId() == R.id.tw_post_tweet) {
            try {
                String durationAsSentence = TimeUtils.parseDurationAsSentence(raceDuration);

                Intent intent = new TweetComposer.Builder(this)
                        .text("I just finished an amazing running session. I managed to do " +
                                raceDistanceWithFixedDecimals + " KM in " + durationAsSentence + ". Via @FitMusicApp")
                        .url(new URL(shareUrl.toString()))
                        .createIntent();

                startActivityForResult(intent, TWEET_COMPOSER_REQUEST_CODE);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

}