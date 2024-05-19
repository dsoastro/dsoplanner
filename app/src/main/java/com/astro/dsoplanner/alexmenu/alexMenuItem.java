package com.astro.dsoplanner.alexmenu;

import com.astro.dsoplanner.R;

public class alexMenuItem {
	
	/**
	 * Some global variables.
	 */
	private String mCaption = null;
	private int mImageResourceId = -1;
	private int mId = -1;
	private boolean hidemenu = true;
	
	public alexMenuItem(int id, String n, int im, boolean hide) {
		mCaption = n;
		mImageResourceId = im;
		mId = id;
		hidemenu = hide;
	}
	
	public void setCaption(String caption) { mCaption =caption;	}
	
	public String getCaption() { return mCaption; }

	public void setImageResourceId(int imageResourceId) { mImageResourceId = imageResourceId; }

	public int getImageResourceId() { return mImageResourceId;	}

	public void setId(int id) { mId = id; }

	public int getId() { return mId; }

	public boolean wantsHide() {
		return hidemenu;
	}
	
}
