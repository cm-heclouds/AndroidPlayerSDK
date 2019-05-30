package com.ont.player.sample.network;

import android.text.TextUtils;

import com.ont.player.sample.def.IApiListener;
import com.ont.player.sample.def.IDataListener;
import com.ont.player.sample.def.IRequestDef;
import com.ont.player.sample.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by betali on 2018/3/19.
 */

public class PlayUrlRequest extends OntPlayerRequest {
    private final String TAG = "PlayUrlRequest";

    boolean is_live;
    String device_id;
    String channel_id;
    String protocol_type;
    String begin_time;    // 历史流才有
    String end_time;      // 历史流才有
    IDataListener request_listener;

    public PlayUrlRequest(String api_key) {
        super(api_key);
    }

    @Override
    String getRequestType() {
        return IRequestDef.IRequestTypeDef.GET;
    }

    @Override
    String getRequestUrl() {

        return String.format(IRequestDef.IRequestUrlDef.API_URL + "/play_address?device_id=%s&channel_id=%s&protocol_type=%s", device_id, channel_id, protocol_type);
    }

    @Override
    String getRequestBody() {

        return null;
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

                    request_listener.onComplete(IRequestDef.IRequestResultDef.ERR_NULL, 0, "返回空");
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(null != jsonObject){
                        if(0 != jsonObject.optInt("errno")){
                            if(null != request_listener) {
                                request_listener.onComplete(jsonObject.optInt("errno"), 0, jsonObject.optString("error"));
                            }
                        }else{
                            JSONObject data = (JSONObject) jsonObject.get("data");
                            String ipPort = data.optString("addr");
                            String accessToken = data.optString("accessToken");
                            String type = data.optString("type");

                            //播放协议类型 0:RTMP 1:HLS 2：https-hls
                            String addr;
                            if("1".equals(protocol_type) || "2".equals(protocol_type)){
                                // HLS只有直播流，没有历史流
                                addr = type + "://%s/live/live_%s_%s/index.m3u8?%s";
                                addr = String.format(addr, ipPort, device_id, channel_id, accessToken);
                            }else{
                                // rtmp有直播流，有历史流
                                if (is_live) {
                                    // 直播流
                                    addr = type + "://%s/live/%s-%s?%s";
                                    addr = String.format(addr, ipPort, device_id, channel_id, accessToken);
                                } else {
                                    // 历史流
                                    addr = type + "://%s/rvod/%s-%s-%s-%s?%s";
                                    String beginTime = begin_time.replace("-", "").replace(" ", "").replace(":", "");
                                    String endTime = end_time.replace("-", "").replace(" ", "").replace(":", "");
                                    addr = String.format(addr, ipPort, device_id, channel_id, beginTime, endTime, accessToken);
                                }
                            }

                            LogUtil.i(TAG, "result:" + addr);
                            if(null != request_listener) {
                                request_listener.onComplete(IRequestDef.IRequestResultDef.ERR_OK, 0, addr);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public PlayUrlRequest setIs_live(boolean is_live) {
        this.is_live = is_live;
        return this;
    }

    public PlayUrlRequest setDevice_id(String device_id) {
        this.device_id = device_id;
        return this;
    }

    public PlayUrlRequest setChannel_id(String channel_id) {
        this.channel_id = channel_id;
        return this;
    }

    public PlayUrlRequest setProtocol_type(String protocol_type) {
        this.protocol_type = protocol_type;
        return this;
    }

    public PlayUrlRequest setBegin_time(String begin_time) {
        this.begin_time = begin_time;
        return this;
    }

    public PlayUrlRequest setEnd_time(String end_time) {
        this.end_time = end_time;
        return this;
    }

    public PlayUrlRequest setRequest_listener(IDataListener request_listener) {
        this.request_listener = request_listener;
        return this;
    }
}
