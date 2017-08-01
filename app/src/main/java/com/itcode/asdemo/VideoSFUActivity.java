package com.itcode.asdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ztgame.videoengine.NativeVideoEngine;
import com.ztgame.voiceengine.NativeVoiceEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoSFUActivity extends Activity {

    private ImageButton disconnectButton;
    private ImageButton toggleMuteButton;
    private ImageButton toggleSpeakerButton;
    private RelativeLayout remoteRelLayout;
    private TextView tvRoomId;
    private TextView tvUserName;

    private List<Integer> removeId = new ArrayList<Integer>();

    private Button button1;
    private Button button2;
    private int switchSytle = 0;

    private Map<String, SurfaceView> surfaceViewMap;

    private String username;
    private String roomId;

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
        setContentView(R.layout.activity_videosfu);

        final Intent intent = getIntent();

        username = intent.getStringExtra("username");
        roomId = intent.getStringExtra("roomId");
        //init nativeVideoEngine
        mNVEngine = NativeVideoEngine.getInstance();

        surfaceViewMap = new HashMap<String, SurfaceView>();


        remoteRelLayout = (RelativeLayout) findViewById(R.id.remoteContainer);

        tvRoomId = (TextView)findViewById(R.id.tv_roomId);
        tvUserName = (TextView)findViewById(R.id.tv_userName);

        tvRoomId.setText("当前房间号：" + roomId);
        tvUserName.setText("当前用户ID：" + username);

        disconnectButton = (ImageButton)findViewById(R.id.button_call_disconnect);
        toggleMuteButton = (ImageButton)findViewById(R.id.button_call_toggle_mic);
        toggleSpeakerButton = (ImageButton)findViewById(R.id.button_call_toggle_speak);

        button1 = (Button)findViewById(R.id.Button1);
        button2 = (Button)findViewById(R.id.Button2);

        //init voice mic and speaker status
        NativeVoiceEngine.getInstance().setLouderSpeaker(blouderspeak);
        toggleSpeakerButton.setAlpha(blouderspeak ? 1.0f : 0.3f);

        NativeVoiceEngine.getInstance().setSendVoice(bunmute);
        toggleMuteButton.setAlpha(bunmute ? 1.0f : 0.3f);

        NativeVoiceEngine.getInstance().registerEventHandler(mListener);

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


                NativeVoiceEngine.getInstance().registerEventHandler(null);
                NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
                for(String key : surfaceViewMap.keySet()){
                    SurfaceView mSurfaceView = surfaceViewMap.get(key);
                    if(mSurfaceView == null)
                        return;

                    remoteRelLayout.removeView(mSurfaceView);
                    mNVEngine.destroyRenderView(mSurfaceView);
                }
                surfaceViewMap.clear(); ;
                removeId.clear();

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

    private int count = 10;

    private void setLayoutRule(SurfaceView mSurfaceView, int index){
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;
        int px = 200*metric.densityDpi/DisplayMetrics.DENSITY_DEFAULT;
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(width/2, px);
        mSurfaceView.setId(index);
        switch (index){
            case 10: {
                param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            }
            case 11: {
                param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            }
            case 12: {
                param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            }
            case 13: {
                param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            }
            default:
                break;
        }
        remoteRelLayout.addView(mSurfaceView, param);
    }

    private void addSurfaceView(String userId){
        SurfaceView mSurfaceView = mNVEngine.createRenderView();
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

        int ret = mNVEngine.observerRemoteTargetVideo(userId, mSurfaceView);
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

    private void removeSurfaceView(String userId){
        SurfaceView mSurfaceView = surfaceViewMap.get(userId);
        if(mSurfaceView == null)
            return;
        mNVEngine.observerRemoteTargetVideo(userId, null);
        mSurfaceView.setVisibility(View.INVISIBLE);
        int id = mSurfaceView.getId();
        if(id == (count-1)){
            count--;
        }
        else{
            removeId.add(id);
        }
        remoteRelLayout.removeView(mSurfaceView);
        mNVEngine.destroyRenderView(mSurfaceView);
        surfaceViewMap.remove(userId) ;
    }

    private void Init(){
        mNVEngine.startSendVideo();
        addSurfaceView(username);
    }

    private NativeVoiceEngine.EventHandler mListener = new NativeVoiceEngine.EventHandler() {
        @Override
        public void onEventUserJoinRoom(String uid){
            Toast.makeText(VideoSFUActivity.this, uid, Toast.LENGTH_LONG).show();
            String uuid = uid.split("\\[")[1].split("\\]")[0].split("\"")[1];
            addSurfaceView(uuid);
        }
        @Override
        public void onEventUserLeaveRoom(String uid){
            String uuid = uid.split("\\[")[1].split("\\]")[0].split("\"")[1];
            Toast.makeText(VideoSFUActivity.this, uuid, Toast.LENGTH_LONG).show();
            removeSurfaceView(uuid);
        }
    };

    @Override
    public void onBackPressed(){
        NativeVoiceEngine.getInstance().registerEventHandler(null);
        NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
        for(String key : surfaceViewMap.keySet()){
            SurfaceView mSurfaceView = surfaceViewMap.get(key);
            if(mSurfaceView == null)
                return;
            remoteRelLayout.removeView(mSurfaceView);
            mNVEngine.destroyRenderView(mSurfaceView);
        }
        surfaceViewMap.clear(); ;
        removeId.clear();

        super.onBackPressed();
    }



    public void jumpOnClick1(View view){
        switch (view.getId()){
            case R.id.Button1: {
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
                                    addSurfaceView(input);
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

                break;
            }
            case R.id.Button2: {
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
                                    removeSurfaceView(input);
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

                break;
            }

//            case R.id.Button3: {
                //mNVEngine.switchRemoteVideoShowStyle(switchSytle%6+1);
                //mNVEngine.switchUserShowPostionIndex("[\"\",\"\",\"123\",\"456\"]");
                //switchSytle = switchSytle + 1;
//                break;
//            }

        }
    }


}
