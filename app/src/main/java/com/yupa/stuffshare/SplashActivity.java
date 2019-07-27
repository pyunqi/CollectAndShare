package com.yupa.stuffshare;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.yupa.stuffshare.cview.MutedVideoView;


public class SplashActivity extends AppCompatActivity {

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        MutedVideoView vView = findViewById(R.id.video_view);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                + R.raw.splashn);

        if (vView != null) {
            vView.setVideoURI(video);
            vView.setZOrderOnTop(true);
            vView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    jump();
                }
            });


            vView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    jump();
                    return false;
                }
            });

            vView.start();

        } else {

            jump();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void jump() {
        if (isFinishing())
            return;
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    public class SplashThread extends Thread {

        @Override
        public void run() {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Start the MainActivity
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    // Close this activity
                }
            });

        }
    }
}
