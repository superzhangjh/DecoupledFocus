package com.superzhang.myapplication;

import android.app.Application;

import com.superzhang.myapplication.focus.decoupledfocus.DecoupledFocusUtils;
import com.superzhang.myapplication.focus.decoupledfocus.other.FocusParam;

/**
 * Created by 张坚鸿 on 2019/2/18 11:20
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化焦点框工具类，建议在 Application 中做默认设置， 只需要设置一次所有 activity 都有效
        FocusParam focusParam = new FocusParam();
        focusParam.zoom = 1.2f;
        focusParam.drawableResource = R.drawable.shape_focus;
        focusParam.scaleTag = getString(R.string.string_focus);
        focusParam.ignoreTag = getString(R.string.string_ignore);
        focusParam.scaleTag = getString(R.string.string_scale);
        focusParam.focusNoScaleTag = getString(R.string.string_no_scale);
        focusParam.isScrollFollowing = true;
        DecoupledFocusUtils.getInstance().init(focusParam);
    }
}
