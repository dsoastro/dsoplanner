package com.astro.dsoplanner.scopedrivers;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.InputDialog;
import com.astro.dsoplanner.Logr;
import com.astro.dsoplanner.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import com.astro.dsoplanner.AstroTools.RaDecRec;


public class WiFiCom implements ComInterface {
    private static final String TAG = "WiFiCom";

    public enum SocketType {TCP, UDP}

    InetAddress address;
    int port;
    TelescopeDriver driver;
    LocalBroadcastManager localBroadcastManager;
    private ConnectedThread connectedThread = null;//reading
    private ConnectedOutThread connectedOutThread = null;//writing
    DatagramSocket datagramSocket;
    Socket tcpSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    SocketType socketType;
    private boolean isConnected = false;
    long last_time_toast = 0;
    private static final String WIFI_CONNECTION_LOST = "WiFi connection lost";
    private static final String WIFI_CONNECTION_ERROR = "WiFi connection error";
    private static final String WIFI_CONNECTION_CLOSED = "WiFi connection closed";


    public void setParams(InetAddress address, int port, TelescopeDriver driver, LocalBroadcastManager localBroadcastManager, SocketType socketType) {
        this.address = address;
        this.port = port;
        this.driver = driver;
        this.localBroadcastManager = localBroadcastManager;
        this.socketType = socketType;
    }

    //error message with OK button
    public static void error(final String m) {

        Intent intent = new Intent(Constants.BTCOMM_MESSAGE_BROADCAST);
        intent.putExtra(Constants.BTCOMM_MESSAGE, m);
        LocalBroadcastManager.getInstance(Global.getAppContext()).sendBroadcast(intent);
    }

    public void open() {
        if (isConnected())
            return;
        if (socketType == SocketType.UDP) {
            try {
                datagramSocket = new DatagramSocket();
            } catch (SocketException e) {
                CommunicationManager.pop(WIFI_CONNECTION_ERROR);
                setConnected(false);
                return;
            }
        }

        connectedThread = new ConnectedThread();
        connectedThread.start();
    }

    public void stop() {
        Logr.d(TAG, "stop");
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (driver != null) {
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
                        try {
                            driver.setPosition(r.ra, r.dec);
                            driver.slewToPosition();
                        } catch (Exception e) {
                            Logr.d(TAG, "send, e=" + e);
                        }
                    }

                }
            };
            connectedOutThread = new ConnectedOutThread(runnable);
            connectedOutThread.start();
        } else {
            Logr.d(TAG, "conntectedoutthread!=null, alive= " + connectedOutThread.isAlive());
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

    public void cancelContinous() {
        if (connectedOutThread != null)
            connectedOutThread.interrupt();
    }

    /**
     * @param period period in millis
     */
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

    private class OutImpl extends OutputStream {


        @Override
        public void write(int var1) throws IOException {
            byte[] buf = {(byte) var1};
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            datagramSocket.send(packet);
        }

        @Override
        public void write(byte[] b) throws IOException {

            DatagramPacket packet = new DatagramPacket(b, b.length, address, port);
            datagramSocket.send(packet);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            DatagramPacket packet = new DatagramPacket(b, off, len, address, port);
            datagramSocket.send(packet);
            String msg = new String(b, off, len, StandardCharsets.UTF_8);
            Logr.d(TAG, "msg=" + msg);
        }
    }

    private class ConnectedThread extends Thread {


        public ConnectedThread() {
            driver.setOutputStream(new OutImpl());
        }

        public void run() {
            boolean connected = false;
            if (socketType == SocketType.UDP) {
                try {
                    connected = address.isReachable(2000);
                } catch (IOException e) {
                    Logr.d(TAG, "1. e=" + e);
                    CommunicationManager.pop(WIFI_CONNECTION_ERROR);
                    setConnected(false);
                    return;
                }
                if (connected)
                    setConnected(true);
                else {
                    CommunicationManager.pop(WIFI_CONNECTION_ERROR);
                    setConnected(false);
                    return;
                }
                if (driver.getDriver() == TelescopeDriver.DSC) {
                    TelescopeDriverDsc driverDsc = (TelescopeDriverDsc) driver;
                    driverDsc.sendResolution();
                }
                byte[] buffer = new byte[1024];  // buffer store for the stream

                while (true) {
                    try {
                        // Read from the InputStream
                        Logr.d(TAG, "Connected thread. within cycle");
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        datagramSocket.receive(packet);

                        if (packet.getLength() != 0) {
                            synchronized (driver) {
                                driver.addMessage(packet.getData(), packet.getLength());
                                driver.notifyAll();
                            }
                        }
                    } catch (Exception e) {
                        Logr.d(TAG, "Connected thread. Exception=" + e.getMessage());
                        error(WIFI_CONNECTION_LOST);
                        setConnected(false);
                        if (driver != null) driver.cancel();
                        break;
                    }
                }
            } else {
                InputStream tmpIn = null;
                OutputStream tmpOut = null;
                try {
                    tcpSocket = new Socket(address, port);
                    tmpIn = tcpSocket.getInputStream();
                    tmpOut = tcpSocket.getOutputStream();
                } catch (IOException e) {
                    CommunicationManager.pop(WIFI_CONNECTION_ERROR);
                    setConnected(false);
                    return;
                }
                mmInStream = tmpIn;
                mmOutStream = tmpOut;
                driver.setOutputStream(tmpOut);
                setConnected(true);
                if (driver.getDriver() == TelescopeDriver.DSC) {
                    TelescopeDriverDsc driverDsc = (TelescopeDriverDsc) driver;
                    driverDsc.sendResolution();
                }

                byte[] buffer = new byte[1024];  // buffer store for the stream
                int bytes; // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs
                while (true) {
                    try {
                        // Read from the InputStream
                        Log.d(TAG, "Connected thread. within cycle");
                        bytes = mmInStream.read(buffer);
                        if (bytes != 0) {
                            synchronized (driver) {
                                driver.addMessage(buffer, bytes);
                                driver.notifyAll();
                            }
                        }
                    } catch (Exception e) {
                        Logr.d(TAG, "Connected thread. Exception=" + e.getMessage());
                        error(WIFI_CONNECTION_LOST);
                        setConnected(false);
                        if (driver != null) driver.cancel();
                        break;
                    }
                }
            }
        }


        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                CommunicationManager.pop(WIFI_CONNECTION_CLOSED);
                setConnected(false);
                if (socketType == SocketType.UDP)
                    datagramSocket.close();
                else
                    tcpSocket.close();


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
