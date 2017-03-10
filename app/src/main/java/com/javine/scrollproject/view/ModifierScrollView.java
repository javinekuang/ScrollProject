package com.javine.scrollproject.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ScrollView;

import java.lang.reflect.Field;

/**
 * Created by KuangYu on 2017/3/8 0008.
 */

public class ModifierScrollView extends ScrollView {

    float x = 0.f;
    float y = 0.f;
    private float actionDownY = 0.f;
    int touchSlop;
    private int headerHeight = 0;
    private boolean isHeaderShow = true;
    private int scrollDirectState = 0; //滑动方向
    private boolean isDangerPoint;
    private boolean isChildScrollTop = false;
    private ViewGroup childGroup;

    public interface CanScrollListener{
        /**
         * ScrollView是否可以滑动
         * @param deltaY
         * @return true if ScrollView do scroll, return false if ChildView do scroll.
         */
        boolean canScroll(float deltaY);
    }

    private CanScrollListener listener;

    public ModifierScrollView(Context context) {
        this(context, null);
    }

    public ModifierScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ModifierScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void setHeaderHeight(int headerHeight) {
        this.headerHeight = headerHeight;
    }

    public void setCanScrollListener(CanScrollListener listener){
        this.listener = listener;
    }

    public boolean isHeaderShow() {
        return isHeaderShow;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        super.onInterceptTouchEvent(ev);
        boolean isIntercept = false;
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                y = ev.getRawY();
                actionDownY = ev.getRawY();
                isIntercept = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaY = y - ev.getRawY();
                if (Math.abs(deltaY) > touchSlop && listener != null){
                    if(listener.canScroll(deltaY)){
                        isIntercept = true;
                    }else{
                        isIntercept = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                actionDownY = 0.f;
                isIntercept = false;

        }
        Log.d("Javine", "isIntercept = "+String.valueOf(isIntercept));
        return isIntercept;
    }

    public void setChildGroup(ViewGroup childGroup){
        this.childGroup = childGroup;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int groupFlag = getGroupFlag();
        Log.d("Javine", "dispatchTouchEvent : DisallowIntercept = "+ (groupFlag & 0x80000) +"  "+ onFilterTouchEventForSecurity(ev));
        Log.d("Javine", "dispatchTouchEvent : mFirstTouchTarget = "+ getFirstTouchTarget());
        if (ev.getAction() == MotionEvent.ACTION_MOVE){
            Log.d("Javine", "dispatchTouchEvent : dangerPoint = "+isDangerPoint + " isChildScrollTop = " + isChildScrollTop);
            if (isDangerPoint && scrollDirectState > 0 ){
                isDangerPoint = false;
                ev.setAction(MotionEvent.ACTION_DOWN);
                requestDisallowInterceptTouchEvent(false);
            }else if (isChildScrollTop() && !isChildScrollTop){
                isChildScrollTop = true;
                ev.setAction(MotionEvent.ACTION_DOWN);
                requestDisallowInterceptTouchEvent(false);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isChildScrollTop(){
        if (childGroup != null){
            View view = childGroup.getChildAt(0);
            Log.d("Javine", "isChildScrollTop() : top = "+view.getTop());
            if (view.getTop() < 0){
                isChildScrollTop = false;
            }
            if (view.getTop() >= 0){
                return true;
            }
        }
        return false;
    }

    private Object getFirstTouchTarget(){
        try {
            Field firstTouchField = ViewGroup.class.getDeclaredField("mFirstTouchTarget");
            firstTouchField.setAccessible(true);
            return firstTouchField.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getGroupFlag(){
        try {
            Field groupFlagField = ViewGroup.class.getDeclaredField("mGroupFlags");
            groupFlagField.setAccessible(true);
            Integer groupFlag = (Integer) groupFlagField.get(this);
            return groupFlag;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        int direct = t - oldt;
        Log.d("Javine", "onScrollChanged : top = "+ t);
        if (Math.abs(t - headerHeight) < 15){  //临界点
            isDangerPoint = true;
        }
        if (t >= headerHeight) {
            isHeaderShow = false;
        } else {
            isHeaderShow = true;
        }
        scrollDirectState = direct;
    }

}
