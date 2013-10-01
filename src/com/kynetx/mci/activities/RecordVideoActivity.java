package com.kynetx.mci.activities;

import java.io.File;
import java.util.UUID;

import com.kynetx.mci.R;
//import com.kynetx.mci.R.layout;
//import com.kynetx.mci.R.menu;
import com.kynetx.mci.config.Constants;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class RecordVideoActivity extends Activity {
	
	public static final String FILE_PATH_ROOT = "mci_media";
	public static final String FILE_PATH_VIDEO = "mci_video";
	public static final String FILE_NAME_VIDEO = "video_";
	public static final String VIDEO_EXT_MP4 = ".mp4";
	
	//final private static String RECORDED_FILE = "/myvideo.mp4";
    MediaRecorder videoRecorder;
    MediaPlayer player;
    String pathForAppFiles = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO + "/";
    String fileName;
    String guid;
 
    //Code to call the save Media screen on stop record. 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        final Button record = (Button) findViewById(R.id.record);
        final Button stop = (Button) findViewById(R.id.stop);
        final Button stopPlayback = (Button) findViewById(R.id.stop_playback);
        final Button play = (Button) findViewById(R.id.play);
        
        final SurfaceView surface = new SurfaceView(getApplicationContext());
        final SurfaceHolder surfaceHolder = surface.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    
        FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
        frame.addView(surface);
        guid = UUID.randomUUID().toString();

        fileName = FILE_NAME_VIDEO + guid + VIDEO_EXT_MP4;
        checkForDirectory();
        
        record.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (videoRecorder == null) {
                    videoRecorder = new MediaRecorder();
                }

                // Fully qualified path name. In this case, we use the Files subdir
                //String pathForAppFiles = getFilesDir().getAbsolutePath();
                //pathForAppFiles = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO;
                pathForAppFiles += fileName;
                Log.d("Video filename:",pathForAppFiles );
                
           
                videoRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                videoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                videoRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                videoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                videoRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

                videoRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                
                
                videoRecorder.setOutputFile(pathForAppFiles);

                try {
                    videoRecorder.prepare();
                    videoRecorder.start();
                    stop.setVisibility(View.VISIBLE);
                    record.setVisibility(View.GONE);
                    play.setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.e("Video", "Failed to prepare and start video recording", e);
                    videoRecorder.release();
                    videoRecorder = null;
                } 
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (videoRecorder == null)
                    return;
                videoRecorder.stop();
                videoRecorder.reset();
                videoRecorder.release();
                videoRecorder = null;
                
                //String pathForAppFiles = getFilesDir().getAbsolutePath();
                //pathForAppFiles = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO;
                //pathForAppFiles += fileName;;
                Log.d("Video filename:", pathForAppFiles);

                ContentValues values = new ContentValues(10);

                values.put(MediaStore.MediaColumns.TITLE, "RecordedVideo");
                values.put(VideoColumns.ALBUM, "Your Groundbreaking Movie");
                values.put(VideoColumns.ARTIST, "Your Name");
                values.put(MediaColumns.DISPLAY_NAME, "The Video File You Recorded In Media App");

                values.put(MediaStore.MediaColumns.TITLE, "RecordedVideo");
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                values.put(MediaColumns.DATA, pathForAppFiles);

                Uri videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                if (videoUri == null) {
                    Log.d("Video", "Content resolver failed");
                    return;
                }

                // Force Media scanner to refresh now. Technically, this is
                // unnecessary, as the media scanner will run periodically but
                // helpful for testing.
                Log.d("Video URI", "Path = " + videoUri.getPath());
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoUri));


                stop.setVisibility(View.GONE);
                record.setVisibility(View.VISIBLE);
                //play.setVisibility(View.VISIBLE);
                String uri = FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO + "/" + fileName;
                saveVideo(uri);

            }

        });
               
        
        play.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (player == null) {
                    player = new MediaPlayer ();
                }
                try {
                        
                    // Fully qualified path name. In this case, we use the Files subdir
                    //String audioFilePath = getFilesDir().getAbsolutePath();
                	//pathForAppFiles = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO;
                	pathForAppFiles += fileName;
                    Log.d("Video filename:",pathForAppFiles );
                    
                    player.setDataSource(pathForAppFiles);
                    player.prepare();
                    player.start();
                } catch (Exception e) {
                    Log.e("Video", "Playback failed.", e);
                }

                stopPlayback.setVisibility(View.VISIBLE);
                record.setVisibility(View.GONE);
                play.setVisibility(View.GONE);

            }

        });
        
        
        stopPlayback.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (player == null)return;
                player.stop();
                player.release();
                player = null;
                stopPlayback.setVisibility(View.GONE);
                record.setVisibility(View.VISIBLE);
                play.setVisibility(View.VISIBLE);
                
            }
            
        });
    }
    
    private void checkForDirectory()
    {
    	
		File dir = new File(pathForAppFiles);
		if(!dir.exists())
		{
			dir.mkdirs();
		}
    }
    
    private void saveVideo(String videoFileName)
    {
    	Intent saveMedia = new Intent(this, SaveMediaActivity.class);
		String file = null;
		saveMedia.putExtra(Constants.EXTRA_MEDIA_TYPE, Constants.MEDIA_TYPE_VIDEO);
		saveMedia.putExtra(Constants.EXTRA_GUID, this.guid);
		saveMedia.putExtra(Constants.EXTRA_MEDIA_PATH, videoFileName);
		startActivity(saveMedia);
    }
    
    @Override
    protected void onPause() {
        if (videoRecorder != null) {
            videoRecorder.release();
            videoRecorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoRecorder = new MediaRecorder();
        player = new MediaPlayer();
    }

}
