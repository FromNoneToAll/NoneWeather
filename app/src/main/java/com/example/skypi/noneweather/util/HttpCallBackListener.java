package com.example.skypi.noneweather.util;

/**
 * Created by Skypi on 2015/12/13.
 */
public interface HttpCallBackListener {
    void onFinish(String response);

    void onError(Exception e);
}
