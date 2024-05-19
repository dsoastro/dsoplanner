package com.astro.dsoplanner.alexmenu;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.astro.dsoplanner.SettingsActivity;

public class alexMenu {
	
	private static final String MENU_LIST_MAY_NOT_BE_MODIFIED_WHILE_MENU_IS_DISPLAYED = "Menu list may not be modified while menu is displayed.";
	
	/**
	 * Some global variables.
	 */
	private ArrayList<alexMenuItem> mMenuItems;
	private OnMenuItemSelectedListener mListener = null;
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private PopupWindow mPopupWindow = null;
	private boolean mIsShowing = false;
	private boolean mHideOnSelect = true;
	private int mRows = 0;
	private int mItemsPerLineInPortraitOrientation = 3;
	private int mItemsPerLineInLandscapeOrientation = 6;
	private boolean nightmode = false;
	private boolean bottomMenu = true;
	private float textSize = 12;
	private int menuId;
	private boolean onyxmode; 
	private String mHeader="";

	/**
	 * The interface for returning the item clicked.
	 */
	public interface OnMenuItemSelectedListener {
		public void MenuItemSelectedEvent(alexMenuItem selection);
	}

	/**
	 * Use this method to determine if the menu is currently displayed to the user.
	 * @return boolean isShowing
	 */	
	public boolean isNotShowing() { return !mIsShowing; }

	/**
	 * This setting controls whether or not the menu closes after an item is selected.
	 * @param doHideOnSelect
	 * @return void
	 */	
	public void setHideOnSelect(boolean doHideOnSelect) { mHideOnSelect = doHideOnSelect; } 

	/**
	 * Use this method to decide how many of your menu items you'd like one a single line.
	 * This setting in particular applied to portrait orientation.
	 * @param count
	 * @return void
	 */	
	public void setItemsPerLineInPortraitOrientation(int count) { mItemsPerLineInPortraitOrientation = count; }

	/**
	 * Use this method to decide how many of your menu items you'd like one a single line.
	 * This setting in particular applied to landscape orientation.
	 * @param count
	 * @return void
	 */	
	public void setItemsPerLineInLandscapeOrientation(int count) { mItemsPerLineInLandscapeOrientation = count; }

	/**
	 * Use this method to assign your menu items. You can only call this when the menu is hidden.
	 * @param items
	 * @return void
	 * @throws Exception "Menu list may not be modified while menu is displayed."
	 */	
	public synchronized void setMenuItems(ArrayList<alexMenuItem> items) throws Exception {
		if (mIsShowing) {
			throw new Exception(MENU_LIST_MAY_NOT_BE_MODIFIED_WHILE_MENU_IS_DISPLAYED);
		}
		mMenuItems = items;
	}

	/**
	 * This is our constructor.  Note we require a layout inflater.  There is probably a way to
	 * grab one of these from the local context but I failed to find it.
	 * @param context
	 * @param listener
	 * @param lo
	 * @return void
	 */	
	public alexMenu(Context context, OnMenuItemSelectedListener listener, LayoutInflater lo) {
		mListener = listener;
		mMenuItems = new ArrayList<alexMenuItem>();
		mContext = context;
		mLayoutInflater = lo;
	}

	public void setHeader(CharSequence charSequence){
		mHeader = charSequence.toString();
	}

	private void hideNavBar(final PopupWindow window){
		View decorView = window.getContentView();
		decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				// Note that system bars will only be "visible" if none of the
				// LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
				if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
					// TODO: The system bars are visible. Make any desired
					// adjustments to your UI, such as showing the action bar or
					// other navigational controls.
					View decorView = window.getContentView();
					decorView.setSystemUiVisibility(
							View.SYSTEM_UI_FLAG_IMMERSIVE
									// Set the content to appear under the system bars so that the
									// content doesn't resize when the system bars hide and show.

									// Hide the nav bar and status bar
									| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
									| View.SYSTEM_UI_FLAG_FULLSCREEN);
				} else {
					// TODO: The system bars are NOT visible. Make any desired
					// adjustments to your UI, such as hiding the action bar or
					// other navigational controls.
				}
			}
		});
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		decorView.setSystemUiVisibility(
				//View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				//        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_IMMERSIVE);
		//| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
	}

	/**
	 * Display your menu. Not we require a view from the parent.  This is so we can get
	 * a window token.  It doesn't matter which view on the parent is passed in.
	 * @param v
	 * @return void
	 */	
	public synchronized void show(View v) {
		mIsShowing = true;
		boolean isLandscape = false;
		int dispWidth;
		int itemCount = mMenuItems.size();
		boolean full_screen = SettingsActivity.isHideNavBar(mContext);
		//onyxmode = Settings.getDarkSkin();
		//nightmode = Settings.getNightMode();

		if (itemCount<1) return; //no menu items to show
		if (mPopupWindow != null) return; //already showing
		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		dispWidth = display.getWidth();
		if ( dispWidth > display.getHeight()) isLandscape = true;
		View mView= mLayoutInflater.inflate(com.astro.dsoplanner.R.layout.alex_menu, null);

		if(mHeader.length()!=0){ //want a header
			View vh=mView.findViewById(com.astro.dsoplanner.R.id.alex_menu_caption);
			((TextView)vh).setText(mHeader);
			vh.setVisibility(View.VISIBLE);
			mView.findViewById(com.astro.dsoplanner.R.id.alex_menu_headline).setVisibility(View.VISIBLE);
		}
		else {
			mView.findViewById(com.astro.dsoplanner.R.id.alex_menu_caption).setVisibility(View.GONE);
			if(!bottomMenu) //only for popup
				mView.findViewById(com.astro.dsoplanner.R.id.alex_menu_headline).setVisibility(View.GONE);
		}

		if(!bottomMenu) { //Context menu
			mPopupWindow = new PopupWindow(mView,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);//ws false
			//mPopupWindow.showAsDropDown(v); //show as hanging from the object SAND: Temporarily modified, not working as expected.
			int dRes = nightmode?com.astro.dsoplanner.R.drawable.dlg_red :
				(onyxmode?com.astro.dsoplanner.R.drawable.dlg_blk:com.astro.dsoplanner.R.drawable.dlg_wht);
			Drawable b = mContext.getResources().getDrawable(dRes);
			mPopupWindow.setBackgroundDrawable(b); //Must Have!
			//mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setWidth((int)(dispWidth*0.75));
			mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
			if(full_screen) {
				SettingsActivity.hideNavBarForView(mPopupWindow.getContentView());
			}
			mPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

		}
		else { //Options Menu
			mPopupWindow = new PopupWindow(mView,LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT, true);//ws false
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable()); //Must Have!
			//mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
			mPopupWindow.setWidth(dispWidth);
			mView.setBackgroundColor(0xff000000);

			if(full_screen) {
				SettingsActivity.hideNavBarForView(mPopupWindow.getContentView());
			}


			mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
		}

		mView.setOnKeyListener(new OnKeyListener() {
			public synchronized boolean onKey(View v, int keyCode, KeyEvent event) {
				if (mIsShowing && event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_MENU) {
					hide();
					return true; //eat it
				}
				return false;
			}
		});

		//Required to process back button
		mPopupWindow.setOnDismissListener(new OnDismissListener(){
			public void onDismiss() {
				mIsShowing = false;
				mPopupWindow = null;
			}
		});

		//Isert cells for menuitems from left to right, top to bottom
		int divisor = mItemsPerLineInPortraitOrientation;
		if (isLandscape) divisor = mItemsPerLineInLandscapeOrientation;
		int remainder = 0;
		if (itemCount < divisor) {
			mRows = 1;
			remainder = itemCount;
		} else {
			mRows = (itemCount / divisor);
			remainder = itemCount % divisor;
			if (remainder != 0) mRows++;
		}
		TableLayout table = (TableLayout)mView.findViewById(com.astro.dsoplanner.R.id.alex_menu_table);
		table.removeAllViews();
		for (int i=0; i < mRows; i++) {
			TableRow row = null;
			TextView tv = null;
			ImageView iv = null;
			//create headers
			row = new TableRow(mContext);
			row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			for (int j=0; j< divisor; j++) {
				if (i*divisor+j >= itemCount) break;
				final alexMenuItem cmi = mMenuItems.get(i*divisor+j);
				View itemLayout = mLayoutInflater.inflate(com.astro.dsoplanner.R.layout.alex_menu_item, null);
				tv = (TextView)itemLayout.findViewById(com.astro.dsoplanner.R.id.alex_menu_item_caption);
				tv.setText(cmi.getCaption());
				//set text color
				Resources r = mContext.getResources();
				if(bottomMenu) //always on black background
					tv.setTextColor(nightmode?
							r.getInteger(com.astro.dsoplanner.R.color.red_normal_color) :
								r.getInteger(com.astro.dsoplanner.R.color.white));//red/white
				/* Rely on the theme skin
				 * else //popup menu (red/white/default)
					if(nightmode||onyxmode)
						tv.setTextColor(nightmode?
								r.getColorStateList(com.astro.dsoplanner.R.xml.redtext) :
									r.getColorStateList(com.astro.dsoplanner.R.xml.onyxtext));
				*/
				
				tv.setTextSize(textSize);
				iv = (ImageView)itemLayout.findViewById(com.astro.dsoplanner.R.id.alex_menu_item_icon);
				int imres = cmi.getImageResourceId();
				if(imres!=0) //not empty image view ordered
					iv.setImageResource(cmi.getImageResourceId());
				else { //no icon
					itemLayout.findViewById(com.astro.dsoplanner.R.id.alex_menu_item_icon).setVisibility(View.GONE);
					tv.setPadding(0, 18, 0, 18);
				}

				if(!bottomMenu){ //popup menu
					itemLayout.setMinimumHeight(80);
					itemLayout.setBackgroundResource(com.astro.dsoplanner.R.drawable.alex_menu_selector_v);
					itemLayout.findViewById(com.astro.dsoplanner.R.id.menuindivider).setVisibility(View.VISIBLE);
					tv.setPadding(0, 28, 0, 28);
				}

				itemLayout.setOnClickListener( new OnClickListener() {
					public void onClick(View v) {
						mListener.MenuItemSelectedEvent(cmi);
						if (cmi.wantsHide()) hide();
					}
				});
				row.addView(itemLayout);
			}
			table.addView(row);
		}
	}

	/**
	 * Hide your menu.
	 * @return void
	 */	
	public synchronized void hide() {
		mIsShowing = false;
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
			mPopupWindow = null;
		}
	}

	public synchronized void setSkin(boolean night, boolean onyx){
		nightmode = night;
		onyxmode = onyx;
	}

	public void makeFloat() {
		bottomMenu = false;
	}

	public void setTextSize(int i) {
		textSize = i;
	}

	public void setMenuItemId(int index) {
		menuId = index;
	}
	public int getMenuItemId() {
		return menuId;
	}
}
