package com.astro.dsoplanner;

import java.util.regex.Pattern;

import android.text.TextUtils;



public class ResponseData {
	
    private static final String WRONG_NUMBER_OF_FIELDS = "Wrong";
	
	public int responseCode;
    public int nonce;
    public String packageName;
    public String versionCode;
    public String userId;
    public long timestamp;
    /** Response-specific data. */
    public String extra;

    /**
     * Parses response string into ResponseData.
     *
     * @param responseData response data string
     * @throws IllegalArgumentException upon parsing error
     * @return ResponseData object
     */
    public static ResponseData parse(String responseData) {
        // Must parse out main response data and response-specific data.
    	int index = responseData.indexOf(':');
    	String mainData, extraData;
    	if ( -1 == index ) {
    		mainData = responseData;
    		extraData = "";
    	} else {
    		mainData = responseData.substring(0, index);
    		extraData = index >= responseData.length() ? "" : responseData.substring(index+1);
    	}

        String [] fields = TextUtils.split(mainData, Pattern.quote("|"));
        if (fields.length < 6) {
            throw new IllegalArgumentException(WRONG_NUMBER_OF_FIELDS);
        }

        ResponseData data = new ResponseData();
        data.extra = extraData;
        data.responseCode = Integer.parseInt(fields[0]);
        data.nonce = Integer.parseInt(fields[1]);
        data.packageName = fields[2];
        data.versionCode = fields[3];
        // Application-specific user identifier.
        data.userId = fields[4];
        data.timestamp = Long.parseLong(fields[5]);

        return data;
    }

    @Override
    public String toString() {
        return TextUtils.join("|", new Object [] { responseCode, nonce, packageName, versionCode,
            userId, timestamp});
    }
}
