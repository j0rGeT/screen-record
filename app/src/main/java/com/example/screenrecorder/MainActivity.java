package com.example.screenrecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_PERMISSION = 1001;
    
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private ScreenRecorderService mScreenRecorderService;
    
    private Button btnRecord;
    private TextView tvStatus;
    private boolean isRecording = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initMediaProjection();
        checkPermissions();
    }
    
    private void initViews() {
        btnRecord = findViewById(R.id.btn_record);
        tvStatus = findViewById(R.id.tv_status);
        
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startScreenRecord();
                } else {
                    stopScreenRecord();
                }
            }
        });
        
        updateUI();
    }
    
    private void initMediaProjection() {
        mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }
    
    private void checkPermissions() {
        String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        };
        
        boolean needRequest = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }
        
        if (needRequest) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
        }
    }
    
    private void startScreenRecord() {
        if (mProjectionManager != null) {
            Intent captureIntent = mProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CODE);
        }
    }
    
    private void stopScreenRecord() {
        if (mScreenRecorderService != null) {
            mScreenRecorderService.stopRecording();
            mScreenRecorderService = null;
            mMediaProjection = null;
            isRecording = false;
            updateUI();
            Toast.makeText(this, "录制已停止，视频已保存", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            
            if (mMediaProjection != null) {
                mScreenRecorderService = new ScreenRecorderService(this, mMediaProjection);
                mScreenRecorderService.startRecording();
                isRecording = true;
                updateUI();
                Toast.makeText(this, "开始录制屏幕", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "获取屏幕录制权限失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "需要存储和音频权限才能正常录制", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void updateUI() {
        if (isRecording) {
            btnRecord.setText("停止录制");
            btnRecord.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            tvStatus.setText("正在录制...");
        } else {
            btnRecord.setText("开始录制");
            btnRecord.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            tvStatus.setText("就绪");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScreenRecorderService != null) {
            mScreenRecorderService.stopRecording();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
    }
}
