package com.pinq.bluegray;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.pinq.bluegray.data.PreferenceHandler;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity {

    RecyclerView mMessages;
    MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        mMessages = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mMessages.setLayoutManager(llm);

        mMessages.setAdapter(new GameAdapter(this, mMessages));

        BlueGray.setForeground(true);

        mMediaPlayer = MediaPlayer.create(this, R.raw.generic);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();

        Intent intent = new Intent(this, NotificationService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

        findViewById(R.id.credits).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.credits);
                dialog.getWindow().setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                dialog.show();
                dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

                // Swap the 'standard' bitmap background with one that has no minimum width/height.
                BitmapDrawable background = (BitmapDrawable)dialog.findViewById(R.id.root).getBackground(); // assuming you have bg_tile as background.
                BitmapDrawable newBackground = new BitmapDrawable(background.getBitmap()) {
                    @Override
                    public int getMinimumWidth() {
                        return 0;
                    }

                    @Override
                    public int getMinimumHeight() {
                        return 0;
                    }
                };
                newBackground.setTileModeXY(background.getTileModeX(), background.getTileModeY());
                dialog.findViewById(R.id.root).setBackgroundDrawable(newBackground);
            }
        });

        findViewById(R.id.change_lang).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.change_lang);
                dialog.getWindow().setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                dialog.show();
                dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

                // Swap the 'standard' bitmap background with one that has no minimum width/height.
                BitmapDrawable background = (BitmapDrawable)dialog.findViewById(R.id.root).getBackground(); // assuming you have bg_tile as background.
                BitmapDrawable newBackground = new BitmapDrawable(background.getBitmap()) {
                    @Override
                    public int getMinimumWidth() {
                        return 0;
                    }

                    @Override
                    public int getMinimumHeight() {
                        return 0;
                    }
                };
                newBackground.setTileModeXY(background.getTileModeX(), background.getTileModeY());
                dialog.findViewById(R.id.root).setBackgroundDrawable(newBackground);

                dialog.findViewById(R.id.english).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PreferenceHandler.setLanguage(MainActivity.this, "en");
                        ((GameAdapter)mMessages.getAdapter()).languageChanged();
                        dialog.dismiss();
                        ((SlidingUpPanelLayout)findViewById(R.id.root)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                });
                dialog.findViewById(R.id.turkish).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PreferenceHandler.setLanguage(MainActivity.this, "tr");
                        ((GameAdapter)mMessages.getAdapter()).languageChanged();
                        dialog.dismiss();
                        ((SlidingUpPanelLayout)findViewById(R.id.root)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                });
                dialog.findViewById(R.id.german).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PreferenceHandler.setLanguage(MainActivity.this, "de");
                        ((GameAdapter)mMessages.getAdapter()).languageChanged();
                        dialog.dismiss();
                        ((SlidingUpPanelLayout)findViewById(R.id.root)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                });
                dialog.findViewById(R.id.azerbaijani).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PreferenceHandler.setLanguage(MainActivity.this, "az");
                        ((GameAdapter)mMessages.getAdapter()).languageChanged();
                        dialog.dismiss();
                        ((SlidingUpPanelLayout)findViewById(R.id.root)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                });
            }
        });

        ((SlidingUpPanelLayout)findViewById(R.id.root)).addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                findViewById(R.id.dumen).setRotation(360*slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        BlueGray.setForeground(false);
        mMediaPlayer.pause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        BlueGray.setForeground(true);
        mMediaPlayer.start();
    }
}
