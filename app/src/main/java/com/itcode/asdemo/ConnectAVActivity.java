package com.itcode.asdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ztgame.voiceengine.NativeVoiceEngine;
import com.ztgame.voiceengine.RTChatSDKVoiceListener;
import com.ztgame.voiceengine.ReceiveDataFromC;

import java.util.Random;


public class ConnectAVActivity extends Activity {
    private static final String TAG = "ConnectAVActivity";
    private static final int CONNECTION_REQUEST = 1;

//    //内网key
//    private static final String innerAppKey = "7324e82e18d9d16ca4783aa5f872adf54d17a0175f48fa7c1af0d80211dfff82";
//    private static final String innerAppId = "1fcfaa5cdc01502e";
//    private static final String innerPlatformServerUrl = "192.168.114.7:18888";

    //外网key
    private static final String outerAppId = "3768c59536565afb";
    private static final String outerAppKey = "df191ec457951c35b8796697c204382d0e12d4e8cb56f54df6a54394be74c5fe";
//    private static final String outerPlatformServerUrl = "room.audio.mztgame.com";
    private static final String outerPlatformServerUrl = "115.159.251.79:8080";


    private final int kVideo_normalDefinition = 3;

    private EditText roomEditText;
    private SharedPreferences sharedPref;
    private String keyprefRoom;

    private String keyprefUsername;
    private NativeVoiceEngine rtChatSdk = null;
    private ReceiveDataFromC receiveDataFromC;
    private String userName = "";
    private String ObserverUserName = "";
    private String userKey = "";
    private EditText etUserName;
    private EditText etUserKey;

    private String appid = "";
    private String appkey = "";
    private String platformUrl = "";


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
                switch (cmdType) {
                    case 1://初始化
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String msg = "SDK初始化失败 ------";
                                if (error == 1) {
                                    msg = "SDK初始化成功 ------";
                                }
                                Toast.makeText(ConnectAVActivity.this, msg, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, msg + error);
                            }
                        });
                        break;
                    case 7://进入房间
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String msg = "用户进入房间失败 ------";
                                if (error == 1) {
                                    msg = "用户进入房间成功 ------";
                                    lauchTestVideoActiviy();
                                }
                                Toast.makeText(ConnectAVActivity.this, msg, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, msg + error);
                            }
                        });
                        break;
                }
            }
        });


        // Get setting keys.
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefRoom = getString(R.string.pref_room_key);

        setContentView(R.layout.activity_connect);

        keyprefUsername = getString(R.string.pref_view_someone_username_key);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etUserKey = (EditText) findViewById(R.id.etUserKey);

        etUserName.setText(getRandomString(10));
        etUserKey.setText(getRandomString(11));

        roomEditText = (EditText) findViewById(R.id.room_edittext);
        roomEditText.requestFocus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        rtChatSdk.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rtChatSdk.unRegister();
        rtChatSdk = null;
        this.finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        String room = roomEditText.getText().toString();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyprefRoom, room);
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        String room = sharedPref.getString(keyprefRoom, "");
        roomEditText.setText(room);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECTION_REQUEST) {
            Log.d(TAG, "Return: " + resultCode);
            setResult(resultCode);
        }
    }

    private void connectToRoom(String roomId) {
//若是外网，则不需要设置platformUrl
//        rtChatSdk.customRoomServerAddr(platformUrl);
        int retCode;
        if (roomId == null) {
            Toast.makeText(this, "请输入房间号", Toast.LENGTH_SHORT).show();
            return;
        } else {
            retCode = rtChatSdk.requestJoinPlatformRoom(roomId, kVideo_normalDefinition);
            Log.d(TAG, "进入房间返回的值：retCode:" + retCode);
        }
    }

    private void lauchTestVideoActiviy() {
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("username", ObserverUserName);
        startActivityForResult(intent, CONNECTION_REQUEST);
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

    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.initButton: {
//                //内网
//                appid = innerAppId;
//                appkey = innerAppKey;
//                platformUrl = innerPlatformServerUrl;
                //外网
                appid = outerAppId;
                appkey = outerAppKey;
                platformUrl = outerPlatformServerUrl;
                rtChatSdk.initSDK(appid, appkey);

                break;
            }
            case R.id.button_test1: {
                userName = etUserName.getText().toString().trim();
                userKey = etUserKey.getText().toString().trim();
                if (TextUtils.isEmpty(userName))
                    userName = "nameChange";
                rtChatSdk.setUserInfo(userName, userKey);

                ObserverUserName = sharedPref.getString(
                        keyprefUsername, getString(R.string.pref_username_default));
                if (TextUtils.isEmpty(ObserverUserName)) {
                    ObserverUserName = userName;
                }

                connectToRoom(roomEditText.getText().toString());

                break;
            }
        }
    }
}
