package com.netbyte.vtunnel.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.activity.MainActivity;
import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.model.Config;
import com.netbyte.vtunnel.model.Global;
import com.netbyte.vtunnel.thread.VPNThread;
import com.netbyte.vtunnel.model.AppConst;

import java.util.concurrent.TimeUnit;

public class MyVPNService extends VpnService {
    private Config config;
    private PendingIntent pendingIntent;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private IPService ipService;

    public MyVPNService() {
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    public void onCreate() {
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        switch (intent.getAction()) {
            case AppConst.BTN_ACTION_CONNECT:
                // init config
                initConfig(intent);
                // create notification
                createNotification();
                // start VPN
                startVPN();
                return START_STICKY;
            case AppConst.BTN_ACTION_DISCONNECT:
                // stop VPN
                stopVPN();
                return START_NOT_STICKY;
            default:
                return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRevoke() {
        stopVPN();
    }

    private void initConfig(Intent intent) {
        SharedPreferences preferences = this.getSharedPreferences(AppConst.APP_NAME, Activity.MODE_PRIVATE);
        String server = preferences.getString("server", AppConst.DEFAULT_SERVER_ADDRESS);
        String path = preferences.getString("path", AppConst.DEFAULT_PATH);
        String dns = preferences.getString("dns", AppConst.DEFAULT_DNS);
        String key = preferences.getString("key", AppConst.DEFAULT_KEY);
        String bypassApps = preferences.getString("bypass_apps", "");
        boolean obfuscate = preferences.getBoolean("obfuscate", false);
        String serverAddress;
        int serverPort;
        if (server.contains(":")) {
            serverAddress = server.split(":")[0];
            serverPort = Integer.parseInt(server.split(":")[1]);
        } else {
            serverAddress = server;
            serverPort = AppConst.DEFAULT_SERVER_PORT;
        }
        this.config = new Config(serverAddress, serverPort, path, dns, key, bypassApps, obfuscate);
        this.ipService = new IPService(config.getServerAddress(), config.getServerPort(), config.getKey());
    }

    private void createNotification() {
        NotificationChannel channel = new NotificationChannel(AppConst.NOTIFICATION_CHANNEL_ID, AppConst.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        notificationBuilder = new NotificationCompat.Builder(this, channel.getId());
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pendingIntent)
                .setContentTitle(AppConst.APP_NAME)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true);
    }

    public void startVPN() {
        try {
            if (Global.RUNNING) {
                stopVPN();
                TimeUnit.SECONDS.sleep(3);
            }
            Global.RUNNING = true;
            VPNThread vpnThread = new VPNThread(config, this, ipService, notificationManager, notificationBuilder);
            vpnThread.start();
            Log.i(AppConst.DEFAULT_TAG, "VPN started");
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, "error on startVPN:" + e.toString());
        }
    }

    public void stopVPN() {
        this.resetGlobalVar();
        this.stopSelf();
        Log.i(AppConst.DEFAULT_TAG, "VPN stopped");
    }

    private void resetGlobalVar() {
        Global.IS_CONNECTED = false;
        Global.RUNNING = false;
        Global.START_TIME = 0;
    }

}
