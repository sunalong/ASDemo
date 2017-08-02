package com.ztgametest;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ztgame.voiceengine.NativeVoiceEngine;
import com.ztgame.voiceengine.RTChatSDKVoiceListener;
import com.ztgame.voiceengine.ReceiveDataFromC;

import java.io.IOException;
import java.util.Random;


public class AudioActivity extends Activity implements View.OnClickListener {

    private Button btnInitSdk;

    private Button btnJoinRoom;
    private Button btnLeaveRoom;
    private Button btnOpenSpeaker;
    private Button btnCloseSpeaker;

    private Button btnUpVolume;
    private Button btnDownVolume;
    private Button btnVoiceMute;
    private Button btnPlayMusic;
    private Button btnStopMusic;

    private EditText etRoomServer;
    private EditText etRoomId;
    private EditText etUserName;
    private EditText etUserKey;

    private NativeVoiceEngine rtChatSdk;
    ReceiveDataFromC receiveDataFromC;

    private final String TAG = "AudioActivity";

    private final int kVoiceOnly = 1;
    private final boolean isInnerNet = true;

    private String mAppKey;
    private String mAppId;
    private String mPlatformUrl;

    //内网key
    private static final String innerAppKey = "7324e82e18d9d16ca4783aa5f872adf54d17a0175f48fa7c1af0d80211dfff82";
    private static final String innerAppId = "1fcfaa5cdc01502e";
    private static final String innerPlatformServerUrl = "192.168.114.7:18888";

    //外网key
    private static final String outerAppId = "3768c59536565afb";
    private static final String outerAppKey = "df191ec457951c35b8796697c204382d0e12d4e8cb56f54df6a54394be74c5fe";
    //private static final String outerPlatformServerUrl = "115.159.251.79:8080";
    private static final String outerPlatformServerUrl = "room.audio.mztgame.com:8080";

    private MediaPlayer mediaPlayer;

    private String mResourcePath = Environment.getExternalStorageDirectory().
            getAbsolutePath();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rtChatSdk = NativeVoiceEngine.getInstance();
        rtChatSdk.register(this);
        rtChatSdk.setDebugLogEnabled(true);
        receiveDataFromC = new ReceiveDataFromC();
        receiveDataFromC.setRtChatSDKVoiceListener(new RTChatSDKVoiceListener() {
            @Override
            public void rtchatsdkListener(int cmdType, final int error, String dataPtr, int dataSize) {
                Log.i(TAG, dataPtr);
                switch (cmdType) {
                    case 1://初始化
                    {
                        Toast.makeText(AudioActivity.this, "初始化完毕" + error, Toast.LENGTH_SHORT).show();
                    }
                    break;
                    case 7://进入房间
                    {
                        Toast.makeText(AudioActivity.this, "进入房间" + error, Toast.LENGTH_SHORT).show();
                    }
                    break;
                    default:
                        break;
                }
            }
        });

        setContentView(R.layout.activity_audio);
        initView();

        setOnListener();
        etUserName.setText(getRandomString(10));
        etUserKey.setText(getRandomString(11));
        if (isInnerNet){
            mAppId = innerAppId;
            mAppKey = innerAppKey;
            mPlatformUrl = innerPlatformServerUrl;
        }else{
            mAppId = outerAppId;
            mAppKey = outerAppKey;
            mPlatformUrl = outerPlatformServerUrl;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        rtChatSdk.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void initView() {
        btnInitSdk = (Button) findViewById(R.id.btnInitSdk);

        etRoomId = (EditText) findViewById(R.id.etRoomId);
        etRoomServer = (EditText) findViewById(R.id.etRoomServer);

        etUserName = (EditText) findViewById(R.id.etUserName);
        etUserKey = (EditText) findViewById(R.id.etUserKey);

        btnJoinRoom = (Button) findViewById(R.id.btnJoinRoom);
        btnLeaveRoom = (Button) findViewById(R.id.btnLeaveRoom);

        btnOpenSpeaker = (Button) findViewById(R.id.btnOpenSpeaker);
        btnCloseSpeaker = (Button) findViewById(R.id.btnCloseSpeeker);

        btnUpVolume = (Button) findViewById(R.id.btnUpVolume);
        btnDownVolume = (Button) findViewById(R.id.btnDownVolume);
        btnVoiceMute = (Button) findViewById(R.id.btnVoiceMute);

        btnPlayMusic = (Button) findViewById(R.id.btnPlayMusic);
        btnStopMusic = (Button) findViewById(R.id.btnStopMusic);
    }

    @Override
    public void onDestroy() {
        rtChatSdk.unRegister();
        super.onDestroy();
    }

    private void setOnListener() {
        btnInitSdk.setOnClickListener(this);
        btnUpVolume.setOnClickListener(this);
        btnDownVolume.setOnClickListener(this);
        btnJoinRoom.setOnClickListener(this);
        btnLeaveRoom.setOnClickListener(this);
        btnOpenSpeaker.setOnClickListener(this);
        btnCloseSpeaker.setOnClickListener(this);
        btnVoiceMute.setOnClickListener(this);
        btnPlayMusic.setOnClickListener(this);
        btnStopMusic.setOnClickListener(this);
    }

    float volumeValue = 5;
    boolean mute = true;

    public MediaPlayer createLocalMp3(String file){
        MediaPlayer  mp=new MediaPlayer();
        try{
            mp.setDataSource(file);
        }  catch (IOException e) {
            e.printStackTrace();
        }
        return mp;
    }

    private void playMusic(){
        boolean createState=false;
        if(mediaPlayer==null){
            mediaPlayer=createLocalMp3(mResourcePath+"/test/1.mp3");
            createState=true;
        }

        mediaPlayer.setOnCompletionListener(new OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(mResourcePath+"/test/1.mp3");
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();

                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            if(createState){
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //开始播放音频
                mediaPlayer.start();
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    void stopMusic(){
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }

    @Override
    public void onClick(View v) {
        int retCode = 0;
        String userName;
        String userKey;
        switch (v.getId()) {
            case R.id.btnInitSdk:
                rtChatSdk.initSDK(mAppId, mAppKey);
                break;
            case R.id.btnJoinRoom:
                userName = etUserName.getText().toString().trim();
                userKey = etUserKey.getText().toString().trim();
                if (TextUtils.isEmpty(userName))
                    userName = "nameChange";
                rtChatSdk.setUserInfo(userName, userKey);

                String roomServerStr = etRoomServer.getText().toString().trim();
                if(TextUtils.isEmpty(roomServerStr)){
                    roomServerStr = mPlatformUrl;
                }
                if(isInnerNet)
                    rtChatSdk.customRoomServerAddr(roomServerStr);

                if (TextUtils.isEmpty(etRoomId.getText())) {
                    Toast.makeText(this, "请输入房间号", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    retCode = rtChatSdk.requestJoinPlatformRoom(etRoomId.getText().toString().trim(), kVoiceOnly, 4);
                    Toast.makeText(this, "进入房间返回的值：retCode:" + retCode, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnLeaveRoom:
                retCode = rtChatSdk.requestLeavePlatformRoom();
                Toast.makeText(this, "离开房间返回的值：retCode:" + retCode, Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOpenSpeaker:
                retCode = rtChatSdk.setLouderSpeaker(true);
                Toast.makeText(this, "打开扬声器:" + retCode, Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCloseSpeeker:
                retCode = rtChatSdk.setLouderSpeaker(false);
                Toast.makeText(this, "关闭扬声器:" + retCode, Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnVoiceMute:
                mute = !mute;
                //设置静音与否
                rtChatSdk.setSendVoice(mute);
                break;
            case R.id.btnUpVolume:
                volumeValue += 1;
                if (volumeValue >= 10)
                    volumeValue = 10;
                retCode = rtChatSdk.adjustSpeakerVolume(volumeValue);
                Toast.makeText(this, volumeValue + "加声音：retCode:" + retCode, Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnDownVolume:
                volumeValue -= 1;
                if (volumeValue <= 0)
                    volumeValue = 0;
                retCode = rtChatSdk.adjustSpeakerVolume(volumeValue);
                Toast.makeText(this, volumeValue + "减声音：retCode:" + retCode, Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnPlayMusic:
                rtChatSdk.startPlayFileAsMic(mResourcePath+"/test/music.wav");
                //playMusic();
                break;
            case R.id.btnStopMusic:
                rtChatSdk.stopPlayFileAsMic();
                //stopMusic();
                break;
        }
    }

    public static String getRandomString(int strLength) {
        String sourceChar = "abcdefghigklmnopqrstuvwxyz0123456789ABCDEFGHIGKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strLength; i++) {
            sb.append(sourceChar.charAt(random.nextInt(sourceChar.length())));
        }
        return sb.toString();
    }

    @Override
    public void onBackPressed(){
        NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
        super.onBackPressed();
    }
}
