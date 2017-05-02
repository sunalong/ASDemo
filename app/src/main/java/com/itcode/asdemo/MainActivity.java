/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itcode.asdemo;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.itcode.asdemo.R;
import com.ztgame.voiceengine.NativeVoiceEngine;
import com.ztgame.voiceengine.RTChatSDKVoiceListener;
import com.ztgame.voiceengine.ReceiveDataFromC;

import org.json.JSONObject;

import java.util.Random;

//import com.alibaba.fastjson.JSON;

public class MainActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private Button btnInitSdk;
    private Button btnCustomRoomServerAddr;
    private Button btnJoinRoom;
    private Button btnLeaveRoom;
    private Button btnSetLouderSpeaker;
    private Button btnAdjustVolume;

    private RadioGroup rgVoiceToText;

    private Button btnUpVolume;
    private Button btnDownVolume;
    private Button btnVoiceMute;
    private Button btnGetAudioSetting;

    private Button btnSetParams;
    private Button btnCallback;

    private Button btnRecord;
    private Button btnPlay;
    private Button btnCancelRecord;
    private Button btnChangeUser;

    private EditText etRoomServer;
    private EditText etRoomId;
    private EditText etUserName;
    private EditText etUserKey;

    private NativeVoiceEngine rtChatSdk;
    ReceiveDataFromC receiveDataFromC;
    private static String FileURL = "http://giant.audio.mztgame.com/wangpan.php";
    private String downloadUrlLocal;
    private String voiceTextLocal;
    private EditText etResult;
    private static String TAG = MainActivity.class.getSimpleName();

    private final int kVoiceOnly = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rtChatSdk = new NativeVoiceEngine();
        rtChatSdk.register(this);
        rtChatSdk.setDebugLogEnabled(true);
        receiveDataFromC = new ReceiveDataFromC();
        receiveDataFromC.setRtChatSDKVoiceListener(new RTChatSDKVoiceListener() {
            @Override
            public void rtchatsdkListener(int cmdType, final int error, String dataPtr, int dataSize) {
                Log.i(TAG, "-回调到MainActivity中-jni_log----cmdType:" + cmdType + " error:" + error + " dataPtr:" + dataPtr + " dataSize:" + dataSize);
                switch (cmdType) {
                    case 1://初始化
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "初始化完毕" + error, 0).show();
                            }
                        });
                        Log.i(TAG, "-MainActivity-初始化完毕------lala" + error);
                        break;
                    case 7://进入房间
                        Log.i(TAG, "-MainActivity-joinRoom------lala" + error);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "进入房间" + error, 0).show();
                            }
                        });
                        break;
                    case 25://录音结束，上传成功
                        FileData fileData = getDataFromJson(dataPtr);
                        String voiceText;
                        float duration;
                        if (fileData == null) {
                            downloadUrlLocal = null;
                            voiceText = null;
                            duration = 0;
                        } else {
                            downloadUrlLocal = fileData.getUrl();
                            voiceText = fileData.getText();
                            duration = Float.valueOf(fileData.getDuration());
                        }
                        Log.i(TAG, "-录音结束-jni_log----error:" + error + " downloadUrlLocal:" + downloadUrlLocal + " duration:" + duration + " text:" + voiceText);
                        voiceTextLocal = voiceText;
                        etResult.setText("回调后的翻译：" + voiceTextLocal);
                        break;
                    case 35://播放结束
                        Toast.makeText(MainActivity.this, "播放完毕", 0).show();
                        Log.i(TAG, "-MainActivity-播放完毕------error" + error + " " + dataSize);
                        break;
                }
            }
        });

        setContentView(R.layout.activity_main);
        initView();

        setOnListener();
        etUserName.setText(getRandomString(10));
        etUserKey.setText(getRandomString(11));
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//        rtChatSdk.requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        rtChatSdk.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static String AppID = "58536195";

    private void initView() {
        rgVoiceToText = (RadioGroup) findViewById(R.id.rgVoiceToText);
        btnInitSdk = (Button) findViewById(R.id.btnInitSdk);
        btnCustomRoomServerAddr = (Button) findViewById(R.id.btnCustomRoomServerAddr);
        btnJoinRoom = (Button) findViewById(R.id.btnJoinRoom);
        btnLeaveRoom = (Button) findViewById(R.id.btnLeaveRoom);
        btnSetLouderSpeaker = (Button) findViewById(R.id.btnSetLouderSpeaker);
        btnAdjustVolume = (Button) findViewById(R.id.btnAdjustVolume);

        btnUpVolume = (Button) findViewById(R.id.btnUpVolume);
        btnDownVolume = (Button) findViewById(R.id.btnDownVolume);
        btnVoiceMute = (Button) findViewById(R.id.btnVoiceMute);
        btnGetAudioSetting = (Button) findViewById(R.id.btnGetAudioSetting);

        btnSetParams = (Button) findViewById(R.id.btnSetParams);
        btnCallback = (Button) findViewById(R.id.btnCallback);

        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnCancelRecord = (Button) findViewById(R.id.btnCancelRecord);
        btnChangeUser = (Button) findViewById(R.id.btnChangeUser);

        etRoomServer = (EditText) findViewById(R.id.etRoomServer);
        etRoomId = (EditText) findViewById(R.id.etRoomId);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etUserKey = (EditText) findViewById(R.id.etUserKey);
        etResult = (EditText) findViewById(R.id.etResult);
    }

    @Override
    public void onDestroy() {
        rtChatSdk.unRegister();
        super.onDestroy();
    }

    private void setOnListener() {
        rgVoiceToText.setOnCheckedChangeListener(this);
        btnUpVolume.setOnClickListener(this);
        btnDownVolume.setOnClickListener(this);
        btnInitSdk.setOnClickListener(this);
        btnCustomRoomServerAddr.setOnClickListener(this);
        btnJoinRoom.setOnClickListener(this);
        btnLeaveRoom.setOnClickListener(this);
        btnSetLouderSpeaker.setOnClickListener(this);
        btnAdjustVolume.setOnClickListener(this);
        btnVoiceMute.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnRecord.setOnClickListener(this);
        btnCallback.setOnClickListener(this);
        btnSetParams.setOnClickListener(this);
        btnCancelRecord.setOnClickListener(this);
        btnChangeUser.setOnClickListener(this);
        btnGetAudioSetting.setOnClickListener(this);
    }

    float volumeValue = 5;
    boolean mute = true;
    boolean isPlaying = false;
    boolean isRecording = false;

    @Override
    public void onClick(View v) {
        int retCode = 0;
        String userName;
        String userKey;
        switch (v.getId()) {
            case R.id.btnChangeUser:
                userName = etUserName.getText().toString().trim();
                userKey = etUserKey.getText().toString().trim();
                if (TextUtils.isEmpty(userName))
                    userName = "nameChange";
                rtChatSdk.setUserInfo(userName, userKey);
                Toast.makeText(MainActivity.this, "改变语音聊天登录用户信息", 0).show();
                break;
            case R.id.btnCancelRecord:
                rtChatSdk.cancelRecordedVoice();
                isRecording = false;
                btnRecord.setText("已经取消录音");
                Toast.makeText(MainActivity.this, "取消录音", 0).show();
                break;
            case R.id.btnRecord:
                if (isRecording) {
                    rtChatSdk.stopRecordVoice();
                    btnRecord.setText("已经停止录音");
                    //Todo:停止翻译
                } else {
                    boolean recordVoiceRet = rtChatSdk.startRecordVoice(convertVoiceToText);
                    Toast.makeText(this, " startRecordVoicecode=" + recordVoiceRet, 0).show();
//                    if (!recordVoiceRet) {//为false时（如当权限被禁止时）录音不成功，所以状态不变化
//                      //现被注释原因：4.0时即使权限被禁，依然会执行录音，只是不会成功录音、上传罢了，所以状态需要改变
//                        return;
//                    }
                    btnRecord.setText("正在录音");
                }
                isRecording = !isRecording;
                break;
            case R.id.btnPlay:
                if (isPlaying) {
                    rtChatSdk.stopPlayLocalVoice();
                    btnPlay.setText("已经停止播放");
                } else {
                    if (downloadUrlLocal == null) {//当4.0禁止权限时，可以初始化，但不可录音，所以此url有可能为空
                        Toast.makeText(this, "downloadUrlLocal:" + downloadUrlLocal, 0).show();
                        return;
                    }
                    rtChatSdk.startPlayLocalVoice(downloadUrlLocal);
                    btnPlay.setText("正在播放");
                }
                isPlaying = !isPlaying;
                break;
            case R.id.btnUpVolume:
                volumeValue += 1;
                if (volumeValue >= 10)
                    volumeValue = 10;
                retCode = rtChatSdk.adjustSpeakerVolume(volumeValue);
                Toast.makeText(this, volumeValue + "加声音：retCode:" + retCode, 0).show();
                break;
            case R.id.btnDownVolume:
                volumeValue -= 5;
                if (volumeValue <= 0)
                    volumeValue = 0;
                retCode = rtChatSdk.adjustSpeakerVolume(volumeValue);
                Toast.makeText(this, volumeValue + "减声音：retCode:" + retCode, 0).show();

                break;
            case R.id.btnInitSdk:
                userName = etUserName.getText().toString().trim();
                userKey = etUserKey.getText().toString().trim();
                if (TextUtils.isEmpty(userName))
                    userName = "fuckName";
                rtChatSdk.initSDK("3768c59536565afb", "df191ec457951c35b8796697c204382d0e12d4e8cb56f54df6a54394be74c5fe");
                Toast.makeText(this, "初始化返回的值：retCode:" + retCode, 0).show();
                break;
            case R.id.btnCustomRoomServerAddr:
                userName = etUserName.getText().toString().trim();
                userKey = etUserKey.getText().toString().trim();
                if (TextUtils.isEmpty(userName))
                    userName = "nameChange";
                rtChatSdk.setUserInfo(userName, userKey);

                rtChatSdk.setParams(FileURL, AppID);

                String roomServerStr = etRoomServer.getText().toString().trim();
                if(TextUtils.isEmpty(roomServerStr)){
                    Toast.makeText(this, "请输入IP:", 0).show();
                    return;
                }
                rtChatSdk.customRoomServerAddr(roomServerStr);
                break;
            case R.id.btnJoinRoom:
                if (TextUtils.isEmpty(etRoomId.getText())) {
                    Toast.makeText(this, "请输入房间号", 0).show();
                    return;
                } else {
                    retCode = rtChatSdk.requestJoinPlatformRoom(etRoomId.getText().toString().trim(), kVoiceOnly);
                    Toast.makeText(this, "进入房间返回的值：retCode:" + retCode, 0).show();
                }

                break;
            case R.id.btnLeaveRoom:
                retCode = rtChatSdk.requestLeavePlatformRoom();
                Toast.makeText(this, "离开房间返回的值：retCode:" + retCode, 0).show();
                break;
            case R.id.btnSetLouderSpeaker:
                retCode = rtChatSdk.setLouderSpeaker(true);
                Toast.makeText(this, "打开扬声器:" + retCode, 0).show();
                break;
            case R.id.btnAdjustVolume:
                //TODO:获取操作设备的调整值
                retCode = rtChatSdk.setLouderSpeaker(false);
                Toast.makeText(this, "关闭扬声器:" + retCode, 0).show();
                break;
            case R.id.btnVoiceMute:
                //设置静音与否
                rtChatSdk.setSendVoice(!mute);
                mute = !mute;
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

    private boolean convertVoiceToText = false;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int retCode = -1;
        switch (checkedId) {
            case R.id.rbVoiceToText:
                convertVoiceToText = true;
                break;
            case R.id.rbVoiceToTextStop:
                convertVoiceToText = false;
                break;
        }
    }


    private static FileData getDataFromJson(String msg) {
        try {
            if (TextUtils.isEmpty(msg))
                return null;
            Log.i(TAG, "MainActivity delete alibaba:" + msg);
            JSONObject jsonObject = new JSONObject(msg);
            String url = jsonObject.getString("url");
            String duration = jsonObject.getString("duration");
            String filesize = jsonObject.getString("filesize");
            String text = jsonObject.getString("text");
            String labelid = jsonObject.getString("labelid");
            FileData user = new FileData();
            user.setDuration(duration);
            user.setUrl(url);
            user.setFilesize(filesize);
            user.setText(text);
            user.setLabelid(labelid);
            Log.i(TAG, "MainActivity delete alibaba:" + user.toString());
            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
