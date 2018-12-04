package com.ont.player.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ont.player.sample.def.IDataListener;
import com.ont.player.sample.def.IRequestDef;
import com.ont.player.sample.mode.DeviceEntryInfo;
import com.ont.player.sample.network.LevelRequest;
import com.ont.player.sample.network.NetworkClient;
import com.ont.player.sample.network.PlayUrlRequest;

/**
 * 用于获取、配置直播
 */

public class LivePage extends FrameLayout {

    private Context mContext;
    private DeviceEntryInfo mDeviceEntryInfo;

    private RadioGroup mRadioGroupProtocol;
    private RadioGroup mRadioGroupLevel;
    private EditText mEdtUrl;

    public LivePage(Context context) {
        super(context);
        initView(context);
    }

    public LivePage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LivePage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setDeviceEntryInfo(DeviceEntryInfo deviceEntryInfo) {

        this.mDeviceEntryInfo = deviceEntryInfo;
    }

    private void initView(Context context) {

        mContext = context;

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.page_live, this);
        mEdtUrl = findViewById(R.id.edt_url);
        mRadioGroupProtocol = findViewById(R.id.radio_group_protocol);

        findViewById(R.id.btn_get).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(mDeviceEntryInfo.mDeviceId) || TextUtils.isEmpty(mDeviceEntryInfo.mChannelId) || TextUtils.isEmpty(mDeviceEntryInfo.mApiKey)) {

                    Toast.makeText(mContext, mContext.getString(R.string.tip_input_setting), Toast.LENGTH_SHORT).show();
                    return;
                }

                updateLevel(mDeviceEntryInfo.mApiKey, mDeviceEntryInfo.mDeviceId, mDeviceEntryInfo.mChannelId, getCheckedLevel());
                PlayUrlRequest request = new PlayUrlRequest(mDeviceEntryInfo.mApiKey)
                        .setIs_live(true)
                        .setChannel_id(mDeviceEntryInfo.mChannelId)
                        .setDevice_id(mDeviceEntryInfo.mDeviceId)
                        .setProtocol_type(getCheckedProtocol())
                        .setRequest_listener(new IDataListener() {

                            @Override
                            public void onComplete(int apiErr, int dataErr, String response) {

                                if (apiErr == IRequestDef.IRequestResultDef.ERR_OK) {

                                    mEdtUrl.setText(response);
                                } else {

                                    Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                NetworkClient.doRequest(request);
            }
        });

        mRadioGroupLevel = findViewById(R.id.radio_group_level);
        mRadioGroupLevel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                updateLevel(mDeviceEntryInfo.mApiKey, mDeviceEntryInfo.mDeviceId, mDeviceEntryInfo.mChannelId, getCheckedLevel());
            }
        });

        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String url = mEdtUrl.getText().toString().trim();
                if(TextUtils.isEmpty(url)){

                    Toast.makeText(mContext, mContext.getString(R.string.tip_play), Toast.LENGTH_SHORT).show();
                    return;
                }

                mContext.startActivity(new Intent(mContext, PlayerActivity.class)
                        .setData(Uri.parse(url))
                        .putExtra(PlayerActivity.IS_LIVE, true)
                        .putExtra(PlayerActivity.IS_LOCAL, false)
                        .putExtra(PlayerActivity.CAN_RECORD, true)
                        .putExtra(PlayerActivity.VIDEO_TITLE, "直播")
                        .putExtra(PlayerActivity.DEVICE_ID, mDeviceEntryInfo.mDeviceId)
                        .putExtra(PlayerActivity.Channel_ID, mDeviceEntryInfo.mChannelId)
                        .putExtra(PlayerActivity.API_KEY, mDeviceEntryInfo.mApiKey));
            }
        });
    }

    private void updateLevel(final String apiKey, final String deviceId, String channel, int level){

        LevelRequest request = new LevelRequest(apiKey)
                .setChannel_id(channel)
                .setDevice_id(deviceId)
                .setLevel(level)
                .setRequest_listener(new IDataListener() {

                    @Override
                    public void onComplete(int apiErr, int dataErr, String response) {

                        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    }
                });
        NetworkClient.doRequest(request);
    }

    private String getCheckedProtocol() {

        //0:RTMP 1:HLS 2:HTTPS-HLS
        String protocolType = "0";
        if (mRadioGroupProtocol.getCheckedRadioButtonId() == R.id.radio_rtmp) {

            protocolType = "0";
        } else if (mRadioGroupProtocol.getCheckedRadioButtonId() == R.id.radio_hls) {

            protocolType = "1";
        } else {

            protocolType = "2";
        }

        return protocolType;
    }

    private int getCheckedLevel() {

        int level = 3;
        if(R.id.radio_level_2 == mRadioGroupLevel.getCheckedRadioButtonId()) {

            level = 2;
        } else if(R.id.radio_level_3 == mRadioGroupLevel.getCheckedRadioButtonId()) {

            level = 3;
        } else if(R.id.radio_level_4 == mRadioGroupLevel.getCheckedRadioButtonId()) {

            level = 4;
        }

        return level;
    }
}
