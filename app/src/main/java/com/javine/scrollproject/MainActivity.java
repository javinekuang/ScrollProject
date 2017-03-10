package com.javine.scrollproject;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.javine.scrollproject.view.ModifierScrollView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    List<String> mData = new ArrayList<>();
    MyAdapter mAdapter;
    private Context mContext;
    ModifierScrollView scrollView;
    ListView listView;
    TextView tvHeader;
    float y = 0.f;

    Object firstTouchObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initData();
        mAdapter = new MyAdapter();
        initView();
    }

    private void initView() {
        scrollView = (ModifierScrollView) findViewById(R.id.scrollView);
        listView = (ListView) findViewById(R.id.listView);
        tvHeader = (TextView) findViewById(R.id.tv_header);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        listView.getLayoutParams().height = metrics.heightPixels;
        listView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        scrollView.setHeaderHeight(200);
        scrollView.setChildGroup(listView);
        getFirstTouchTarget();//通过反射获取mFirstTouchTarget属性
        scrollView.setCanScrollListener(new ModifierScrollView.CanScrollListener() {
            @Override
            public boolean canScroll(float deltaY) {
                getFirstTouchTarget();
                Log.d("Javine", "mFirstTouchTarget = "+firstTouchObj);
                if (deltaY > 0){ //上滑
                    if (scrollView.isHeaderShow()){
                        return true;
                    }else{
                        return false;
                    }
                }else{ //下滑
                    if (listView.getFirstVisiblePosition() == 0){
                        View child = listView.getChildAt(0);
                        if (child.getTop() == 0){
                            return true;
                        }
                    }
                    return false;
                }
            }
        });
    }

    private void getFirstTouchTarget(){
        try {
            Field firstTouchField = ViewGroup.class.getDeclaredField("mFirstTouchTarget");
            firstTouchField.setAccessible(true);
            firstTouchObj = firstTouchField.get(scrollView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData(){
        for (int i = 0; i < 15; i++){
            mData.add(""+i);
        }
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                TextView textView = new TextView(mContext);
                textView.setHeight(120);
                textView.setGravity(Gravity.CENTER);
                convertView = textView;
            }
            TextView view = (TextView) convertView;
            view.setText(mData.get(position));
            return convertView;
        }
    }
}
