package com.gaganode.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!MinerService.IsServiceRunning(context)){
            MinerService.StartService(context);
        }
    }
}