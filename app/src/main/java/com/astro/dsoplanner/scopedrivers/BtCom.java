package com.astro.dsoplanner.scopedrivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.InputDialog;

import com.astro.dsoplanner.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;

import com.astro.dsoplanner.AstroTools.RaDecRec;

public class BtCom implements ComInterface {

    private static final String BLUETOOTH_CONNECTION_ERROR = "Bluetooth connection error";
    private static final String BLUETOOTH_CONNECTION_CLOSED = "Bluetooth connection closed";
    private static final String BLUETOOTH_CONNECTION_LOST = "Bluetooth connection lost";
    private static final String BLUETOOTH_ABSENT = "Bluetooth absent";
    private static final String PLEASE_ENABLE_BLUETOOTH = "Please enable bluetooth";
    private static final String CONNECTED = "Connected";


    public static final String INVALID_RETURN_FROM_SCOPE = "Invalid return from scope";

    private static final String _00001101_0000_1000_8000_00805F9B34FB = "00001101-0000-1000-8000-00805F9B34FB";

    private static final String TAG = BtCom.class.getSimpleName();
    private ConnectThread connectThread = null;
    private ConnectedThread connectedThread = null;//reading
    private ConnectedOutThread connectedOutThread = null;//writing
    private TelescopeDriver driver;
    LocalBroadcastManager localBroadcastManager;
    BluetoothDevice device;
    private boolean isConnected = false;
    long last_time_toast = 0;


    public void setParams(BluetoothDevice device, TelescopeDriver driver, LocalBroadcastManager localBroadcastManager) {
        this.device = device;
        this.driver = driver;
        this.localBroadcastManager = localBroadcastManager;
    }

    public void open() {//,Handler handler){
        Log.d(TAG, "open");
        if (connectThread != null)
            if (connectThread.isAlive())
                return;
        if (isConnected())
            return;

        Log.d(TAG, "open within if");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            CommunicationManager.error(BLUETOOTH_ABSENT);
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            CommunicationManager.error(PLEASE_ENABLE_BLUETOOTH);
            return;
        }
        connectThread = new ConnectThread(device);
        connectThread.start();
        Log.d(TAG, "open at the end");


    }

    public void stop() {
        Log.d(TAG, "stop");
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (driver != null) {//maybe that stop is pressed without open
            driver.cancel();
        }
    }

    /*
     * 2000 ra/dec
     * UI thread
     */
    public void send(double ra, double dec) {
        if (!isConnected()) return;

        final RaDecRec r;
        if (driver.getEpoch() == TelescopeDriver.EPOCH_CURRENT)
            r = AstroTools.precession(ra, dec, Calendar.getInstance());
        else
            r = new RaDecRec(ra, dec);
        if (connectedOutThread == null || !connectedOutThread.isAlive()) {
            connectedOutThread = null;
            Runnable runnable = new Runnable() {
                public void run() {
                    synchronized (driver) {
                        driver.setPosition(r.ra, r.dec);
                        driver.slewToPosition();
                    }

                }
            };
            connectedOutThread = new ConnectedOutThread(runnable);
            connectedOutThread.start();
        } else {
            Log.d(TAG, "conntectedoutthread!=null, alive= " + connectedOutThread.isAlive());
            //called from the main thread so no handler necessary
            long now = Calendar.getInstance().getTimeInMillis();
            if (now - last_time_toast > CommunicationManager.MIN_DELAY_BETWEEN_TOASTS) {
                InputDialog.toast(Global.getAppContext().getString(R.string.the_scope_is_busy_try_later), Global.getAppContext()).show();
                last_time_toast = now;
            }
        }

    }

    public synchronized boolean isConnected() {
        return isConnected;
    }

    private synchronized void setConnected(boolean status) {
        isConnected = status;
        updateConnectionStatus();
    }

    private void updateConnectionStatus() {//in SettingsBt
        Intent intent = new Intent(Constants.BTCOMM_UPDATE_BROADCAST);
        localBroadcastManager.sendBroadcast(intent);
    }

    public void cancelContinous() {
        if (connectedOutThread != null)
            connectedOutThread.interrupt();
    }

    public void readContinous(final int period) {
        if (!isConnected()) return;
        if (connectedOutThread == null || !connectedOutThread.isAlive()) {
            connectedOutThread = null;
            Runnable r = new Runnable() {
                public void run() {
                    boolean interrupted = false;
                    while (!interrupted) {
                        RaDecRec rec = null;
                        long millisStart = Calendar.getInstance().getTimeInMillis();
                        try {
                            synchronized (driver) {
                                rec = driver.getPosition();
                            }
                        } catch (Exception e) {
                        }
                        if (rec == null) {
                            CommunicationManager.pop(CommunicationManager.NO_RESPONSE_FROM_SCOPE);
                            Intent intent = new Intent(Constants.BTCOMM_RADEC_BROADCAST);
                            intent.putExtra(Constants.BTCOMM_SCOPE_TRACKING_OFF, true);
                            localBroadcastManager.sendBroadcast(intent);
                            return;
                        }
                        long millisEnd = Calendar.getInstance().getTimeInMillis();
                        long duration = millisEnd - millisStart;

                        RaDecRec recf;
                        if (driver.getEpoch() == TelescopeDriver.EPOCH_CURRENT)
                            recf = AstroTools.get2000RaDec(rec.ra, rec.dec, Calendar.getInstance());
                        else
                            recf = new RaDecRec(rec.ra, rec.dec);

                        Intent intent = new Intent(Constants.BTCOMM_RADEC_BROADCAST);
                        intent.putExtra(Constants.BTCOMM_RA, recf.ra);
                        intent.putExtra(Constants.BTCOMM_DEC, recf.dec);
                        intent.putExtra(Constants.BTCOMM_SCOPE, driver.getDriver());
                        localBroadcastManager.sendBroadcast(intent);
                        if (duration < period) {
                            try {
                                Thread.sleep(period - duration);
                            } catch (InterruptedException e) {
                                interrupted = true;
                            }
                        }
                        if (Thread.interrupted())
                            interrupted = true;
                    }
                    Intent intent = new Intent(Constants.BTCOMM_RADEC_BROADCAST);
                    intent.putExtra(Constants.BTCOMM_SCOPE_TRACKING_OFF, true);
                    localBroadcastManager.sendBroadcast(intent);
                }
            };
            connectedOutThread = new ConnectedOutThread(r);
            connectedOutThread.start();
        } else {
            long now = Calendar.getInstance().getTimeInMillis();
            if (now - last_time_toast > CommunicationManager.MIN_DELAY_BETWEEN_TOASTS) {
                CommunicationManager.pop(CommunicationManager.THE_SCOPE_IS_BUSY_TRY_LATER);
                last_time_toast = now;
            }
        }


    }


    public void read() {
        if (!isConnected()) return;
        if (connectedOutThread == null || !connectedOutThread.isAlive()) {
            connectedOutThread = null;
            Runnable r = new Runnable() {
                public void run() {
                    RaDecRec rec;
                    synchronized (driver) {
                        rec = driver.getPosition();
                    }
                    if (rec == null) {
                        CommunicationManager.pop(CommunicationManager.NO_RESPONSE_FROM_SCOPE);
                        return;
                    }
                    RaDecRec recf;
                    if (driver.getEpoch() == TelescopeDriver.EPOCH_CURRENT)
                        recf = AstroTools.get2000RaDec(rec.ra, rec.dec, Calendar.getInstance());
                    else
                        recf = new RaDecRec(rec.ra, rec.dec);

                    Intent intent = new Intent(Constants.BTCOMM_RADEC_BROADCAST);
                    intent.putExtra(Constants.BTCOMM_RA, recf.ra);
                    intent.putExtra(Constants.BTCOMM_DEC, recf.dec);
                    intent.putExtra(Constants.BTCOMM_SCOPE, driver.getDriver());
                    localBroadcastManager.sendBroadcast(intent);
                }
            };
            connectedOutThread = new ConnectedOutThread(r);
            connectedOutThread.start();
        } else {
            long now = Calendar.getInstance().getTimeInMillis();
            if (now - last_time_toast > CommunicationManager.MIN_DELAY_BETWEEN_TOASTS) {
                CommunicationManager.pop(CommunicationManager.THE_SCOPE_IS_BUSY_TRY_LATER);
                last_time_toast = now;
            }
        }


    }

    private class ConnectThread extends Thread {


        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final UUID MY_UUID = UUID.fromString(_00001101_0000_1000_8000_00805F9B34FB);


        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the default UUID

                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e) {
                Log.d(TAG, "Connect thread exception=" + e);
                setConnected(false);
                CommunicationManager.error(BLUETOOTH_CONNECTION_ERROR);
            }
            Log.d(TAG, "connection thread init passed");
            mmSocket = tmp;
        }

        public void cancel() {
            try {
                mmSocket.close();

            } catch (Exception e) {
            }

            setConnected(false);

        }

        public void run() {
            //Cancel discovery because it will slow down the connection
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            if (mmSocket == null) {
                Log.d(TAG, "null socket");
                return;
            }
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                sleep(500);
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.d(TAG, "connection exception=" + connectException);
                setConnected(false);
                CommunicationManager.error(BLUETOOTH_CONNECTION_ERROR);

                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            } catch (InterruptedException e) {
                setConnected(false);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                e.printStackTrace();
                return;
            }
            setConnected(true);
            Log.d(TAG, "connected");
            CommunicationManager.pop(CONNECTED);

            // Do work to manage the connection (in a separate thread)
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }
    }

    private class ConnectedThread extends Thread {


        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, "Exception in ConnectedThread" + e);
                CommunicationManager.error(BLUETOOTH_CONNECTION_ERROR);
                setConnected(false);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            driver.setOutputStream(tmpOut);
            if (driver.getDriver() == TelescopeDriver.DSC) {
                TelescopeDriverDsc driverDsc = (TelescopeDriverDsc) driver;
                driverDsc.sendResolution();
            }
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    Log.d(TAG, "Connected thread. within cycle");
                    bytes = mmInStream.read(buffer);
                    if (bytes != 0)
                        // Send the obtained bytes to the UI activity

                        synchronized (driver) {
                            driver.addMessage(buffer, bytes);
                            driver.notifyAll();
                        }
                } catch (Exception e) {
                    Log.d(TAG, "Connected thread. Exception=" + e.getMessage());
                    CommunicationManager.error(BLUETOOTH_CONNECTION_LOST);
                    setConnected(false);
                    if (driver != null) driver.cancel();
                    break;
                }
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                CommunicationManager.pop(BLUETOOTH_CONNECTION_CLOSED);
                setConnected(false);
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();


            } catch (Exception e) {
            }
        }
    }

    /**
     * Thread responsible for communicating with the scope.
     * used by both BTComm send and receive method, so created with Runnable
     * implementing the necessary behaviour
     */
    private class ConnectedOutThread extends Thread {

        public ConnectedOutThread(Runnable r) {
            super(r);
        }


    }

}
