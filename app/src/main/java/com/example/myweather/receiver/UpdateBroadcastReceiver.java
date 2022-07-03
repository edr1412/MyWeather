package com.example.myweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.myweather.service.WeatherDataUpdateService;

/**
 * 接收开机广播，启动后台更新服务
 */

public class UpdateBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //防止没有“settings”，若没有则创建一个
        SharedPreferences.Editor editor = context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
        editor.commit();
        //检测设置里是否打开了后台更新服务
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isBackUpdate = prefs.getBoolean("back_update", false);
        if (isBackUpdate) {
            //发送启动后台更新服务的Intent命令
            Intent i = new Intent(context, WeatherDataUpdateService.class);
            i.putExtra("anHour", -1);
            context.startService(i);
        }

    }

}
