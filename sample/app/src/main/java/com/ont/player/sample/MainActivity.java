package com.ont.player.sample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText mEdtDeviceId;
    private EditText mEdtChannel;
    private EditText mEdtApiKey;
    private TextView mTextEntry;
    private boolean mTextEntryEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEdtDeviceId = findViewById(R.id.edt_device_id);
        mEdtChannel = findViewById(R.id.edt_channel);
        mEdtApiKey = findViewById(R.id.edt_api_key);
        mTextEntry = findViewById(R.id.text_entry);

        mTextEntry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                saveDeviceInfo();
                Intent intent = new Intent(MainActivity.this, PlayerSelectActivity.class);
                intent.putExtra(PlayerActivity.DEVICE_ID, getDeviceId())
                        .putExtra(PlayerActivity.Channel_ID, getChannelId())
                        .putExtra(PlayerActivity.API_KEY, getApiKey());
                startActivity(intent);
            }
        });
        bindEditWatcher();
        initDeviceInfo();
    }

    public String getDeviceId() {

        return mEdtDeviceId.getText().toString().trim();
    }

    public String getChannelId() {

        return mEdtChannel.getText().toString().trim();
    }

    public String getApiKey() {

        return mEdtApiKey.getText().toString().trim();
    }

    private void saveDeviceInfo() {

        SharedPreferences sp = getSharedPreferences("player_device_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("device_id", getDeviceId());
        edit.putString("channel_id", getChannelId());
        edit.putString("api_key", getApiKey());
        edit.commit();
    }

    private boolean initDeviceInfo() {

        String deviceId = "";
        String channelId = "";
        String apiKey = "";

        SharedPreferences sp = getSharedPreferences("player_device_info", Context.MODE_PRIVATE);
        if (sp != null) {

            deviceId = sp.getString("device_id", "");
            channelId = sp.getString("channel_id", "");
            apiKey = sp.getString("api_key", "");

            if (TextUtils.isEmpty(channelId) || TextUtils.isEmpty(apiKey)) {

                initDefaultDeviceInfo();
                return false;
            }

            mEdtDeviceId.setText(deviceId);
            mEdtChannel.setText(channelId);
            mEdtApiKey.setText(apiKey);
            return true;
        } else {

            initDefaultDeviceInfo();
            return false;
        }
    }

    private void initDefaultDeviceInfo() {

        mEdtDeviceId.setText("xxxxxxx");
        mEdtChannel.setText("xxx");
        mEdtApiKey.setText("xxxxxxxxxxxxxxx");
    }

    private void bindEditWatcher() {

        mEdtDeviceId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                changeEntryStatus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mEdtChannel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                changeEntryStatus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mEdtApiKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                changeEntryStatus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void changeEntryStatus() {

        if (TextUtils.isEmpty(getDeviceId()) || TextUtils.isEmpty(getChannelId()) || TextUtils.isEmpty(getApiKey())) {

            if (mTextEntryEnable) {

                mTextEntryEnable = false;
                mTextEntry.setEnabled(false);
                mTextEntry.setTextColor(getResources().getColor(R.color.colorText1));
                mTextEntry.setBackgroundResource(R.drawable.btn_background_0);
            }
        } else {

            if (!mTextEntryEnable) {

                mTextEntryEnable = true;
                mTextEntry.setEnabled(true);
                mTextEntry.setTextColor(getResources().getColor(android.R.color.white));
                mTextEntry.setBackgroundResource(R.drawable.btn_background_2);
            }
        }
    }
}





