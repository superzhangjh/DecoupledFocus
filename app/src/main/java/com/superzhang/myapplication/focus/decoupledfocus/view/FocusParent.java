package com.superzhang.myapplication.focus.decoupledfocus.view;

import android.app.Activity;
import android.support.v4.view.NestedScrollingChild2;
import android.support.v4.view.NestedScrollingParent2;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.superzhang.myapplication.focus.decoupledfocus.other.DecoupledViewListener;
import com.superzhang.myapplication.focus.decoupledfocus.other.FocusParam;
import com.superzhang.myapplication.focus.decoupledfocus.other.FocusTag;

public class FocusParent {

    //xml布局中的第一个控件
    private View parent;
    //默认配置参数
    private FocusParam param;
    //焦点框
    private FocusView focusView;
    //旧的焦点view
    private View oldFocusView;
    //新的焦点view
    private View newFocusView;
    //焦点更新监听
    private DecoupledViewListener viewListener;

    public FocusParent(Activity activity, FocusParam param, DecoupledViewListener viewListener){
        this.param = param;
        this.viewListener = viewListener;
        ViewGroup contentView = activity.findViewById(android.R.id.content);
        parent = contentView.getChildAt(0);
        //创建焦点View
        focusView = new FocusView(contentView.getContext());
        focusView.setFocusable(false);
        focusView.setFocusableInTouchMode(false);
        //解决一开始无焦点时焦点框在全屏显示不好看的问题
        focusView.setAlpha(0);
        //添加到android.R.id.content中
        contentView.addView(focusView);
        initListener();
    }

    /**
     * 初始化监听事件
     */
    private void initListener() {
//        ((ViewGroup)parent).setClipChildren(true);
        //设置全局焦点监听，当获得焦点时，更改焦点框的位置
        parent.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                if (newFocus == null){
                    return;
                }
                oldFocusView = oldFocus;
                newFocusView = newFocus;
                //这里使用post的方式，避免该view的绘制还没完成就进行焦点处理，导致焦点框大小异常
                newFocus.post(new Runnable() {
                    @Override
                    public void run() {
                        if (focusView!=null){
                            focusView.upDateLocation(
                                    getParam(newFocusView, oldFocusView),
                                    newFocusView,
                                    getLocation(newFocusView),
                                    getFcousTag(newFocusView),
                                    false);
                        }
                        upDateViews(oldFocusView,newFocusView, getFcousTag(newFocusView).isScale()? param.zoom: 1.0f);
                    }
                });
            }
        });

        //目标view滑动时，焦点框跟随,这里针对RecyclerView选中居中的设置
        parent.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (newFocusView != null && focusView != null){
                    updateScrolledChanged();
                }
            }
        });
    }

    private void updateScrolledChanged(){
        if (focusView != null){
            ViewGroup group = (ViewGroup) newFocusView.getParent();
            if (group instanceof ScrollingView
                    || group instanceof ScrollView
                    || group instanceof NestedScrollingParent2
                    || group instanceof NestedScrollingChild2){
                if (focusView.isAnimating()){
                    newFocusView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateScrolledChanged();
                        }
                    }, 100L);
                } else {
                    focusView.cancelAnimation();
                    focusView.upDateLocation(
                            getParam(newFocusView, oldFocusView),
                            newFocusView,
                            getLocation(newFocusView),
                            getFcousTag(newFocusView),
                            true);
                }
            }
        }
    }

    /**
     * 获得焦点参数
     * @param newFocusView
     * @param oldFocus
     */
    private FocusParam getParam(View newFocusView, View oldFocus){
        return  viewListener == null? param: viewListener.onUpdateViewLocation(param, newFocusView, oldFocus);
    }

    /**
     * 获取View在布局中的位置
     * @param view
     * @return location
     */
    private int[] getLocation(View view){
        if (view == null){
            return null;
        }
        return getLocation(view, null);
    }

    /**
     * 获取View在布局中的位置
     * @param view
     * @param location 坐标点 location[0]:left location[1]:top
     * @return location
     */
    private int[] getLocation(View view, int[] location){
        if (location == null){
            location = new int[2];
        }
        location[0] += view.getLeft();
        location[1] += view.getTop();

        //适配ViewPager的item间距, 如果你的Viewpager自定义成垂直的，要单独处理。。
        if (view instanceof ViewPager){
            ViewPager viewPager = (ViewPager) view;
            if (viewPager.getCurrentItem() > 0){
                location[0] -= viewPager.getCurrentItem() * viewPager.getWidth();
            }
        }

        //如果父布局不存在， 或者父布局是FrameContentLayout，则返回坐标结果
        return view.getParent()!=null &&  !(((View)view.getParent()).getId() == android.R.id.content)?
                getLocation((View) view.getParent(), location): location;
    }

    /**
     * 更新View的缩放状态
     * @param animView 焦点框的目标View
     * @param oldView 上次更新的view
     */
    private void upDateViews(View oldView, View animView, float zoom){
        //如果不可见，不进行缩放
        if (!param.visiable){
            zoom = 1.0f;
        }

        //更新目标View
        if (animView!=null && zoom != animView.getScaleX()){
            animView.bringToFront();
            ViewCompat.animate(animView)
                    .scaleX(zoom)
                    .scaleY(zoom)
                    .setDuration(param.duration)
                    .start();
        }
        //缩放旧View到原大小
        if (oldView != null){
            ViewCompat.animate(oldView)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(param.duration)
                    .start();
        }
    }

    /**
     * 判断该View是否是不进行焦点处理的View
     * 忽略的view可获取焦点，但是不参与缩放与焦点框，仅仅是动画效果
     * @param view
     */
    private FocusTag getFcousTag(View view){
        if (view != null && view.getTag() instanceof String){
            String tag = (String) view.getTag();
            if (tag.equals(param.scaleTag)){
                return FocusTag.FOCUS_HIDE_SCALE;
            } else if (tag.equals(param.focusNoScaleTag)){
                return FocusTag.FOCUS_SHOW_NOSCALE;
            } else if (tag.equals(param.ignoreTag)){
                return FocusTag.FCOUS_HIDE_NOSCALE;
            }
        }
        return FocusTag.FOCUS_SHOW_SCALE;
    }

    public void release(){
        parent = null;
        focusView = null;
        viewListener = null;
    }
}
