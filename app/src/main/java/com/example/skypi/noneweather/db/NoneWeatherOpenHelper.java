package com.example.skypi.noneweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Skypi on 2015/12/13.
 */
public class NoneWeatherOpenHelper extends SQLiteOpenHelper {

    /*Province 表 */
    public static final String CREATE_PROVINCE = "create table Province("
            +"id integer primary key autoincrement,"
            +"province_name text,"
            +"province_code text)";

    /*City 表*/
    public static final String CREATE_CITY ="create table City("
            +"id integer primary key autoincrement,"
            +"city_name text,"
            +"city_code text,"
            +"province_id integer)";

    /*County 表*/
    public static final String CREATE_COUNTY ="create table County("
            +"id integer primary key autoincrement,"
            +"county_name text,"
            +"county_code text,"
            +"city_id integer)";




    public NoneWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
       db.execSQL(CREATE_PROVINCE);  //创建 province表
        db.execSQL(CREATE_CITY);  //创建 city表
        db.execSQL(CREATE_COUNTY);  //创建 county表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {

    }
}
