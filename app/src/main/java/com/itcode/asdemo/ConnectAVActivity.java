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
    public interface ReconnectListener {
        public void reconnectListener(boolean closed);
    }

    private boolean bTest = false;
    private String roomId = "";
    private static final String TAG = "ConnectAVActivity";
    private static final int CONNECTION_REQUEST = 1;

    //内网key
    private static final String innerAppKey = "7324e82e18d9d16ca4783aa5f872adf54d17a0175f48fa7c1af0d80211dfff82";
    private static final String innerAppId = "1fcfaa5cdc01502e";
    private static final String innerPlatformServerUrl = "192.168.114.7:18888";

    //外网key
    private static final String outerAppId = "3768c59536565afb";
    private static final String outerAppKey = "df191ec457951c35b8796697c204382d0e12d4e8cb56f54df6a54394be74c5fe";
    private static final String outerPlatformServerUrl = "room.audio.mztgame.com";
//    private static final String outerPlatformServerUrl = "115.159.251.79:8080";

    private final int kVideo_normalDefinition = 3;
    private final int kLookLiveBC = 16;
    public static ReconnectListener reconnectListener;
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

    private boolean haveStartActivity = false;

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
                        Log.i("Test", "进入房间收到的返回值：" + cmdType + " error:" + error + " dataPtr:" + dataPtr);
//                        Toast.makeText(ConnectAVActivity.this, dataPtr + System.currentTimeMillis(), Toast.LENGTH_LONG).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String msg = "用户进入房间失败 ------";

                                if (error == 1) {
                                    if (haveStartActivity) {//防断网重连
                                        Log.i("Test", "已经进入房间，不再次启动本Activity");
                                    } else {
                                        msg = "用户进入房间成功 ------";
                                        lauchTestVideoActiviy();
                                        haveStartActivity = true;
                                        Toast.makeText(ConnectAVActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                    Log.i(TAG, msg + error);
                                }
                            }
                        });
                        break;
                }
            }
        });

        reconnectListener = new ReconnectListener() {
            @Override
            public void reconnectListener(boolean closed) {
                haveStartActivity = false;
                Log.i("Test", "回调，重置haveStartActivity为：" + haveStartActivity);
            }
        };

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

    private void lauchSUFVideoActiviy() {
        Intent intent = new Intent(this, VideoSFUActivity.class);
        intent.putExtra("username", this.userName);
        intent.putExtra("roomId", this.roomId);
        startActivityForResult(intent, CONNECTION_REQUEST);
    }

    private void connectToRoom(String roomId) {
//若是外网，则不需要设置platformUrl
//        rtChatSdk.customRoomServerAddr(platformUrl);
        int retCode;
        if (roomId == null) {
            Toast.makeText(this, "请输入房间号", Toast.LENGTH_SHORT).show();
            return;
        } else {
            this.roomId = roomId;
            retCode = rtChatSdk.requestJoinPlatformRoom(roomId, kVideo_normalDefinition, 6);
            Log.d(TAG, "进入房间返回的值：retCode:" + retCode);
        }
    }


    private void connectToRoom(String roomId, int mediaType) {
        //若是外网，则不需要设置platformUrl
        if(isInnerNet){
        rtChatSdk.setSdkParams("{\"LiveServerAddr\":\"" + platformUrl + "\",\"RoomServerAddr\":\"" + platformUrl + "\"}");
        }
        int retCode;
        if (roomId == null) {
            Toast.makeText(this, "请输入房间号", Toast.LENGTH_SHORT).show();
            return;
        } else {
            this.roomId = roomId;
            retCode = rtChatSdk.requestJoinPlatformRoom(roomId, mediaType, 4);
            Log.d(TAG, "进入房间返回的值：retCode:" + retCode);
        }
    }

    private void lauchTestVideoActiviy() {

        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("username", ObserverUserName);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

    private static boolean isInnerNet = true;

    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.initButton: {
                if (isInnerNet) {
                    //内网
                    appid = innerAppId;
                    appkey = innerAppKey;
                    platformUrl = innerPlatformServerUrl;

                } else {
//                外网
                    appid = outerAppId;
                    appkey = outerAppKey;
                    platformUrl = outerPlatformServerUrl;
                }
                rtChatSdk.initSDK(appid, appkey);

                break;
            }
            case R.id.button_test1:

                bTest = true;
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

                connectToRoom(roomEditText.getText().toString(), kVideo_normalDefinition);

                break;

            case R.id.button_live_test2:
                bTest = true;
                userName = etUserName.getText().toString().trim();
                userKey = etUserKey.getText().toString().trim();
                if (TextUtils.isEmpty(userName))
                    userName = "nameChange";
                rtChatSdk.setUserInfo(userName, userKey);

                connectToRoom(roomEditText.getText().toString(), kLookLiveBC);
                break;
//            case R.id.button_test3:
//                Intent intent = new Intent(this, CallActivity.class);
//                startActivityForResult(intent, CONNECTION_REQUEST);
//                break;
        }
    }
}
