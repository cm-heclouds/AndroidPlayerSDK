package com.ont.player.sample.network;

import android.text.TextUtils;

import com.ont.player.sample.def.IApiListener;
import com.ont.player.sample.def.IDataListener;
import com.ont.player.sample.def.IRequestDef;
import com.ont.player.sample.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by betali on 2018/3/20.
 */

public class HistoryVideoListRequest extends OntPlayerRequest {
    private final String TAG = "HistoryVideoListRequest";

    String device_id;
    String cmd_uuid;
    IDataListener request_listener;
    public HistoryVideoListRequest(String api_key) {
        super(api_key);
    }

    public HistoryVideoListRequest setDevice_id(String device_id) {
        this.device_id = device_id;
        return this;
    }

    public HistoryVideoListRequest setCmd_uuid(String cmd_uuid) {
        this.cmd_uuid = cmd_uuid;
        return this;
    }

    public HistoryVideoListRequest setRequest_listener(IDataListener request_listener) {
        this.request_listener = request_listener;
        return this;
    }

    @Override
    String getRequestType() {
        return IRequestDef.IRequestTypeDef.GET;
    }

    @Override
    String getRequestUrl() {
        return String.format(IRequestDef.IRequestUrlDef.API_URL + "/cmd_resp?device_id=%s&cmd_uuid=%s", device_id, cmd_uuid);
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
                    if(null != request_listener) {
                        request_listener.onComplete(IRequestDef.IRequestResultDef.ERR_NULL, 0, "返回空");
                    }
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(null != jsonObject){
                        if(0 != jsonObject.optInt("errno")){
                            if(null != request_listener) {
                                request_listener.onComplete(jsonObject.optInt("errno"), 0, "指令发送失败:" + jsonObject.optString("error"));
                            }
                        }else {
                            JSONObject dev_resp = new JSONObject();
                            dev_resp.put("resp_code", 0);
                            dev_resp.put("resp_data", jsonObject.optString("resp"));
                            if (request_listener != null) {
                                request_listener.onComplete(IRequestDef.IRequestResultDef.ERR_OK, 0, dev_resp.toString());
                            }
                        }
                    }
                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        };
    }
}
