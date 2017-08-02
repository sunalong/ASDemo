package com.ztgametest;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CheckBox;

import org.webrtc.RendererCommon.ScalingType;

/**
 * Fragment for call control.
 */
public class CallFragment extends Fragment {
  private View controlView;
  private TextView contactView;
  private ImageButton disconnectButton;
  private CheckBox viewSomeOneCheckBox;
  private CheckBox openRemoteVideoCheckBox;
  private CheckBox displayRemoteViewCheckBox;
  private CheckBox sendLocalVideoCheckBox;
  private CheckBox enableBeautifyCheckBox;
  private CheckBox displayLocalViewCheckBox;
  private ImageButton cameraSwitchButton;
  private ImageButton videoScalingButton;
  private ImageButton toggleMuteButton;
  private TextView captureFormatText;
  private SeekBar captureFormatSlider;
  private OnCallEvents callEvents;
  private ScalingType scalingType;
  private boolean videoCallEnabled = true;

  /**
   * Call control interface for container activity.
   */
  public interface OnCallEvents {
    void onCallHangUp();
    void onCameraSwitch();
    void onVideoScalingSwitch(ScalingType scalingType);
    void onCaptureFormatChange(int width, int height, int framerate);
    boolean onToggleMic();

    void onOneViewSwitch(boolean isChecked);
    void onOpenRemoteVideo(boolean isChecked);

    void onDisplayRemoteView(boolean isChecked);
    void onSendLocalVideo(boolean isChecked);
    void onDisplayLocalView(boolean isChecked);
    void onOpenBeautify(boolean isChecked);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    controlView = inflater.inflate(R.layout.fragment_call, container, false);

    // Create UI controls.
    contactView = (TextView) controlView.findViewById(R.id.contact_name_call);
    disconnectButton = (ImageButton) controlView.findViewById(R.id.button_call_disconnect);
    cameraSwitchButton = (ImageButton) controlView.findViewById(R.id.button_call_switch_camera);
    videoScalingButton = (ImageButton) controlView.findViewById(R.id.button_call_scaling_mode);
    toggleMuteButton = (ImageButton) controlView.findViewById(R.id.button_call_toggle_mic);
    viewSomeOneCheckBox = (CheckBox) controlView.findViewById(R.id.switch_one_view_checkbox);
    openRemoteVideoCheckBox = (CheckBox) controlView.findViewById(R.id.open_remote_video_checkbox);
    displayRemoteViewCheckBox = (CheckBox) controlView.findViewById(R.id.display_remote_view_checkbox);
    sendLocalVideoCheckBox = (CheckBox) controlView.findViewById(R.id.send_local_video_checkbox);
    enableBeautifyCheckBox = (CheckBox) controlView.findViewById(R.id.open_beautify_video_checkbox);
    displayLocalViewCheckBox = (CheckBox) controlView.findViewById(R.id.display_local_view_checkbox);
    captureFormatText = (TextView) controlView.findViewById(R.id.capture_format_text_call);
    captureFormatSlider = (SeekBar) controlView.findViewById(R.id.capture_format_slider_call);

    // Add buttons click events.
    disconnectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        callEvents.onCallHangUp();
      }
    });

    cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        callEvents.onCameraSwitch();
      }
    });

    viewSomeOneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        callEvents.onOneViewSwitch(isChecked);
      }
    });

    openRemoteVideoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        callEvents.onOpenRemoteVideo(isChecked);
      }
    });

    displayRemoteViewCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        callEvents.onDisplayRemoteView(isChecked);
      }
    });

    sendLocalVideoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        callEvents.onSendLocalVideo(isChecked);
      }
    });

    enableBeautifyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        callEvents.onOpenBeautify(isChecked);
      }
    });

    displayLocalViewCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        callEvents.onDisplayLocalView(isChecked);
      }
    });

    videoScalingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (scalingType == ScalingType.SCALE_ASPECT_FILL) {
          videoScalingButton.setBackgroundResource(R.mipmap.ic_action_full_screen);
          scalingType = ScalingType.SCALE_ASPECT_FIT;
        } else {
          videoScalingButton.setBackgroundResource(R.mipmap.ic_action_return_from_full_screen);
          scalingType = ScalingType.SCALE_ASPECT_FILL;
        }
        callEvents.onVideoScalingSwitch(scalingType);
      }
    });
    scalingType = ScalingType.SCALE_ASPECT_FILL;

    toggleMuteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean enabled = callEvents.onToggleMic();
        toggleMuteButton.setAlpha(enabled ? 1.0f : 0.3f);
      }
    });

    return controlView;
  }

  @Override
  public void onStart() {
    super.onStart();

    boolean captureSliderEnabled = false;
    Bundle args = getArguments();
    if (args != null) {
      videoCallEnabled = args.getBoolean(CallActivity.EXTRA_VIDEO_CALL, true);
    }
    /*{
      cameraSwitchButton.setVisibility(View.INVISIBLE);
      viewSomeOneCheckBox.setVisibility(View.INVISIBLE);
      openRemoteVideoCheckBox.setVisibility(View.INVISIBLE);
      displayRemoteViewCheckBox.setVisibility(View.INVISIBLE);
      sendLocalVideoCheckBox.setVisibility(View.INVISIBLE);
      displayLocalViewCheckBox.setVisibility(View.INVISIBLE);
    }*/

    if (captureSliderEnabled) {
      //captureFormatSlider.setOnSeekBarChangeListener(
      //    new CaptureQualityController(captureFormatText, callEvents));
    } else {
      captureFormatText.setVisibility(View.GONE);
      captureFormatSlider.setVisibility(View.GONE);
    }
  }

  // TODO(sakal): Replace with onAttach(Context) once we only support API level 23+.
  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    callEvents = (OnCallEvents) activity;
  }
}
