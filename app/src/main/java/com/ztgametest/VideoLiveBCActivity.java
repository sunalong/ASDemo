package com.ztgametest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ztgame.videoengine.NativeVideoEngine;
import com.ztgame.voiceengine.NativeVoiceEngine;

import java.util.Map;

public class VideoLiveBCActivity extends Activity {

    private ImageButton disconnectButton;

    private RelativeLayout remoteRelLayout;
    private TextView tvRoomId;
    private TextView tvUserName;

    private Map<String, SurfaceView> surfaceViewMap;

    private String username;
    private String roomId;

    private NativeVideoEngine mNVEngine;
    SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videolivebc);

        final Intent intent = getIntent();

        username = intent.getStringExtra("username");
        roomId = intent.getStringExtra("roomId");
        //init nativeVideoEngine
        mNVEngine = NativeVideoEngine.getInstance();

        remoteRelLayout = (RelativeLayout) findViewById(R.id.remoteContainer);

        tvRoomId = (TextView)findViewById(R.id.tv_roomId);
        tvUserName = (TextView)findViewById(R.id.tv_userName);

        tvRoomId.setText("当前房间号：" + roomId);
        tvUserName.setText("当前用户ID：" + username);

        disconnectButton = (ImageButton)findViewById(R.id.button_call_disconnect);

        mSurfaceView = mNVEngine.createRenderView(1);

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
                mNVEngine.destroyRenderView(mSurfaceView);
                finish();
            }
        });

        Init();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //mVideoEngineImp.onActivityResult(requestCode, resultCode, data, glLocalSurfaceViewContainer.getSurfaceView());
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }

    private void Init(){
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        remoteRelLayout.addView(mSurfaceView, param);
        int ret = mNVEngine.observerRemoteTargetVideo("", mSurfaceView);
    }

    @Override
    public void onBackPressed(){
        NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
        mNVEngine.destroyRenderView(mSurfaceView);
        super.onBackPressed();
    }
}
