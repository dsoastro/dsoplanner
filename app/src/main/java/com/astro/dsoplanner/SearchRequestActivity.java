package com.astro.dsoplanner;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListStringLoaderImp;
import com.astro.dsoplanner.infolist.InfoListStringSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.OutputStream;
import java.io.IOException;



public class SearchRequestActivity extends ParentListActivity implements OnGestureListener {
		
	private static final String MENU_ERROR2 = "Menu error! ";	
	
	private static final String TAG = "SRA";
	private final int MENU_IMPORT = 1;
	private final int MENU_EXPORT = 2;
	private static final String MENU_ERROR = MENU_ERROR2;
	private static final int EXPORT_CODE = 1;
	ListAdapter mAdapter;
	
	private alexMenu aMenu;
	private alexMenu contextMenu;
	private int activeSearchRequest=-1;//not selected
	ExportData exportData;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EXPORT_CODE && resultCode == RESULT_OK && data != null){
			exportData.process(data);
		}
	}
	
	static class SearchRequestFiller implements InfoListFiller {
		private Collection<SearchRequestItem> collection;
		public SearchRequestFiller(Collection<SearchRequestItem> collection){
			this.collection=collection;
		}		
		public Iterator<SearchRequestItem> getIterator(){
			return collection.iterator();
		}
	}
	
	private class ListAdapter extends BaseAdapter{
		private LayoutInflater mInflater;


		public ListAdapter() {
			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public View getView(int position, View convertView,ViewGroup parent){
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.srequest_item, null);
			}   
			Log.d(TAG,"position="+position);
			InfoList list= ListHolder.getListHolder().get(InfoList.SREQUEST_LIST);
			SearchRequestItem item=(SearchRequestItem)list.get(position);
			TextView nameTV=(TextView)convertView.findViewById(R.id.srequest_name);
			nameTV.setText(item.name);
			
			ChB ch=(ChB)convertView.findViewById(R.id.srequest_ch);
			ch.position=position;
			if(position==activeSearchRequest){
				ch.setChecked(true);
			}
			else {
				ch.setChecked(false);
			}
			
			
			((TextView)convertView.findViewById(R.id.srequest_string1)).setText(item.sqlString);
			((TextView)convertView.findViewById(R.id.srequest_string2)).setText(item.localString);		

			//make dark background
			if(SettingsActivity.getDarkSkin()|| SettingsActivity.getNightMode())
				convertView.setBackgroundColor(0xff000000);

			return convertView;
		}
		public int getCount(){
			InfoList list=ListHolder.getListHolder().get(InfoList.SREQUEST_LIST);
			return list.getCount();
		}
		public Object getItem(int position){
			InfoList list=ListHolder.getListHolder().get(InfoList.SREQUEST_LIST);
			return list.get(position);
		}
		public long getItemId(int position){
			return position;
		}
	}
	
	public void onListItemClick(ListView parent,View v,int position,long id){
		int pos=((ChB)(v.findViewById(R.id.srequest_ch))).position; //get checkbox element position
		activeSearchRequest=pos;
        mAdapter.notifyDataSetChanged();
	}	
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putBoolean("destroyed", true);
    }
	Handler initHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			setContentView(R.layout.srequest_activity);  
			mAdapter=new ListAdapter();
			setListAdapter(mAdapter);		

			findViewById(R.id.srequest_add).setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View arg0) {
					SearchRequestItem item=new SearchRequestItem("","","");
					NewRequestDialog.ProcessItem pi=new NewRequestDialog.ProcessItem() {
						
						public void process(SearchRequestItem item) {
							final InfoList iL=ListHolder.getListHolder().get(InfoList.SREQUEST_LIST);
							
							iL.fill(new SearchRequestActivity.SearchRequestFiller(Arrays.asList(new SearchRequestItem[]{item})));
							mAdapter.notifyDataSetChanged();
							
						}
					};
					registerDialog(new NewRequestDialog(item,pi, SearchRequestActivity.this).getDialog()).show();
					
				}
			});
			

			
			initAlexMenu();
			initAlexContextMenu();
			

			SharedPreferences prefs= SettingsActivity.getSharedPreferences(SearchRequestActivity.this);
			activeSearchRequest=prefs.getInt(Constants.SRA_ACTIVE_SEARCH_REQUEST, -1);
		}
	
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		exportData = new ExportData(this,EXPORT_CODE, "");
		initHandler.handleMessage(null);
	}
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if (keyCode==KeyEvent.KEYCODE_MENU) {
			aMenu.show(findViewById(R.id.srequest_add));
			return true;
		}
		else if(keyCode==KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event); 
	} 

	@Override
	public void onPause(){
		super.onPause();
		SettingsActivity.putSharedPreferences(Constants.SRA_ACTIVE_SEARCH_REQUEST, activeSearchRequest,this);
		new Prefs(this).saveList(InfoList.SREQUEST_LIST);
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.srequest_context_menu, menu);
	}	

	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();	
		int pos=(int) info.id;
		return parseContextMenu(item.getItemId(), pos) ? true : super.onContextItemSelected(item);
	}
	public boolean parseContextMenu(int id, int index) {
		switch (id){
		case R.id.srequest_edit:
			InfoList iL=ListHolder.getListHolder().get(InfoList.SREQUEST_LIST);
			SearchRequestItem item=(SearchRequestItem)iL.get(index);
			NewRequestDialog.ProcessItem pi=new NewRequestDialog.ProcessItem(){
				public void process(SearchRequestItem item){
					mAdapter.notifyDataSetChanged();
				}
			};
			NewRequestDialog rd=new NewRequestDialog(item,pi,this);
			registerDialog(rd.getDialog()).show();
			return true;
		case R.id.srequest_remove:	
			class RemoveItem implements Runnable{

				private int pos;
				public RemoveItem(int pos){
					this.pos=pos;
				}
				public void run(){
					
					InfoList iL=ListHolder.getListHolder().get(InfoList.SREQUEST_LIST);					
					iL.remove(pos);
					if(activeSearchRequest!=-1){
						if(pos<activeSearchRequest){
							activeSearchRequest--;
						}
						else 
							if(pos==activeSearchRequest){
								activeSearchRequest=-1;
							}
					}
					SearchRequestActivity.this.runOnUiThread(new Runnable(){
						public void run(){
							mAdapter.notifyDataSetChanged();
						}
					});
					
				}
			}
			RemoveItem r=new RemoveItem(index);
			registerDialog(AstroTools.getDialog(this,getString(R.string.do_you_really_want_to_remove_this_item_), r)).show();
			return true;
		
			
		default: return false;	
		}
	}
	
	public boolean parseMenu(int id){
		switch (id) {
		case MENU_EXPORT:
			final ExportData.Predicate<OutputStream> r = outstream -> {
					
				InfoList infoList=ListHolder.getListHolder().get(InfoList.SREQUEST_LIST);
				InfoListSaver saver=new InfoListStringSaverImp(outstream);
							
				try{
					boolean noError=infoList.save(saver);
					Log.d(TAG,"export, noError=" + noError);
					return noError;
				}
				catch(Exception ignore){
					return false;
				}
				finally {
					try{
						saver.close();
					}
					catch (Exception e){ }
				}
				
			};
			exportData.setCodeToRun(r);
			exportData.start();
			
			return true;   
		case MENU_IMPORT: //import more records to the current list
			IPickFileCallback listener = new IPickFileCallback(){

				public void callbackCall(Uri uri){
					try {
						readStream(getContentResolver().openInputStream(uri));
					} catch (IOException e) {
						//e.printStackTrace();
						Log.d(TAG,"e=" + e);
						onError();
					}
				}

				void onError() {
					Toast.makeText(SearchRequestActivity.this,
						R.string.import_error, Toast.LENGTH_SHORT).show();
				}

				public void readStream(InputStream inputStream) {
					InfoListLoader loader = new InfoListStringLoaderImp(inputStream);
					ErrorHandler eh = ListHolder.getListHolder().get(InfoList.SREQUEST_LIST).load(loader);
					if (eh.hasError())
						eh.showError(SearchRequestActivity.this);
					mAdapter.notifyDataSetChanged();
				}
			};

			SelectFileActivity.setPath(SettingsActivity.getFileDialogPath(getApplicationContext()));
			SelectFileActivity.setListener(listener);
			Intent fileDialog = new Intent(this, SelectFileActivity.class);
			startActivity(fileDialog);
			
			return true;
		}
		return false;
	}
	private void initAlexMenu() {

		boolean dayMode = !nightMode;

		aMenu = new alexMenu(this,new OnMenuItemSelectedListener() {
			public void MenuItemSelectedEvent(alexMenuItem selection) {
				parseMenu(selection.getId());
			}}, 
			getLayoutInflater());
		aMenu.setHideOnSelect(true);
		aMenu.setItemsPerLineInPortraitOrientation(4);
		aMenu.setItemsPerLineInLandscapeOrientation(4);
		aMenu.setSkin(nightMode, SettingsActivity.getDarkSkin());
		
		//mine
		float text_size=getResources().getDimension(R.dimen.text_size_small);//mine
		float density=getResources().getDisplayMetrics().density;
		text_size=text_size/density;
		aMenu.setTextSize((int)text_size);

		//load the menu items
		ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

		menuItems.add(new alexMenuItem(MENU_EXPORT, 
				getString(R.string.export), dayMode?R.drawable.am_load_v:R.drawable.ram_load_v, true ));
		menuItems.add(new alexMenuItem(MENU_IMPORT, 
				getString(R.string.import2), dayMode?R.drawable.am_save_v:R.drawable.ram_save_v, true ));

		if (aMenu.isNotShowing()){
			try {
				aMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				InputDialog.message(SearchRequestActivity.this,MENU_ERROR + e.getMessage(), 0).show();
			}	
		}
	}
	
	private void initAlexContextMenu() {
		contextMenu = new alexMenu(this, new OnMenuItemSelectedListener() {
			public void MenuItemSelectedEvent(alexMenuItem selection) {
				parseContextMenu(selection.getId(), contextMenu.getMenuItemId());
			}}, 
			getLayoutInflater());
		contextMenu.setHideOnSelect(true);
		contextMenu.setItemsPerLineInPortraitOrientation(1);
		contextMenu.setItemsPerLineInLandscapeOrientation(1);
		contextMenu.setSkin(nightMode, SettingsActivity.getDarkSkin());
		
		//mine
		float text_size=getResources().getDimension(R.dimen.table_main_text_size);//mine
		float density=getResources().getDisplayMetrics().density;
		text_size=text_size/density;
		contextMenu.setTextSize((int)text_size);//contextMenu.setTextSize(18)

		contextMenu.makeFloat();

		//load the menu items
		ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

		menuItems.add(new alexMenuItem(R.id.srequest_edit, getString(R.string.edit), 0, true ));
		menuItems.add(new alexMenuItem(R.id.srequest_remove, getString(R.string.remove), 0, true ));
		
		if (contextMenu.isNotShowing()){
			try {
				contextMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				InputDialog.message(SearchRequestActivity.this,MENU_ERROR2 + e.getMessage(), 0).show();
			}	
		}
		getListView().setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> arg0, View v,	int index, long arg3) {
				contextMenu.setMenuItemId(index);
				contextMenu.setHeader(((TextView)v.findViewById(R.id.srequest_name)).getText());
				contextMenu.show(v);
				return true;
			}

		});
	}
	
	@Override
	protected void onDestroy(){
		try{
			aMenu.hide();
			contextMenu.hide();
		}
		catch(Exception e){}
		super.onDestroy();
	}
	
	
	//Gesture Detector (just implement OnGestureListener in the Activity)
  	GestureDetector gDetector = new GestureDetector(this);
  	@Override
  	public boolean onTouchEvent(MotionEvent me) {
  		return gDetector.onTouchEvent(me);
  	}
  	public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
  		if(start==null || finish==null) return false;
  		float dy = start.getRawY() - finish.getRawY();
  		float dx = start.getRawX() - finish.getRawX();
  		if (dy>Global.flickLength){ //up
  			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
  			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
  			return true;
  		}
  		else if(dx > Global.flickLength) { //left
  			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
  			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
  			return true;
  		}
  		return false;
  	}
  	public void onLongPress(MotionEvent e) {}
  	public void onShowPress(MotionEvent e) {}
  	public boolean onDown(MotionEvent e) {return true;}
  	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {return false;}
  	public boolean onSingleTapUp(MotionEvent e) {return false;}
  	//-----------
}

class NewRequestDialog{
	
	private static final String VIS_0_1 = "(vis>0.1)";
	private static final String ALT_0 = "(alt>0)";
	private static final String A_3 = "(a>3)";
	private static final String TYPE_GC_TYPE_PN = "((type=GC)|(type=PN))";
	private static final String M_0_C_0 = "((m>0)|(c>0))";
	
	private static final String TAG="NRD";
	String name="";
	String sql="";
	String local="";
	String time="";
	SearchRequestItem item;
	Context context;
	ProcessItem pi;
	boolean dirty=false;
	private boolean ignoreDirty = false;
	
	//defines what to do with the changed item
	public interface ProcessItem{
		public void process(SearchRequestItem item);
	}
	public NewRequestDialog(SearchRequestItem item,ProcessItem pi,Context context) {//used for edit
		ignoreDirty = false;
		name=item.name;
		sql=item.sqlString;
		local=item.localString;
		//time=item.timeString;
		this.context=context; 
		this.pi=pi;
		this.item=item;
		//op=true;
	}
	
	
	public Dialog getDialog(){

		final InputDialog dl=new InputDialog(context);
		dl.setTitle(context.getString(R.string.set_search_rules));
		dl.insertLayout(R.layout.srequest_newitem_dialog);
		LinearLayout ll=(LinearLayout)dl.findViewById(R.id.srequest_ll);
		dl.setHelp(R.string.srequest_desc);

		EditText es1=(EditText)dl.findViewById(R.id.srequest_sql);
		es1.setText(sql);
		es1.setHint(M_0_C_0+'&'+TYPE_GC_TYPE_PN+'&'+A_3);
		
		EditText es2=(EditText)dl.findViewById(R.id.srequest_local);
		es2.setText(local);
		es2.setHint(ALT_0+'&'+VIS_0_1);
		
		dl.setPositiveButton(context.getString(R.string.ok), new InputDialog.OnButtonListener() {
			
			public void onClick(String value) {
				EditText es1=(EditText)dl.findViewById(R.id.srequest_sql);
				sql=es1.getText().toString().replace(" ", "").replace("\n", "").replace("\r", "");//todo dont remove within text strings
				EditText es2=(EditText)dl.findViewById(R.id.srequest_local);
				local=es2.getText().toString().replace(" ", "").replace("\n", "").replace("\r", "");
				dl.hide();//hide edit req dialog
				boolean dirty=(!item.sqlString.equals(sql)
						||!item.localString.equals(local)
						||ignoreDirty );
				if(dirty){
					ignoreDirty = false; //reset error
					item.sqlString=sql;
					item.localString=local;	
					boolean sqlFlag=checkSqlString(sql);
					Log.d(TAG,"sql Flag="+sqlFlag);
					
					String errormsg = "";
					if(!sqlFlag)
						errormsg += context.getString(R.string.bad_syntax_in_the_sql_expression_);
					if(!checkLocalString(local))
						errormsg += context.getString(R.string.bad_syntax_in_the_local_expression_);
					
					if(errormsg.length()>0){
						InputDialog derr = new InputDialog(context);
						derr.setTitle(context.getString(R.string.errors_in_the_input));
						derr.setMessage(errormsg + context.getString(R.string.would_you_like_to_save_the_request_anyway_for_later_review_));
						derr.setPositiveButton(context.getString(R.string.yes), new InputDialog.OnButtonListener() {
							public void onClick(String value) {
								saveRequests();
							}
						});
						derr.setNegativeButton(context.getString(R.string.no), new InputDialog.OnButtonListener() {
							public void onClick(String value) {
								// Return back to the input
								NewRequestDialog d2 = new NewRequestDialog(item,pi,context);
								d2.ignoreDirty();
								d2.getDialog().show();
							}
						});
						derr.show();
					}
					else
						saveRequests();
				}
			}

			private void saveRequests() {
				InputDialog d = new InputDialog(context);
				d.setType(InputDialog.DType.INPUT_STRING);
				d.setTitle(context.getString(R.string.enter_request_name));
				d.setValue(item.name);
				
				d.setPositiveButton(context.getString(R.string.ok), new InputDialog.OnButtonListener() {
					public void onClick(String value) {
						item.name = value;
						pi.process(item);
					}
				});
				d.show();
			}
		});
		dl.setNegativeButton(context.getString(R.string.cancel));
		
		return dl;
	}
	private boolean checkSqlString(String sql){
		Analisator an=new Analisator();
		an.setInputString(sql);
		an.dsoInitSQLrequest();
		Set<String> numericFields=getDbListItemsNumFields();//numeric fields for all databases
		for(String s:numericFields){
			an.addVar(s, 0);
		}
		try{
			an.compile();
		}
		catch(UnsupportedOperationException e){
			return false;
		}
		return true;
	}
	private Set<String> getDbListItemsNumFields(){
		Set<String>set=new HashSet<String>();
		InfoList list=ListHolder.getListHolder().get(InfoList.DB_LIST);
		Iterator it=list.iterator();
		for(;it.hasNext();){
			set.addAll(((DbListItem)it.next()).ftypes.getNumericFields());
		}
		return set;
	}
	private boolean checkLocalString(String local){
		if("".equals(local))//no rule if local string is not selected
			return true;
		Analisator an=new Analisator();
		an.setInputString(local);
		an.dsoInitLocalrequest();
		try{
			an.compile();
		}
		catch(UnsupportedOperationException e){
			return false;
		}
		return true;
	}
	public void ignoreDirty(){
		ignoreDirty = true;
	}
	
}
	

