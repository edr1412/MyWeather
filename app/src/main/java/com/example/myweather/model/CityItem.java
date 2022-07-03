package com.example.myweather.model;

/**
 * 左边抽屉式侧边栏的列表项 的实体类；
 */
public class CityItem {

    private String cityName;

    public CityItem(String cityName) {
        this.cityName = cityName;
    }

    public String getCityName() {
        return cityName;
    }

}
