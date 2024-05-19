package com.astro.dsoplanner.infolist;

import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

public class SkyToolsLoader extends SkySafariLoaderImpl {
    private static final String TAG = SkyToolsLoader.class.getSimpleName();

    public SkyToolsLoader(BufferedReader reader, Context context) {
        super(reader, context);
    }

    @Override
    public void process(String s) {
        line += s + "\n";
        String[] arr = s.split(",");
        for (String sa : arr) {
            String[] arr2 = sa.trim().split(" ");
            if (arr2.length == 2) {
                map.put(arr2[0], arr2[1]);
                Log.d(TAG, arr2[0] + " " + arr2[0].length() + "=" + arr2[1] + " " + arr2[1].length());

            } else if (arr2.length == 1) {
                map.put(arr2[0], "");
            }
            Pattern p = Pattern.compile("[0-9]+");
            Matcher m = p.matcher(sa);
            if (!m.find()) {
                map.put(sa, "");
            }
        }
        state = REPORT_OBJECT;

    }
}
