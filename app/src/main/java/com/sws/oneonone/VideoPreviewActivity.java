package com.sws.oneonone;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.size.AspectRatio;
import com.sws.oneonone.fragment.NewChallengeFragment;
import com.sws.oneonone.util.BaseActivity;
import com.sws.oneonone.util.DiscardPopup;
import com.sws.oneonone.util.NotificationBarColor;


public class VideoPreviewActivity extends BaseActivity {

    private VideoView videoView;
    RelativeLayout rootlayout;
    NotificationBarColor header = new NotificationBarColor();
    private static VideoResult videoResult;
    private static Boolean isImage;
    private static String comeFrom;

    public static void setVideoResult(@Nullable VideoResult result, @Nullable Boolean isImg, @Nullable String address) {
        videoResult = result;
        isImage = isImg;
        comeFrom = address;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        header.fullScreen(this);
        setContentView(R.layout.activity_video_preview);
        final VideoResult result = videoResult;
        if (result == null) {
            finish();
            return;
        }

        rootlayout = findViewById(R.id.root_layout);
    videoView = findViewById(R.id.video);
        ImageView send = findViewById(R.id.ivSend);
        ImageView ivClose = findViewById(R.id.ivClose);
        ivClose.setOnClickListener(view -> {
            DiscardPopup discardPopup = new DiscardPopup();
            discardPopup.discardDialog(rootlayout, VideoPreviewActivity.this);
        });
        send.setOnClickListener(view -> {

            if (videoView.isPlaying()){
                videoView.pause();
            }
            if (comeFrom.equals("WaterChallengeFragment")){
               onBackPressed();
            } else {
                replaceFragment(new NewChallengeFragment());
               // onBackPressed();
            }
        });
        videoView.setOnClickListener(view -> playVideo());

        AspectRatio ratio = AspectRatio.of(result.getSize());
        MediaController controller = new MediaController(this);
        //controller.setAnchorView(videoView);
        controller.setMediaPlayer(videoView);
        videoView.setMediaController(controller);
        videoView.setVideoURI(Uri.fromFile(result.getFile()));
        videoView.setOnPreparedListener(mp -> {
            ViewGroup.LayoutParams lp = videoView.getLayoutParams();
            float videoWidth = mp.getVideoWidth();
            float videoHeight = mp.getVideoHeight();
            float viewWidth = videoView.getWidth();
            lp.height = (int) (viewWidth * (videoHeight / videoWidth));
            videoView.setLayoutParams(lp);
            playVideo();

            if (result.isSnapshot()) {
                // Log the real size for debugging reason.
                Log.e("VideoPreview", "The video full size is " + videoWidth + "x" + videoHeight);
            }
        });
    }

    void playVideo() {
        if (!videoView.isPlaying()) {
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()){
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        header.fullScreen(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            setVideoResult(null, isImage, comeFrom);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            Toast.makeText(this, "Sharing...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("video/*");
            Uri uri = FileProvider.getUriForFile(this,
                    this.getPackageName() + ".provider",
                    videoResult.getFile());
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}