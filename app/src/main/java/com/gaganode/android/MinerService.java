package com.gaganode.android;


import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.gaganode.sdk.MinerSdk;

import java.util.Random;

public class MinerService extends Service {


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        //start notification
        startNotification();

        ///acquire partial_wake_lock
        PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "service:miner");
        if (null != wakeLock) {wakeLock.acquire();}

        ////using sdk
        SharedPreferences miner_sdk_sp= this.getSharedPreferences("miner_sdk",MODE_PRIVATE);
        long node_id=miner_sdk_sp.getLong("node_id", Math.abs(new Random().nextLong()));
        String miner_sdk_token=miner_sdk_sp.getString("token","");
        miner_sdk_sp.edit().putLong("node_id",node_id).apply();
        //
        MinerSdk.Init(miner_sdk_token,node_id);
        MinerSdk.Start();

        //keep alive set alarm pending task
        AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent restartServiceR = new Intent(this, RestartServiceReceiver.class);
        restartServiceR.setAction("keep_service");

        PendingIntent restartIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            restartIntent = PendingIntent.getBroadcast(this, 0, restartServiceR, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_CANCEL_CURRENT );
        }else{
            restartIntent = PendingIntent.getBroadcast(this, 0, restartServiceR, 0);
        }
        alarms.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), 60000L, restartIntent);
    }



    public static void StartService(Context context){
        Intent minerService = new Intent(context, MinerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            context.startForegroundService(minerService);
        }else{
            context.startService(minerService);
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "gaga_miner_c_id";
        String channelName = "gaga_miner_c_name";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.enableLights(true);
        channel.setShowBadge(true);

        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    private void startNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        Notification notification = new NotificationCompat.Builder(this, channelId).setOngoing(true)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("gaga")
        .setPriority(PRIORITY_MIN)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .build();
        startForeground(new Random().nextInt(1000) +1, notification);
    }


    public static boolean IsServiceRunning(Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MinerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}








