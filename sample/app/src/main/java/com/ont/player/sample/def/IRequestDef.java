package com.ont.player.sample.def;

/**
 * Created by betali on 2018/3/19.
 */

public interface IRequestDef {

    interface IRequestTypeDef {
        String GET = "GET";
        String POST = "POST";
        String PUT = "PUT";
    }

    interface IRequestResultDef {
        int ERR_OK = 0;
        int ERR_NETWORK = -19998;
        int ERR_NULL = -19999;
    }

    interface ICmdStatusDef {

        int STATUS_NULL = -19999; //未定义
        int STATUS_OFFLINE = 0; //设备不在线|device not online
        int STATUS_SENDING = 1; //命令已创建| sending
        int STATUS_SEND = 2; //命令已发往设备| send ok
        int STATUS_SEND_ERROR = 3;//命令发往设备失败| Send error 
        int STATUS_OK = 4;//设备正常响应| ok
        int STATUS_TIME_OUT = 5;//命令执行超时| time out
        int STATUS_RESP_ERROR = 6;//设备响应数据错误 | resp data error
    }

    interface IRequestUrlDef {

        String API_URL = "http://api.heclouds.com";
    }
}
