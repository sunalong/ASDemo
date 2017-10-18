package com.ztgametest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.Button;

import com.ztgame.videoengine.NativeVideoEngine;
import com.ztgame.videoengine.VideoEngineImpl;
import com.ztgame.voiceengine.NativeVoiceEngine;
import com.ztgametest.ui.SurfaceViewContainer;

public class VideoActivity extends Activity {

    public static final boolean NATIVETEST = true;

    private CheckBox viewSomeOneCheckBox;
    private CheckBox openRemoteVideoCheckBox;
    private CheckBox displayRemoteViewCheckBox;
    private CheckBox sendLocalVideoCheckBox;
    private CheckBox displayLocalViewCheckBox;
    private CheckBox cameraSwitchCheckBox;

    private ImageButton disconnectButton;
    private ImageButton toggleMuteButton;
    private ImageButton toggleSpeakerButton;
    private RelativeLayout localSurfaceViewContainer;
    private RelativeLayout remoteSurfaceViewContainer;

    private Button button1;
    private Button button2;
    private Button button3;
    private int switchSytle = 0;
    private boolean isRecording = false;

    private SurfaceViewContainer glLocalSurfaceViewContainer;
    private SurfaceViewContainer glRemoteSurfaceViewContainer;


    private String username;

    private boolean blouderspeak = false;
    private boolean bunmute = true;

    private VideoEngineImpl mVideoEngineImp;
    private NativeVideoEngine mNVEngine;

    private boolean isBeautify;
    private boolean enableRomoteView = true;

    enum enVideoSource
    {
        kVideoSourceNull,
        kVideoSourceFrontCamera,
        kVideoSourceBackCamera,
        kVideoSourceScreen
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        final Intent intent = getIntent();

        username = intent.getStringExtra("username");
        //init nativeVideoEngine
        mVideoEngineImp = VideoEngineImpl.getInstance();
        mNVEngine = NativeVideoEngine.getInstance();

        isBeautify = false;

        //init surface view
        localSurfaceViewContainer = (RelativeLayout) findViewById(R.id.surfaceViewLocalContainer);
        remoteSurfaceViewContainer = (RelativeLayout) findViewById(R.id.surfaceViewRemoteContainer);
        glLocalSurfaceViewContainer = new SurfaceViewContainer(this);
        glRemoteSurfaceViewContainer = new SurfaceViewContainer(this);
        //add view
        localSurfaceViewContainer.addView(glLocalSurfaceViewContainer);
        remoteSurfaceViewContainer.addView(glRemoteSurfaceViewContainer);

        displayLocalViewCheckBox = (CheckBox)findViewById(R.id.checkBox1);
        sendLocalVideoCheckBox = (CheckBox)findViewById(R.id.checkBox2);
        cameraSwitchCheckBox = (CheckBox)findViewById(R.id.checkBox3);

        openRemoteVideoCheckBox = (CheckBox)findViewById(R.id.checkBox10);
        displayRemoteViewCheckBox = (CheckBox)findViewById(R.id.checkBox11);
        viewSomeOneCheckBox = (CheckBox)findViewById(R.id.checkBox12);

        disconnectButton = (ImageButton)findViewById(R.id.button_call_disconnect);
        toggleMuteButton = (ImageButton)findViewById(R.id.button_call_toggle_mic);
        toggleSpeakerButton = (ImageButton)findViewById(R.id.button_call_toggle_speak);

        button1 = (Button)findViewById(R.id.Button1);
        button2 = (Button)findViewById(R.id.Button2);
        button3 = (Button)findViewById(R.id.Button3);

        //init voice mic and speaker status
        NativeVoiceEngine.getInstance().setLouderSpeaker(blouderspeak);
        toggleSpeakerButton.setAlpha(blouderspeak ? 1.0f : 0.3f);

        NativeVoiceEngine.getInstance().setSendVoice(bunmute);
        toggleMuteButton.setAlpha(bunmute ? 1.0f : 0.3f);

        SurfaceView mRemoteSurfaceView = glRemoteSurfaceViewContainer.getSurfaceView();
        if(mRemoteSurfaceView!=null)
            mRemoteSurfaceView.setVisibility(View.INVISIBLE);
        viewSomeOneCheckBox.setVisibility(View.INVISIBLE);

        toggleMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bunmute = !bunmute;
                NativeVoiceEngine.getInstance().setSendVoice(bunmute);
                toggleMuteButton.setAlpha(bunmute ? 1.0f : 0.3f);
            }
        });

        toggleSpeakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blouderspeak = !blouderspeak;
                NativeVoiceEngine.getInstance().setLouderSpeaker(blouderspeak);
                toggleSpeakerButton.setAlpha(blouderspeak ? 1.0f : 0.3f);
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
                mNVEngine.destroyRenderView(glLocalSurfaceViewContainer.getSurfaceView());
                mNVEngine.destroyRenderView(glRemoteSurfaceViewContainer.getSurfaceView());
                finish();
            }
        });

        sendLocalVideoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(NATIVETEST)
                        mNVEngine.startSendVideo();
                    else
                        mVideoEngineImp.startSendVideo();
                    Toast.makeText(VideoActivity.this, "发送本地视频", Toast.LENGTH_LONG).show();
                }
                else {
                    if(NATIVETEST)
                        mNVEngine.stopSendVideo();
                    else
                        mVideoEngineImp.stopSendLocalVideo();
                    Toast.makeText(VideoActivity.this, "停止发送本地视频", Toast.LENGTH_LONG).show();
                }
            }
        });

        displayLocalViewCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mNVEngine.observerLocalVideoWindow(isChecked,glLocalSurfaceViewContainer.getSurfaceView());
                SurfaceView mLocalSurfaceView = glLocalSurfaceViewContainer.getSurfaceView();
                if(mLocalSurfaceView!=null)
                    mLocalSurfaceView.setVisibility(isChecked?View.VISIBLE:View.INVISIBLE);
                if(isChecked == false)
                    sendLocalVideoCheckBox.setChecked(isChecked);
            }
        });

        cameraSwitchCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(NATIVETEST)
                        mNVEngine.switchCamera(enVideoSource.kVideoSourceBackCamera.ordinal()); //back
                    else
                        mVideoEngineImp.switchCamera(false);
                }
                else {
                    if(NATIVETEST)
                        mNVEngine.switchCamera(enVideoSource.kVideoSourceFrontCamera.ordinal()); //front
                    else
                        mVideoEngineImp.switchCamera(true);
                }
                Toast.makeText(VideoActivity.this, "切换摄像头中......", Toast.LENGTH_LONG).show();
            }
        });

        openRemoteVideoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    enableRomoteView = true;
                    mNVEngine.observerRemoteTargetVideo("", glRemoteSurfaceViewContainer.getSurfaceView());
                    Toast.makeText(VideoActivity.this, "打开接受远端视频", Toast.LENGTH_LONG).show();
                }
                else {
                    enableRomoteView = false;
                    viewSomeOneCheckBox.setChecked(false);
                    mNVEngine.observerRemoteTargetVideo("", null);
                    Toast.makeText(VideoActivity.this, "关闭接受远端视频", Toast.LENGTH_LONG).show();
                }
                SurfaceView mRemoteSurfaceView = glRemoteSurfaceViewContainer.getSurfaceView();
                if(mRemoteSurfaceView!=null)
                    mRemoteSurfaceView.setVisibility(isChecked?View.VISIBLE:View.INVISIBLE);

                viewSomeOneCheckBox.setVisibility(isChecked?View.VISIBLE:View.INVISIBLE);
            }
        });

        /*displayRemoteViewCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mNativeVideoEngine.displayRemoteView(isChecked);
            }
        });*/

        viewSomeOneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String msg;
                if(isChecked) {
                    msg = "切换到单人模式观看";
                    mNVEngine.observerRemoteTargetVideo("", null);
                    mNVEngine.observerRemoteTargetVideo(username, glRemoteSurfaceViewContainer.getSurfaceView());
                }
                else {
                    msg = "切换到多人模式观看";
                    mNVEngine.observerRemoteTargetVideo(username, null);
                    if(enableRomoteView)
                        mNVEngine.observerRemoteTargetVideo("", glRemoteSurfaceViewContainer.getSurfaceView());
                }
                Toast.makeText(VideoActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mVideoEngineImp.onActivityResult(requestCode, resultCode, data, glLocalSurfaceViewContainer.getSurfaceView());
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
        mNVEngine.destroyRenderView(glLocalSurfaceViewContainer.getSurfaceView());
        mNVEngine.destroyRenderView(glRemoteSurfaceViewContainer.getSurfaceView());
        super.onBackPressed();
    }

    private void updateCheckBoxStatus(boolean status){
        sendLocalVideoCheckBox.setChecked(false);
        displayLocalViewCheckBox.setChecked(status);
        cameraSwitchCheckBox.setChecked(false);

        openRemoteVideoCheckBox.setChecked(false);
        displayRemoteViewCheckBox.setChecked(false);
        viewSomeOneCheckBox.setChecked(false);
    }

    public void jumpOnClick1(View view){
        switch (view.getId()){
            case R.id.Button1: {
                //updateCheckBoxStatus(true);
               // mNativeVideoEngine.setInitView();
               // mNativeVideoEngine.setInitView(glLocalSurfaceViewContainer.getSurfaceView(), glRemoteSurfaceViewContainer.getSurfaceView());
                //NativeVideoEngine1.getInstance().registerPrivate(this);

                //mVideoEngineImp.createPeerConnection();
                //mVideoEngineImp.Init();
                mVideoEngineImp.startScreenCapture(this);
                break;
            }
            case R.id.Button2: {
                //updateCheckBoxStatus(false);
                //mNativeVideoEngine.release();
                //mNativeVideoEngine.destroyRenderView(glLocalSurfaceViewContainer.getSurfaceView());
                //mNativeVideoEngine.destroyRenderView(glRemoteSurfaceViewContainer.getSurfaceView());
                //mNVEngine.release();
               // mNVEngine.destroyRenderView(glLocalSurfaceViewContainer.getSurfaceView());
                //mNVEngine.destroyRenderView(glRemoteSurfaceViewContainer.getSurfaceView());
                isBeautify = !isBeautify;
                mNVEngine.enableBeautify(isBeautify);
                if(isBeautify){
                    button2.setText("关闭美颜");
                    Toast.makeText(this, "打开美颜", Toast.LENGTH_LONG).show();
                }
                else
                {
                    button2.setText("打开美颜");
                    Toast.makeText(this, "关闭美颜", Toast.LENGTH_LONG).show();
                }

                break;
            }

            case R.id.Button3: {
                //mNVEngine.switchRemoteVideoShowStyle(switchSytle%6+1);
                //switchSytle = switchSytle + 1;
                isRecording = ! isRecording;
                if(isRecording) {
                    button3.setText("关闭录制");
                    NativeVoiceEngine.getInstance().startRecordConference("test", true,"222.73.196.99/hls");
                }
                else{
                    button3.setText("打开录制");
                    NativeVoiceEngine.getInstance().stopRecordConference();
                }

                break;
            }

            /*case R.id.button3: {
                //mNativeVideoEngine.sendLocalVideo(true);
                break;
            }

            case R.id.button4: {
               // mNativeVideoEngine.sendLocalVideo(true);
                break;
            }*/
        }
    }


}
