package com.astro.dsoplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NotificationDisplayActivity extends ParentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i=getIntent();
		setContentView(R.layout.notification);

		Button bContinue = (Button)findViewById(R.id.notifContinue);
		bContinue.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				finish();
			}
		});
		Button bCancel = (Button)findViewById(R.id.notifCancel);
		bCancel.setOnClickListener(new OnClickListener() {	

			public void onClick(View v){
				Runnable r=new Runnable(){
					public void run(){
						MultipleDSSdownloadIntentService.stop();
						NotificationDisplayActivity.this.finish();
					}
				};
				InputDialog d=AstroTools.getDialog(NotificationDisplayActivity.this,getString(R.string.this_will_cancel_all_downloads), r);
				d.show();
			}
		});
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		hideMenuBtn();
	}

	
}
