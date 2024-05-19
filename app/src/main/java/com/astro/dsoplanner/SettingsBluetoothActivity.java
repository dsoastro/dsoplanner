package com.astro.dsoplanner;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import android.preference.PreferenceManager;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.astro.dsoplanner.scopedrivers.CelestronDriver;
import com.astro.dsoplanner.scopedrivers.ComInterface;
import com.astro.dsoplanner.scopedrivers.CommunicationManager;
import com.astro.dsoplanner.scopedrivers.DscDriver;
import com.astro.dsoplanner.scopedrivers.IOptronDriver;
import com.astro.dsoplanner.scopedrivers.MeadeDriver;
import com.astro.dsoplanner.scopedrivers.TelescopeDriver;
import com.astro.dsoplanner.scopedrivers.WiFiCom;

import android.os.Build;

public class SettingsBluetoothActivity extends ParentPreferenceActivity {
    private static final String SCOPE_TYPE = "scope_type2";
    private static final String CONNECTION_TYPE = "connection_type2";
    private static final String PORT_TYPE = "port_type2";
    private static final String BT_NAME = "bt_name";
    private static final String BT_ADDRESS = "bt_address";
    private static final String PAIRED_DEVICE = "paired_device";
    private static final String BT_CONNECT = "bt_connect";
    private static final String BT_DISCONNECT = "bt_disconnect";

    public static final int BLUETOOTH_CONNECTION = 0;
    public static final int WIFI_CONNECTION = 1;
    public static final int TCP = 0;
    public static final int UDP = 1;
    private int connection_type = WIFI_CONNECTION;
    private int port_type = TCP;


    private static final String TAG = SettingsBluetoothActivity.class.getSimpleName();
    public static final int requestCode = 1;
    String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};

    private boolean isPermissionAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean flag = true;
            for (String permission : permissions) {
                int perm = ActivityCompat.checkSelfPermission(this, permission);

                if (perm != PackageManager.PERMISSION_GRANTED) {
                    flag = false;
                }
            }
            return flag;


        }
        return true;
    }

    private void showPermissionDialog() {
        registerDialog(InputDialog.message(SettingsBluetoothActivity.this, getString(R.string.please_grant_bluetooth_permission_first))).show();

    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message m) {

            switch (m.arg1) {
                case 1:
                    initConnectSummary();
                    break;
            }
        }
    };
    InputDialog connDialog;//dialog is closed via handler call back from BTComm
    /**
     * receives broadcasts from bt comm
     */
    BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            initConnectSummary();
        }
    };


    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BTCOMM_UPDATE_BROADCAST);

        LocalBroadcastManager.getInstance(this).registerReceiver(btReceiver, filter);
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btReceiver);
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    private final static int CELESTRON_16 = 0;
    private final static int CELESTRON_32 = 1;
    private final static int SKY_WATCHER_16 = 2;
    private final static int SKY_WATCHER_32 = 3;
    private final static int MEADE = 4;
    private final static int IOPTRON = 5;
    private final static int DSC = 6;

    private int scope = 0;//scope type selected


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_bt);
        findPreference(BT_DISCONNECT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {


                CommunicationManager.getComModule().stop();
                return true;
            }
        });

        findPreference(BT_CONNECT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                if (!isPermissionAvailable()) {
                    showPermissionDialog();
                    return true;
                }


                if (connDialog != null && connDialog.isShowing())//connecting already
                    return true;

                TelescopeDriver driver = null;
                switch (scope) {
                    case CELESTRON_16:
                    case SKY_WATCHER_16:
                        driver = new CelestronDriver(CelestronDriver.SHORT_DATA);
                        break;
                    case CELESTRON_32:
                    case SKY_WATCHER_32:
                        driver = new CelestronDriver(CelestronDriver.LONG_DATA);
                        break;
                    case MEADE:
                        driver = new MeadeDriver();
                        break;
                    case IOPTRON:
                        driver = new IOptronDriver();
                        break;
                    case DSC:
                        String azResolutionStr = PreferenceManager.getDefaultSharedPreferences(SettingsBluetoothActivity.this).
                                getString(getString(R.string.d_dsc_az_resolution), "");
                        String altResolutionStr = PreferenceManager.getDefaultSharedPreferences(SettingsBluetoothActivity.this).
                                getString(getString(R.string.d_dsc_alt_resolution), "");
                        int iazr = AstroTools.getInteger(azResolutionStr, 4000, -100000, 100000);
                        int ialtr = AstroTools.getInteger(altResolutionStr, 4000, -100000, 100000);
                        driver = new DscDriver(iazr, ialtr);
                        //driver = new DscDriverStub();
                        break;
                }
                if (connection_type == BLUETOOTH_CONNECTION) {
                    if (btName.isEmpty()) {
                        InputDialog.message(SettingsBluetoothActivity.this, R.string.no_paired_device_found_please_pair_with_your_bt_dongle_first).show();
                        return true;
                    }
                    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(btAddress);
                    if (device == null) {
                        InputDialog.message(SettingsBluetoothActivity.this, R.string.no_paired_device_found_please_pair_with_your_bt_dongle_first).show();
                        return true;
                    }
                    connDialog = new InputDialog(SettingsBluetoothActivity.this);
                    connDialog.setTitle(getString(R.string.connecting_));
                    connDialog.setPositiveButton(getString(R.string.cancel), new InputDialog.OnButtonListener() {

                        public void onClick(String value) {
                            CommunicationManager.getComModule().stop();

                        }
                    });


                    if (driver != null) {
                        CommunicationManager.setActiveCommunication(CommunicationManager.ActiveCommunication.BLUETOOTH);
                        ComInterface btcom = CommunicationManager.getComModule();
                        CommunicationManager.setBtParams(device, driver, LocalBroadcastManager.getInstance(SettingsBluetoothActivity.this));
                        registerDialog(connDialog).show();
                        btcom.open();
                    }
                } else {
                    String ipString = PreferenceManager.getDefaultSharedPreferences(SettingsBluetoothActivity.this).
                            getString(getString(R.string.d_wifi_ip_address), "");
                    String portStr = PreferenceManager.getDefaultSharedPreferences(SettingsBluetoothActivity.this).
                            getString(getString(R.string.d_wifi_port), "");
                    int port = AstroTools.getInteger(portStr, 0, 0, 100000);
                    InetAddress address;
                    try {
                        address = InetAddress.getByName(ipString);
                    } catch (UnknownHostException e) {
                        InputDialog.toast("Wrong ip address!", getApplicationContext()).show();
                        return true;
                    }

                    connDialog = new InputDialog(SettingsBluetoothActivity.this);
                    connDialog.setTitle(getString(R.string.connecting_));
                    connDialog.setPositiveButton(getString(R.string.cancel), new InputDialog.OnButtonListener() {

                        public void onClick(String value) {
                            CommunicationManager.getComModule().stop();

                        }
                    });


                    if (driver != null) {
                        CommunicationManager.setActiveCommunication(CommunicationManager.ActiveCommunication.WIFI);
                        ComInterface wifi = CommunicationManager.getComModule();
                        if (port_type == TCP)
                            CommunicationManager.setWiFiParams(address, port, driver, LocalBroadcastManager.getInstance(SettingsBluetoothActivity.this), WiFiCom.SocketType.TCP);
                        else
                            CommunicationManager.setWiFiParams(address, port, driver, LocalBroadcastManager.getInstance(SettingsBluetoothActivity.this), WiFiCom.SocketType.UDP);

                        wifi.open();
                        registerDialog(connDialog).show();
                    }

                }

                return true;
            }
        });

        findPreference(PAIRED_DEVICE).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {
                if (!isPermissionAvailable()) {
                    showPermissionDialog();
                    return true;
                }

                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                List<CharSequence> items = new ArrayList<CharSequence>();
                final List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
                    items.add(device.getName());
                    devices.add(device);
                }
                if (items.size() == 0) {
                    InputDialog.toast(getString(R.string.no_paired_device_found_please_pair_with_your_bt_dongle_first), SettingsBluetoothActivity.this).show();
                    return true;
                }
                int pos = 0;
                String[] cs = new String[items.size()];
                for (int i = 0; i < cs.length; i++) {
                    cs[i] = (String) items.get(i);
                    if (cs[i] != null && cs[i].equals(btName))
                        pos = i;

                }

                //As there is no OnOK defined the dialog will quit on item click
                InputDialog d = new InputDialog(SettingsBluetoothActivity.this);
                d.setValue(String.valueOf(pos));
                d.setTitle(getString(R.string.select_paired_device));
                d.setNegativeButton(getString(R.string.cancel));
                d.setListItems(cs, new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        int which = AstroTools.getInteger(value, 0, 0, 10000);
                        SettingsActivity.putSharedPreferences(BT_ADDRESS, devices.get(which).getAddress(), SettingsBluetoothActivity.this);
                        SettingsActivity.putSharedPreferences(BT_NAME, devices.get(which).getName(), SettingsBluetoothActivity.this);
                        initBtPairedDevice();
                    }
                });
                registerDialog(d).show();
                return true;
            }
        });
        findPreference(getString(R.string.connection_type)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                InputDialog d = new InputDialog(SettingsBluetoothActivity.this);
                d.setTitle("Select connection type");
                d.setValue(String.valueOf(connection_type));
                Log.d(TAG, "connection type=" + connection_type);
                d.setNegativeButton(getString(R.string.cancel));
                Context context = getApplicationContext();
                d.setListItems(new String[]{"Bluetooth", "WiFi"}, new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        int which = AstroTools.getInteger(value, 0, 0, 10000);
                        SharedPreferences prefs = SettingsActivity.getSharedPreferences(SettingsBluetoothActivity.this);//getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
                        prefs.edit().putInt(CONNECTION_TYPE, which).commit();
                        updateConnectionType();
                    }
                });
                registerDialog(d).show();
                return true;
            }
        });
        findPreference(getString(R.string.port_type)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                InputDialog d = new InputDialog(SettingsBluetoothActivity.this);
                d.setTitle("Select port type");
                d.setValue(String.valueOf(port_type));
                d.setNegativeButton(getString(R.string.cancel));
                Context context = getApplicationContext();
                d.setListItems(new String[]{"TCP", "UDP"}, new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        int which = AstroTools.getInteger(value, 0, 0, 10000);
                        SharedPreferences prefs = SettingsActivity.getSharedPreferences(SettingsBluetoothActivity.this);//getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
                        prefs.edit().putInt(PORT_TYPE, which).commit();
                        updatePortType();
                    }
                });
                registerDialog(d).show();
                return true;
            }
        });


        findPreference(getString(R.string.scope_type)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                InputDialog d = new InputDialog(SettingsBluetoothActivity.this);
                d.setTitle(getString(R.string.select_telescope_type));
                d.setValue(String.valueOf(scope));
                d.setNegativeButton(getString(R.string.cancel));
                Context context = getApplicationContext();
                d.setListItems(new String[]{context.getString(R.string.celestron_16_bit), context.getString(R.string.celestron_32_bit), context.getString(R.string.skywatcher_16_bit), context.getString(R.string.skywatcher_32_bit), context.getString(R.string.meade), "iOptron", "Dsc"}, new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        int which = AstroTools.getInteger(value, 0, 0, 10000);
                        SharedPreferences prefs = SettingsActivity.getSharedPreferences(SettingsBluetoothActivity.this);//getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
                        prefs.edit().putInt(SCOPE_TYPE, which).commit();
                        updateScopeType();
                    }
                });
                registerDialog(d).show();
                return true;
            }
        });
        initBtPairedDevice();
        updateScopeType();
        updateConnectionType();
        updatePortType();
    }

    private String btName = "";//name of the selected paired device
    private String btAddress = "";//address of the selected paired device

    private void initBtPairedDevice() {
        btAddress = SettingsActivity.getStringFromSharedPreferences(this, BT_ADDRESS, "");//prefs.getString(BT_ADDRESS, "");
        btName = SettingsActivity.getStringFromSharedPreferences(this, BT_NAME, "");//prefs.getString(BT_NAME, "");
        findPreference(PAIRED_DEVICE).setSummary(btName);
    }

    private void updateScopeType() {
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(this);
        scope = prefs.getInt(SCOPE_TYPE, IOPTRON);
        switch (scope) {
            case CELESTRON_16:
                findPreference(getString(R.string.scope_type)).setSummary(getString(R.string.celestron_16_bit));
                break;
            case CELESTRON_32:
                findPreference(getString(R.string.scope_type)).setSummary(getString(R.string.celestron_32_bit));
                break;
            case SKY_WATCHER_16:
                findPreference(getString(R.string.scope_type)).setSummary(getString(R.string.skywatcher_16_bit));
                break;
            case SKY_WATCHER_32:
                findPreference(getString(R.string.scope_type)).setSummary(getString(R.string.skywatcher_32_bit));
                break;
            case MEADE:
                findPreference(getString(R.string.scope_type)).setSummary(getString(R.string.meade));
                break;
            case IOPTRON:
                findPreference(getString(R.string.scope_type)).setSummary("iOptron");
                break;
            case DSC:
                findPreference(getString(R.string.scope_type)).setSummary("DSC");
                break;

        }
    }

    private void updateConnectionType() {
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(this);
        connection_type = prefs.getInt(CONNECTION_TYPE, WIFI_CONNECTION);
        Log.d(TAG, "connection type=" + connection_type);

        switch (connection_type) {
            case BLUETOOTH_CONNECTION:
                findPreference(getString(R.string.connection_type)).setSummary("Bluetooth");
                break;
            case WIFI_CONNECTION:
                findPreference(getString(R.string.connection_type)).setSummary("WiFi");
                break;

        }
    }

    private void updatePortType() {
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(this);
        port_type = prefs.getInt(PORT_TYPE, TCP);

        switch (port_type) {
            case TCP:
                findPreference(getString(R.string.port_type)).setSummary("TCP");
                break;
            case UDP:
                findPreference(getString(R.string.port_type)).setSummary("UDP");
                break;

        }
    }

    private void initConnectSummary() {
        String name = "";
        if (CommunicationManager.getComModule().isConnected()) {
            name = getString(R.string.connected);

        } else {
            name = getString(R.string.not_connected);
        }
        if (connDialog != null) {
            if (connDialog.isShowing())
                connDialog.dismiss();
        }
        findPreference(BT_CONNECT).setSummary(name);

    }

    @Override
    public void onResume() {
        initConnectSummary();
        registerReceiver();
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String explanation = getString(R.string.bluetooth_permission_is_required_to_connect_to_devices_would_you_like_to_grant_this_permission);
            Runnable r = new Runnable() {
                @Override
                public void run() {

                }
            };
            AstroTools.askForPermissionsOrRun(this, r, permissions,
                    explanation, requestCode, true);
        }


    }

    @Override
    protected void onPause() {
        unregisterReceiver();
        super.onPause();
    }
}
