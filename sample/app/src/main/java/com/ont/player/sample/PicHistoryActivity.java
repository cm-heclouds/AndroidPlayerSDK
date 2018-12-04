package com.ont.player.sample;

import android.content.Intent;
import android.os.Bundle;;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.List;
import java.util.Map;

/**
 * Created by armou on 2018/7/13.
 */

public class PicHistoryActivity extends AppCompatActivity {

    private String mApiKey;
    private String mChannelId;
    private String mDeviceId;

    private PicHistoryListView mPicHistoryListView;

    private List<Map<String,Object>> mList = null;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.pic_history));

        Intent intent = getIntent();
        mApiKey = intent.getStringExtra("api_key");
        mChannelId = intent.getStringExtra("channel_id");
        mDeviceId = intent.getStringExtra("device_id");

        mPicHistoryListView = new PicHistoryListView().setHostActivity(this);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, mPicHistoryListView)
                .commitAllowingStateLoss();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getApiKey() {

        return mApiKey;
    }

    public String getChannelId() {

        return mChannelId;
    }

    public String getDeviceId() {

        return mDeviceId;
    }
}
