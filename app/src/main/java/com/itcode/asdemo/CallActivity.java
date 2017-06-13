package com.itcode.asdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.itcode.asdemo.ui.PercentFrameLayout;
import com.ztgame.videoengine.NativeVideoEngine;
import com.ztgame.videoengine.RTChatVideoClient;
import com.ztgame.videoengine.VideoEngineImpl;
import com.ztgame.voiceengine.NativeVoiceEngine;

import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SurfaceViewRenderer;

public class CallActivity extends Activity implements RTChatVideoClient.RTChatVideoEvents,CallFragment.OnCallEvents {
  private static final String TAG = "CallRTCClient";

  public static final String EXTRA_USERNAME = "com.ztgametest.USERNAME";
  public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
  public static final String EXTRA_SCREENCAPTURE = "org.appspot.apprtc.SCREENCAPTURE";
  public static final String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
  public static final String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
  public static final String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";


  private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;


  // Peer connection statistics callback period in ms.
  private static final int STAT_CALLBACK_PERIOD = 1000;
  // Local preview screen position before call is connected.
  private static final int LOCAL_X_CONNECTING = 0;
  private static final int LOCAL_Y_CONNECTING = 0;
  private static final int LOCAL_WIDTH_CONNECTING = 100;
  private static final int LOCAL_HEIGHT_CONNECTING = 100;
  // Local preview screen position after call is connected.
  private static final int LOCAL_X_CONNECTED = 72;
  private static final int LOCAL_Y_CONNECTED = 72;
  private static final int LOCAL_WIDTH_CONNECTED = 25;
  private static final int LOCAL_HEIGHT_CONNECTED = 25;
  // Remote video screen position
  private static final int REMOTE_X = 0;
  private static final int REMOTE_Y = 0;
  private static final int REMOTE_WIDTH = 100;
  private static final int REMOTE_HEIGHT = 100;
  private RTChatVideoClient rtchatvideoclient = null;
  private SurfaceViewRenderer localRender;
  private SurfaceViewRenderer remoteRenderScreen;
  private PercentFrameLayout localRenderLayout;
  private PercentFrameLayout remoteRenderLayout;
  private ScalingType scalingType;
  private Toast logToast;
  private boolean activityRunning;
  private boolean isError;
  private boolean callControlFragmentVisible = true;
  private long callStartedTimeMs = 0;
  private boolean micEnabled = true;
  private boolean screencaptureEnabled = false;
  private static Intent mediaProjectionPermissionResultData;
  private static int mediaProjectionPermissionResultCode;

  // Controls
  private CallFragment callFragment;
  private HudFragment hudFragment;

  private boolean videoCallEnabled;
  private String userName = "";

  private VideoEngineImpl mVideoEngineImp;
  private NativeVideoEngine mNVEngine;
  private boolean isFrontCamera;



  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set window styles for fullscreen-window size. Needs to be done before
    // adding content.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    /*getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON
        | LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_SHOW_WHEN_LOCKED
        | LayoutParams.FLAG_TURN_SCREEN_ON);
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    */
    setContentView(R.layout.activity_call);

    scalingType = ScalingType.SCALE_ASPECT_FILL;

    // Create UI controls.
    //localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
    //remoteRenderScreen = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
    localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
    remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);
    callFragment = new CallFragment();
    hudFragment = new HudFragment();

    // Show/hide call control fragment on view click.
    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        toggleCallControlFragmentVisibility();
      }
    };

    final Intent intent = getIntent();
    userName = intent.getStringExtra(EXTRA_USERNAME);

    mVideoEngineImp = VideoEngineImpl.getInstance();
    mNVEngine = NativeVideoEngine.getInstance();
    isFrontCamera = true;

    // Create video renderers.
    //rootEglBase = EglBase.create();
    //localRender.init(rootEglBase.getEglBaseContext(), null);
    //remoteRenderScreen.init(rootEglBase.getEglBaseContext(), null);

    localRender =(SurfaceViewRenderer)mNVEngine.createRenderView();
    remoteRenderScreen = (SurfaceViewRenderer)mNVEngine.createRenderView();



    localRenderLayout.addView(localRender);
    remoteRenderLayout.addView(remoteRenderScreen);

    localRender.setOnClickListener(listener);
    remoteRenderScreen.setOnClickListener(listener);

    localRender.setZOrderMediaOverlay(true);
    localRender.setEnableHardwareScaler(true /* enabled */);
    remoteRenderScreen.setEnableHardwareScaler(true /* enabled */);
    //updateVideoView();


    int videoWidth = intent.getIntExtra(EXTRA_VIDEO_WIDTH, 0);
    int videoHeight = intent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0);

    screencaptureEnabled = intent.getBooleanExtra(EXTRA_SCREENCAPTURE, false);
    // If capturing format is not specified for screencapture, use screen resolution.
    if (screencaptureEnabled /*&& videoWidth == 0 && videoHeight == 0*/) {
      DisplayMetrics displayMetrics = new DisplayMetrics();
      WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
      windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
      videoWidth = displayMetrics.widthPixels;
      videoHeight = displayMetrics.heightPixels;
    }

    videoCallEnabled = true;

    // Send intent arguments to fragments.
    callFragment.setArguments(intent.getExtras());
    hudFragment.setArguments(intent.getExtras());
    // Activate call and HUD fragments and start the call.
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    ft.add(R.id.call_fragment_container, callFragment);
    ft.add(R.id.hud_fragment_container, hudFragment);
    ft.commit();

    if (screencaptureEnabled) {
      MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
      startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }else {
      startCall();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
      return;
    mediaProjectionPermissionResultCode = resultCode;
    mediaProjectionPermissionResultData = data;
    startCall();
  }


  // Activity interfaces
  /*@Override
  public void onStop() {
    super.onStop();
    activityRunning = false;
    // Don't stop the video when using screencapture to allow user to show other apps to the remote
    // end.
   if (rtchatvideoclient != null && !screencaptureEnabled) {
     rtchatvideoclient.stopVideoSource();
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    activityRunning = true;
    // Video is not paused for screencapture. See onPause.
    //if (rtchatvideoclient != null && !screencaptureEnabled) {
    //  rtchatvideoclient.startVideoSource();
    //}
  }
*/
  @Override
  protected void onDestroy() {
    //Thread.setDefaultUncaughtExceptionHandler(null);

    if (logToast != null) {
      logToast.cancel();
    }
    activityRunning = false;

    super.onDestroy();
  }


  @Override
  public void onBackPressed(){
    mNVEngine.destroyRenderView(localRender);
    mNVEngine.destroyRenderView(remoteRenderScreen);
    NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
    super.onBackPressed();
  }


  // CallFragment.OnCallEvents interface implementation.
  @Override
  public void onCallHangUp() {
    disconnect();
  }

  @Override
  public void onCameraSwitch() {
    isFrontCamera = ! isFrontCamera;
    if(isFrontCamera) {
      mNVEngine.switchCamera(VideoActivity.enVideoSource.kVideoSourceFrontCamera.ordinal()); //front
    }
    else {
      mNVEngine.switchCamera(VideoActivity.enVideoSource.kVideoSourceBackCamera.ordinal()); //back
    }
  }

  @Override
  public void onOneViewSwitch(boolean isChecked) {
    String msg;
    if (isChecked) {
      msg = "切换到单人模式观看";
      mNVEngine.observerSomeOneVideo(userName);
    }
    else {
      msg = "切换到多人模式观看";
      mNVEngine.observerSomeOneVideo("");
    }
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onOpenRemoteVideo(boolean isChecked) {

  }

  @Override
  public void onDisplayRemoteView(boolean isChecked) {
    if(isChecked) {
      mNVEngine.startObserverRemoteVideo(remoteRenderScreen);
      Toast.makeText(this, "显示远端视频", Toast.LENGTH_LONG).show();
      updateVideoView();
    }
    else {
      mNVEngine.stopObserverRemoteVideo();
      Toast.makeText(this, "关闭远端视频", Toast.LENGTH_LONG).show();
      updateVideoView1();
    }
  }

  @Override
  public void onOpenBeautify(boolean isChecked){
    mNVEngine.enableBeautify(isChecked);
    if(isChecked){
      Toast.makeText(this, "打开美颜", Toast.LENGTH_LONG).show();
    }
    else
    {
      Toast.makeText(this, "关闭美颜", Toast.LENGTH_LONG).show();
    }
  }


  @Override
  public void onSendLocalVideo(boolean isChecked) {
        /*
            if(isChecked){
              Toast.makeText(this, "发送本地视频", Toast.LENGTH_LONG).show();
            }
          else
            {
              Toast.makeText(this, "停止发送本地视频", Toast.LENGTH_LONG).show();
            }

        }*/
  }


  @Override
  public void onDisplayLocalView(boolean isChecked) {
        /*mNVEngine.observerLocalVideoWindow(isChecked, localRender);
        if(isChecked)
            Toast.makeText(this, "显示近端视频", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "关闭近端视频", Toast.LENGTH_LONG).show();
            */
  }



  @Override
  public void onVideoScalingSwitch(ScalingType scalingType) {
    this.scalingType = scalingType;
    updateVideoView();
  }

  @Override
  public void onCaptureFormatChange(int width, int height, int framerate) {
    if (rtchatvideoclient != null) {
      rtchatvideoclient.changeCaptureFormat(width, height, framerate);
    }
  }

  @Override
  public boolean onToggleMic() {
    if (rtchatvideoclient != null) {
      micEnabled = !micEnabled;
    }
    NativeVoiceEngine.getInstance().setSendVoice(micEnabled);
    return micEnabled;
  }

  // Helper functions.
  private void toggleCallControlFragmentVisibility() {
    if (!callFragment.isAdded()) {
      return;
    }
    // Show/hide call control fragment
    callControlFragmentVisible = !callControlFragmentVisible;
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    if (callControlFragmentVisible) {
      ft.show(callFragment);
      ft.show(hudFragment);
    } else {
      ft.hide(callFragment);
      ft.hide(hudFragment);
    }
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ft.commit();
  }

  private void updateVideoView1() {
    localRenderLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
    localRender.setScalingType(scalingType);
    localRender.setMirror(false);

    remoteRenderLayout.setPosition(LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
    remoteRenderScreen.setScalingType(ScalingType.SCALE_ASPECT_FIT);
    remoteRenderScreen.setMirror(true);

    localRender.requestLayout();
    remoteRenderScreen.requestLayout();
  }

  private void updateVideoView() {
    remoteRenderLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
    remoteRenderScreen.setScalingType(scalingType);
    remoteRenderScreen.setMirror(false);

    localRenderLayout.setPosition(LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
    localRender.setScalingType(ScalingType.SCALE_ASPECT_FIT);
    localRender.setMirror(true);

    localRender.requestLayout();
    remoteRenderScreen.requestLayout();
  }

  private void startCall() {
    callStartedTimeMs = System.currentTimeMillis();
    if (videoCallEnabled) {
      //rtchatvideoclient.createPeerConnection(rootEglBase.getEglBaseContext(), localRender, remoteRenderScreen);
      mNVEngine.startSendVideo();
      mNVEngine.observerLocalVideoWindow(true,localRender);
    }
  }

  // Disconnect from remote resources, dispose of local resources, and exit.
  private void disconnect() {
    activityRunning = false;
    mNVEngine.destroyRenderView(localRender);
    mNVEngine.destroyRenderView(remoteRenderScreen);
    NativeVoiceEngine.getInstance().requestLeavePlatformRoom();
    finish();
  }

  private void disconnectWithErrorMessage(final String errorMessage) {
    if ( !activityRunning) {
      Log.e(TAG, "Critical error: " + errorMessage);
      disconnect();
    } else {
      new AlertDialog.Builder(this)
              .setTitle(getText(R.string.channel_error_title))
              .setMessage(errorMessage)
              .setCancelable(false)
              .setNeutralButton(R.string.ok,
                      new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                          dialog.cancel();
                          disconnect();
                        }
                      })
              .create()
              .show();
    }
  }

  // Log |msg| and Toast about it.
  private void logAndToast(String msg) {
    Log.d(TAG, msg);
    if (logToast != null) {
      logToast.cancel();
    }
    logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
    logToast.show();
  }

  private void reportError(final String description) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!isError) {
          isError = true;
          disconnectWithErrorMessage(description);
        }
      }
    });
  }


  @Override
  public void onConnectionClosed() {}

  @Override
  public void onInitSuccess(){}

  @Override
  public void onConnectionError(final String description) {
    //reportError(description);
  }
}
