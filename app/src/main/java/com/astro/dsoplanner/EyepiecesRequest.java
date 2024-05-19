package com.astro.dsoplanner;

import android.os.Bundle;

public class EyepiecesRequest extends Request {
	
	private static final String EYEPIECES_RECORD = "eyepiecesRecord";
	private static final String ACTION2 = "action";
	private static final String RECORD2 = ", record=";
	private static final String EYEPIECES_REQUEST_ACTION = "EyepiecesRequest [action=";
	
	static final int EDIT_EYEPIECES_ACTION=2;

	int action = 0;
	EyepiecesRecord record = null;

	public EyepiecesRequest(EyepiecesRecord record){	//edit ep
		this.record=record;
		action=EDIT_EYEPIECES_ACTION;
	}
	
	@Override
	public String toString() {
		return EYEPIECES_REQUEST_ACTION + action + RECORD2
				+ record + "]";
	}

	public Bundle getBundle(){
		Bundle b=new Bundle();
		b.putInt(ACTION2, action);
		b.putBundle(EYEPIECES_RECORD, record.getBundle());
		return b;
	}
	public EyepiecesRecord getRecord() {
		return record;
	}
	public int getKind(){
		return EYEPIECES_REQUEST;
	}

}
