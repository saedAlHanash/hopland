package com.hopeland.pda.example.Helpers.View;/*
package com.example.qareeb_driver.Helpers.View;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;


import android.os.Build;
import android.os.CountDownTimer;

import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;


import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.qareeb_driver.AppConfig.SharedPreference;
import com.example.qareeb_driver.R;
import com.example.qareeb_driver.UI.Activities.MainActivity;

import static com.example.qareeb_driver.AppConfig.SharedPreference.REMOVE_TIMER;
import static com.example.qareeb_driver.AppConfig.SharedPreference.getInstance;

public class MyService extends Service {


    Intent intent = new Intent("TIMER");

    long time;

    public MyService() {
    }

    @Override
    public void onCreate() {
        getInstance(this);
        StartForeground();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            time = intent.getLongExtra("timer", 0);
        } catch (Exception ignore) {
        }

        countTime(time);


        return START_STICKY;
    }

    public void countTime(long duration) {
        new CountDownTimer(duration, 1000) {

            public void onTick(long millisUntilFinished) {

                Log.d("SAED___", "onTick: " + millisUntilFinished);
                intent.putExtra("timer", millisUntilFinished);

                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);

            }

            public void onFinish() {
                REMOVE_TIMER();
                intent.putExtra("timer", -1);
                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                stopSelf();
            }

        }.start();
    }

    private void StartForeground() {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 */
/* Request code *//*
, intent, PendingIntent.FLAG_ONE_SHOT);

        String CHANNEL_ID = "channel_location";
        String CHANNEL_NAME = "channel_location";

        NotificationCompat.Builder builder = null;
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            builder.setChannelId(CHANNEL_ID);
            builder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        }

        builder.setContentTitle("نسيت كلمة المرور");
        builder.setContentText("في انتظار اعادة ارسال رمز التحقيق ");
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_logo);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(101, notification);
    }

    @Override
    public void onDestroy() {

        SharedPreference.REMOVE_TIMER();
        Log.e("SAED___", "onDestroy: --------------------------");
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}*/
