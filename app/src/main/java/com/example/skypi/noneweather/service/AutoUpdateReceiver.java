package com.example.skypi.noneweather.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Sky on 2015/12/14.
 */
public class AutoUpdateReceiver  extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context,AutoUpdateReceiver.class);
        context.startService(i);
    }
}
