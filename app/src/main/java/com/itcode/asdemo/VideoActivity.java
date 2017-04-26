package com.itcode.asdemo;

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

import com.itcode.asdemo.ui.SurfaceViewContainer;
import com.ztgame.videoengine.NativeVideoEngine;
import com.ztgame.voiceengine.NativeVoiceEngine;

public class VideoActivity extends Activity {

    private CheckBox viewSomeOneCheckBox;
    private CheckBox openRemoteVideoCheckBox;
    private CheckBox sendLocalVideoCheckBox;
    private CheckBox displayLocalViewCheckBox;
    private CheckBox cameraSwitchCheckBox;

    private ImageButton disconnectButton;
    private ImageButton toggleMuteButton;
    private ImageButton toggleSpeakerButton;
    private RelativeLayout localSurfaceViewContainer;
    private RelativeLayout remoteSurfaceViewContainer;

    private SurfaceViewContainer glLocalSurfaceViewContainer;
    private SurfaceViewContainer glRemoteSurfaceViewContainer;

    private String username;

    private boolean blouderspeak = false;
    private boolean bunmute = true;

    private NativeVideoEngine mNVEngine;

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
        mNVEngine = NativeVideoEngine.getInstance();
            mNVEngine.Init(this);
        initView();

        //init voice mic and speaker status
        NativeVoiceEngine.getInstance().setLouderSpeaker(blouderspeak);
        toggleSpeakerButton.setAlpha(blouderspeak ? 1.0f : 0.3f);

        NativeVoiceEngine.getInstance().setSendVoice(bunmute);
        toggleMuteButton.setAlpha(bunmute ? 1.0f : 0.3f);

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
                    mNVEngine.release();
                NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
                finish();
            }
        });

        sendLocalVideoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                        mNVEngine.startSendVideo();
                    Toast.makeText(VideoActivity.this, "发送本地视频", Toast.LENGTH_LONG).show();
                }
                else {
                        mNVEngine.stopSendVideo();
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
                    mLocalSurfaceView.setVisibility(isChecked? View.VISIBLE: View.INVISIBLE);
            }
        });

        cameraSwitchCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //mNativeVideoEngine.switchCamera(!isChecked);
                if(isChecked) {
                        mNVEngine.switchCamera(enVideoSource.kVideoSourceBackCamera.ordinal()); //back
                }
                else {
                        mNVEngine.switchCamera(enVideoSource.kVideoSourceFrontCamera.ordinal()); //front
                }
                Toast.makeText(VideoActivity.this, "切换摄像头中......", Toast.LENGTH_LONG).show();
            }
        });

        openRemoteVideoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                        mNVEngine.startObserverRemoteVideo(glRemoteSurfaceViewContainer.getSurfaceView());
                    Toast.makeText(VideoActivity.this, "打开接受远端视频", Toast.LENGTH_LONG).show();
                }
                else {
                        mNVEngine.stopObserverRemoteVideo();
                    Toast.makeText(VideoActivity.this, "关闭接受远端视频", Toast.LENGTH_LONG).show();
                }
                SurfaceView mRemoteSurfaceView = glRemoteSurfaceViewContainer.getSurfaceView();
                if(mRemoteSurfaceView!=null)
                    mRemoteSurfaceView.setVisibility(isChecked? View.VISIBLE: View.INVISIBLE);
            }
        });
        viewSomeOneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String msg;
                if(isChecked) {
                    msg = "切换到单人模式观看";
                        mNVEngine.observerSomeOneVideo(username);
                }
                else {
                    msg = "切换到多人模式观看";
                        mNVEngine.observerSomeOneVideo("");
                }
                Toast.makeText(VideoActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });


    }

    private void initView() {
        //init surface view
        localSurfaceViewContainer = (RelativeLayout) findViewById(R.id.surfaceViewLocalContainer);
        remoteSurfaceViewContainer = (RelativeLayout) findViewById(R.id.surfaceViewRemoteContainer);
        glLocalSurfaceViewContainer = new SurfaceViewContainer(this);
        glRemoteSurfaceViewContainer = new SurfaceViewContainer(this);
        //add view
        localSurfaceViewContainer.addView(glLocalSurfaceViewContainer);
        remoteSurfaceViewContainer.addView(glRemoteSurfaceViewContainer);

        sendLocalVideoCheckBox = (CheckBox)findViewById(R.id.checkBox1);
        displayLocalViewCheckBox = (CheckBox)findViewById(R.id.checkBox2);
        cameraSwitchCheckBox = (CheckBox)findViewById(R.id.checkBox3);

        openRemoteVideoCheckBox = (CheckBox)findViewById(R.id.checkBox10);
        viewSomeOneCheckBox = (CheckBox)findViewById(R.id.checkBox12);

        disconnectButton = (ImageButton)findViewById(R.id.button_call_disconnect);
        toggleMuteButton = (ImageButton)findViewById(R.id.button_call_toggle_mic);
        toggleSpeakerButton = (ImageButton)findViewById(R.id.button_call_toggle_speak);
    }

    @Override
    protected void onDestroy() {
            mNVEngine.destroyRenderView(glLocalSurfaceViewContainer.getSurfaceView());
            mNVEngine.destroyRenderView(glRemoteSurfaceViewContainer.getSurfaceView());
            mNVEngine.release();
        NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
        finish();
        super.onDestroy();
    }
}
