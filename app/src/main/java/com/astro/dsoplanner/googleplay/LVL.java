package com.astro.dsoplanner.googleplay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.astro.dsoplanner.Global;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.pm.Signature;

import java.security.MessageDigest;

import android.util.Base64;

public class LVL {
    public static interface Passable {
        public void pass(String s);
    }

    private static final String TAG = "LVL";
    private static final Random RANDOM = new Random();

    public LVL(String urlKey) {
        this.urlKey = urlKey;
    }

    String urlKey;
    ServiceConnection connection;
    Context context;


    class LicenseListener extends android.os.Binder {
        public LicenseListener(Passable passable) {
            this.passable = passable;
        }
        Passable passable;

        private Map<String, String> decodeExtras(String extras) {
            Map<String, String> results = new HashMap<String, String>();
            try {
                URI rawExtras = new URI("?" + extras);
                List<NameValuePair> extraList = URLEncodedUtils.parse(rawExtras, "UTF-8");
                for (NameValuePair item : extraList) {
                    String name = item.getName();
                    int i = 0;
                    while (results.containsKey(name)) {
                        name = item.getName() + ++i;
                    }
                    results.put(name, item.getValue());
                }
            } catch (URISyntaxException e) {
                Log.w(TAG, "Invalid syntax error while decoding extras data from server.");
            }
            return results;
        }

        public boolean onTransact(int op, Parcel in, Parcel reply, int flags) {
            String listener = "com.android.vending.licensing.ILicenseResultListener";

            if (op == 1) {
                in.enforceInterface(listener);
                int code = in.readInt();
                Log.d(TAG, "code=" + code);
                String data = in.readString();

                com.astro.dsoplanner.googleplay.ResponseData rawData = null;
                try {
                    rawData = ResponseData.parse(data);
                } catch (Exception e) {
                    Log.e(TAG, "Could not parse response.");

                }
                Map<String, String> extras = decodeExtras(rawData.extra);

                for (Map.Entry<String, String> entry : extras.entrySet()) {
                    if (entry.getKey().equals(urlKey)) {
                        Log.d(TAG, "key=" + entry.getKey() + " value=" + entry.getValue());
                        passable.pass(entry.getValue());
                        cleanupService(context);
                        break;
                    }

                }
                Log.d(TAG, data);
            }
            return true;
        }
    }


    public void connectToGooglePlayServers(Context context, Passable passable) {
        Log.d(TAG, "checkLicense");
        this.context = context;

        final String service = "com.android.vending.licensing.ILicensingService";

        connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(TAG, "connected to Service");

                Parcel d = Parcel.obtain();
                try {
                    d.writeInterfaceToken(service);
                    int token = RANDOM.nextInt();
                    d.writeLong(token);
                    d.writeString(Global.PACKAGE_NAME_PRO);
                    d.writeStrongBinder(new LicenseListener(passable));
                    binder.transact(1, d, null, IBinder.FLAG_ONEWAY);
                } catch (RemoteException e) {
                }
                d.recycle();
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };
        Log.d(TAG, "start intent");
        Intent i = new Intent(service);
        i.setPackage("com.android.vending");
        context.bindService(i, connection, Context.BIND_AUTO_CREATE);
    }


    private void cleanupService(Context context) {

        try {
            context.unbindService(connection);
        } catch (Exception e) {
            Log.e(TAG, "Unable to unbind from licensing service (already unbound)");
        }


    }

    public static boolean isPlayStoreApp(Context context) {
        String signature = getSignature(context).trim();
        Log.d(TAG, "signature=" + signature + " " + signature.length() + " " + Global.PLAYSTORE_SIGNATURE.equals(signature));
        return Global.PLAYSTORE_SIGNATURE.equals(signature);
    }
    //https://stackoverflow.com/questions/52898066/get-signatures-is-deprecated

    public static String getSignature(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (packageInfo == null || packageInfo.signatures == null || packageInfo.signatures.length == 0 || packageInfo.signatures[0] == null) {
                return null;
            }
            Signature signature = packageInfo.signatures[0];
            return signatureDigest(signature);


        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static String signatureDigest(Signature sig) {
        byte[] signature = sig.toByteArray();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] digest = md.digest(signature);
            return Base64.encodeToString(digest, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }


}

