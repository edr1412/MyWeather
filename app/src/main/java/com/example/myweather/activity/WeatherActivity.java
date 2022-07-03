package com.example.myweather.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;


import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myweather.R;
import com.example.myweather.model.CityItem;
import com.example.myweather.adapter.CityItemAdapter;
import com.example.myweather.service.WeatherDataUpdateService;
import com.example.myweather.util.IHttpCallbackListener;
import com.example.myweather.util.HttpUtil;
import com.example.myweather.util.ToastUtil;
import com.example.myweather.util.ParseUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class WeatherActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, OnClickListener {

    //定义当前活动的一个实例，用于在其他活动类中调用
    protected static Activity instance = null;

    // 滑动刷新
    private SwipeRefreshLayout swipe_container;

    // 进度对话框
    private ProgressDialog progress_dialog;

    // 侧滑菜单
    private DrawerLayout drawer_layout;
    private View menu_list;
    private ListView city_list;
    private Button menu_left;
    private Button add_city;
    private Button setting;

    //首页天气信息页面视图
    private LinearLayout weather_info;

    // 已选城市列表
    private HashMap<String, String> cityName_weatherCode = new HashMap<String, String>(10);
    private ArrayList<CityItem> cities = new ArrayList<CityItem>(10);

    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;

    // 用于显示城市名
    private TextView city_name_tv;
    private TextView city_name_drawer_tv;

    // 用于显示当前时间
    private TextView update_time_tv;

    // 用于显示当前温度
    private TextView wendu_tv;

    // 用于显示天气信息对应的图片
    private ImageView ic_00_iv;
    private ImageView ic_0_iv;
    private ImageView ic_1_iv;
    private ImageView ic_2_iv;
    private ImageView ic_3_iv;
    private ImageView ic_4_iv;
    private ImageView ic_000_iv;

    // 其他信息
    private TextView high_00_tv;
    private TextView low_00_tv;
    private TextView date_00_tv;
    private TextView type_00_tv;
    private TextView fengli_00_tv;
    private TextView divide_00;

    private TextView high_0_tv;
    private TextView low_0_tv;
    private TextView date_0_tv;
    private TextView type_0_tv;
    private TextView fengli_0_tv;
    private TextView du_tv;
    private TextView divide_0;
    private TextView type_tv;
    private TextView high_tv;
    private TextView low_tv;
    private TextView divide_tv;

    private TextView high_1_tv;
    private TextView low_1_tv;
    private TextView date_1_tv;
    private TextView type_1_tv;
    private TextView fengli_1_tv;
    private TextView divide_1;

    private TextView high_2_tv;
    private TextView low_2_tv;
    private TextView date_2_tv;
    private TextView type_2_tv;
    private TextView fengli_2_tv;
    private TextView divide_2;

    private TextView high_3_tv;
    private TextView low_3_tv;
    private TextView date_3_tv;
    private TextView type_3_tv;
    private TextView fengli_3_tv;
    private TextView divide_3;

    private TextView high_4_tv;
    private TextView low_4_tv;
    private TextView date_4_tv;
    private TextView type_4_tv;
    private TextView fengli_4_tv;
    private TextView divide_4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_MyWeather);

        setContentView(R.layout.home_page);

        findView();

        setHorizontalSwipe();

        // 更新当前城市列表
        updateAll(null, null);

        // 若首次启动APP，则先选择城市
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isFirstStart = prefs.getBoolean("first_start", true);
        if (isFirstStart) {
            Intent intent = new Intent(this, ChooseAreaActivity.class);
            startActivity(intent);
        }

        // 发送桌面小部件启动的广播
        Intent intent = new Intent("com.example.myweather.MY_WIDGETPROVIDER_BROADCAST");
        sendBroadcast(intent);

        // 根据设置记录，判断是否需要启动后台更新服务
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isBackUpdate = prefs.getBoolean("back_update", true);
        if (isBackUpdate) {
            Intent i = new Intent(this, WeatherDataUpdateService.class);
            i.putExtra("anHour", -1);
            startService(i);
        }

        // Extra有县级代号时就去查询天气
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            //如果是前2次添加城市，就弹出下滑刷新的提示信息
            prefs = getSharedPreferences("settings", MODE_PRIVATE);
            int isFirstAddCity = prefs.getInt("first_add_city", 1);
            if (isFirstAddCity == 1) {

                alert = null;
                builder = new AlertDialog.Builder(WeatherActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                alert = builder.setMessage("提示：\n下滑可手动刷新天气，也可在设置里自定义自动更新间隔").setCancelable(false).setPositiveButton("好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create(); // 创建AlertDialog对象
                alert.show(); // 显示对话框

                SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
                editor.putInt("first_add_city", isFirstAddCity + 1);
                editor.commit();
            } else if (isFirstAddCity == 2) {

                alert = null;
                builder = new AlertDialog.Builder(WeatherActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                alert = builder.setMessage("提示：\n左右滑动可快速切换城市，或者也可在侧边栏中选择").setCancelable(false).setPositiveButton("好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create(); // 创建AlertDialog对象
                alert.show(); // 显示对话框

                SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
                editor.putInt("first_add_city", isFirstAddCity + 1);
                editor.commit();
            }

            queryWithCountyCodeAndUpdate(countyCode);
        }

    }

    /**
     * 县级代号 查 天气代号，再查天气，并更新
     */
    private void queryWithCountyCodeAndUpdate(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryAndUpdate(address, "countyCode", null);
    }

    /**
     * 天气代号 查 天气，并更新
     */
    private void querywithWeatherCodeAndUpdate(String weatherCode) {
        String address = "http://wthrcdn.etouch.cn/weather_mini?citykey=" + weatherCode;
        queryAndUpdate(address, "weatherCode", weatherCode);
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息，并更新
     */
    private void queryAndUpdate(final String address, final String type, final String weatherCode) {
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new IHttpCallbackListener() {

            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        // 从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            querywithWeatherCodeAndUpdate(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    // 处理服务器返回的天气信息,存储到SharedPreferences
                    final String city = ParseUtil.handleWeatherResponse(WeatherActivity.this, response);
                    //主线程 处理后续
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // 将得到的城市名与城市天气代号对应存储起来，下次刷新天气的时候会用到
                            SharedPreferences.Editor editor = getSharedPreferences("city2weathercode", MODE_PRIVATE).edit();
                            editor.putString(city, weatherCode);
                            editor.commit();
                            // 更新城市列表并展示天气
                            updateAll(city, "added");
                            closeProgressDialog();
                            ToastUtil.showToast(WeatherActivity.this, "天气数据已更新", Toast.LENGTH_SHORT);

                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        closeProgressDialog();
                        ToastUtil.showToast(WeatherActivity.this, "请检查网络连接", Toast.LENGTH_SHORT);
                    }
                });
            }
        });
    }

    /**
     * 输入cityName，更新current_city为cityName，从SharedPreferences读取对应的天气信息，并显示到界面上
     */
    private void showWeather(String cityName) {
        if (cityName == null) {
            city_name_tv.setText("未选择城市");
            city_name_drawer_tv.setText("未选择");
            update_time_tv.setText(null);
            wendu_tv.setText("Hi");

            high_00_tv.setText(null);
            low_00_tv.setText(null);
            date_00_tv.setText("请打开左侧菜单，选择城市");
            type_00_tv.setText(null);
            fengli_00_tv.setText(null);
            ic_00_iv.setImageResource(R.mipmap.tr);
            divide_00.setText(null);

            high_0_tv.setText(null);
            low_0_tv.setText(null);
            date_0_tv.setText(null);
            type_0_tv.setText(null);
            fengli_0_tv.setText(null);
            ic_0_iv.setImageResource(R.mipmap.tr);
            du_tv.setText(null);
            divide_0.setText(null);
            type_tv.setText(null);
            high_tv.setText(null);
            low_tv.setText(null);
            divide_tv.setText(null);
            ic_000_iv.setImageResource(R.mipmap.tr);

            high_1_tv.setText(null);
            low_1_tv.setText(null);
            date_1_tv.setText(null);
            type_1_tv.setText(null);
            fengli_1_tv.setText(null);
            ic_1_iv.setImageResource(R.mipmap.tr);
            divide_1.setText(null);

            high_2_tv.setText(null);
            low_2_tv.setText(null);
            date_2_tv.setText(null);
            type_2_tv.setText(null);
            fengli_2_tv.setText(null);
            ic_2_iv.setImageResource(R.mipmap.tr);
            divide_2.setText(null);

            high_3_tv.setText(null);
            low_3_tv.setText(null);
            date_3_tv.setText(null);
            type_3_tv.setText(null);
            fengli_3_tv.setText(null);
            ic_3_iv.setImageResource(R.mipmap.tr);
            divide_3.setText(null);

            high_4_tv.setText(null);
            low_4_tv.setText(null);
            date_4_tv.setText(null);
            type_4_tv.setText(null);
            fengli_4_tv.setText(null);
            ic_4_iv.setImageResource(R.mipmap.tr);
            divide_4.setText(null);

        } else {
            // 存储当前城市名
            SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
            editor.putString("current_city", cityName);
            editor.commit();

            SharedPreferences prefs = getSharedPreferences(cityName, MODE_PRIVATE);
            city_name_tv.setText(prefs.getString("city", null));
            city_name_drawer_tv.setText(prefs.getString("city", null));
            update_time_tv.setText(prefs.getString("update_time", null));
            wendu_tv.setText(prefs.getString("wendu", null));
            //背景切换
            //((LinearLayout)findViewById(R.id.main_layout)).setBackgroundResource(R.mipmap.bg_sun);

            high_00_tv.setText(prefs.getString("high_00", null));
            low_00_tv.setText(prefs.getString("low_00", null));
            date_00_tv.setText(prefs.getString("date_00", null));
            type_00_tv.setText(prefs.getString("type_00", null));
            fengli_00_tv.setText(prefs.getString("fengli_00", null));
            ic_00_iv.setImageResource(selectImage(prefs.getString("type_00", null)));
            divide_00.setText("/");

            high_0_tv.setText(prefs.getString("high_0", null));
            low_0_tv.setText(prefs.getString("low_0", null));
            date_0_tv.setText(prefs.getString("date_0", null));
            type_0_tv.setText(prefs.getString("type_0", null));
            fengli_0_tv.setText(prefs.getString("fengli_0", null));
            ic_0_iv.setImageResource(selectImage(prefs.getString("type_0", null)));
            du_tv.setText("°");
            divide_0.setText("/");
            type_tv.setText(prefs.getString("type_0", null));
            high_tv.setText(prefs.getString("high_0", null));
            low_tv.setText(prefs.getString("low_0", null));
            divide_tv.setText("/");
            ic_000_iv.setImageResource(selectImage(prefs.getString("type_0", null)));

            high_1_tv.setText(prefs.getString("high_1", null));
            low_1_tv.setText(prefs.getString("low_1", null));
            date_1_tv.setText(prefs.getString("date_1", null));
            type_1_tv.setText(prefs.getString("type_1", null));
            fengli_1_tv.setText(prefs.getString("fengli_1", null));
            ic_1_iv.setImageResource(selectImage(prefs.getString("type_1", null)));
            divide_1.setText("/");

            high_2_tv.setText(prefs.getString("high_2", null));
            low_2_tv.setText(prefs.getString("low_2", null));
            date_2_tv.setText(prefs.getString("date_2", null));
            type_2_tv.setText(prefs.getString("type_2", null));
            fengli_2_tv.setText(prefs.getString("fengli_2", null));
            ic_2_iv.setImageResource(selectImage(prefs.getString("type_2", null)));
            divide_2.setText("/");

            high_3_tv.setText(prefs.getString("high_3", null));
            low_3_tv.setText(prefs.getString("low_3", null));
            date_3_tv.setText(prefs.getString("date_3", null));
            type_3_tv.setText(prefs.getString("type_3", null));
            fengli_3_tv.setText(prefs.getString("fengli_3", null));
            ic_3_iv.setImageResource(selectImage(prefs.getString("type_3", null)));
            divide_3.setText("/");

            high_4_tv.setText(prefs.getString("high_4", null));
            low_4_tv.setText(prefs.getString("low_4", null));
            date_4_tv.setText(prefs.getString("date_4", null));
            type_4_tv.setText(prefs.getString("type_4", null));
            fengli_4_tv.setText(prefs.getString("fengli_4", null));
            ic_4_iv.setImageResource(selectImage(prefs.getString("type_4", null)));
            divide_4.setText("/");
        }
    }

    /**
     * 根据得到的天气type，返回对应的天气图片Id
     */
    public static int selectImage(String type) {
        int icId;
        switch (type) {
            case "阴":
                icId = R.mipmap.weather_cloudy_day;
                break;
            case "多云":
                icId = R.mipmap.weather_cloudy_weather;
                break;
            case "雾":
                icId = R.mipmap.weather_fog;
                break;
            case "霾":
                icId = R.mipmap.weather_haze;
                break;
            case "大雨":
                icId = R.mipmap.weather_rain_heavy;
                break;
            case "小雨":
                icId = R.mipmap.weather_rain_light;
                break;
            case "中雨":
                icId = R.mipmap.weather_rain_moderate;
                break;
            case "小到中雨":
                icId = R.mipmap.weather_rain_light_moderate;
                break;
            case "中到大雨":
                icId = R.mipmap.weather_rain_moderate_heavy;
                break;
            case "阵雨":
                icId = R.mipmap.weather_rain_shower;
                break;
            case "雷阵雨":
                icId = R.mipmap.weather_rain_thunderstorms;
                break;
            case "暴雨":
                icId = R.mipmap.weather_rain_torrential;
                break;
            case "雨夹雪":
                icId = R.mipmap.weather_sleet;
                break;
            case "大雪":
                icId = R.mipmap.weather_snow_heavy;
                break;
            case "小雪":
                icId = R.mipmap.weather_snow_light;
                break;
            case "中雪":
                icId = R.mipmap.weather_snow_moderate;
                break;
            case "阵雪":
                icId = R.mipmap.weather_snow_shower;
                break;
            case "暴雪":
                icId = R.mipmap.weather_snow_torrential;
                break;
            case "晴":
                icId = R.mipmap.weather_sunny_day;
                break;
            default:
                icId = R.mipmap.not_applicable;
                break;
        }
        return icId;
    }

    /**
     * 触发滑动刷新后执行的操作
     */
    @Override
    public void onRefresh() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String cityName = prefs.getString("current_city", null);
        prefs = getSharedPreferences("city2weathercode", MODE_PRIVATE);
        final String weatherCode = prefs.getString(cityName, null);
        if (!TextUtils.isEmpty(weatherCode)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    querywithWeatherCodeAndUpdate(weatherCode);
                    swipe_container.setRefreshing(false);
                }
            }, 0); // 0秒后发送消息，停止刷新
        } else {
            ToastUtil.showToast(WeatherActivity.this, "先添加一个城市吧", Toast.LENGTH_SHORT);
            swipe_container.setRefreshing(false);
        }
    }

    /**
     * 为按键注册监听事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_left:
                drawer_layout.openDrawer(menu_list);
                break;
            case R.id.add_city:
                Intent intent_add_city = new Intent(this, ChooseAreaActivity.class);
                intent_add_city.putExtra("from_weather_activity", true);
                startActivity(intent_add_city);
                finish();
                break;
            case R.id.setting:
                Intent intent_setting = new Intent(this, SettingActivity.class);
                drawer_layout.closeDrawers();
                startActivity(intent_setting);
                break;
            default:
                break;
        }
    }

    /**
     * 更新城市列表，然后调用showWeather()更新天气:
     * 1/(cityName, null)-删除cityName.
     * 2/(null, "removed")-更新城市列表，并指定第一个城市为current_city，然后展示当前城市天气.
     * 3/(cityName, "added")-更新城市列表，并指定cityName为current_city，然后展示当前城市天气.
     * 4/(null, null)-更新城市列表，然后展示当前城市天气.
     *
     */
    private void updateAll(String cityName, String flag) {
        if (cityName != null && flag == null) {
            // 只传入城市名时：删除该城市并更新
            SharedPreferences.Editor editor = getSharedPreferences("city2weathercode", MODE_PRIVATE).edit();
            editor.remove(cityName);
            editor.commit();
            editor = getSharedPreferences(cityName, MODE_PRIVATE).edit();
            editor.clear();
            editor.commit();
            updateAll(null, "removed");
        } else {
            // 更新关注的城市：重灌 cityName_weatherCode 和 cities
            // 所有添加过的城市都会在city2weathercode中有记录，故从city2weathercode得到城市列表即可
            SharedPreferences prefs = getSharedPreferences("city2weathercode", MODE_PRIVATE);
            cityName_weatherCode.clear();
            cityName_weatherCode.putAll((HashMap<String, String>) prefs.getAll());
            cities.clear();
            ArrayList<String> temp = new ArrayList<String>(10);
            temp.addAll(cityName_weatherCode.keySet());

            // 初始化城市列表数据
            for (int i = 0; i < temp.size(); i++) {
                CityItem t = new CityItem(temp.get(i));
                cities.add(t);
            }

            // 如果有传入flag标记，则需要重新指定current_city,并更新天气为current_city的数据
            if (flag != null) {
                if (flag.equals("removed")) {
                    // 更新当前城市的标记，并显示当前列表中第一个城市
                    SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
                    if (temp.size() != 0) {
                        editor.putString("current_city", temp.get(0));
                        editor.commit();
                        showWeather(temp.get(0));
                    } else {
                        editor.putString("current_city", null);
                        editor.commit();
                        showWeather(null);
                    }
                } else if (flag.equals("added")) {
                    SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
                    editor.putString("current_city", cityName);
                    editor.commit();
                    showWeather(cityName);
                }
            } else {
                //防止没有“settings”，若没有则创建一个
                SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
                editor.commit();
                prefs = getSharedPreferences("settings", MODE_PRIVATE);
                showWeather(prefs.getString("current_city", null));
            }

            CityItemAdapter adapter = new CityItemAdapter(WeatherActivity.this, R.layout.city_item, cities);
            city_list.setAdapter(adapter);

            // 为城市列表建立点击监听事件（显示点击的城市）
            city_list.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    drawer_layout.closeDrawers();
                    CityItem t = cities.get(position);
                    showWeather(t.getCityName());
                }
            });

            // 为城市列表建立长按监听事件（删除长按的城市）
            city_list.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                    // 弹出确认删除对话框
                    alert = null;
                    builder = new AlertDialog.Builder(WeatherActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                    alert = builder.setMessage("确定删除此城市吗").setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CityItem t = cities.get(position);
                            updateAll(t.getCityName(), null);
                        }
                    }).create(); // 创建AlertDialog对象
                    alert.show(); // 显示对话框

                    // 长按事件后不想继续执行点击事件时，应返回true
                    return true;
                }
            });
        }
    }

    /**
     * 判断Back按键
     */
    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(menu_list)) {
            drawer_layout.closeDrawers();
        } else {
            finish();
        }
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progress_dialog == null) {
            progress_dialog = new ProgressDialog(this);
            progress_dialog.setMessage("加载中...");
            progress_dialog.setCanceledOnTouchOutside(false);
        }
        progress_dialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progress_dialog != null) {
            progress_dialog.dismiss();
        }
    }

    private void findView() {

        instance = this;

        swipe_container = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipe_container.setOnRefreshListener(this);

        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menu_list = findViewById(R.id.menu_list);
        city_list = (ListView) findViewById(R.id.city_list);

        weather_info = (LinearLayout) findViewById(R.id.weather_info);

        menu_left = (Button) findViewById(R.id.menu_left);
        menu_left.setOnClickListener(this);
        add_city = (Button) findViewById(R.id.add_city);
        add_city.setOnClickListener(this);
        setting = (Button) findViewById(R.id.setting);
        setting.setOnClickListener(this);

        city_name_tv = (TextView) findViewById(R.id.city_name);
        city_name_drawer_tv = (TextView) findViewById(R.id.city_name_drawer);
        update_time_tv = (TextView) findViewById(R.id.update_time);
        wendu_tv = (TextView) findViewById(R.id.wendu);

        ic_00_iv = (ImageView) findViewById(R.id.ic_00);
        ic_0_iv = (ImageView) findViewById(R.id.ic_0);
        ic_1_iv = (ImageView) findViewById(R.id.ic_1);
        ic_2_iv = (ImageView) findViewById(R.id.ic_2);
        ic_3_iv = (ImageView) findViewById(R.id.ic_3);
        ic_4_iv = (ImageView) findViewById(R.id.ic_4);
        ic_000_iv = (ImageView) findViewById(R.id.icon_0);

        high_00_tv = (TextView) findViewById(R.id.high_00);
        low_00_tv = (TextView) findViewById(R.id.low_00);
        date_00_tv = (TextView) findViewById(R.id.date_00);
        type_00_tv = (TextView) findViewById(R.id.type_00);
        fengli_00_tv = (TextView) findViewById(R.id.fengli_00);
        divide_00 = (TextView) findViewById(R.id.divide_00);

        high_0_tv = (TextView) findViewById(R.id.high_0);
        low_0_tv = (TextView) findViewById(R.id.low_0);
        date_0_tv = (TextView) findViewById(R.id.date_0);
        type_0_tv = (TextView) findViewById(R.id.type_0);
        fengli_0_tv = (TextView) findViewById(R.id.fengli_0);
        du_tv = (TextView) findViewById(R.id.du);
        divide_0 = (TextView) findViewById(R.id.divide_0);

        type_tv = (TextView) findViewById(R.id.type);
        high_tv = (TextView) findViewById(R.id.high);
        low_tv = (TextView) findViewById(R.id.low);
        divide_tv = (TextView) findViewById(R.id.divide);

        high_1_tv = (TextView) findViewById(R.id.high_1);
        low_1_tv = (TextView) findViewById(R.id.low_1);
        date_1_tv = (TextView) findViewById(R.id.date_1);
        type_1_tv = (TextView) findViewById(R.id.type_1);
        fengli_1_tv = (TextView) findViewById(R.id.fengli_1);
        divide_1 = (TextView) findViewById(R.id.divide_1);

        high_2_tv = (TextView) findViewById(R.id.high_2);
        low_2_tv = (TextView) findViewById(R.id.low_2);
        date_2_tv = (TextView) findViewById(R.id.date_2);
        type_2_tv = (TextView) findViewById(R.id.type_2);
        fengli_2_tv = (TextView) findViewById(R.id.fengli_2);
        divide_2 = (TextView) findViewById(R.id.divide_2);

        high_3_tv = (TextView) findViewById(R.id.high_3);
        low_3_tv = (TextView) findViewById(R.id.low_3);
        date_3_tv = (TextView) findViewById(R.id.date_3);
        type_3_tv = (TextView) findViewById(R.id.type_3);
        fengli_3_tv = (TextView) findViewById(R.id.fengli_3);
        divide_3 = (TextView) findViewById(R.id.divide_3);

        high_4_tv = (TextView) findViewById(R.id.high_4);
        low_4_tv = (TextView) findViewById(R.id.low_4);
        date_4_tv = (TextView) findViewById(R.id.date_4);
        type_4_tv = (TextView) findViewById(R.id.type_4);
        fengli_4_tv = (TextView) findViewById(R.id.fengli_4);
        divide_4 = (TextView) findViewById(R.id.divide_4);
    }

    /**
     * 首页天气信息视图页面添加左右滑动监听，以实现切换城市
     */
    private void setHorizontalSwipe(){
        weather_info.setOnTouchListener(new View.OnTouchListener() {

            float x1 = 0;
            float x2 = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        ArrayList<String> temp = new ArrayList<String>(10);
                        temp.addAll(cityName_weatherCode.keySet());
                        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                        int currentCityIndex = temp.indexOf(prefs.getString("current_city", null));
                        if (x1 - x2 > 50 && temp.size() > 1) {
                            if (currentCityIndex + 1 < temp.size()) {
                                showWeather(temp.get(currentCityIndex + 1));
                            } else {
                                ToastUtil.showToast(WeatherActivity.this, "已是最后一个城市 ", Toast.LENGTH_SHORT);
                            }
                        } else if (x2 - x1 > 50 && temp.size() > 1) {
                            if (currentCityIndex - 1 >= 0) {
                                showWeather(temp.get(currentCityIndex - 1));
                            } else {
                                ToastUtil.showToast(WeatherActivity.this, "已是第一个城市", Toast.LENGTH_SHORT);
                            }
                        }
                        break;
                }
                return true;
            }
        });
    }

}


