package com.ont.player.sample.def;

/**
 * Created by betali on 2018/3/19.
 */

public class IRequestDef {

    public static final class IRequestTypeDef {
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
    }

    public static final class IRequestResultDef {
        public static final int ERR_OK = 0;
        public static final int ERR_NETWORK = -19998;
        public static final int ERR_NULL = -19999;
    }

    public static final class ICmdStatusDef {
        public static final int STATUS_NULL = -19999; //未定义
        public static final int STATUS_OFFLINE = 0; //设备不在线|device not online
        public static final int STATUS_SENDING = 1; //命令已创建| sending
        public static final int STATUS_SEND = 2; //命令已发往设备| send ok
        public static final int STATUS_SEND_ERROR = 3;//命令发往设备失败| Send error 
        public static final int STATUS_OK = 4;//设备正常响应| ok
        public static final int STATUS_TIME_OUT = 5;//命令执行超时| time out
        public static final int STATUS_RESP_ERROR = 6;//设备响应数据错误 | resp data error
    }

    public static final class IRequestUrlDef {
        public static String API_URL = "";
    }
}
