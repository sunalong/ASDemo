/**
 * Created by luke on 2017/2/21.
 */

package com.ztgametest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ztgame.voiceengine.NativeVoiceEngine;
import com.ztgame.voiceengine.RTChatSDKVoiceListener;
import com.ztgame.voiceengine.ReceiveDataFromC;

import java.util.Random;


public class ConnectAVActivity extends Activity {
    private static final String TAG = "ConnectAVActivity";
    private static final int CONNECTION_REQUEST = 1;
    private static final int REMOVE_FAVORITE_INDEX = 0;

    //外网key
    private static final String outerAppId = "3768c59536565afb";
    private static final String outerAppKey = "df191ec457951c35b8796697c204382d0e12d4e8cb56f54df6a54394be74c5fe";
    private static final String outerPlatformServerUrl = "115.159.251.79:8080";

    private final int kVoiceOnly = 1;
    private final int kVideo_normalDefinition = 3;
    private final int kVideo_highDefinition = 7;
    private final int kVideo_veryHighDefinition = 11;
    private final int kLookLiveBC = 16;


    private ImageButton connectButton;
    private EditText roomEditText;
    private SharedPreferences sharedPref;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefRoom;

    private String keyprefUsername;
    private NativeVoiceEngine rtChatSdk = null;
    ReceiveDataFromC receiveDataFromC;
    private String ObserverUserName = "";
    private String roomId = "";
    private String userName = "";
    private String userKey = "";
    private EditText etUserName;
    private EditText etUserKey;

    private String appid = "";
    private String appkey = "";
    private int activityStatus = 0;
    private boolean isLunched = false;

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
                        String msg = "SDK初始化失败 ------";
                        if (error == 1)
                        {
                            //hadInit = true;
                            msg = "SDK初始化成功 ------";
                        };
                        Toast.makeText(ConnectAVActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, msg + error);
                    }
                    break;
                    case 7://进入房间
                    {
                        String msg = "用户进入房间失败 ------";
                        if (error == 1 && !isLunched) {
                            msg = "用户进入房间成功 ------";
                            switch (activityStatus) {
                                case 0: {
                                    lauchTestVideoActiviy();
                                    break;
                                }
                                case 1: {
                                    lauchSUFVideoActiviy();
                                    break;
                                }
                                case 2: {
                                    lauchLiveBCVideoActiviy();
                                    break;
                                }
                                default:
                                    break;
                            }

                        }
                        Toast.makeText(ConnectAVActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, msg + error);
                    }
                    break;
                }
            }
        });

        // Get setting keys.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefRoom = getString(R.string.pref_room_key);
        setContentView(R.layout.activity_connect);

        keyprefUsername = getString(R.string.pref_view_someone_username_key);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etUserKey = (EditText) findViewById(R.id.etUserKey);

        etUserName.setText(getRandomString(5));
        etUserKey.setText(getRandomString(6));


        roomEditText = (EditText) findViewById(R.id.room_edittext);
        roomEditText.requestFocus();

        connectButton = (ImageButton) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(connectListener);
        //Init sdk
        final Intent intent = getIntent();

        appid = outerAppId;
        appkey = outerAppKey;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connect_menu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        rtChatSdk.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items.
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }  else {
            return super.onOptionsItemSelected(item);
        }
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
        isLunched = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isLunched = false;
        if (requestCode == CONNECTION_REQUEST) {
            Log.d(TAG, "Return: " + resultCode);
            setResult(resultCode);
            //finish();
        }
    }

    /**
    * Get a value from the shared preference or from the intent, if it does not
    * exist the default is used.
    */
    private String sharedPrefGetString(int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = getString(defaultId);
        if (useFromIntent) {
            String value = getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        }
    }

    /**
    * Get a value from the shared preference or from the intent, if it does not
    * exist the default is used.
    */
    private boolean sharedPrefGetBoolean(int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        boolean defaultValue = Boolean.valueOf(getString(defaultId));
        if (useFromIntent) {
            return getIntent().getBooleanExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getBoolean(attributeName, defaultValue);
        }
    }

    /**
    * Get a value from the shared preference or from the intent, if it does not
    * exist the default is used.
    */
    private int sharedPrefGetInteger(int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        if (useFromIntent) {
            return getIntent().getIntExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        }
    }

    private void connectToRoom(String roomId, int mediaType) {

//        platformUrl = sharedPrefGetString(R.string.pref_room_server_url_key,
//                "platformIp", R.string.pref_room_server_url_default, false);
//
//        liveServerUrl = sharedPrefGetString(R.string.pref_live_server_url_key, "liveServerIp", R.string.pref_room_server_url_default, false);
//
//        rtChatSdk.setSdkParams("{\"LiveServerAddr\":\""+liveServerUrl + "\",\"RoomServerAddr\":\""  +  platformUrl + "\"}");
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

    private void lauchTestVideoActiviy(){
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("username", ObserverUserName);
        isLunched = true;
        startActivityForResult(intent, CONNECTION_REQUEST);
    }

    private void lauchLiveBCVideoActiviy(){
        Intent intent = new Intent(this, VideoLiveBCActivity.class);
        intent.putExtra("username", this.userName);
        intent.putExtra("roomId", this.roomId);
        isLunched = true;
        startActivityForResult(intent, CONNECTION_REQUEST);
    }

    private void lauchSUFVideoActiviy(){
        Intent intent = new Intent(this, VideoSFUActivity.class);
        intent.putExtra("username", this.userName);
        intent.putExtra("roomId", this.roomId);
        isLunched = true;
        startActivityForResult(intent, CONNECTION_REQUEST);
    }

    private void launchActivity(boolean useValuesFromIntent, int runTimeMs) {

        // Video call enabled flag.
        boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
            CallActivity.EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
            CallActivity.EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

        // Check Capture to texture.
        boolean captureToTexture = true;

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (useValuesFromIntent) {
            videoWidth = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_WIDTH, 0);
            videoHeight = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 0);
        }
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution = sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                     Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (useValuesFromIntent) {
            cameraFps = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_FPS, 0);
        }
        if (cameraFps == 0) {
            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Start activity.

        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(CallActivity.EXTRA_USERNAME, ObserverUserName);
        intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
        intent.putExtra(CallActivity.EXTRA_SCREENCAPTURE, useScreencapture);
        intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
        intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
        intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);

        startActivityForResult(intent, CONNECTION_REQUEST);

    }

    public static String getRandomString(int strLength) {
        String sourceChar = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strLength; i++) {
            sb.append(sourceChar.charAt(random.nextInt(sourceChar.length())));
        }
        return sb.toString();
    }

    private final OnClickListener connectListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            userName = etUserName.getText().toString().trim();
            userKey = etUserKey.getText().toString().trim();
            if (TextUtils.isEmpty(userName))
                userName = "nameChange";
            rtChatSdk.setUserInfo(userName, userKey);
            ObserverUserName = sharedPref.getString(
                    keyprefUsername, getString(R.string.pref_username_default));
            if(TextUtils.isEmpty(ObserverUserName)){
                ObserverUserName = userName;
            }
            connectToRoom(roomEditText.getText().toString(), kVideo_normalDefinition);
        }
    };

    public void OnClick(View view){
        switch (view.getId()){
            case R.id.initButton: {
                String network = sharedPrefGetString(R.string.pref_network_key,
                        "networksetting", R.string.pref_network_default, false);
               if(network.equals("outer-net")){
                   appid = outerAppId;
                   appkey = outerAppKey;
               }
                //init SDK
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
                if(TextUtils.isEmpty(ObserverUserName)){
                    ObserverUserName = userName;
                }

                activityStatus = 0;

                connectToRoom(roomEditText.getText().toString(), kVideo_normalDefinition);

                break;
            }
            case R.id.button_live_test2: {
                userName = etUserName.getText().toString().trim();
                userKey = etUserKey.getText().toString().trim();
                if (TextUtils.isEmpty(userName))
                    userName = "nameChange";
                rtChatSdk.setUserInfo(userName, userKey);

                activityStatus = 2;

                connectToRoom(roomEditText.getText().toString(), kLookLiveBC);
                break;
            }
            case R.id.button_test3: {
                userName = etUserName.getText().toString().trim();
                userKey = etUserKey.getText().toString().trim();
                if (TextUtils.isEmpty(userName))
                    userName = "nameChange";
                rtChatSdk.setUserInfo(userName, userKey);
                ObserverUserName = sharedPref.getString(
                        keyprefUsername, getString(R.string.pref_username_default));
                if(TextUtils.isEmpty(ObserverUserName)){
                    ObserverUserName = userName;
                }
                activityStatus = 1;
                connectToRoom(roomEditText.getText().toString(), kVideo_normalDefinition);
                break;
            }
        }
    }
}
