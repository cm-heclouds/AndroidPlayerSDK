package com.ont.player.sample.network;

import android.text.TextUtils;

import com.ont.player.sample.def.IApiListener;
import com.ont.player.sample.def.IDataListener;
import com.ont.player.sample.def.IRequestDef;
import com.ont.player.sample.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by betali on 2018/7/18.
 */

public class UploadRequest extends OntPlayerRequest {

    private final String TAG = "UploadRequest";
    String device_id;
    String channel_id;
    String format; //"jpg/png"
    String name;
    String desc;
    String filePath;
    IDataListener request_listener;

    public UploadRequest(String api_key) {
        super(api_key);
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setRequest_listener(IDataListener request_listener) {
        this.request_listener = request_listener;
    }

    @Override
    String getRequestType() {

        return IRequestDef.IRequestTypeDef.PUT;
    }

    @Override
    String getRequestUrl() {

        return String.format(IRequestDef.IRequestUrlDef.API_URL + "/ipc/video/picture/upload?device_id=%s&channel_id=%s&format=%s&name=%s&desc=%s", device_id, channel_id, format, name, desc);
    }

    @Override
    String getRequestBody() {

        return filePath;
    }

    @Override
    void initListener() {
        api_listner = new IApiListener() {
            @Override
            public void onComplete(boolean success, String response) {

                LogUtil.i(TAG, "response:" + response);

                if (!success) {

                    request_listener.onComplete(IRequestDef.IRequestResultDef.ERR_NETWORK, 0, response);
                    return;
                }

                if(TextUtils.isEmpty(response)){
                    if(null != request_listener) {
                        request_listener.onComplete(IRequestDef.IRequestResultDef.ERR_NULL, 0, "返回空");
                    }
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(null != jsonObject){
                        if(null != request_listener) {
                            request_listener.onComplete(jsonObject.optInt("errno"), 0, jsonObject.optString("error"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
