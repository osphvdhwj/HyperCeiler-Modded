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

    private static Object mSoftApCallbackProxy;

    private void registerReceivers(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // We don't need WIFI_AP_STATE_CHANGED anymore, we'll use SoftApCallback

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                String action = intent.getAction();
                if (action == null) return;

                if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action) || WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                    handleWifiStateChange(ctx);
                } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action) || BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    handleBluetoothStateChange(ctx);
                }
            }
        }, filter);

        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && mSoftApCallbackProxy == null) {
                Class<?> callbackClass = Class.forName("android.net.wifi.WifiManager$SoftApCallback");
                mSoftApCallbackProxy = java.lang.reflect.Proxy.newProxyInstance(
                    context.getClassLoader(),
                    new Class<?>[]{callbackClass},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("onConnectedClientsChanged".equals(methodName)) {
                            handleHotspotClientsChanged(context, args);
                        } else if ("onStateChanged".equals(methodName)) {
                            int state = (int) args[0];
                            if (state != 13 && mHotspotRunnable != null) { // 13 is WIFI_AP_STATE_ENABLED
                                mHandler.removeCallbacks(mHotspotRunnable);
                            } else if (state == 13) {
                                // when enabled, assume 0 clients initially, start timer
                                handleHotspotClientsChanged(context, new Object[]{java.util.Collections.emptyList()});
                            }
                        }
                        return null;
                    }
                );
                java.util.concurrent.Executor executor = Runnable::run;
                XposedHelpers.callMethod(wifiManager, "registerSoftApCallback", executor, mSoftApCallbackProxy);
            }
        } catch (Throwable t) {
            // SoftApCallback not found or failed to register
        }
    }

    private void handleHotspotClientsChanged(Context context, Object[] args) {
        if (!mPrefsMap.getBoolean("system_framework_auto_turn_off_hotspot")) return;
        if (args == null || args.length == 0) return;

        int clientCount = 0;
        if (args[0] instanceof java.util.List) {
            clientCount = ((java.util.List<?>) args[0]).size();
        } else if (args.length > 1 && args[1] instanceof java.util.List) {
            clientCount = ((java.util.List<?>) args[1]).size();
        }

        if (mHotspotRunnable != null) {
            mHandler.removeCallbacks(mHotspotRunnable);
        }

        if (clientCount == 0) {
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
}
