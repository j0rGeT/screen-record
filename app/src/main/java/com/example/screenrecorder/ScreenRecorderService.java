package com.example.screenrecorder;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenRecorderService {
    private static final String TAG = "ScreenRecorderService";
    
    private Context mContext;
    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;
    
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private String mVideoPath;
    
    public ScreenRecorderService(Context context, MediaProjection mediaProjection) {
        mContext = context;
        mMediaProjection = mediaProjection;
        initScreenConfig();
    }
    
    private void initScreenConfig() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mScreenDensity = metrics.densityDpi;
        
        Log.d(TAG, "Screen: " + mScreenWidth + "x" + mScreenHeight + ", density: " + mScreenDensity);
    }
    
    public void startRecording() {
        try {
            initMediaRecorder();
            createVirtualDisplay();
            mMediaRecorder.start();
            Log.d(TAG, "Screen recording started, output: " + mVideoPath);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start recording", e);
        }
    }
    
    public void stopRecording() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
                mVirtualDisplay = null;
            }
            
            Log.d(TAG, "Screen recording stopped, saved to: " + mVideoPath);
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop recording", e);
        }
    }
    
    private void initMediaRecorder() throws IOException {
        mMediaRecorder = new MediaRecorder();
        
        // 创建保存目录
        File recordDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "ScreenRecords");
        if (!recordDir.exists()) {
            recordDir.mkdirs();
        }
        
        // 生成文件名
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        mVideoPath = new File(recordDir, "screen_record_" + timestamp + ".mp4").getAbsolutePath();
        
        // 配置MediaRecorder
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoPath);
        
        // 视频编码配置
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoEncodingBitRate(8 * 1024 * 1024); // 8Mbps
        
        // 音频编码配置
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setAudioEncodingBitRate(128 * 1024); // 128kbps
        
        mMediaRecorder.prepare();
    }
    
    private void createVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
            "ScreenRecorder",
            mScreenWidth,
            mScreenHeight,
            mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mMediaRecorder.getSurface(),
            null,
            null
        );
    }
    
    public String getVideoPath() {
        return mVideoPath;
    }
}
