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

public class LevelRequest extends OntPlayerRequest {
    private final String TAG = "LevelRequest";

    private String channel_id;
    private String device_id;
    private int level;
    private IDataListener request_listener;

    public LevelRequest(String api_key) {
        super(api_key);
    }

    @Override
    String getRequestType() {
        return IRequestDef.IRequestTypeDef.POST;
    }

    @Override
    String getRequestUrl() {
        return String.format(IRequestDef.IRequestUrlDef.API_URL + "/ipc/video/cmds?device_id=%s&qos=0&type=1", device_id);
    }

    @Override
    String getRequestBody() {
        try {
            JSONObject cmdSub = new JSONObject();
            cmdSub.put("level", level);
            cmdSub.put("channel_id", Integer.valueOf(channel_id));

            JSONObject cmd = new JSONObject();
            cmd.put("cmd", cmdSub);
            cmd.put("type", "video");
            cmd.put("cmdId", 6);
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
                    if(null != request_listener)
                        request_listener.onComplete(IRequestDef.IRequestResultDef.ERR_NULL, 0, "返回空");
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(null != jsonObject){
                        if(0 != jsonObject.optInt("errno")){
                            if(null != request_listener)
                                request_listener.onComplete(jsonObject.optInt("errno"), 0, "指令发送失败:" + jsonObject.optString("error"));
                        }else{

                            JSONObject data = (JSONObject) jsonObject.get("data");
                            String cmd_uuid = data.optString("cmd_uuid");
                            int cmd_status = IRequestDef.ICmdStatusDef.STATUS_NULL;
                            if (data.has("cmd_status")) {
                                cmd_status = data.optInt("cmd_status");
                            }
                            LogUtil.i(TAG, "result:" + cmd_uuid + " status:" + cmd_status);
                            parseCmdStatus(cmd_status, cmd_uuid);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public LevelRequest setChannel_id(String channel_id) {
        this.channel_id = channel_id;
        return this;
    }

    public LevelRequest setDevice_id(String device_id) {
        this.device_id = device_id;
        return this;
    }

    public LevelRequest setLevel(int level) {
        this.level = level;
        return this;
    }

    public LevelRequest setRequest_listener(IDataListener request_listener) {
        this.request_listener = request_listener;
        return this;
    }

    private void parseCmdStatus(int cmd_status, String cmd_uuid) {

        String errMessage = null;
        switch (cmd_status) {
            case IRequestDef.ICmdStatusDef.STATUS_SENDING:
            case IRequestDef.ICmdStatusDef.STATUS_NULL:
                //命令发送中，继续查询命令状态
                doCmdStatusRequest(cmd_uuid);
                return;
            case IRequestDef.ICmdStatusDef.STATUS_SEND:
            case IRequestDef.ICmdStatusDef.STATUS_OK:
                errMessage = "指令发送成功";
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

    private void doCmdStatusRequest(String cmd_uuid) {

        CheckCmdRequest request = new CheckCmdRequest(api_key)
                .setDevice_id(device_id)
                .setCmd_uuid(cmd_uuid)
                .setQos_type(0)
                .setRequest_listener(request_listener);
        NetworkClient.doRequest(request);
    }
}
