package com.ont.player.sample.def;

/**
 * Created by betali on 2018/3/19.
 */

public interface IDataListener {

    void onComplete(int apiErr, int dataErr, String response);
}
