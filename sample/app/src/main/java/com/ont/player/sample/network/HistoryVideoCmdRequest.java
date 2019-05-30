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

public class HistoryVideoCmdRequest extends OntPlayerRequest {
    private final String TAG = "HistoryVideoCmdRequest";

    String device_id;
    String channel_id;
    int page;            // 1为起始页
    int per_page;
    IDataListener request_listener;
    public HistoryVideoCmdRequest(String api_key) {
        super(api_key);
    }

    public HistoryVideoCmdRequest setDevice_id(String device_id) {
        this.device_id = device_id;
        return this;
    }

    public HistoryVideoCmdRequest setChannel_id(String channel_id) {
        this.channel_id = channel_id;
        return this;
    }

    public HistoryVideoCmdRequest setPage(int page) {

        if (page >= 1) {
            this.page = page;
        } else {
            this.page = 1;
        }
        return this;
    }

    public HistoryVideoCmdRequest setPer_page(int per_page) {
        this.per_page = per_page;
        return this;
    }

    public void nextPage() {

        page++;
    }

    public void prePage() {

        if (page > 1) {

            page--;
        }
    }

    public HistoryVideoCmdRequest setRequest_listener(IDataListener request_listener) {
        this.request_listener = request_listener;
        return this;
    }

    @Override
    String getRequestType() {
        return IRequestDef.IRequestTypeDef.POST;
    }

    @Override
    String getRequestUrl() {
        return String.format(IRequestDef.IRequestUrlDef.API_URL + "/cmds?device_id=%s&qos=1&type=0", device_id);
    }

    @Override
    String getRequestBody() {
        try {
            JSONObject cmdSub = new JSONObject();
            cmdSub.put("channel_id", Integer.valueOf(channel_id));
            cmdSub.put("page", page);
            cmdSub.put("per_page", per_page);

            JSONObject cmd = new JSONObject();
            cmd.put("cmd", cmdSub);
            cmd.put("type", "video");
            cmd.put("cmdId", 10);
            return cmd.toString();
        }catch (JSONException e){

        }
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
                        }else{
                            JSONObject data = (JSONObject) jsonObject.get("data");
                            doHistoryVideoListRequest(data);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void doHistoryVideoListRequest(JSONObject data) {

        final String cmd_uuid = data.optString("cmd_uuid");
        int cmd_status = IRequestDef.ICmdStatusDef.STATUS_NULL;
        if (data.has("cmd_status")) {
            cmd_status = data.optInt("cmd_status");
        }
        String errMessage;

        switch (cmd_status) {

            case IRequestDef.ICmdStatusDef.STATUS_SENDING:
            case IRequestDef.ICmdStatusDef.STATUS_SEND:
            case IRequestDef.ICmdStatusDef.STATUS_NULL:
                //命令发送中，继续查询命令状态
                CheckCmdRequest request = new CheckCmdRequest(api_key)
                        .setDevice_id(device_id)
                        .setCmd_uuid(cmd_uuid)
                        .setQos_type(1)
                        .setRequest_listener(new IDataListener() {
                            @Override
                            public void onComplete(int apiErr, int dataErr, String response) {

                                LogUtil.i(TAG, "response:" + response);
                                if(apiErr != IRequestDef.IRequestResultDef.ERR_OK || dataErr != IRequestDef.ICmdStatusDef.STATUS_OK) {
                                    if(null != request_listener) {
                                        request_listener.onComplete(apiErr, dataErr, response);
                                    }
                                    return;
                                }
                                HistoryVideoListRequest request = new HistoryVideoListRequest(api_key)
                                        .setDevice_id(device_id)
                                        .setCmd_uuid(cmd_uuid)
                                        .setRequest_listener(request_listener);
                                NetworkClient.doRequest(request);
                            }
                        });
                NetworkClient.doRequest(request);
                return;
            case IRequestDef.ICmdStatusDef.STATUS_OK:
                errMessage = data.optString("dev_resp");
                break;
            case IRequestDef.ICmdStatusDef.STATUS_OFFLINE:
                errMessage = "设备不在线";
                break;
            case IRequestDef.ICmdStatusDef.STATUS_SEND_ERROR:
                errMessage = "指令发往设备失败";
                break;
            case IRequestDef.ICmdStatusDef.STATUS_TIME_OUT:
                errMessage = "指令执行超时";
                break;
            case IRequestDef.ICmdStatusDef.STATUS_RESP_ERROR:
                errMessage = "设备响应错误";
                break;
            default:
                errMessage = "指令发送未知错误";
                break;
        }
        if (request_listener != null) {
            request_listener.onComplete(IRequestDef.IRequestResultDef.ERR_OK, cmd_status, errMessage);
        }
    }
}
