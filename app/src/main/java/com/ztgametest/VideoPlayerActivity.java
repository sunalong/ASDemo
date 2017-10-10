/**
 * Created by luke on 2017/09/07.
 */
package com.ztgametest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ztgame.videoengine.NativeVideoEngine;
import com.ztgame.videoengine.VideoEngineImpl;
import com.ztgame.voiceengine.NativeVoiceEngine;
import com.ztgame.voiceengine.RTChatEventInterface;
import com.ztgametest.afilechooser.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoPlayerActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private int REQUEST_CHOOSER = 1234;

    private String mResourcePath = Environment.getExternalStorageDirectory().
            getAbsolutePath();

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private RelativeLayout mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private Switch mAudioTracksSwitch;
    private Switch mAudioMuteSwitch;
    private Switch mAudioSpeakLoudSwitch;
    private SeekBar mSeekBar;
    private TextView tvRoomId;
    private TextView tvUserName;
    private String username;
    private String roomId;

    private boolean blouderspeak = false;
    private boolean bunmute = true;
    private NativeVideoEngine mNVEngine;
    private VideoEngineImpl mVideoEngineImp;

    private List<Integer> removeId = new ArrayList<Integer>();

    private Map<String, SurfaceView> surfaceViewMap;

    private Button playMediaBt;
    private Context context;
    private boolean isPlaying = false;
    private boolean isSending = false;
    private boolean isPause = false;
    private boolean isTest = false;

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;

    private int sourceType = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_playervideo);

        this.context = VideoPlayerActivity.this;

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = (RelativeLayout) findViewById(R.id.fullscreen_content);
        tvRoomId = (TextView) findViewById(R.id.tv_roomId);
        tvUserName = (TextView) findViewById(R.id.tv_userName);
        playMediaBt = (Button)findViewById(R.id.btn_play_media);

        final Intent intent = getIntent();
        username = intent.getStringExtra("username");
        roomId = intent.getStringExtra("roomId");
        tvRoomId.setText("当前房间号：" + roomId);
        tvUserName.setText("当前用户ID：" + username);
        mNVEngine = NativeVideoEngine.getInstance();
        mVideoEngineImp = VideoEngineImpl.getInstance();

        surfaceViewMap = new HashMap<String, SurfaceView>();

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        mAudioTracksSwitch = (Switch) findViewById(R.id.audiotrack_switch);
        mAudioSpeakLoudSwitch = (Switch) findViewById(R.id.speakLouder_switch);
        mAudioMuteSwitch = (Switch) findViewById(R.id.mute_switch);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);

        NativeVoiceEngine.getInstance().setLouderSpeaker(blouderspeak);
        NativeVoiceEngine.getInstance().setSendVoice(bunmute);

        NativeVoiceEngine.getInstance().registerEventHandler(mListener);
        mAudioTracksSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAudioTracksSwitch.isChecked())
                {
                    NativeVoiceEngine.getInstance().setAudioTrack(2);
                }else{
                    NativeVoiceEngine.getInstance().setAudioTrack(1);
                }
            }
        });

        mAudioSpeakLoudSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blouderspeak = mAudioSpeakLoudSwitch.isChecked();
                NativeVoiceEngine.getInstance().setLouderSpeaker(blouderspeak);
            }
        });

        mAudioMuteSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bunmute = mAudioMuteSwitch.isChecked();
                NativeVoiceEngine.getInstance().setSendVoice(bunmute);
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                NativeVoiceEngine.getInstance().adjustMusicVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekBar.setProgress(100);
    }

    private int count = 10;
    private void setLayoutRule(SurfaceView mSurfaceView, int index){
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;
        int px = 300*metric.densityDpi/DisplayMetrics.DENSITY_DEFAULT;
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(width, px);
        mSurfaceView.setId(index);
        switch (index){
            case 10: {
                param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            }
            case 11: {
                param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            }
            default:
                break;
        }
        mContentView.addView(mSurfaceView, param);
    }

    private void addSurfaceView(String userId, boolean islocalview){
        SurfaceView mSurfaceView = mNVEngine.createRenderView(0);
        int addCount = count;
        boolean removeIdFlag = false;
        if(removeId.isEmpty()) {
            removeIdFlag = true;
        }
        else{
            int min = 20;
            for(int it:removeId){
                min = it< min ? it:min;
            }
            addCount = min;
            removeId.remove(Integer.valueOf(min));
        }
        int ret = 0;
        if(islocalview)
            ret = mNVEngine.observerMovieVideoWindow(true, mSurfaceView);
        else
            ret = mNVEngine.observerRemoteTargetVideo(userId, mSurfaceView);

        if(ret == 0){
            mNVEngine.destroyRenderView(mSurfaceView);
            return;
        }
        setLayoutRule(mSurfaceView, addCount);

        surfaceViewMap.put(userId,mSurfaceView);
        if(removeIdFlag)
            count++;

        if (mSurfaceView != null)
            mSurfaceView.setVisibility(View.VISIBLE);
    }

    private void removeSurfaceView(String userId, boolean islocalview){
        SurfaceView mSurfaceView = surfaceViewMap.get(userId);
        if(mSurfaceView == null)
            return;
        if(islocalview)
            mNVEngine.observerMovieVideoWindow(false, mSurfaceView);
        else
            mNVEngine.observerRemoteTargetVideo(userId, null);
        mSurfaceView.setVisibility(View.INVISIBLE);
        int id = mSurfaceView.getId();
        if(id == (count-1)){
            count--;
        }
        else{
            removeId.add(id);
        }
        mContentView.removeView(mSurfaceView);
        mNVEngine.destroyRenderView(mSurfaceView);
        surfaceViewMap.remove(userId) ;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSER && resultCode == RESULT_OK) {
            final Uri uri = data.getData();
            String path = FileUtils.getPath(context, uri);

            if(isPlaying){
                NativeVoiceEngine.getInstance().stopPlayFileAsSource();
                removeSurfaceView(username+"local", true);
                isPlaying = false;
            }

            if(isSending){
                mNVEngine.stopSendVideo();
                isSending = false;
            }

            //"http://192.168.114.6/love/love.m3u8"
            int ret = NativeVoiceEngine.getInstance().startPlayFileAsSource(path, sourceType);
            if(ret > 0) {
                mSeekBar.setProgress(100);
                mAudioTracksSwitch.setChecked(false);
                addSurfaceView(username+"local", true);
                if((sourceType & 0x01) == 1) {
                    int isSend = mNVEngine.startSendVideo();
                    if(isSend > 0){
                        isSending = true;
                    }
                }
                isPlaying = true;
            }
            Toast.makeText(context, path + " File Added!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed(){
        if(isPlaying){
            isPlaying = false;
            NativeVoiceEngine.getInstance().stopPlayFileAsSource();
        }

        NativeVoiceEngine.getInstance().registerEventHandler(null);
        NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
        for(String key : surfaceViewMap.keySet()){
            SurfaceView mSurfaceView = surfaceViewMap.get(key);
            if(mSurfaceView == null)
                return;
            mContentView.removeView(mSurfaceView);
            mNVEngine.destroyRenderView(mSurfaceView);
        }
        surfaceViewMap.clear(); ;
        removeId.clear();

        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //startPreview();
            } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showToast("请打开摄像机权限");
            } else {
                showToast("请打开录音权限");
            }
        }
    }

    private void updateStatus() {

    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    private RTChatEventInterface mListener = new RTChatEventInterface() {
        @Override
        public void onEventUserJoinRoom(String uid){
            Toast.makeText(context, uid, Toast.LENGTH_LONG).show();
            //playVideo(uid);
        }
        @Override
        public void onEventUserLeaveRoom(String uid){

        }
    };
    public void jumpOnClick1(View view){
        switch (view.getId()){
            case R.id.Button1: {

                if(surfaceViewMap.size() >= 2){
                    Toast.makeText(getApplicationContext(), "Demo仅支持两个播放窗口！！", Toast.LENGTH_LONG).show();
                    break;
                }
                final EditText et = new EditText(this);

                new AlertDialog.Builder(this).setTitle("输入观看用户id：")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String input = et.getText().toString();
                                if (input.equals("")) {
                                    Toast.makeText(getApplicationContext(), "用户id空！" + input, Toast.LENGTH_LONG).show();
                                }
                                else {
                                    addSurfaceView(input, false);
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

                break;
            }
            case R.id.Button2: {
                //isPause = !isPause;
                //NativeVoiceEngine.getInstance().pausePlayFileAsSource(isPause);

                final EditText et = new EditText(this);

                new AlertDialog.Builder(this).setTitle("输入删除用户id：")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String input = et.getText().toString();
                                if (input.equals("")) {
                                    Toast.makeText(getApplicationContext(), "用户id空！" + input, Toast.LENGTH_LONG).show();
                                }
                                else {
                                    removeSurfaceView(input, false);
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

                break;
            }

            case R.id.btn_test1:{
                SurfaceView mSurfaceView = surfaceViewMap.get(username + "local");
                isTest = !isTest;
                if(isTest)
                    mNVEngine.setWindowRenderMode(1,mSurfaceView);
                else
                    mNVEngine.setWindowRenderMode(0,mSurfaceView);
                break;
            }

            case R.id.btn_play_media: {
                if(surfaceViewMap.size() >= 2){
                    Toast.makeText(getApplicationContext(), "Demo仅支持两个播放窗口！！", Toast.LENGTH_LONG).show();
                    break;
                }
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentapiVersion >= Build.VERSION_CODES.KITKAT) {
                    Intent getContentIntent = FileUtils.createGetAudioIntent();
                    Intent intent = Intent.createChooser(getContentIntent, "Select a file");
                    try {
                        startActivityForResult(intent, REQUEST_CHOOSER);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    Intent intent_upload = new Intent();
                    intent_upload.setType("audio");
                    intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                    try {
                        startActivityForResult(intent_upload, REQUEST_CHOOSER);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Intent getContentIntent = FileUtils.createGetAudioIntent();
                        Intent intent = Intent.createChooser(getContentIntent, "Select a file");
                        try {
                            startActivityForResult(intent, REQUEST_CHOOSER);
                        } catch (ActivityNotFoundException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                break;
            }

        }
    }
}
