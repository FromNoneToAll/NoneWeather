package com.example.skypi.noneweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.skypi.noneweather.R;
import com.example.skypi.noneweather.db.NoneWeatherDB;
import com.example.skypi.noneweather.model.City;
import com.example.skypi.noneweather.model.County;
import com.example.skypi.noneweather.model.Province;
import com.example.skypi.noneweather.util.HttpCallBackListener;
import com.example.skypi.noneweather.util.HttpUtil;
import com.example.skypi.noneweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Skypi on 2015/12/13.
 */
public class ChooseAreaActivity extends Activity {
     public static final int LEVEL_PROVINCE = 0;
     public static final int LEVEL_CITY = 1;
     public static final int LEVEL_COUNTY = 2;

     private ProgressDialog progressDialog;
     private TextView titleText;
     private ListView listView;
     private ArrayAdapter<String> adapter;
     private NoneWeatherDB noneWeatherDB;
     private List<String> dataList = new ArrayList<String>();

     /*省列表*/
     private List<Province> provinceList;
     /*城市列表*/
     private List<City> cityList;
     /*县市列表*/
     private List<County> countyList;

     /*选中的省份*/
     private Province selectedProvince;
     /*选中的城市*/
     private City selectedCity;
     /*选中的县市*/
     private County selectedCounty;
     /*选中的当前级别*/
     private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);

        listView= (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        noneWeatherDB = NoneWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince =provinceList.get(index);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(index);
                    queryCounties();
                }
            }


        });queryProvinces(); // 加载省级数据
    }

    /*查询全国的省，优先从数据库查询，如果没查到再去服务器*/
    private void queryProvinces() {
        provinceList = noneWeatherDB.loadProvinces();
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel =LEVEL_PROVINCE;
        }else{
            queryFromServer(null,"province");
        }
    }

    /*查询选中省的市，优先从数据库查询，如果没查到再去服务器*/
    private void queryCities() {
        cityList = noneWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size()>0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel =LEVEL_CITY;
        }else{
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }

    /*查询选中市的县，优先从数据库查询，如果没查到再去服务器*/
    private void queryCounties() {
        countyList = noneWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size()>0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel =LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }


    /*根据传入的代号和类型从服务器上查询省市县*/
    private void queryFromServer(final String code, final String type) {
        String address;
        if(!TextUtils.isEmpty(code)){
            address="http://www.weather.com.cn/data/list3/city" +code+".xml";
        }else{
            address ="http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address,new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvincesResponse(noneWeatherDB,response);
                }else if ("city".equals(type)){
                    result = Utility.handleCitiesResponse(noneWeatherDB, response, selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountiesResponse(noneWeatherDB, response, selectedCity.getId());
                }
                if (result){
                    // 通过runOnUiThread() 方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
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
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //关闭进度对话框
    private void closeProgressDialog() {
        if (progressDialog !=null){
            progressDialog.dismiss();
        }

    }
    //显示进度对话框
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载啦~~");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

  // 捕获Back按键，根据当前级别判断，此时应该返回市列表还是省列表，或是直接退出

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            finish();
        }

    }
}

