/**
 * Created by luke on 2017/3/15.
 */

package com.itcode.asdemo.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import com.ztgame.videoengine.VideoEngineImpl;
import com.ztgame.videoengine.NativeVideoEngine;


public class SurfaceViewContainer extends RelativeLayout {
    private SurfaceView surfaceView;
    private Context context;
    public SurfaceViewContainer(Context context) {
        super(context);
        this.context = context ;
        init();
    }

    public SurfaceViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context ;
        init();
    }

    public SurfaceViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context ;
        init();
    }
    private void init()
    {
        setBackgroundColor(getResources().getColor(android.R.color.black));
        this.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        surfaceView = NativeVideoEngine.getInstance().createRenderView();
        if(surfaceView != null) {
            surfaceView.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            addView(surfaceView);
        }
    }
    public SurfaceView getSurfaceView()
    {
        return  surfaceView;
    }


}
