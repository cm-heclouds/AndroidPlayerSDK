package com.ont.player.sample.network;

import com.ont.player.sample.def.IApiListener;

/**
 * Created by betali on 2018/3/19.
 */

public abstract class OntPlayerRequest {

    protected String api_key;
    protected IApiListener api_listner;

    public OntPlayerRequest(String api_key) {

        this.api_key = api_key;
        initListener();
    }

    String getApiKey(){
        return api_key;
    }
    IApiListener getApiListener() { return api_listner; }

    abstract String getRequestType();
    abstract String getRequestUrl();
    abstract String getRequestBody();
    abstract void initListener();
}
