package com.superzhang.myapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.superzhang.myapplication.focus.decoupledfocus.DecoupledFocusUtils;
import com.superzhang.myapplication.focus.decoupledfocus.other.DecoupledViewListener;
import com.superzhang.myapplication.focus.decoupledfocus.other.FocusParam;

public class MainActivity extends AppCompatActivity {

    private TextView tvDelay;
    private FocusParam focusParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDelay = findViewById(R.id.tv_delay);

        //在当前 activity 中使用焦点控件
//        DecoupledFocusUtils.getInstance().bind(this); //如果不需要针对单独 view 做处理，调用这句即可
        DecoupledFocusUtils.getInstance().bind(this, new DecoupledViewListener() {
            @NonNull
            @Override
            public FocusParam onUpdateViewLocation(FocusParam defaultParam, View focusView, View oldFocusView) {
                if (focusView.getId() == tvDelay.getId()){
                    defaultParam.duration = 2000L;
                } else {
                    defaultParam.duration = focusParam.duration;
                }
                return defaultParam;
            }
        });

        //拿到默认的设置参数 (需要先在 Application 中初始化该工具类)
        focusParam = DecoupledFocusUtils.getInstance().getParam();
    }
}
