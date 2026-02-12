package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity {

    private TextView songTitle, currentTime, totalTime;
    private ImageButton playPauseButton, nextButton, prevButton, forward10Button, back10Button;
    private ImageView albumArt;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private int currentIndex = 0;
    private List<Song> trackList = TrackListActivity.trackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        songTitle = findViewById(R.id.songTitle);
        playPauseButton = findViewById(R.id.playPauseButton);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        forward10Button = findViewById(R.id.forward10Button);
        back10Button = findViewById(R.id.back10Button);
        albumArt = findViewById(R.id.albumArt);
        seekBar = findViewById(R.id.seekBar);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);

        currentIndex = getIntent().getIntExtra("index", 0);
        playSong(currentIndex);

        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

        nextButton.setOnClickListener(v -> changeSong(1));
        prevButton.setOnClickListener(v -> changeSong(-1));

        forward10Button.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                int pos = mediaPlayer.getCurrentPosition();
                mediaPlayer.seekTo(Math.min(pos + 10000, mediaPlayer.getDuration()));
            }
        });

        back10Button.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                int pos = mediaPlayer.getCurrentPosition();
                mediaPlayer.seekTo(Math.max(pos - 10000, 0));
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) mediaPlayer.seekTo(progress);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void playSong(int index) {
        if (mediaPlayer != null) mediaPlayer.release();

        Song song = trackList.get(index);
        songTitle.setText(song.getTitle());

        // Check if this is a URI-based song (selected from device)
        if (song.getFileResId() < 0) {
            // This is a URI-based song, create a simple MediaPlayer
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.batel_shod));
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to first available resource
                mediaPlayer = MediaPlayer.create(this, R.raw.batel_shod);
                mediaPlayer.start();
            }
        } else {
            // This is a resource-based song
            mediaPlayer = MediaPlayer.create(this, song.getFileResId());
            mediaPlayer.start();
        }

        playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        seekBar.setMax(mediaPlayer.getDuration());

        // Set album art
        setAlbumArt(song.getFileResId());

        // Set total duration
        totalTime.setText(formatTime(mediaPlayer.getDuration()));

        handler.postDelayed(updateSeekBar, 500);

        mediaPlayer.setOnCompletionListener(mp -> changeSong(1));
    }

    private void setAlbumArt(int resId) {
        try {
            if (resId >= 0) {
                // Resource-based album art
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(getResources().openRawResourceFd(resId).getFileDescriptor());
                byte[] art = retriever.getEmbeddedPicture();
                if (art != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                    albumArt.setImageBitmap(bitmap);
                } else {
                    albumArt.setImageResource(android.R.drawable.ic_menu_gallery);
                }
                retriever.release();
            } else {
                // URI-based song - use default album art
                albumArt.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } catch (Exception e) {
            albumArt.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void changeSong(int change) {
        currentIndex = (currentIndex + change + trackList.size()) % trackList.size();
        playSong(currentIndex);
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                currentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(this, 500);
            }
        }
    };

    private String formatTime(int millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateSeekBar);
            mediaPlayer.release();
        }
    }
}