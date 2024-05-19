package com.astro.dsoplanner;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.database.NoteDatabase;
import com.astro.dsoplanner.graph.GraphActivity;
import com.astro.dsoplanner.graph.GraphRec;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListCollectionFiller;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListStringSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.infolist.ObsListFiller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.OutputStream;
//list with observation notes


public class NoteListActivity extends ParentListActivity implements OnGestureListener {

    private static final String AUDIO = " audio";
    private static final String MENU_ERROR = "Menu error!";
    private static final String BUNDLE = "bundle";
    private static final String NOTES = "Notes";

    private static final int EXPORT_CODE = 1;

    int dbID = -1;
    int listID = -1;
    int ngc = 0;
    InputDialog pd = null;
    TextView tv;
    private boolean allNotes = false;//shows all notes or notes for one object
    private static final String TAG = NoteListActivity.class.getSimpleName();
    private boolean newcomment = false;
    Player mPlayer = null;

    private alexMenu aMenu;
    private alexMenu contextMenu;
    private boolean dirtyObsPref = false;

    private boolean showPics = false; //whether to show pics. set by updHandler

    InfoList noteList = new InfoListImpl(NOTES, NoteRecord.class);
    private HandlerThread workerThread2;
    private Handler workerHandler2;
    ExportData exportData;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXPORT_CODE && resultCode == RESULT_OK && data != null) {
            exportData.process(data);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (pd != null) pd.dismiss();
            updateListArray();

        }
    };

    class NoteListFiller implements InfoListFiller {
        private AstroCatalog catalog;
        List<NoteRecord> list = new ArrayList<NoteRecord>();

        /**
         * search for notes corresponding to the object
         *
         * @param obj
         */
        public NoteListFiller(AstroObject obj) {
            NoteDatabase db = new NoteDatabase();
            ErrorHandler eh = new ErrorHandler();
            db.open(eh);
            if (eh.hasError()) {
                eh.showError(NoteListActivity.this);
                return;
            }

            list = db.search(obj);

            db.close();

        }

        public NoteListFiller(String searchString) {
            NoteDatabase db = new NoteDatabase();
            ErrorHandler eh = new ErrorHandler();
            db.open(eh);
            if (eh.hasError()) {
                eh.showError(NoteListActivity.this);
                return;
            }
            list = db.searchContentInclusive(searchString);
            db.close();
        }

        public Iterator getIterator() {
            return list.iterator();
        }


    }

    private NLadapter mAdapter;
    Map<NoteRecord, AstroObject> map = new HashMap<NoteRecord, AstroObject>();//keeping refs to astro objects

    private class NLadapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private static final int NON_BOLD = 1;
        private static final int BOLD = 2;

        public NLadapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public View getView(int position, View convertView, ViewGroup parent) {

            boolean remake = false;//remake convert view if its structure is different from the one indicated by shows pics

            if (convertView != null) {
                ImageView iv = (ImageView) convertView.findViewById(R.id.note_image);
                if (showPics) {
                    remake = (iv == null);
                } else {
                    remake = (iv != null);
                }
            }
            Log.d(TAG, "convertView=" + convertView + " remake=" + remake + " show=" + showPics);
            if (convertView == null || remake) {
                convertView = mInflater.inflate(showPics ? R.layout.notelist_item : R.layout.notelist_item_no_pic, null);

            }

            NoteRecord rec = (NoteRecord) noteList.get(position);

            if (showPics) {
                AstroObject obj = null;
                if (map.containsKey(rec)) {
                    obj = map.get(rec);

                    if (obj != null) {
                        ObservationListActivity.setImage(getApplicationContext(), convertView, obj, handler, R.id.note_image, workerHandler2, true);
                    }

                }
                if (obj == null) {
                    ImageView iv = (ImageView) convertView.findViewById(R.id.note_image);
                    iv.setTag(rec);//every convert view should have a tag!!!otherwise
                    //it may get a convert view with an object tag which it does not override!
                    iv.setImageBitmap(null);
                    iv.setVisibility(View.GONE);
                }

            }

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(rec.date);
            String s = DetailsActivity.makeDateString(c, true) + " " + DetailsActivity.makeTimeString(c, false);
            if (!rec.path.isEmpty()) s = s + AUDIO;
            ((TextView) convertView.findViewById(R.id.notelist_datetime)).setText(s);

            ((TextView) convertView.findViewById(R.id.notelist_dso)).setText("" + rec.name);
            ((TextView) convertView.findViewById(R.id.notelist_note)).setText("" + rec.note);

            //make dark background
            if (SettingsActivity.getDarkSkin() || SettingsActivity.getNightMode())
                convertView.setBackgroundColor(0xff000000);

            return convertView;
        }

        public int getCount() {
            Log.d(TAG, "getCount=" + noteList.getCount());
            return noteList.getCount();
        }

        public Object getItem(int position) {
            return noteList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }
    }

    /**
     * prepares and sets astro images in asynchronous way
     * every convert view should have a tag even if it does not have a picture!!!
     * otherwise
     *
     * @param view    - view to take image view from
     * @param obj     - object for which make picture
     * @param handler - handler from ui thread
     * @param id      - image view resource id
     */
    public static void setImage(Context context, final View view, final AstroObject obj, final Handler handler, int id) {
        final List<String> list = DetailsActivity.getPicturePaths(context, obj);
        final ImageView iv = (ImageView) view.findViewById(id);
        iv.setTag(obj);//needed to check later if this view belongs to somebody else
        if (list.size() == 0) {
            iv.setImageBitmap(null);
            return;
        }
        Runnable r = new Runnable() {
            public void run() {
                final Bitmap image = PictureActivity.getBitmap(list.get(0), context);  //BitmapFactory.decodeFile(list.get(0));
                if (image != null) {


                    handler.post(new Runnable() {
                        public void run() {
                            if (iv == null) return;//the view may have been killed
                            Object tag = iv.getTag();
                            if (tag == null)
                                return;
                            if (!obj.equals(tag))//this is to check if this view now belongs to another object)))
                                return;
                            iv.setImageBitmap(image);
                            if (SettingsActivity.getNightMode()) {
                                ColorFilter filter = new LightingColorFilter(0xffff0000, 1);
                                iv.setColorFilter(filter);
                            } else
                                iv.setColorFilter(null);
                        }
                    });

                }
                return;
            }
        };
        new Thread(r).start();
    }


    private static final int SHOW_PIC = 1;
    private static final int DO_NOT_SHOW_PIC = 2;
    Handler updHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.arg1) {
                case SHOW_PIC:
                    showPics = true;
                    break;
                case DO_NOT_SHOW_PIC:
                    showPics = false;
                    break;
            }
            mAdapter.notifyDataSetChanged();
        }
    };

    /**
     * updates map<NoteRecord,AstroObject> where NoteRecord corresponds to AstroObject
     * this map is needed to draw pictures quickly
     * and to determine if there are pics at all
     * the map is built in a non ui thread as it is a time consuming process not to be done in UI
     * especially for long note lists
     *
     * @author leonid
     */
    class UpdatingThread extends Thread {
        //Map<NoteRecord,AstroObject>map=new HashMap<NoteRecord,AstroObject>();
        List<NoteRecord> list = new ArrayList<NoteRecord>();
        Handler handler;

        public UpdatingThread(Handler handler) {
            this.handler = handler;
            for (Object o : noteList) {
                list.add((NoteRecord) o);
            }
        }

        public void run() {
            NoteDatabase db = new NoteDatabase(NoteListActivity.this);
            ErrorHandler eh = new ErrorHandler();
            db.open(eh);
            if (eh.hasError())
                return;
            for (NoteRecord rec : list) {
                AstroObject obj = db.getObject(rec, eh);
                if (obj != null)
                    map.put(rec, obj);
            }

            boolean showpic = false;

            if (areImagesOn()) {
                for (Map.Entry<NoteRecord, AstroObject> e : map.entrySet()) {
                    List<String> list = DetailsActivity.getPicturePaths(getApplicationContext(), e.getValue());
                    Log.d(TAG, "upd thread, obj=" + e.getValue() + " size=" + list.size());
                    if (list.size() > 0)
                        showpic = true;
                }
            }


            Message msg = new Message();
            if (showpic)
                msg.arg1 = SHOW_PIC;
            else
                msg.arg1 = DO_NOT_SHOW_PIC;

            msg.obj = map;
            handler.sendMessage(msg);
        }

    }

    NoteRequest request;
    private Button bN;

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null) mPlayer.release();
        mPlayer = null;
        if (dirtyObsPref) {
            int obsList = SettingsActivity.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

            new Prefs(this).saveList(obsList);
            dirtyObsPref = false;
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "request=" + request);
        onResumeCode();
        super.onResume();
    }

    private void onResumeCode() {
        initList();
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    int answer = NOT_DEFINED;
    final static int NOT_DEFINED = 0;
    final static int SHOW_NOTES_WITH_NAMES = 1;
    final static int DONOT_SHOW_NOTES_WITH_NAMES = 2;
    List<NoteRecord> list;//=new ArrayList<NoteRecord>();
    List<NoteRecord> list2;//=

    private void initList() {
        list = new ArrayList<NoteRecord>();
        list2 = new ArrayList<NoteRecord>();
        noteList.removeAll();

        NoteDatabase db = new NoteDatabase();
        ErrorHandler eh = new ErrorHandler();
        db.open(eh);
        if (eh.hasError()) {
            eh.showError(NoteListActivity.this);
            return;
        }

        if (request.action == NoteRequest.SEARCH_NOTES) {
            list = db.searchContentInclusive(request.record.name);
            noteList.fill(new InfoListCollectionFiller(list));
            updateList();
        } else {
            list = db.search(request.obj);//null to see all notes
            noteList.fill(new InfoListCollectionFiller(list));
            updateList();
        }
        db.close();

    }

    private void updateList() {
        switch (currentSort) {
            case SORT_TIME:
                noteList.sort(cmp);
                break;
            case SORT_NAME:
                noteList.sort(cmpS);
                break;
        }
        mAdapter.notifyDataSetChanged();
        tv.setText("" + noteList.getCount() + getString(R.string._notes));

        for (Object o : noteList) {
            Log.d(TAG, "o=" + o);
        }

        new UpdatingThread(updHandler).start();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = getIntent().getBundleExtra(BUNDLE);
            if (b == null) {
                finish();
                return;
            }
            request = new NoteRequest(b);


            if (request.action == NoteRequest.GET_ALL_NOTES)
                setContentView(R.layout.notelist2);
            else
                setContentView(R.layout.notelist);

            if (ImportDatabaseIntentService.isBeingImported(Constants.NOTE_DATABASE_NAME)) {
                InputDialog.toast(getString(R.string.import_for_this_database_is_running_), NoteListActivity.this).show();
                finish();
                return;
            }
            workerThread2 = new HandlerThread("");
            workerThread2.start();
            workerHandler2 = new Handler(workerThread2.getLooper());

            tv = (TextView) findViewById(R.id.text_notelist);
            mAdapter = new NLadapter();
            setListAdapter(mAdapter);

            if (ALEX_MENU_FLAG) {
                initAlexMenu();
                initAlexContextMenu();
            } else
                registerForContextMenu(getListView()); //old style


            final Activity a = NoteListActivity.this;
            OnClickListener oclNote = new OnClickListener() {
                public void onClick(View v) {
                    if (request.obj == null)
                        return;
                    Command command = new NewNoteCommand(NoteListActivity.this, request.obj, Calendar.getInstance(), "");
                    command.execute();
                }
            };

            bN = (Button) findViewById(R.id.bNoteListNewNote); //SAND: need one global view (was under if before
            if (request.action == NoteRequest.GET_OBJECT_NOTES) {
                //adding new note for the object on button click
                bN.setOnClickListener(oclNote);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        exportData = new ExportData(this, EXPORT_CODE, "");
        initHandler.handleMessage(null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notelist_menu, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return parseMenu(item.getItemId());
    }

    //SORT
    int orderTime = 1;//sort order by time
    int orderName = 1;//sort order by name

    private static final int SORT_TIME = 2;
    private static final int SORT_NAME = 3;
    int currentSort = SORT_TIME;
    //sort by time
    Comparator cmp = new Comparator() {
        public int compare(Object lhs, Object rhs) {
            if (lhs instanceof NoteRecord && rhs instanceof NoteRecord) {
                NoteRecord lhsRec = (NoteRecord) lhs;
                NoteRecord rhsRec = (NoteRecord) rhs;
                if (lhsRec.date == rhsRec.date) return 0;
                if (lhsRec.date < rhsRec.date) return -1 * orderTime;
                return orderTime;
            }
            throw new ClassCastException();
        }
    };
    //sort by name
    Comparator cmpS = new Comparator() {
        public int compare(Object lhs, Object rhs) {
            if (lhs instanceof NoteRecord && rhs instanceof NoteRecord) {
                NoteRecord lhsRec = (NoteRecord) lhs;
                NoteRecord rhsRec = (NoteRecord) rhs;
                return lhsRec.name.compareTo(rhsRec.name) * orderName;
            }
            throw new ClassCastException();
        }
    };


    private boolean areImagesOn() {
        return SettingsActivity.getSharedPreferences(getApplicationContext()).getBoolean(Constants.SHOW_NOTELIST_IMAGES, true);
    }

    private void setShowImagesFlag(boolean flag) {
        SettingsActivity.putSharedPreferences(Constants.SHOW_NOTELIST_IMAGES, flag, getApplicationContext());
    }


    public boolean parseMenu(int id) {
        int order;
        switch (id) {
            case R.id.images_notelist_menu:
                setShowImagesFlag(!areImagesOn());
                updateList();
                return true;
            case R.id.export_notelist_menu:
                final ExportData.Predicate<OutputStream> r = outstream -> {

                    InfoListSaver saver = new InfoListStringSaverImp(outstream);

                    try {
                        boolean noError = noteList.save(saver);
                        return noError;
                    } catch (Exception ignore) {
                        return false;
                    } finally {
                        try {
                            saver.close();
                        } catch (Exception e) {
                        }
                    }
                };
                exportData.setCodeToRun(r);
                exportData.start();
                return true;

            case R.id.paste_notelist_menu:
                startPasting();
                return true;

            case R.id.import_notelist_menu:
                IPickFileCallback listener = new IPickFileCallback() {

                    public void callbackCall(Uri uri) {

                        SettingsActivity.persistUriIfNeeded(uri, getApplicationContext());
                        importFrom(uri.toString());
                    }

                    public void importFrom(String importLocation) {
                        InputDialog d = new InputDialog(NoteListActivity.this);
                        d.setMessage(getResources().getString(R.string.did_you_export));
                        d.setPositiveButton(getString(R.string.yes), new InputDialog.OnButtonListener() {

                            @Override
                            public void onClick(String value) {
                                startImporting(importLocation, false);//do not ignore custom db refs

                            }
                        });
                        d.setNegativeButton(getString(R.string.no), new InputDialog.OnButtonListener() {

                            @Override
                            public void onClick(String value) {
                                startImporting(importLocation, true);//ignore custom db refs

                            }
                        });
                        registerDialog(d).show();

                    }
                };


                SelectFileActivity.setPath(SettingsActivity.getFileDialogPath(getApplicationContext()));
                SelectFileActivity.setListener(listener);
                Intent fileDialog = new Intent(this, SelectFileActivity.class);
                startActivity(fileDialog);


                return true;
            case R.id.share_notelist_menu:
                if (SettingsActivity.nightGuard(this)) return true;

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InfoListSaver saver = new InfoListStringSaverImp(out, Global.SHARE_LINES_LIMIT, new Handler());
                noteList.save(saver);
                String s = out.toString();
                new ShareCommand(this, s).execute();
                return true;
            case R.id.time_notelist_menu:
                orderTime = -orderTime;
                noteList.sort(cmp);

                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.ngc_notelist_menu:
                orderName = -orderName;
                noteList.sort(cmpS);

                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.search_notelist_menu:
                InputDialog d1 = new InputDialog(NoteListActivity.this);
                d1.setType(InputDialog.DType.INPUT_STRING);
                d1.setTitle(getString(R.string.search_note_database_for_the_string_));
                d1.setValue("");

                d1.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        if (!value.isEmpty())
                            new SearchNotes(NoteListActivity.this, value).execute();
                    }
                });
                d1.setNegativeButton(getString(R.string.cancel));
                registerDialog(d1).show();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notelist_context_menu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        return parseContextMenu(item.getItemId(), (int) info.id) ? true : super.onContextItemSelected(item);
    }

    private boolean parseContextMenu(int itemId, int id) {
        final NoteRecord rec = (NoteRecord) noteList.get(id);
        Log.d(TAG, "rec=" + rec);
        NoteRequest request;
        Command command;
        ErrorHandler eh = new ErrorHandler();
        switch (itemId) {
            case R.id.notelist_play:
                Log.d(TAG, "playing, rec=" + rec);
                boolean playing = false;
                if (!rec.path.isEmpty()) {//there is some recording path
                    final File f = new File(Global.notesPath, rec.path);
                    if (f.exists()) {
                        mPlayer = new Player(f.getAbsolutePath());
                        mPlayer.startPlaying();
                        playing = true;
                    }
                }
                if (!playing)
                    registerDialog(InputDialog.message(NoteListActivity.this, R.string.there_is_no_audio_for_this_note, 0)).show();
                return true;
            case R.id.picture_notelist_menu:
                command = new PictureCommand(this, rec);
                command.execute();
                return true;
            case R.id.detail_notelist_menu:
                AstroObject obj = new NoteDatabase().getObject(rec, eh);
                Log.d(TAG, "obj=" + obj + " rec=" + rec);
                if (eh.hasError())
                    eh.showError(this);
                if (obj == null) return true;
                new DetailsCommand(obj, this).execute();
                return true;
            case R.id.notelist_graph:
                final AstroObject obj1 = new NoteDatabase().getObject(rec, eh);
                if (eh.hasError())
                    eh.showError(this);
                if (obj1 == null) {
                    registerDialog(InputDialog.message(this, R.string.this_action_could_not_be_performed_for_temporary_object_note, 0)).show();
                    return true;
                }
                goToStarChart(obj1, rec);


                return true;
            case R.id.remove_notelist_menu:
                class DialogImplRemove implements InputDialog.OnButtonListener {
                    private NoteRecord nr;//position in NoteDatabase
                    private int listID;//position in NoteList

                    public DialogImplRemove(NoteRecord nr, int listID) {
                        this.nr = nr;
                        this.listID = listID;
                    }

                    public void onClick(String v) {

                        noteList.remove(listID);
                        NoteDatabase db = new NoteDatabase();
                        ErrorHandler eh = new ErrorHandler();
                        db.open(eh);
                        if (eh.hasError()) {
                            eh.showError(NoteListActivity.this);
                            return;
                        }
                        db.remove(nr);
                        db.close();
                        updateListArray();
                    }
                }


                int listID = id;

                InputDialog dl = new InputDialog(NoteListActivity.this);
                dl.setTitle(getString(R.string.note_list_confirmation));
                dl.setMessage(getString(R.string.do_you_want_to_remove_the_note_));
                dl.setPositiveButton(getString(R.string.ok), new DialogImplRemove(rec, listID));
                dl.setNegativeButton(getString(R.string.cancel));
                registerDialog(dl).show();


                return true;
            case R.id.removeall_notelist_menu:
                class DialogImplRemoveAll implements InputDialog.OnButtonListener {
                    private Set<NoteRecord> nrSet;
                    private Set<Integer> listIDSet;//position in NoteList

                    public void onClick(String v) {
                        Iterator it = noteList.iterator();
                        NoteDatabase db = new NoteDatabase();
                        ErrorHandler eh = new ErrorHandler();
                        db.open(eh);
                        if (eh.hasError()) {
                            eh.showError(NoteListActivity.this);
                            return;
                        }
                        for (; it.hasNext(); ) {
                            NoteRecord nr = (NoteRecord) it.next();
                            db.remove(nr);
                        }
                        db.close();
                        noteList.removeAll();
                        updateListArray();
                    }
                }


                InputDialog dl2 = new InputDialog(NoteListActivity.this);
                dl2.setTitle(getString(R.string.note_list_confirmation));
                dl2.setMessage(getString(R.string.do_you_want_to_remove_all_of_the_notes_));
                dl2.setPositiveButton(getString(R.string.ok), new DialogImplRemoveAll());
                dl2.setNegativeButton(getString(R.string.cancel));
                registerDialog(dl2).show();

                return true;
            case R.id.addall_notelist_menu:
                NoteDatabase db = new NoteDatabase();
                db.open(eh);
                if (eh.hasError()) {
                    eh.showError(NoteListActivity.this);
                    return true;
                }
                List<NoteRecord> list1 = new ArrayList<NoteRecord>();
                Iterator it = noteList.iterator();
                for (; it.hasNext(); ) {
                    list1.add((NoteRecord) it.next());
                }
                List<AstroObject> list2 = db.getObjects(list1, eh);
                if (eh.hasError()) {
                    eh.showError(NoteListActivity.this);

                }
                InfoListFiller filler = new ObsListFiller(list2);
                int obsList = SettingsActivity.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
                InfoList iL = ListHolder.getListHolder().get(obsList);
                iL.fill(filler);
                dirtyObsPref = true;
            default:
                return false;
        }

    }

    private void goToStarChart(final AstroObject obj1, final NoteRecord rec) {
        InputDialog dimp = new InputDialog(NoteListActivity.this);
        dimp.setMessage(getString(R.string.would_you_like_to_set_the_time_and_location_as_recorded_with_this_note_));
        dimp.setPositiveButton(getString(R.string.yes), new InputDialog.OnButtonListener() {
            public void onClick(String v) {
                SettingsActivity.putSharedPreferences(Constants.GRAPH_OBJECT, obj1, getApplicationContext());
                int zoom = SettingsActivity.getSharedPreferences(getApplicationContext()).getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);
                long millis = rec.date;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(millis);
                Point.setLST(AstroTools.sdTime(calendar));
                new GraphRec(zoom, obj1.getAz(), obj1.getAlt(), calendar, obj1).save(getApplicationContext());//add graph settings for Graph Activity to process it

                Intent i = new Intent(NoteListActivity.this, GraphActivity.class);
                i.putExtra(Constants.GRAPH_CALLING, Constants.NOTE_LIST_GRAPH_CALLING);
                startActivity(i);
            }
        });
        dimp.setNegativeButton(getString(R.string.no), new InputDialog.OnButtonListener() {
            public void onClick(String v) {
                SettingsActivity.putSharedPreferences(Constants.GRAPH_OBJECT, obj1, getApplicationContext());
                int zoom = SettingsActivity.getSharedPreferences(getApplicationContext()).getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);

                Point.setLST(AstroTools.sdTime(AstroTools.getDefaultTime(getApplicationContext())));
                new GraphRec(zoom, obj1.getAz(), obj1.getAlt(), AstroTools.getDefaultTime(getApplicationContext()), obj1).save(getApplicationContext());//add graph settings for Graph Activity to process it

                Intent i = new Intent(NoteListActivity.this, GraphActivity.class);

                startActivity(i);
            }
        });
        registerDialog(dimp).show();
    }

    private void startImporting(String importLocation, boolean ignoreCustomDbRefs) {
        Intent intent = new Intent(this, ImportDatabaseIntentService.class);

        intent.putExtra(Constants.IDIS_DBNAME, Constants.NOTE_DATABASE_NAME);
        intent.putExtra(Constants.IDIS_PASTING, false);
        intent.putExtra(Constants.IDIS_FILENAME, importLocation);
        intent.putExtra(Constants.IDIS_NOTES, true);
        intent.putExtra(Constants.IDIS_IGNORE_NGCIC_REF, false);
        intent.putExtra(Constants.IDIS_IGNORE_CUSTOMDB_REF, ignoreCustomDbRefs);
        ImportDatabaseIntentService.registerImportToService(Constants.NOTE_DATABASE_NAME);
        try {
            startService(intent);
        } catch (Exception ignored) {
        }
        finish();
    }


    private void startPasting() {
        Intent intent = new Intent(this, ImportDatabaseIntentService.class);

        intent.putExtra(Constants.IDIS_DBNAME, Constants.NOTE_DATABASE_NAME);
        intent.putExtra(Constants.IDIS_PASTING, true);
        intent.putExtra(Constants.IDIS_NOTES, true);
        intent.putExtra(Constants.IDIS_IGNORE_NGCIC_REF, false);
        intent.putExtra(Constants.IDIS_IGNORE_CUSTOMDB_REF, true);
        ImportDatabaseIntentService.registerImportToService(Constants.NOTE_DATABASE_NAME);
        try {
            startService(intent);
        } catch (Exception e) {
        }

        finish();
    }

    private void updateListArray() {

        mAdapter.notifyDataSetChanged();
        tv.setText("" + noteList.getCount() + getString(R.string._notes));
        new UpdatingThread(updHandler).start();

    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        NoteRecord rec = (NoteRecord) noteList.get(position);
        NoteRequest request = new NoteRequest(rec);
        Intent i = new Intent(this, NoteActivity.class);
        i.putExtra(BUNDLE, request.getBundle());
        startActivity(i);


    }

    //overriding menu button
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ALEX_MENU_FLAG && keyCode == KeyEvent.KEYCODE_MENU) {
            aMenu.show(tv);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initAlexMenu() {

        boolean dayMode = !nightMode;

        aMenu = new alexMenu(this, new OnMenuItemSelectedListener() {
            public void MenuItemSelectedEvent(alexMenuItem selection) {
                parseMenu(selection.getId());
            }
        },
                getLayoutInflater());
        aMenu.setHideOnSelect(true);
        aMenu.setItemsPerLineInPortraitOrientation(4);
        aMenu.setItemsPerLineInLandscapeOrientation(4);
        aMenu.setSkin(nightMode, SettingsActivity.getDarkSkin());
        //mine
        float text_size = getResources().getDimension(R.dimen.text_size_small);//mine
        float density = getResources().getDisplayMetrics().density;
        text_size = text_size / density;
        aMenu.setTextSize((int) text_size);
        //load the menu items
        ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

        menuItems.add(new alexMenuItem(R.id.export_notelist_menu,
                getString(R.string.export), dayMode ? R.drawable.am_load_v : R.drawable.ram_load_v, true));
        menuItems.add(new alexMenuItem(R.id.import_notelist_menu,
                getString(R.string.import2), dayMode ? R.drawable.am_save_v : R.drawable.ram_save_v, true));
        menuItems.add(new alexMenuItem(R.id.ngc_notelist_menu,
                getString(R.string.name2), dayMode ? R.drawable.am_sort_v : R.drawable.ram_sort_v, true));
        menuItems.add(new alexMenuItem(R.id.time_notelist_menu,
                getString(R.string.note_time), dayMode ? R.drawable.am_sort_v : R.drawable.ram_sort_v, true));
        menuItems.add(new alexMenuItem(R.id.paste_notelist_menu,
                getString(R.string.paste), dayMode ? R.drawable.am_paste_v : R.drawable.ram_paste_v, true));
        menuItems.add(new alexMenuItem(R.id.share_notelist_menu,
                getString(R.string.share_all), dayMode ? R.drawable.am_share_v : R.drawable.ram_share_v, true));
        menuItems.add(new alexMenuItem(R.id.search_notelist_menu,
                getString(R.string.search_text), dayMode ? R.drawable.am_search_v : R.drawable.ram_search_v, true));

        menuItems.add(new alexMenuItem(R.id.images_notelist_menu,
                getString(R.string.images), dayMode ? R.drawable.am_image_v : R.drawable.ram_image_v, true));

        if (aMenu.isNotShowing()) {
            try {
                aMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog alert = new InputDialog(NoteListActivity.this);
                alert.show(MENU_ERROR, e.getMessage());
            }
        }
    }

    private void initAlexContextMenu() {
        contextMenu = new alexMenu(this, new OnMenuItemSelectedListener() {
            public void MenuItemSelectedEvent(alexMenuItem selection) {
                parseContextMenu(selection.getId(), contextMenu.getMenuItemId());
            }
        },
                getLayoutInflater());
        contextMenu.setHideOnSelect(true);
        contextMenu.setItemsPerLineInPortraitOrientation(1);
        contextMenu.setItemsPerLineInLandscapeOrientation(1);
        contextMenu.setSkin(nightMode, SettingsActivity.getDarkSkin());

        //mine
        float text_size = getResources().getDimension(R.dimen.table_main_text_size);//mine
        float density = getResources().getDisplayMetrics().density;
        text_size = text_size / density;
        contextMenu.setTextSize((int) text_size);//contextMenu.setTextSize(18)

        contextMenu.makeFloat();

        //load the menu items
        ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

        menuItems.add(new alexMenuItem(R.id.notelist_graph, getString(R.string.star_chart), 0, true));
        menuItems.add(new alexMenuItem(R.id.notelist_play, getString(R.string.play), 0, true));
        menuItems.add(new alexMenuItem(R.id.picture_notelist_menu, getString(R.string.show_image), 0, true));
        menuItems.add(new alexMenuItem(R.id.detail_notelist_menu, getString(R.string.details), 0, true));
        menuItems.add(new alexMenuItem(R.id.remove_notelist_menu, getString(R.string.remove), 0, true));
        menuItems.add(new alexMenuItem(R.id.removeall_notelist_menu, getString(R.string.remove_all), 0, true));
        menuItems.add(new alexMenuItem(R.id.addall_notelist_menu, getString(R.string.add_all_to_observation_list), 0, true));
        if (contextMenu.isNotShowing()) {
            try {
                contextMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(NoteListActivity.this, "Menu error! " + e.getMessage(), 0).show();
            }
        }
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v, int index, long arg3) {
                contextMenu.setMenuItemId(index);
                contextMenu.setHeader(((TextView) v.findViewById(R.id.notelist_dso)).getText());
                contextMenu.show(v);
                return true;
            }

        });
    }

    protected void onDestroy() {
        try {
            aMenu.hide();
            contextMenu.hide();
        } catch (Exception e) {
        }
        super.onDestroy();

        //fix Null pointer exception bug???
		/* Free. v43
		Caused by: java.lang.NullPointerException:
         at com.astro.dsoplanner.AZST22.onDestroy (AZST22.java:15)
		 */
        try {
            workerThread2.getLooper().quit();
        } catch (Exception e) {
        }
    }

    //Gesture Detector (just implement OnGestureListener in the Activity)
    GestureDetector gDetector = new GestureDetector(this);

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return gDetector.onTouchEvent(me);
    }

    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
        if (start == null || finish == null) return false;
        float dy = start.getRawY() - finish.getRawY();
        float dx = start.getRawX() - finish.getRawX();
        if (dy > Global.flickLength) { //up
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
            return true;
        } else if (dx > Global.flickLength) { //left
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            return true;
        }
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    //-----------
}
