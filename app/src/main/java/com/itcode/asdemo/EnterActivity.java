package com.itcode.asdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.itcode.asdemo.R;

public class EnterActivity extends Activity {
    Intent intent;

    private static final int CONNECTIONIM_REQUEST = 1;
    private static final int CONNECTIONAV_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
    }

    public void jumpOnClick(View view) {
        switch (view.getId()) {
            case R.id.avbutton: {
                intent = new Intent(this, ConnectAVActivity.class);

                startActivityForResult(intent, CONNECTIONAV_REQUEST);
                break;
            }
            case R.id.imbutton: {
                intent = new Intent(this, MainActivity.class);
                startActivityForResult(intent, CONNECTIONIM_REQUEST);
                break;
            }
        }
    }


}
