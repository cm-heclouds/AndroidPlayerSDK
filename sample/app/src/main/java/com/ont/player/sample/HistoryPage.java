package com.ont.player.sample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.flyco.tablayout.SlidingTabLayout;
import com.ont.player.sample.mode.DeviceEntryInfo;

/**
 * 用于加载本地视频文件的播放记录
 */

public class HistoryPage extends FrameLayout {

    private Context mContext;
    private DeviceEntryInfo mDeviceEntryInfo;

    private String[] mTabTitles;
    private OntListView[] mTabViews;

    public HistoryPage(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public HistoryPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public HistoryPage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setDeviceEntryInfo(DeviceEntryInfo deviceEntryInfo) {

        mDeviceEntryInfo = deviceEntryInfo;

        for (int index = 0; index < mTabViews.length; index++) {

            mTabViews[index].setDeviceEntryInfo(deviceEntryInfo);
        }
    }

    public void updateData(int index) {

        mTabViews[index].updateData();
    }

    private void initView(Context context) {

        mContext = context;

        mTabTitles = new String[3];
        mTabTitles[0] = context.getResources().getString(R.string.device_history);
        mTabTitles[1] = context.getResources().getString(R.string.local_history);
        mTabTitles[2] = context.getResources().getString(R.string.server_history);

        mTabViews = new OntListView[3];
        mTabViews[0] = new DeviceHitoryListView();
        mTabViews[0].setContext(context);
        mTabViews[1] = new LocalHistoryListView();
        mTabViews[1].setContext(context);
        mTabViews[2] = new ServerHistoryListView();
        mTabViews[2].setContext(context);

        initTabLayout(context);
    }

    private void initTabLayout(Context context) {

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.page_history, this);

        ViewPager viewPager = findViewById(R.id.viewpager);
        HistoryPageAdapter adapter = new HistoryPageAdapter(((AppCompatActivity)context).getSupportFragmentManager(), mTabTitles, mTabViews);
        viewPager.setAdapter(adapter);

        SlidingTabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setViewPager(viewPager, mTabTitles);
    }
}
