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
    //内网key
    private static final String innerAppKey = "7324e82e18d9d16ca4783aa5f872adf54d17a0175f48fa7c1af0d80211dfff82";
    private static final String innerAppId = "1fcfaa5cdc01502e";
    private static final String innerPlatformServerUrl = "192.168.114.7:18888";
    private static final String innerLiveServerUrl = "192.168.114.6:8000";

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
    private String keyprefVideoCallEnabled;
    private String keyprefScreencapture;
    private String keyprefCamera2;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefCaptureQualitySlider;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefVideoCodec;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefNetwork;
    private String keyprefHwCodecAcceleration;
    private String keyprefCaptureToTexture;
    private String keyprefFlexfec;
    private String keyprefNoAudioProcessingPipeline;
    private String keyprefAecDump;
    private String keyprefOpenSLES;
    private String keyprefDisableBuiltInAec;
    private String keyprefDisableBuiltInAgc;
    private String keyprefDisableBuiltInNs;
    private String keyprefEnableLevelControl;
    private String keyprefDisplayHud;
    private String keyprefTracing;
    private String keyprefRoomServerUrl;
    private String keyprefRoom;
    private ArrayAdapter<String> adapter;
    private String keyprefEnableDataChannel;
    private String keyprefOrdered;

    private String keyprefMaxRetransmits;
    private String keyprefDataProtocol;
    private String keyprefNegotiated;
    private String keyprefLiveServerUrl;



    private String keyprefUsername;
    private boolean hadInit = false;
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
    private String platformUrl = "";
    private String liveServerUrl = "";
    private int activityStatus = 0;

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
                        if (error == 1) {
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
        keyprefVideoCallEnabled = getString(R.string.pref_videocall_key);
        keyprefScreencapture = getString(R.string.pref_screencapture_key);
        keyprefCamera2 = getString(R.string.pref_camera2_key);
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefCaptureQualitySlider = getString(R.string.pref_capturequalityslider_key);
        keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        keyprefVideoCodec = getString(R.string.pref_videocodec_key);
        keyprefHwCodecAcceleration = getString(R.string.pref_hwcodec_key);
        keyprefCaptureToTexture = getString(R.string.pref_capturetotexture_key);
        keyprefFlexfec = getString(R.string.pref_flexfec_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyprefNetwork = getString(R.string.pref_network_key);
        keyprefNoAudioProcessingPipeline = getString(R.string.pref_noaudioprocessing_key);
        keyprefAecDump = getString(R.string.pref_aecdump_key);
        keyprefOpenSLES = getString(R.string.pref_opensles_key);
        keyprefDisableBuiltInAec = getString(R.string.pref_disable_built_in_aec_key);
        keyprefDisableBuiltInAgc = getString(R.string.pref_disable_built_in_agc_key);
        keyprefDisableBuiltInNs = getString(R.string.pref_disable_built_in_ns_key);
        keyprefEnableLevelControl = getString(R.string.pref_enable_level_control_key);
        keyprefDisplayHud = getString(R.string.pref_displayhud_key);
        keyprefTracing = getString(R.string.pref_tracing_key);
        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoom = getString(R.string.pref_room_key);
        keyprefEnableDataChannel = getString(R.string.pref_enable_datachannel_key);
        keyprefOrdered = getString(R.string.pref_ordered_key);

        keyprefMaxRetransmits = getString(R.string.pref_max_retransmits_key);
        keyprefDataProtocol = getString(R.string.pref_data_protocol_key);
        keyprefNegotiated = getString(R.string.pref_negotiated_key);
        keyprefLiveServerUrl = getString(R.string.pref_live_server_url_key);

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

        //default access inner network for testing
        appid = innerAppId;
        appkey = innerAppKey;
        platformUrl = innerPlatformServerUrl;
        liveServerUrl = innerLiveServerUrl;


        //setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        platformUrl = sharedPrefGetString(R.string.pref_room_server_url_key,
                "platformIp", R.string.pref_room_server_url_default, false);

        liveServerUrl = sharedPrefGetString(R.string.pref_live_server_url_key, "liveServerIp", R.string.pref_room_server_url_default, false);

        rtChatSdk.setSdkParams("{\"LiveServerAddr\":\""+liveServerUrl + "\",\"RoomServerAddr\":\""  +  platformUrl + "\"}");
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
        startActivityForResult(intent, CONNECTION_REQUEST);
    }

    private void lauchLiveBCVideoActiviy(){
        Intent intent = new Intent(this, VideoLiveBCActivity.class);
        intent.putExtra("username", this.userName);
        intent.putExtra("roomId", this.roomId);
        startActivityForResult(intent, CONNECTION_REQUEST);
    }

    private void lauchSUFVideoActiviy(){
        Intent intent = new Intent(this, VideoSFUActivity.class);
        intent.putExtra("username", this.userName);
        intent.putExtra("roomId", this.roomId);
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
                   platformUrl = outerPlatformServerUrl;
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
