package com.example.myweather.util;

public interface IHttpCallbackListener {

    void onFinish(String response);

    void onError(Exception e);

}
