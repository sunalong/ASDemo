/**
 * Created by luke on 2017/2/21.
 */

package com.ztgametest;


import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Settings fragment for AppRTC.
 */
public class SettingsFragment extends PreferenceFragment {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.preferences);
  }
}
