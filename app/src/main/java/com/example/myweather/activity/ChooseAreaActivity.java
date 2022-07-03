package com.example.myweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.example.myweather.R;
import com.example.myweather.model.City;
import com.example.myweather.model.County;
import com.example.myweather.model.Province;
import com.example.myweather.db.AreaDao;
import com.example.myweather.util.IHttpCallbackListener;
import com.example.myweather.util.HttpUtil;
import com.example.myweather.util.ToastUtil;
import com.example.myweather.util.ParseUtil;

public class ChooseAreaActivity extends Activity implements OnClickListener {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private Button back;
    private TextView titleText;
    private ListView cityOptional_lv;
    private ArrayAdapter<String> adapter;
    private AreaDao areaDao;
    private List<String> dataList = new ArrayList<String>();

    private boolean isFromWeatherActivity;

    // 省列表
    private List<Province> provinceList;

    // 市列表
    private List<City> cityList;

    // 县列表
    private List<County> countyList;

    // 选中的省份
    private Province selectedProvince;

    // 选中的城市
    private City selectedCity;

    // 当前选中的级别
    private int currentLevel;

    private void findView() {
        back = (Button) findViewById(R.id.back);
        back.setOnClickListener(this);
        titleText = (TextView) findViewById(R.id.title_text);
        cityOptional_lv = (ListView) findViewById(R.id.city_optional_list);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.choose_area_page);

        findView();

        adapter = new ArrayAdapter<String>(this, R.layout.city_optional_item, dataList);
        cityOptional_lv.setAdapter(adapter);
        areaDao = AreaDao.getInstance(this);
        cityOptional_lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(index);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(index);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    //设置first_start为false
                    SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
                    editor.putBoolean("first_start", false);
                    editor.commit();
                    //putExtra countyCode 给 WeatherActivity intent 即可
                    String countyCode = countyList.get(index).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到则去服务器上查询
     */
    private void queryProvinces() {
        provinceList = areaDao.getProvinces();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            cityOptional_lv.setSelection(0);
            titleText.setText("添加城市");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到则去服务器上查询
     */
    private void queryCities() {
        cityList = areaDao.getCities(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            cityOptional_lv.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到则去服务器查询
     */
    private void queryCounties() {
        countyList = areaDao.getCounties(selectedCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            cityOptional_lv.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /**
     * 根据传入的代号和类型从服务器上查询省市县数据
     * 查完存到本地数据库，再回到本地数据库查询
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new IHttpCallbackListener() {

            @Override
            public void onFinish(String response) {
                // 存数据到本地数据库
                boolean result = false;
                if ("province".equals(type)) {
                    result = ParseUtil.handleProvincesResponse(areaDao, response);
                } else if ("city".equals(type)) {
                    result = ParseUtil.handleCitiesResponse(areaDao, response, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = ParseUtil.handleCountiesResponse(areaDao, response, selectedCity.getId());
                }
                if (result) {
                    // 回到主线程处理
                    // 重新查询本地数据库
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // 回到主线程处理
                // 提示错误
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                        closeProgressDialog();
                        ToastUtil.showToast(ChooseAreaActivity.this, "请检查网络连接", Toast.LENGTH_SHORT);
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 返回按钮的逻辑
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else {
                    isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
                    if (isFromWeatherActivity) {
                        Intent intent = new Intent(this, WeatherActivity.class);
                        startActivity(intent);
                    }
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Back逻辑，同上
     */
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
            if (isFromWeatherActivity) {
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

}
