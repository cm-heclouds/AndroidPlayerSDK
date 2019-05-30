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

public class CheckCmdRequest extends OntPlayerRequest {
    private final String TAG = "CheckCmdRequest";

    private int qos_type; //是否需要设备回应，0，不需要，1需要 不填的默认值为0
    private String cmd_uuid;
    private String device_id;
    private IDataListener request_listener;

    public CheckCmdRequest(String api_key) {
        super(api_key);
    }

    @Override
    String getRequestType() {
        return IRequestDef.IRequestTypeDef.GET;
    }

    @Override
    String getRequestUrl() {
        return String.format(IRequestDef.IRequestUrlDef.API_URL + "/cmd_status?device_id=%s&cmd_uuid=%s", device_id, cmd_uuid);
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

                            request_listener.onComplete(jsonObject.optInt("errno"), 0, "指令执行失败:" + jsonObject.optString("error"));
                        }else{

                            JSONObject data = (JSONObject) jsonObject.get("data");
                            parseCmdStatus(data.optInt("status"));
                        }
                    }
                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        };
    }

    public CheckCmdRequest setCmd_uuid(String cmd_uuid) {

        this.cmd_uuid = cmd_uuid;
        return this;
    }

    public CheckCmdRequest setDevice_id(String device_id) {

        this.device_id = device_id;
        return this;
    }

    public CheckCmdRequest setRequest_listener(IDataListener request_listener) {

        this.request_listener = request_listener;
        return this;
    }

    public CheckCmdRequest setQos_type(int qos_type) {

        this.qos_type = qos_type;
        return this;
    }

    private void parseCmdStatus(int cmd_status) {

        String errMessage = null;
        switch (cmd_status) {
            case IRequestDef.ICmdStatusDef.STATUS_SENDING:
                //命令发送中，继续查询命令状态
                NetworkClient.doRequest(this);
                return;
            case IRequestDef.ICmdStatusDef.STATUS_SEND:
                if (qos_type == 0) {
                    errMessage = "指令发送成功";
                    break;
                } else {
                    NetworkClient.doRequest(this);
                    return;
                }
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
}
