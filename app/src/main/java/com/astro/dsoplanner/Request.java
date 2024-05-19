package com.astro.dsoplanner;

public abstract class Request {
	public static final int NOTE_REQUEST=1;
	public static final int PICTURE_REQUEST=2;
	public static final int TELESCOPE_REQUEST=3;
	public static final int EYEPIECES_REQUEST=4;
	public static final int APP_REQUEST=5;
	public abstract int getKind();
}

