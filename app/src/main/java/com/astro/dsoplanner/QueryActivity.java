package com.astro.dsoplanner;

import static com.astro.dsoplanner.Constants.constellations;
import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.DoubleStarObject;
import com.astro.dsoplanner.graph.CuV;
import com.astro.dsoplanner.graph.GraphActivity;
import com.astro.dsoplanner.graph.GraphRec;
import com.astro.dsoplanner.infolist.ObsInfoListImpl.Item;
import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.util.Holder2;
import java.io.OutputStream;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListStringSaverImp;
import java.io.IOException;

public class QueryActivity extends ParentListActivity implements Handler.Callback,OnGestureListener {
	private static final String NGC = "NGC";
	private static final String MENU_ERROR2 = "Menu error! ";
	private static final String MENU_ERROR = MENU_ERROR2;
	private static final String DATABASE = "Database";
	private static final String STRING = "--";
	
	static final int MENU_NGCIC=1000;
	static final int MENU_COMET=1001;
	static final int MENU_BRIGHT_MINOR_PLANET=1002;	
	public static final int MENU_MIN=1050;

	public static final int EXPORT_CODE = 1;
	private ExportData exportData;
	
    private TextView mTextView;
    private CuV skyview;

	private static final String TAG = QueryActivity.class.getSimpleName();

	private static boolean stop_search=false;
	/**
	 * 
	 * @return true if stop flag set
	 */
	public static synchronized boolean isStopping(){
		return stop_search;
	}
	/**
	 * sets stop flag for database dso selection
	 */
	public static synchronized void setStopFlag(){
		stop_search=true;
	}
	public static synchronized void clearStopFlag(){
		stop_search=false;
	}

	private String[] spinStringArr=new String[1];
	private int[] spinIntArr=new int[1];
	private int spinPos = 0;
	private int item_selected=-1;//used in find/next
	
	private boolean nightMode=false;
	private ListAdapter mAdapter;

	private alexMenu aMenu;
	private alexMenu contextMenu;
	
	private List<Item> displayList=new CopyOnWriteArrayList<Item>();//the list is passed to the controller and used in thread for export
	private InfoList infoList=new InfoListImpl("",Item.class);
	
	
	
	private TextView mSpinButton;
	private Button mFilterButton;
	QueryController controller;
	private boolean checkboxes=false;
	
	BroadcastReceiver geoReceiver=new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean dirty= SettingsActivity.getSharedPreferences(QueryActivity.this).getBoolean(Constants.QUERY_UPDATE,true);
			mFilterButton.setText(dirty?R.string.update:R.string.select);
		}
	};
	
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter(
                Constants.GEO_BROADCAST);
		LocalBroadcastManager.getInstance(this).registerReceiver(geoReceiver,filter);
	}
	private void unregisterReceiver(){
		LocalBroadcastManager.getInstance(this).unregisterReceiver(geoReceiver);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == EXPORT_CODE && resultCode == RESULT_OK && data != null){
			exportData.process(data);
		}
	}
	@Override
	protected void onResume(){
		super.onResume();
		Log.d(TAG,"onResume");
		onResumeCode();
		Log.d(TAG,"onResume over");
	}
	private void onResumeCode(){
		
		SettingsActivity.nightGuardReset();
		boolean dirty= SettingsActivity.getSharedPreferences(this).getBoolean(Constants.QUERY_UPDATE,true)||
				SettingsActivity.getSharedPreferences(this).getBoolean(Constants.SETTINGS_SEARCH_CATALOG_UPDATE, false);
		mFilterButton.setText(dirty?R.string.update:R.string.select);
		registerReceiver();
		
		
		if(isSkyViewOn()&&skyview!=null){
			skyview.initDraw();
			skyview.initExternal( new GraphRec(QueryActivity.this));
		}
		if(SettingsActivity.getBooleanFromSharedPreferences(getApplicationContext(),
				Constants.FIRST_TIME_ENTRY_OBJ_SEL, true)){
			SettingsActivity.putSharedPreferences(Constants.FIRST_TIME_ENTRY_OBJ_SEL,
					false, getApplicationContext());
			registerDialog(InputDialog.message(QueryActivity.this,getString(R.string.menu_warning),0)).show();

		}
	}
	@Override
	protected void onDestroy() {

		try{
			aMenu.hide();
			contextMenu.hide();
		}
		catch(Exception e){}

		super.onDestroy();
		setStopFlag();
		if(controller!=null) controller.dispose();
		if(skyview!=null){
			skyview.setGraphDestroyedFlag();
			skyview.clearLists();
		}
	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean("destroyed", true);
	}
	//override the system search request in night mode only
	@Override
	public boolean onSearchRequested() {
		return AstroTools.invokeSearchActivity(this);
	}
	
	private void setSortFields(){
		TextView tv=(TextView)findViewById(R.id.mq_name);
		tv.setText(R.string.name);
		tv=(TextView)findViewById(R.id.mq_con);
		tv.setText(R.string.con);
		tv=(TextView)findViewById(R.id.mq_type);
		tv.setText(R.string.type);
		tv=(TextView)findViewById(R.id.mq_mag);
		tv.setText(R.string.mag);
		tv=(TextView)findViewById(R.id.mq_dim);
		tv.setText(R.string.dim);
	}
	private void updateSortField(int field){
		setSortFields();
		int order=mapsortorder.get(field);
		char ch;
		if(order>0)
			ch='\u02C7';
		else
			ch='\u02C6';
		switch(field){
		case SORT_NUMBER:
			TextView tv=(TextView)findViewById(R.id.mq_name);
			tv.setText(getString(R.string.name)+ch);
			break;
		case SORT_CON:
			tv=(TextView)findViewById(R.id.mq_con);
			tv.setText(getString(R.string.con)+ch);
			break;
		case SORT_MAG:
			tv=(TextView)findViewById(R.id.mq_mag);
			tv.setText(getString(R.string.mag)+ch);
			break;
		case SORT_TYPE:
			tv=(TextView)findViewById(R.id.mq_type);
			tv.setText(getString(R.string.type)+ch);
			break;
		case SORT_DIM:
			tv=(TextView)findViewById(R.id.mq_dim);
			tv.setText(getString(R.string.dim)+ch);
			break;
		}
		
		
	}
	
	
	
	private void toggleSortOrder(int field){		
		if(field==sortfield){//same field, change the order
			int order=mapsortorder.get(field);
			order=-order;
			mapsortorder.put(field, order);
		}
		else{//diff field, default order
			mapsortorder.put(field,1);
		}
		sortfield=field;
		updateListArray(spinIntArr[spinPos],infoList);
		updateSortField(sortfield);
		
		
		
		mAdapter.notifyDataSetChanged();
	}
	/**
	 * add / remove last column
	 * change padding
	 * @param ch
	 */
	private void modifyContentView(boolean ch){
		View v=findViewById(R.id.mq_empty);
		if(ch)
			v.setVisibility(View.VISIBLE);
		else
			v.setVisibility(View.GONE);
		
		float padding;
		float padding_left;
		if(ch){
			padding=getResources().getDimension(R.dimen.query_padding_ch);
			padding_left=getResources().getDimension(R.dimen.query_padding_left_ch);
		}
		else{
			padding=getResources().getDimension(R.dimen.query_padding_no_ch);
			padding_left=getResources().getDimension(R.dimen.query_padding_left);
		}
		
		View ll=findViewById(R.id.dsoselection_colshead);
		ll.setPadding((int)padding_left, (int)padding, ll.getPaddingRight(), (int)padding);
	}


    Handler initHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			checkboxes= SettingsActivity.getSharedPreferences(getApplicationContext()).getBoolean(Constants.QUERY_CHECK_BOXES, false);
			setContentView(R.layout.mquery);
			Log.d(TAG,"content view set");
			modifyContentView(checkboxes);
			
			clearStopFlag();
            //Other settings
			nightMode= SettingsActivity.getNightMode();
			mTextView=(TextView)findViewById(R.id.textSelect);
			
			setStopBtnVisibility(false);
			View v=findViewById(R.id.qbtnstop);
			v.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					v.setVisibility(View.GONE);
					setStopFlag();
				}
			});
			
			
			TextView tv=(TextView)findViewById(R.id.mq_name);
			tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					toggleSortOrder(SORT_NUMBER);
				}
			});
			
			tv=(TextView)findViewById(R.id.mq_con);
			tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					toggleSortOrder(SORT_CON);
				}
			});
			
			tv=(TextView)findViewById(R.id.mq_type);
			tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					toggleSortOrder(SORT_TYPE);
				}
			});
			
			tv=(TextView)findViewById(R.id.mq_dim);
			tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					toggleSortOrder(SORT_DIM);
				}
			});
			
			tv=(TextView)findViewById(R.id.mq_mag);
			tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					toggleSortOrder(SORT_MAG);
				}
			});
			updateSortField(sortfield);
			
			ChB ch=(ChB)findViewById(R.id.mq_empty);	
			boolean ch_on= SettingsActivity.getSharedPreferences(getApplicationContext()).getBoolean(Constants.QUERY_HEAD_CHECK_BOX, Constants.QUERY_HEAD_CHECK_BOX_DEF_VALUE);
			ch.setChecked(ch_on);
			ch.setOnClickListener(new OnClickListener() {    
				public void onClick(View v) {   					
					boolean status=((ChB)v).isChecked();
					for(Item item:displayList){
						item.y=status;
					}					
					mAdapter.notifyDataSetChanged();
					controller.handleMessage(QueryController.MESSAGE_CHECKBOX_CHOSEN);
				}
			});
			
           
			
			
			
			spinStringArr[0]=getString(R.string.all);
			
			controller=new QueryController(QueryActivity.this);
			controller.addOutboxHandler(new Handler(QueryActivity.this));
			controller.handleMessage(QueryController.MESSAGE_INIT, getIntent());
			
			mAdapter=new ListAdapter();
			setListAdapter(mAdapter);
			
			mSpinButton = (Button)findViewById(R.id.spinner);
			mSpinButton.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					final InputDialog d = new InputDialog(QueryActivity.this);
					d.setTitle(getString(R.string.select_constellation));
					d.setMessage("");
					d.setValue(String.valueOf(spinPos));
					final int oldSpinPos=spinPos;
					d.setListItems(spinStringArr, new InputDialog.OnButtonListener() {
						public void onClick(String value) {
							int pos = AstroTools.getInteger(value, 0,0,10000);
							if(pos == spinStringArr.length - 1) //cancel
								return;
							spinPos = pos;
							if(spinPos!=oldSpinPos){
								controller.handleMessage(QueryController.MESSAGE_FIND_RESET);
								item_selected=-1;
							}
							updateListArray(spinIntArr[spinPos],infoList);
							mAdapter.notifyDataSetChanged();
							mTextView.setText(displayList.size()+ getString(R.string._obj));
							mSpinButton.setText(spinStringArr[spinPos]);
						}
					});
					registerDialog(d).show();
				}

			});
			mFilterButton = (Button)findViewById(R.id.qbtnfilter);

			updateFilterButton();
			mFilterButton.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					boolean dirty= SettingsActivity.getSharedPreferences(QueryActivity.this).getBoolean(Constants.QUERY_UPDATE,true)||
							SettingsActivity.getSharedPreferences(QueryActivity.this).getBoolean(Constants.SETTINGS_SEARCH_CATALOG_UPDATE, false);

					if(dirty) { //Update button 
						parseMenu(R.id.update_query); //will set it to false in handler
					}
					else { //it's Filter button
						parseMenu(R.id.settings);
					}
				}

			});	
			
			
			
			if (ALEX_MENU_FLAG) {
				initAlexMenu();
				initAlexContextMenu();
			}
			else
				registerForContextMenu(getListView()); 
			
			View view=findViewById(R.id.sky_view);
			if(view!=null)
				skyview=(CuV)view;
			
			updateSkyView();
			Log.d(TAG,"onCreate over");
		}

	};
	
	private void updateSkyView(){
		boolean show=isSkyViewOn();

		if(skyview!=null){
			
			if(show){
				skyview.setVisibility(View.VISIBLE);
				skyview.setCallType(CuV.EXTERNAL);
				skyview.setProcessLongTouch();
				skyview.enableDraw();
				controller.setSkyViewFlag();

				skyview.initDraw();
				skyview.initExternal( new GraphRec(QueryActivity.this));
				
			}
			else{
				skyview.setVisibility(View.GONE);
				skyview.disableDraw();
				controller.clearSkyViewFlag();
				skyview.clearLists();
			}
			
			
			
		}
	}
	boolean initRequired=false;//global init
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState); 
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		exportData = new ExportData(this,EXPORT_CODE, "");
		initHandler.handleMessage(null);

	}
	
	/**
	 * called from skyview on long touch
	 */
	public void goFullSkyView(){
		if(skyview==null)return;
		skyview.saveScreenIntoPrefs();
		final Intent i = new Intent(this, GraphActivity.class);
		startActivity(i);
	}
	
	@Override
	public void onPause(){
		super.onPause();
        if(controller!=null) {
            controller.handleMessage(QueryController.MESSAGE_KEEP_CURRENT_LIST, infoList);
            controller.handleMessage(QueryController.MESSAGE_SAVE, spinPos);
        }
        unregisterReceiver();
        ChB ch=(ChB)findViewById(R.id.mq_empty);	
        SettingsActivity.putSharedPreferences(Constants.QUERY_HEAD_CHECK_BOX, ch.isChecked(),getApplicationContext());
        if(controller.isIntentToGraph()){
        	controller.clearGraphIntentFlag();
        }
        else{
        	if(skyview!=null&&isSkyViewOn()){
        		skyview.saveScreenIntoPrefs();
        		skyview.clearLists();
        	}
        }
	}
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
		return parseContextMenu(item.getItemId(), (int)info.id) ? true : super.onContextItemSelected(item);
	}
	private boolean parseContextMenu(int itemId, int objIndex) {

		Item item=displayList.get(objIndex);    	
		controller.handleMessage(QueryController.MESSAGE_SET_TIME);
		NoteRequest request;
		Command command;
		switch (itemId){
		case R.id.addmarked:
			controller.handleMessage(QueryController.MESSAGE_ADD_MARKED_OBSLIST,displayList);
			if(skyview!=null)
				skyview.updateObsList();
			return true;
		case R.id.qseepic:
			controller.handleMessage(QueryController.MESSAGE_SEE_PICTURE,item);			
			return true;
		case R.id.add:	
			controller.handleMessage(QueryController.MESSAGE_ADD_OBSLIST,item);
			if(skyview!=null)
				skyview.updateObsList();
			return true;
		case R.id.addall:
			controller.handleMessage(QueryController.MESSAGE_ADD_ALL_OBSLIST,displayList);
			if(skyview!=null)
				skyview.updateObsList();
			return true;

		case R.id.item1:
			new DetailsCommand(item.x,this).execute();
			return true;
		case R.id.qaddnote:	
			controller.handleMessage(QueryController.MESSAGE_ADD_NOTE,item);			
			return true;
		case R.id.qseenotes:
			controller.handleMessage(QueryController.MESSAGE_SEE_NOTES,item);
			return true;
		case R.id.qseeallnotes:
			controller.handleMessage(QueryController.MESSAGE_SEE_ALL_NOTES,item);
			return true;
		
		default: return false; 
		}

	}
	Map<Integer, DbListItem> menuMap=new HashMap<Integer,DbListItem>();//additional databases list, menuId/item

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		InfoList iL= ListHolder.getListHolder().get(InfoList.DB_LIST);
		Iterator it=iL.iterator();
		SubMenu subMenuDatabase=menu.addSubMenu(DATABASE);

		for(;it.hasNext();){
			DbListItem dbitem=(DbListItem)it.next();
			menuMap.put(dbitem.menuId, dbitem);
			String name=dbitem.dbName;
			name=name.substring(0,Math.min(name.length(), 15));
			subMenuDatabase.add(Menu.NONE,dbitem.menuId,Menu.NONE,name);
		}
		return true;
	}
	
	private boolean isSkyViewOn(){
		return SettingsActivity.getSharedPreferences(getApplicationContext()).getBoolean(Constants.SHOW_QUERY_CUV, false);
	}
	private void setSkyView(boolean show){
		SettingsActivity.putSharedPreferences(Constants.SHOW_QUERY_CUV, show, getApplicationContext());

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return parseMenu(item.getItemId());
	}
	public boolean parseMenu(int id){
		
		switch (id) {
		case R.id.qtoggle:
			boolean show=isSkyViewOn();
			setSkyView(!show);
			updateSkyView();
			if(show&&skyview!=null){//save current screen when switching sky view off as it is not saved in onPause if sky view is off
				skyview.saveScreenIntoPrefs();
			}
			return true;
		case R.id.qch:
			checkboxes=!checkboxes;
			modifyContentView(checkboxes);
			SettingsActivity.putSharedPreferences(Constants.QUERY_CHECK_BOXES, checkboxes, getApplicationContext());
			mAdapter.notifyDataSetChanged();
			return true;
		case R.id.update_query:
			ChB ch=(ChB)findViewById(R.id.mq_empty);
			ch.setChecked(false);
			SettingsActivity.putSharedPreferences(Constants.QUERY_CHECK_BOXES, Constants.QUERY_CHECK_BOXES_DEF_VALUE, getApplicationContext());

			boolean updcat= SettingsActivity.getSharedPreferences(this).getBoolean(Constants.SETTINGS_SEARCH_CATALOG_UPDATE, false);
			if(updcat){
				InfoList iL=ListHolder.getListHolder().get(InfoList.DB_LIST);
				int pos= SettingsActivity.getSharedPreferences(this).getInt(Constants.SETTINGS_SEARCH_CATALOG, 0);
				controller.handleMessage(QueryController.MESSAGE_UPDATE_CATALOG,iL.get(pos));
			}
			else
				controller.handleMessage(QueryController.MESSAGE_UPDATE_LIST);			
			return true;
		case R.id.settings:
			Intent intent=new Intent(this, SettingsSearchActivity.class);
			startActivity(intent);
			return true;   	
			
		case R.id.save_obs:	
			final ExportData.Predicate<OutputStream> r = out -> {

				InfoListSaver saver=new InfoListStringSaverImp(out);
				try{
					saver.addName("")	;
					for(Item item: displayList){
						saver.addObject(item.x);
					}
				}
				catch(IOException ignore){
					return false;
				}
				finally {
					try{
						saver.close();
					}
					catch (Exception e){ }
				}
				return true;
			};
			exportData.setCodeToRun(r);
			exportData.start();


			return true;
		case R.id.search_item:
			onSearchRequested();
			return true;
		
		case R.id.qshare:
			if(SettingsActivity.nightGuard(this)) return true;
			controller.handleMessage(QueryController.MESSAGE_SHARE,displayList);			
			return true;
		case R.id.qfind:
			item_selected=-1;//clear previous "find"
			mAdapter.notifyDataSetChanged();
			String init_string= SettingsActivity.getStringFromSharedPreferences(QueryActivity.this, Constants.FIND_STRING, "");
			//Ask for the "find" string
			InputDialog d = new InputDialog(QueryActivity.this);
			d.setValue(init_string);
			d.setType(InputDialog.DType.INPUT_STRING);
			d.setTitle(getString(R.string.find));
			d.setMessage(getString(R.string.please_enter_the_string_to_find_in_the_list));
			d.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
				public void onClick(String value) {					
					String find_string=InputDialog.getResult();
					SettingsActivity.putSharedPreferences(Constants.FIND_STRING, find_string, QueryActivity.this);
					Holder2<String,List<Item>>data=new Holder2<String,List<Item>>(find_string,displayList);
					controller.handleMessage(QueryController.MESSAGE_FIND,data);
					//r.run();
				}
			});
			d.setNegativeButton(getString(R.string.cancel));
			registerDialog(d).show();
			return true;
		case R.id.qnext:
			controller.handleMessage(QueryController.MESSAGE_FIND_NEXT,null);
			return true;
		}
		return false;
	}
	@Override
	public void onListItemClick(ListView parent,View v,int position,long id){
		if(contextMenu.isNotShowing()) { //trick
			if(skyview!=null&&isSkyViewOn())
				SettingsActivity.putSharedPreferences(Constants.CURRENT_ZOOM_LEVEL, skyview.getZoom(), this);

			controller.handleMessage(QueryController.MESSAGE_SKY_VIEW, displayList.get(position));
			
		}
	}
	
	private void setStopBtnVisibility(boolean vis){
		View v=findViewById(R.id.qbtnstop);
		if(vis){
			v.setVisibility(View.VISIBLE);
		}
		else
			v.setVisibility(View.GONE);
	}
	
	private void updateFilterButton(){
		boolean dirty= SettingsActivity.getSharedPreferences(this).getBoolean(Constants.QUERY_UPDATE, true)||
				SettingsActivity.getSharedPreferences(this).getBoolean(Constants.SETTINGS_SEARCH_CATALOG_UPDATE, false);
		mFilterButton.setText(dirty?R.string.update:R.string.select);
	}
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
		case QueryController.MESSAGE_SHOW_ON_STAR_CHART:
			if(skyview!=null){
				skyview.initExternal((GraphRec)msg.obj);
			}
			return true;
			
		case QueryController.MESSAGE_UPDATE_VIEW:
			spinPos=msg.arg1;
			
			infoList=(InfoList)msg.obj;//need to keep that as displayList only keeps data on the screnn
										// on the basis of selected constellation and
										//infoList keeps the full search result
			initSpinner(infoList);
			if(spinPos>=spinIntArr.length)
				spinPos=0;
			updateListArray(spinIntArr[spinPos],(InfoList)msg.obj);
			mSpinButton = (Button)findViewById(R.id.spinner);
			mSpinButton.setText(spinStringArr[spinPos]);
			mAdapter.notifyDataSetChanged();
			mTextView.setText(displayList.size()+ getString(R.string._obj));
			updateFilterButton();
			setProgressBarIndeterminateVisibility(false);
			setStopBtnVisibility(false);

			return true;
		case QueryController.MESSAGE_UPDATE_FILTER_BTN:
			updateFilterButton();
			return true;

		case QueryController.MESSAGE_INPROGRESS:
			setProgressBarIndeterminateVisibility(true);
			setStopBtnVisibility(true);
			return true;
		case QueryController.MESSAGE_SET_ACTIVITY_NAME:
			setTitle((String)msg.obj);
			return true;
		case QueryController.MESSAGE_ERROR_HANDLER:
			ErrorHandler eh=(ErrorHandler)msg.obj;
			if(eh.hasError())
				eh.showError(this);
			return true;
		case QueryController.MESSAGE_TEXT:
			registerDialog(InputDialog.message(QueryActivity.this,(String)msg.obj,0)).show();
			return true;
		case QueryController.MESSAGE_TEXT_FIND:
			InputDialog d=InputDialog.message(QueryActivity.this,(String)msg.obj,0);
			d.setTitle(getString(R.string.find_results));
			registerDialog(d);
			d.show();
			return true;
		case QueryController.MESSAGE_EXECUTE_ON_UI_THREAD:
			Controller.Executable exec=(Controller.Executable)msg.obj;
			exec.setContext(this);
			exec.run();
			return true;
		case QueryController.MESSAGE_REMOVE_INPROGRESS:
			setProgressBarIndeterminateVisibility(false);
			return true;
		case QueryController.MESSAGE_SET_LIST_LOCATION:
			item_selected=msg.arg1;
			mAdapter.notifyDataSetChanged();
			getListView().setSelection(msg.arg1);
			return true;
		}
		return false;
	} 
	
	private void initSpinner(InfoList infoList){
			Log.d(TAG,"initSpinner, beginning");
			boolean flag=true;        	

			Set<Integer> set=new TreeSet<Integer>();
			
			Iterator iter = infoList.iterator();
			for (;iter.hasNext();){
				flag=true;
				Item obj=(Item)iter.next();				
				if(obj.x == null)
					continue;
				
				int con=obj.x.getCon();
				if(con!=0)
					set.add(con);
			}
			if (flag){
				Iterator<Integer> it=set.iterator();
				int counts=set.size();
				spinIntArr=new int[counts+1];
				spinStringArr=new String[counts+2];

				int i=0;
				while (it.hasNext()){
					i++;
					spinIntArr[i]= it.next();
				}
				for(i=0;i<spinStringArr.length-1;i++)
					spinStringArr[i]=constellations[spinIntArr[i]];
				spinStringArr[0]=getString(R.string.all);
				spinStringArr[spinStringArr.length - 1] = getString(R.string.cancel);
			}
			Log.d(TAG,"initSpinner, end");
	}
	
	private void updateListArray(int con,InfoList infoList){
			Log.d(TAG,"updateListArray, beginning");
			Iterator it=infoList.iterator();
			if(con==0){
				displayList=new ArrayList<Item>();
				for(;it.hasNext();){
					displayList.add((Item)it.next());
				}
				Log.d(TAG,"updateListArray, end");
				sortListArray();
				Log.d(TAG,"sortListArray, end");
				return;
			}
			displayList=new ArrayList<Item>();
			for(;it.hasNext();){
				Item obj=(Item)it.next();
				if(obj.x.getCon()==con)
					displayList.add(obj);
			}
			sortListArray();
			Log.d(TAG,"updateListArray, end");
			

	}
	private void sortListArray(){
		int order=mapsortorder.get(sortfield);
		Comparator cmp=new ComparatorImpl(sortfield,order);
		Collections.sort(displayList, cmp);
	}

    private class ListAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		private static final int NON_BOLD=1;
		private static final int BOLD=2;
		private final Object DOUBLE_OBJECT=new Object();
		private final Object DOUBLE_OBJECT_CH=new Object();
		private final Object OBJECT=new Object();
		private final Object OBJECT_CH=new Object();

		public ListAdapter() {
			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(int position, View convertView,ViewGroup parent){
			boolean bold=(position==item_selected);
			
			Item item=displayList.get(position);
			AstroObject obj=item.x;
			
			boolean double_star=obj.getCatalog()== AstroCatalog.HAAS||obj.getCatalog()==AstroCatalog.WDS;
			Object tag=convertView==null?null:convertView.getTag();
			boolean redo=(convertView==null)||
					((DOUBLE_OBJECT.equals(tag)||DOUBLE_OBJECT_CH.equals(tag))&&!double_star)||								
					(!(DOUBLE_OBJECT.equals(tag)||DOUBLE_OBJECT_CH.equals(tag))&&double_star)||
					((DOUBLE_OBJECT_CH.equals(tag)||OBJECT_CH.equals(tag))&&!checkboxes)||
					((DOUBLE_OBJECT.equals(tag)||OBJECT.equals(tag))&&checkboxes);
			
			if (redo) {
				if(double_star){
					convertView = mInflater.inflate(checkboxes?R.layout.dsoselection_item_double_ch:R.layout.dsoselection_item_double, null);
					convertView.setTag(checkboxes?DOUBLE_OBJECT_CH:DOUBLE_OBJECT);
				}
				else{
					convertView = mInflater.inflate(checkboxes?R.layout.dsoselection_item_ch:R.layout.dsoselection_item, null);
					convertView.setTag(checkboxes?OBJECT_CH:OBJECT);
				}
			
			}
			

			TextView tv_num=(TextView)convertView.findViewById(R.id.dsosel_ngcnum);
            tv_num.setText(obj.getDsoSelName());
            if(bold)
            	tv_num.setTypeface(null, Typeface.BOLD);
            else
            	tv_num.setTypeface(null, Typeface.NORMAL);
           
            TextView tv_con=(TextView)convertView.findViewById(R.id.dsosel_con);
            tv_con.setText(obj.getConString());
            tv_con.setTypeface(null, bold?Typeface.BOLD:Typeface.NORMAL);
            
            TextView tv_type=(TextView)convertView.findViewById(R.id.dsosel_type);
            tv_type.setText(obj.getTypeString());
            tv_type.setTypeface(null, bold?Typeface.BOLD:Typeface.NORMAL);
            
            TextView tv_mag=(TextView)convertView.findViewById(R.id.dsosel_mag);
            tv_mag.setText(Double.isNaN(obj.getMag())?STRING:String.format(Locale.US,"%.1f",obj.getMag()));
            tv_mag.setTypeface(null, bold?Typeface.BOLD:Typeface.NORMAL);
            
            TextView tv_dim=(TextView)convertView.findViewById(R.id.dsosel_dim);
            double dim=Math.max(obj.getA(), obj.getB());
            tv_dim.setText(obj.hasDimension()?
                    (Double.isNaN(dim)?STRING:String.format(Locale.US,"%.1f",dim))
                    : STRING);
            tv_dim.setTypeface(null, bold?Typeface.BOLD:Typeface.NORMAL);
            
            if(double_star){
            	TextView tv_second_line=(TextView)convertView.findViewById(R.id.dsosel_double);
            	if(tv_second_line!=null){
            		if(obj instanceof DoubleStarObject){
            			DoubleStarObject hobj=(DoubleStarObject)obj;
            			String line=hobj.getDsoSelNameSecondLine();
            			tv_second_line.setText(line);
            		}
            	}
            }
            
            ChB ch=(ChB)convertView.findViewById(R.id.dsosel_ch);
            if(ch!=null){
            	ch.setChecked(item.y);
    			ch.setOnClickListener(new OnClickListener() {    
    				public void onClick(View v) {        
    					int pos=((ChB)v).position;
    					boolean status=((ChB)v).isChecked();
    					Item item=displayList.get(pos);//(ObsInfoListImpl.Item)ListHolder.getListHolder().
    					item.y=status;//as display list and underlying list share the same objects, underlying list objects update automatically
    					controller.handleMessage(QueryController.MESSAGE_CHECKBOX_CHOSEN);
    				}
    			});
    			ch.position=position;
            }
			//make dark background
			if(SettingsActivity.getDarkSkin()|| SettingsActivity.getNightMode())
				convertView.setBackgroundColor(0xff000000);
			return convertView;
		}
		public int getCount(){
			return displayList.size();
		}
		public AstroObject getItem(int position){
			return displayList.get(position).x;
		}
		public long getItemId(int position){
			return position;
		}
	}
    
    private static final int SORT_NUMBER=1;   
    private static final int SORT_CON=2;
    private static final int SORT_TYPE=3;
    private static final int SORT_MAG=4;
    private static final int SORT_DIM=5;
    
    private static int sortfield=SORT_NUMBER;//number
    private static Map<Integer,Integer> mapsortorder=new HashMap<Integer,Integer>();
    static{
    	mapsortorder.put(SORT_NUMBER, 1);    	
    	mapsortorder.put(SORT_CON, 1);
    	mapsortorder.put(SORT_TYPE, 1);
    	mapsortorder.put(SORT_MAG, 1);
    	mapsortorder.put(SORT_DIM, 1);
    }
    
    private class ComparatorImpl implements Comparator<Item>{
		private int cmp;
		private int order;

		public ComparatorImpl(int cmpType,int order){ //-1 from lower to high, 1 from high to lower
			this.cmp=cmpType;
			this.order=order;
		}
		
		private int compareNames(String n1,String n2){
			Pattern p=Pattern.compile("[a-zA-Z]+");
			Matcher m=p.matcher(n1);
			int n1s=-1;
			int n1e=-1;
			int n2s=-1;
			int n2e=-1;
			if(m.find()){
				n1s=m.start();
				n1e=m.end();
			}
			m=p.matcher(n2);
			if(m.find()){
				n2s=m.start();
				n2e=m.end();
			}
			if(n1s==n2s&&n1s!=-1&&n1e==n2e&&n2e!=-1){
				String s1=n1.substring(n1s,n1e);
				String s2=n2.substring(n2s,n2e);
				if(s1.equals(s2)){
					String snum1=n1.substring(n1e);
					String snum2=n2.substring(n1e);
					int num1=-1;
					int num2=-1;
					try{
						num1=Integer.parseInt(snum1);
						num2=Integer.parseInt(snum2);
					}
					catch(Exception e){
						
					}
					if(num1!=-1&&num2!=-1){
						if(num1>num2)return 1;
						if(num1==num2)return 0;
						return -1;
					}
				}
			}
			return n1.compareTo(n2);
			
		}
		
		
		public int compare(Item lhs, Item rhs){
			switch(cmp){
			case SORT_NUMBER:
				return (lhs.x.getDsoSelName().compareTo(rhs.x.getDsoSelName())*order);

				
			case SORT_CON:
				
				int lhscon=lhs.x.getCon();
				int rhscon=rhs.x.getCon();
				if(lhscon==0)
					lhscon=AstroTools.getConstellation(AstroTools.getNormalisedRa(lhs.x.getRa()),lhs.x.getDec());
				if(rhscon==0)
					rhscon=AstroTools.getConstellation(AstroTools.getNormalisedRa(rhs.x.getRa()),rhs.x.getDec());

				if(lhscon<rhscon) return -1*order;
				if(lhscon==rhscon) return new ComparatorImpl(SORT_NUMBER,1).compare(lhs,rhs);
				return 1*order;
			case SORT_MAG:
				
				boolean lnan=Double.isNaN(lhs.x.getMag());
				boolean rnan=Double.isNaN(rhs.x.getMag());
				if(lnan&&!rnan)
					return -order;
				if(lnan&&rnan)
					return new ComparatorImpl(SORT_NUMBER,1).compare(lhs,rhs);
				if(!lnan&&rnan){
					return order;
				}
				
				if(lhs.x.getMag()<rhs.x.getMag()) return -1*order;
				if(lhs.x.getMag()==rhs.x.getMag()) return new ComparatorImpl(SORT_NUMBER,1).compare(lhs,rhs);
				return 1*order;

			case SORT_DIM:			
				
				double lhsdim=Math.max(lhs.x.getA(), lhs.x.getB());
				double rhsdim=Math.max(rhs.x.getA(), rhs.x.getB());
				
				lnan=Double.isNaN(lhsdim);
				rnan=Double.isNaN(rhsdim);
				if(lnan&&!rnan)
					return -order;
				if(lnan&&rnan)
					return new ComparatorImpl(SORT_NUMBER,1).compare(lhs,rhs);
				if(!lnan&&rnan){
					return order;
				}
				
				
				if(lhsdim<rhsdim) return -1*order;
				if(lhsdim==rhsdim) return new ComparatorImpl(SORT_NUMBER,1).compare(lhs,rhs);
				return 1*order;
			case SORT_TYPE:
				String lhst=lhs.x.getTypeString();
				String rhst=rhs.x.getTypeString();
				
				int res=lhst.compareTo(rhst)*order;
				if(res==0){
					return new ComparatorImpl(SORT_NUMBER,1).compare(lhs,rhs);
				}
				else{
					return res;
				}
			}
			return -1;
		}
		
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
		aMenu.setItemsPerLineInLandscapeOrientation(6);
		aMenu.setSkin(nightMode, SettingsActivity.getDarkSkin());
		
		//mine
		float text_size=getResources().getDimension(R.dimen.text_size_small);//mine
		float density=getResources().getDisplayMetrics().density;
		text_size=text_size/density;
		aMenu.setTextSize((int)text_size);

		//load the menu items
		ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

		menuItems.add(new alexMenuItem(R.id.search_item, 
				getString(R.string.search), dayMode?R.drawable.am_search_v:R.drawable.ram_search_v, true ));

		menuItems.add(new alexMenuItem(R.id.save_obs, 
				getString(R.string.export), dayMode?R.drawable.am_load_v:R.drawable.ram_load_v, true ));
		menuItems.add(new alexMenuItem(R.id.qshare,
				getString(R.string.share), dayMode?R.drawable.am_share_v:R.drawable.ram_share_v, true ));
		menuItems.add(new alexMenuItem(R.id.qfind,
				getString(R.string.find), dayMode?R.drawable.am_find_in_page_v:R.drawable.ram_find_in_page_v, true ));
		menuItems.add(new alexMenuItem(R.id.qnext,
				getString(R.string.next), dayMode?R.drawable.am_tplus_v:R.drawable.ram_tplus_v, true ));
		menuItems.add(new alexMenuItem(R.id.qch,
				getString(R.string.checkboxes), dayMode?R.drawable.am_check_v:R.drawable.ram_check_v, true ));
		
		menuItems.add(new alexMenuItem(R.id.qtoggle,
				getString(R.string.side_chart), dayMode?R.drawable.am_flip_v:R.drawable.ram_flip_v, true ));


		if (aMenu.isNotShowing()){
			try {
				aMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				InputDialog.message(QueryActivity.this,MENU_ERROR + e.getMessage(), 0).show();
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
		
		Log.d(TAG,"text size="+text_size);
		contextMenu.setTextSize((int)text_size);//contextMenu.setTextSize(18)
		contextMenu.makeFloat();

		//load the menu items
		ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

		menuItems.add(new alexMenuItem(R.id.qseepic, getString(R.string.show_image), 0, true ));
		menuItems.add(new alexMenuItem(R.id.add, getString(R.string.add_to_observation_list), 0, true ));
		menuItems.add(new alexMenuItem(R.id.addmarked, getString(R.string.add_marked_to_observation_list), 0, true ));
		menuItems.add(new alexMenuItem(R.id.addall, getString(R.string.add_all_to_observation_list), 0, true ));
		menuItems.add(new alexMenuItem(R.id.item1, getString(R.string.details), 0, true ));
		menuItems.add(new alexMenuItem(R.id.qaddnote, getString(R.string.add_note), 0, true ));
		menuItems.add(new alexMenuItem(R.id.qseenotes, getString(R.string.see_object_notes), 0, true ));
		menuItems.add(new alexMenuItem(R.id.qseeallnotes, getString(R.string.see_all_notes), 0, true ));

		if (contextMenu.isNotShowing()){
			try {
				contextMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				InputDialog.message(QueryActivity.this,MENU_ERROR2 + e.getMessage(), 0).show();
			}	
		}
		getListView().setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> arg0, View v,	int index, long arg3) {
				AstroObject obj=displayList.get(index).x;
				String dbname= DbManager.getDbName(obj.getCatalog());
				
				String prefix="";
				if(dbname!=null)
					prefix=dbname+": ";
				
				
				contextMenu.setMenuItemId(index);
				contextMenu.setHeader(prefix + ((TextView) v.findViewById(R.id.dsosel_ngcnum)).getText());
				contextMenu.show(v);
				return true;
			}

		});
	}

	public void MenuItemSelectedEvent(alexMenuItem selection) {
		parseMenu(selection.getId());
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Log.d(TAG,"keyCode=" + keyCode + " event=" + event);
		if (ALEX_MENU_FLAG && keyCode==KeyEvent.KEYCODE_MENU) {
			goMenu();
			return true;
		}
		else if(keyCode==KeyEvent.KEYCODE_BACK) {
			goBack();
			return true;
		}

		return super.onKeyDown(keyCode, event); 
	} 

	private void goMenu() {
		aMenu.show(mTextView);
	}
	private void goBack() {
		finish();
	}
	
	
	@Override
	protected int getTestActivityNumber(){
		return TestIntentService.QUERY;
	}
	
	int active_catalog=0;
	@Override
	protected void startTest(int action,int param){
		if(!Global.TEST_MODE)
			return;
		super.startTest(action,param);
		if(action!=TestIntentService.QUERY_LOG)
			active_catalog=action;
		switch(action){
		case TestIntentService.QUERY_NGCIC:
			testCatalog(R.string.select_catalog_ngcic);
			break;
		case TestIntentService.QUERY_MESSIER:
			testCatalog(R.string.select_catalog_messier);
			break;
		case TestIntentService.QUERY_CALDWELL:
			testCatalog(R.string.select_catalog_caldwell);
			break;
		case TestIntentService.QUERY_H400:
			testCatalog(R.string.select_catalog_hershell);
			break;
		case TestIntentService.QUERY_SAC:
			testCatalog(R.string.select_catalog_sac);
			break;	
		case TestIntentService.QUERY_UGC:
			testCatalog(R.string.select_catalog_ugc);
			break;	
		case TestIntentService.QUERY_PGC:
			testCatalog(R.string.select_catalog_pgc);
			break;	
		case TestIntentService.QUERY_LDN:
			testCatalog(R.string.select_catalog_ldn);
			break;	
		case TestIntentService.QUERY_BARNARD:
			testCatalog(R.string.select_catalog_barnard);
			break;	
		case TestIntentService.QUERY_LBN:
			testCatalog(R.string.select_catalog_lbn);
			break;	
		case TestIntentService.QUERY_SH2:
			testCatalog(R.string.select_catalog_sh2);
			break;	
		case TestIntentService.QUERY_PK:
			testCatalog(R.string.select_catalog_pk);
			break;	
		case TestIntentService.QUERY_ABELL:
			testCatalog(R.string.select_catalog_abell);
			break;	
		case TestIntentService.QUERY_HCG:
			testCatalog(R.string.select_catalog_hickson);
			break;	
		case TestIntentService.QUERY_HAAS:
			testCatalog(R.string.select_catalog_bright_ds);
			break;	
		case TestIntentService.QUERY_WDS:
			testCatalog(R.string.select_catalog_wds);
			break;	
		case TestIntentService.QUERY_COMETS:
			testCatalog(R.string.select_catalog_comet);
			break;	
		case TestIntentService.QUERY_MP:
			testCatalog(R.string.select_catalog_planet);
			break;	
		case TestIntentService.QUERY_TEST2:
			Log.d(TAG,"query test2");
			Set<Integer>set=new HashSet<Integer>();
			int first=getFirstCustomCatalog();
			if(first==-1)
				break;
			Log.d(TAG,"first="+first);
			set.add(first);
			SettingsActivity.saveCatalogSelectionPrefs(getApplicationContext(), set, SettingsActivity.DSO_SELECTION);
			testCatalog(R.string.select_catalog_up1);//actually clearing other catalogs
						
			
			break;
		case TestIntentService.QUERY_LOG:
			TestIntentService.print("test="+TestIntentService.getTestNumber()+" result="+(displayList.size()==param)+" size="+displayList.size()+" "+"active_catalog="+active_catalog);
			break;
		case TestIntentService.QUERY_EXPORT:
			parseMenu(R.id.save_obs);
			break;
		case TestIntentService.QUERY_ITEM_CLICK:
			onListItemClick(null,null,1,0);
			break;
		case TestIntentService.QUERY_IMAGE_TEST:

			Item item=displayList.get(0);
			List<String> list= DetailsActivity.getPicturePaths(getApplicationContext(),item.x);

			Bitmap image = PictureActivity.getBitmap(list.get(0),getApplicationContext()); //BitmapFactory.decodeFile(list.get(0));
			if(image!=null){
				TestIntentService.print("test="+TestIntentService.getTestNumber()+" result=true"+" image ok");

			}
			break;
		}
				
		
	}
	private int getFirstCustomCatalog(){
		InfoList list=ListHolder.getListHolder().get(InfoList.DB_LIST);
		for(Object o:list){
			DbListItem item=(DbListItem)o;
			if(item.cat>=AstroCatalog.NEW_CATALOG_FIRST)
				return item.cat;
		}
		return -1;
	}
	
	private void testCatalog(int cat){
		Context context=getApplicationContext();
		SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(context);
		//deselecting all catalogs
		int[] keys= SettingsActivity.getCatalogKeys(SettingsActivity.DSO_SELECTION);
		for(int key:keys){
			sh.edit().putBoolean(context.getString(key), false).commit();
		}
		//selecting the necessary one
		sh.edit().putBoolean(context.getString(cat), true).commit();
		
		Button btn=(Button)findViewById(R.id.qbtnfilter);
		btn.performClick();
		
	}
	
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
  	public void onLongPress(MotionEvent e) {
  	
  		
  	}
  	public void onShowPress(MotionEvent e) {}
  	public boolean onDown(MotionEvent e) {return true;}
  	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {return false;}
  	public boolean onSingleTapUp(MotionEvent e) {return false;}
  	//-----------
}
