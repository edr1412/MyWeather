package com.example.myweather.provider;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.example.myweather.service.WidgetUpdateService;

public class WidgetProvider extends AppWidgetProvider {

    /**
     * 接收开机和启动程序时候的广播时执行
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        context.startService(new Intent(context, WidgetUpdateService.class));
    }

    /**
     * Widget添加到屏幕时执行
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        context.startService(new Intent(context, WidgetUpdateService.class));
    }

    /**
     * Widget更新时执行
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Widget从屏幕移除时执行
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * 最后一个Widget从屏幕移除时执行
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, WidgetUpdateService.class));
    }

}
