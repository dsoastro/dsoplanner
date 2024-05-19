package com.astro.dsoplanner;

import android.os.Bundle;

public class TelescopeRequest extends Request {

    private static final String TELESCOPE_RECORD = "telescopeRecord";
    private static final String ACTION2 = "action";
    private static final String RECORD2 = ", record=";
    private static final String TELESCOPE_REQUEST_ACTION = "TelescopeRequest [action=";


    static final int NEW_SCOPE_ACTION = 1;
    static final int EDIT_SCOPE_ACTION = 2;
    static final int GET_ALL_SCOPES = 3;

    int action = 0;
    TelescopeRecord record = null;

    public TelescopeRequest() {        //new Telescope
        record = new TelescopeRecord();
        action = NEW_SCOPE_ACTION;
    }

    public TelescopeRequest(int iAction) {        //get scope data
        action = iAction;
        record = new TelescopeRecord();
    }

    public TelescopeRequest(TelescopeRecord record) {    //edit scope
        this.record = record;
        action = EDIT_SCOPE_ACTION;
    }

    @Override
    public String toString() {
        return TELESCOPE_REQUEST_ACTION + action + RECORD2
                + record + "]";
    }

    public TelescopeRequest(Bundle b) {
        action = b.getInt(ACTION2);
        Bundle nr = b.getBundle(TELESCOPE_RECORD);
        if (nr != null)
            record = new TelescopeRecord(nr);
        else
            record = new TelescopeRecord();
    }

    public Bundle getBundle() {
        Bundle b = new Bundle();
        b.putInt(ACTION2, action);
        b.putBundle(TELESCOPE_RECORD, record.getBundle());
        return b;
    }

    public TelescopeRecord getRecord() {
        return record;
    }

    public int getKind() {
        return TELESCOPE_REQUEST;
    }

}
