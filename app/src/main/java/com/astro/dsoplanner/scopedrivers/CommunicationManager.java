package com.astro.dsoplanner.scopedrivers;

import java.net.InetAddress;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;


import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.Global;

public class CommunicationManager {
    public static final int MIN_DELAY_BETWEEN_TOASTS = 1000;
    public static final String NO_RESPONSE_FROM_SCOPE = "No response from scope";
    public static final String THE_SCOPE_IS_BUSY_TRY_LATER = "The scope is busy. Try later";

    static BtCom btCom = new BtCom();
    static WiFiCom wifiCom = new WiFiCom();

    public static ComInterface getComModule() {
        if (activeCommunication == ActiveCommunication.BLUETOOTH)
            return btCom;
        else
            return wifiCom;
    }

    public enum ActiveCommunication {
        BLUETOOTH, WIFI
    }

    static ActiveCommunication activeCommunication = ActiveCommunication.WIFI;

    public static void setActiveCommunication(ActiveCommunication com) {
        activeCommunication = com;
    }

    public static void setWiFiParams(InetAddress address, int port, TelescopeDriver driver, LocalBroadcastManager localBroadcastManager, WiFiCom.SocketType socketType) {
        wifiCom.setParams(address, port, driver, localBroadcastManager, socketType);
    }

    public static void setBtParams(BluetoothDevice device, TelescopeDriver driver, LocalBroadcastManager localBroadcastManager) {
        btCom.setParams(device, driver, localBroadcastManager);
    }

    //error message with OK button
    public static void error(final String m) {
        Intent intent = new Intent(Constants.BTCOMM_MESSAGE_BROADCAST);
        intent.putExtra(Constants.BTCOMM_MESSAGE, m);
        LocalBroadcastManager.getInstance(Global.getAppContext()).sendBroadcast(intent);
    }

    public static void pop(final String m) {
        error(m);
    }


}



