package com.superzhang.myapplication.focus.decoupledfocus;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.View;

import com.superzhang.myapplication.focus.decoupledfocus.other.DecoupledViewListener;
import com.superzhang.myapplication.focus.decoupledfocus.other.FocusParam;
import com.superzhang.myapplication.focus.decoupledfocus.view.FocusParent;

import java.util.HashMap;

/**
 * 解耦布局、无侵入的飞框处理工具
 * (1)功能介绍：处理Tv中的飞框，支持缩放、设置动画时长、调节间距、设置贴图文件，支持所有View、ViewGroup(不用自定义View)，可调节焦点框的间距，
 * 不需要写入布局文件中，通过自定义tag改变飞框的表现(是否显示焦点框、是否缩放)
 * (2)使用方法：
 *    (A)在Activity中调用bind放法，每次绘制前会调用DecoupledViewListener的onUpdateViewLocation方法，
 *    所以可以做判断实现不同view不同的参数设置。
 *    (B)使用前需要前需要前在Application中调用init方法，配置默认参数。
 * (3)已知bug：linerLayout会出现布局位置错乱，使用BringToFrontLinearLayout替换LinearLayout即可
 * (4)已知bug：duration设置成0会出现布局偏差，需要请设置成1
 * (5)已知bug：在dialog主题下的activity使用会出现较大偏差
 * (6)已知bug：Recyclerview焦点丢失问题处理，使用QuickTvAdapter、FocusRecyclerView
 * Created by 张坚鸿
 */
public final class DecoupledFocusUtils {

    private static DecoupledFocusUtils utils;
    private FocusParam param;
    private SparseArray<FocusParent> parentSparseArray;

    private DecoupledFocusUtils(){
        parentSparseArray = new SparseArray<>();
    }

    /**
     * 初始化(在Application中使用)
     */
    public void init(FocusParam param){
        this.param = param == null? new FocusParam() : param;
    }

    /**
     * 获取单例
     */
    public static DecoupledFocusUtils getInstance(){
        if (utils == null){
            utils = new DecoupledFocusUtils();
        }
        return utils;
    }

    /**
     * 绑定Activity，在setContentView之后调用
     */
    public void bind(Activity activity){
        createFocusParent(activity, null);
    }

    /**
     * 绑定Fragment
     */
    public void bind(Fragment fragment){
        bind(fragment.getActivity());
    }

    /**
     * 重设当前Activity的焦点配置(不会修改application中配置的)，并绑定Activity
     */
    public void bind(Activity activity, DecoupledViewListener viewListener){
        createFocusParent(activity, viewListener);
    }

    /**
     * 取消绑定
     */
    public void unbind(Activity activity){
        int hashcode = activity.hashCode();
        if (parentSparseArray.get(hashcode) != null){
            parentSparseArray.get(activity.hashCode()).release();
            parentSparseArray.delete(activity.hashCode());
        }
    }

    /**
     * 创建焦点管理
     */
    private void createFocusParent(Activity activity, DecoupledViewListener viewListener){
        if (param!= null && param.enable && parentSparseArray.get(activity.hashCode())==null){
            //创建管理类
            FocusParent parent = new FocusParent(activity, getParam(), viewListener);
            parentSparseArray.put(activity.hashCode(), parent);
        }
    }

    public FocusParam getParam() {
        return param.copy();
    }
}
