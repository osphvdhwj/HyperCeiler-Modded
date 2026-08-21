package com.sevtinge.hyperceiler.hook.module.hook.systemframework.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class AutoTurnOffRadios extends BaseHook {
    
    private static Handler mHandler;
    private static Runnable mWifiRunnable;
    private static Runnable mBluetoothRunnable;
    private static Runnable mHotspotRunnable;

    @Override
    public void init() {
        hookAllMethods("com.android.server.am.ActivityManagerService", "systemReady", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (context == null) return;

                if (mHandler == null) {
                    mHandler = new Handler(Looper.getMainLooper());
                }

                registerReceivers(context);
            }
        });
    }

    private void registerReceivers(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                String action = intent.getAction();
                if (action == null) return;

                if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action) || WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                    handleWifiStateChange(ctx);
                } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action) || BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    handleBluetoothStateChange(ctx);
                } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                    handleHotspotStateChange(ctx, intent);
                }
            }
        }, filter);
    }

    private void handleWifiStateChange(Context context) {
        if (!mPrefsMap.getBoolean("system_framework_auto_turn_off_wifi")) return;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (wifiManager == null || cm == null) return;

        if (!wifiManager.isWifiEnabled()) {
            if (mWifiRunnable != null) mHandler.removeCallbacks(mWifiRunnable);
            return;
        }

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnected = info != null && info.isConnected();

        if (mWifiRunnable != null) {
            mHandler.removeCallbacks(mWifiRunnable);
        }

        if (!isConnected) {
            int minutes = mPrefsMap.getInt("system_framework_auto_turn_off_wifi_timer", 5);
            mWifiRunnable = () -> {
                wifiManager.setWifiEnabled(false);
            };
            mHandler.postDelayed(mWifiRunnable, minutes * 60 * 1000L);
        }
    }

    private void handleBluetoothStateChange(Context context) {
        if (!mPrefsMap.getBoolean("system_framework_auto_turn_off_bluetooth")) return;

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            if (mBluetoothRunnable != null) mHandler.removeCallbacks(mBluetoothRunnable);
            return;
        }

        // Check if any devices are connected
        boolean isConnected = false;
        try {
            int state = btAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
            int a2dpState = btAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
            isConnected = (state == BluetoothProfile.STATE_CONNECTED || a2dpState == BluetoothProfile.STATE_CONNECTED);
        } catch (Exception ignored) {
        }

        if (mBluetoothRunnable != null) {
            mHandler.removeCallbacks(mBluetoothRunnable);
        }

        if (!isConnected) {
            int minutes = mPrefsMap.getInt("system_framework_auto_turn_off_bluetooth_timer", 5);
            mBluetoothRunnable = () -> {
                try {
                    btAdapter.disable();
                } catch (Exception ignored) {}
            };
            mHandler.postDelayed(mBluetoothRunnable, minutes * 60 * 1000L);
        }
    }

    private void handleHotspotStateChange(Context context, Intent intent) {
        if (!mPrefsMap.getBoolean("system_framework_auto_turn_off_hotspot")) return;

        int state = intent.getIntExtra("wifi_state", 11);
        if (state != 13) { // 13 is WIFI_AP_STATE_ENABLED
            if (mHotspotRunnable != null) mHandler.removeCallbacks(mHotspotRunnable);
            return;
        }

        // We assume 0 clients right after enable, or we can check via WifiManager
        // Since Android API doesn't expose hotspot client count publicly, we schedule the turn off,
        // and ideally we'd also listen for client connect/disconnect. 
        // A common broadcast on MIUI for hotspot clients is "android.net.wifi.WIFI_AP_STA_STATUS_CHANGED" or similar.
        // We will just do a basic timeout from when it's enabled if we can't reliably get client count.
        
        if (mHotspotRunnable != null) {
            mHandler.removeCallbacks(mHotspotRunnable);
        }

        int minutes = mPrefsMap.getInt("system_framework_auto_turn_off_hotspot_timer", 5);
        mHotspotRunnable = () -> {
            try {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                XposedHelpers.callMethod(wifiManager, "setWifiApEnabled", null, false);
            } catch (Exception ignored) {}
        };
        mHandler.postDelayed(mHotspotRunnable, minutes * 60 * 1000L);
    }
}
