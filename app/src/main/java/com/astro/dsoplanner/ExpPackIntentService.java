package com.astro.dsoplanner;

import com.astro.dsoplanner.expansion.APKExpansion;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ExpPackIntentService extends IntentService {
	private static final String TAG = ExpPackIntentService.class.getSimpleName();
	
	public ExpPackIntentService(){
		super("0");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		boolean patch=intent.getBooleanExtra(Constants.EP_PATCH_BEING_EXPANDED, false);
		if(!patch){
			Log.d(TAG,"expanding started");
			try{
				new Init(this,null).initDirs(); //not needed but just in case
				APKExpansion.expandDatabasesPack(ExpPackIntentService.this, Global.mainVersion);
			}
			catch(Exception e){
				Log.d(TAG,"error extracting exp pack="+AstroTools.getStackTrace(e));
				//handler.sendMessage(getMessage(res.getString(R.string.error_expanding_expansion_pack),WORK_OVER));

				SettingsActivity.putSharedPreferences(Constants.EP_BEING_EXPANDED, -1L, this);
				sendMessage(Constants.EXP_PACK_ERROR);
				return;
			}
			Init.loadTychoRef();
			Init.loadUcac4Ref(this);
			
			Init.createDSSFileList();
			SettingsActivity.putSharedPreferences(Constants.EP_EXPANDED, true,Global.getAppContext());

			SettingsActivity.putSharedPreferences(Constants.EP_BEING_EXPANDED, -1L, this);
			sendMessage(Constants.EXP_PACK_COMPLETE);
			Log.d(TAG,"exp pack extracted");
		}
		else{
			Log.d(TAG,"expanding patch started");
			try{
				new Init(this,null).initDirs(); //not needed but just in case
				APKExpansion.expandDatabasesPatch(ExpPackIntentService.this, Global.patchVersion);
			}
			catch(Exception e){
				Log.d(TAG,"error extracting exp patch="+AstroTools.getStackTrace(e));
				SettingsActivity.putSharedPreferences(Constants.EP_PATCH_BEING_EXPANDED, -1L, this);
				sendMessage(Constants.EXP_PACK_ERROR);
				return;
			}
			Init.loadTychoShortRef();
			Init.loadNgcRef();			
			Init.loadPgcRef(getApplicationContext());
			Init.loadMilkyWayRef();
			Init.loadConBoundaryRef();
			FileTools.copyComps(getApplicationContext());
			
			SettingsActivity.putSharedPreferences(Constants.EP_PATCH_EXPANDED, true,Global.getAppContext());
			Log.d(TAG,""+ SettingsActivity.getSharedPreferences(Global.getAppContext()).getBoolean(Constants.EP_PATCH_EXPANDED, false));
			SettingsActivity.putSharedPreferences(Constants.EP_PATCH_BEING_EXPANDED, -1L, this);
			sendMessage(Constants.EXP_PACK_COMPLETE);
			Log.d(TAG,"exp patch extracted");
		}
	}
	
	private void sendMessage(int message){
		Intent intent=new Intent(Constants.EXP_PACK_BROADCAST);
		intent.putExtra(Constants.EXP_PACK_MESSAGE,message);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
