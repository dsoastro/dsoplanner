package com.astro.dsoplanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.astro.dsoplanner.InputDialog.OnButtonListener;
import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.database.Db;
import com.astro.dsoplanner.database.TelescopeDatabase;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListStringLoaderImp;
import com.astro.dsoplanner.infolist.InfoListStringSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.io.OutputStream;
import java.io.IOException;

public class EyepiecesListActivity extends ParentListActivity implements OnGestureListener {

    private static final int SELECTOR_ACTIVITY = 5;
    private static final int EXPORT_CODE = 6;
    public static final int OFFSET = 2000;
    private static final String FOV = "fov";
    private static final String LENGTH = "length";
    private static final String MAN = "man";
    private static final String SELECT_NAME_LENGTH_FOV_FROM_LIST_WHERE_ROWID = "select name,length,fov from list where rowid=";

    private final int MENU_IMPORT = 1;
    private final int MENU_EXPORT = 2;
    private final int MENU_EDIT = 3;
    private final int MENU_REMOVE = 4;
    private final int MENU_REMOVEALL = 5;
    private final int MENU_ADD_CCD = 6;

    public final static int MAX_FREE_EP = 3;
    private int callerId = 0;

    // Current telescope's ep_id if any
    // -1 - if it was not called for a particular telescope,
    private int curTelescopeId = -1;
    private int curScopeEPid = -1;
    private int maxEPid = 0; //last EPid number discovered in the db
    private boolean dirty = false; //go dirty if any checkbnoxes are changed

    private Button bN;
    private alexMenu aMenu;
    private alexMenu contextMenu;
    private ELadapter mAdapter;
    EyepiecesRecord rec;

    EpDialog d;
    private static final String TAG = EyepiecesListActivity.class.getSimpleName();
    String action = null;
    boolean action_selected = false;
    ExportData exportData;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECTOR_ACTIVITY && resultCode == RESULT_OK) {
            int rowid = data.getIntExtra(Constants.SELECTOR_RESULT, -1);
            if (rowid != -1) {
                try {
                    Db db = new Db(getApplicationContext(), Constants.EP_DB);
                    db.open();
                    Cursor cursor = db.rawQuery(SELECT_NAME_LENGTH_FOV_FROM_LIST_WHERE_ROWID + rowid);
                    if (cursor.moveToNext()) {
                        String name = cursor.getString(0);
                        double length = cursor.getFloat(1);
                        double fov = cursor.getFloat(2);
                        d.setName(name);
                        d.setFocalLength("" + String.format(Locale.US, "%.1f", length));
                        d.setFOV("" + String.format(Locale.US, "%.1f", fov));
                    }
                    db.close();
                } catch (Exception e) {
                }
            }
        } else if (requestCode == EXPORT_CODE && resultCode == RESULT_OK && data != null) {
            exportData.process(data);
        }
    }

    class Item {
        EyepiecesRecord rec;
        boolean marked;

        public Item(EyepiecesRecord rec, boolean marked) {
            this.rec = rec;
            this.marked = marked;
        }
    }

    List<Item> itemlist = new ArrayList<Item>();

    private void fillItemList() {
        EyepiecesDatabase db = new EyepiecesDatabase();
        db.open();
        List<EyepiecesRecord> list = db.search();
        for (EyepiecesRecord rec : list) {

            boolean check = false;
            if (curScopeEPid >= 0) { //called for a particular scope
                for (String si : rec.getEp_id().split("[,]")) {
                    if (si.length() == 0) break; //stupid java!
                    int eid = AstroTools.getInteger(si, 0, 0, 1000);
                    if (eid > maxEPid)
                        maxEPid = eid; //collect last id used look for maximum eyep id to assign it later to a telescope without eyep id
                    if (curScopeEPid == eid) { //found, set checkbox
                        check = true;
                    }
                }
            }

            itemlist.add(new Item(rec, check));
            Log.d(TAG, "item=" + rec + " " + check);
        }
        db.close();
    }

    private void clearItemList() {
        itemlist = new ArrayList<Item>();
    }

    class EyepiecesListFiller implements InfoListFiller {
        List<EyepiecesRecord> list = new ArrayList<EyepiecesRecord>();

        public EyepiecesListFiller() {
            EyepiecesDatabase db = new EyepiecesDatabase();
            db.open();
            list = db.search();
            db.close();
        }

        public Iterator getIterator() {
            return list.iterator();
        }
    }

    private class ELadapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private InfoList list = null; //ListHolder.getListHolder().get(InfoList.EYEPIECES_LIST);
        private int[] checks = null;
        private int eyepiecesCounter = 0; //counts checkboxes

        public ELadapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.eyepieceslist_item, null);
            }

            Item item = itemlist.get(position);
            EyepiecesRecord rec = item.rec;
            String summary = rec.getSummary();
            ((TextView) convertView.findViewById(R.id.ep_name)).setText(summary);
            ((TextView) convertView.findViewById(R.id.ep_params)).setText("" + rec.getNote());

            //make dark background
            if (SettingsActivity.getDarkSkin() || SettingsActivity.getNightMode())
                convertView.setBackgroundColor(0xff000000);

            CheckBox ch = (CheckBox) convertView.findViewById(R.id.ep_selected);
            ch.setChecked(item.marked);

            if (action_selected) {
                ch.setEnabled(false);
            } else if (callerId == 1) //View only, remove the checkbox
                ch.setVisibility(View.GONE);

            return convertView;
        }

        public int getCount() {
            return itemlist.size();
        }

        public Object getItem(int position) {
            return itemlist.get(position);
        }

        public long getItemId(int position) {
            return position;
        }
    }

    private void saveItemList() {
        if (dirty && curScopeEPid >= 0) {
            saveUserSelection();
            dirty = false;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        saveItemList();

    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exportData = new ExportData(this, EXPORT_CODE, "");

        callerId = getIntent().getIntExtra(Global.EPDATABASE_CALLER, 0);
        action = getIntent().getAction();
        action_selected = Constants.ACTION_EP_SELECT.equals(action);
        if (action != null && (action.contentEquals(Constants.ACTION_EP_SHOW)))
            callerId = 1; //show only
        else if (action_selected) callerId = 0;

        switch (callerId) {
            case 0: //from Graph. Use current telescope
                curScopeEPid = SettingsActivity.getTEPid(); //get current telescope (will be -1 if nothing selected)
                curTelescopeId = SettingsActivity.getTId(); //get current telescope (will be -1 if nothing selected)
                break;
            case 1: //from TelescopeList. Just show the list
                curScopeEPid = -1;
                curTelescopeId = -1;
                break;
            case 2: //from TelescopeList. Get selected telescope
                curScopeEPid = getIntent().getIntExtra(TelescopeRecord.TEP, -1);
                curTelescopeId = getIntent().getIntExtra(TelescopeRecord.TID, -1);
        }

        Log.d(TAG, "caller=" + callerId + " curScope=" + curTelescopeId + "curEPid =" + curScopeEPid);

        setContentView(R.layout.activity_eyepieces_list);

        if (callerId == 0 && curTelescopeId < 1) { //call from Graph but no current telescope selected or default scope in use (empty scope db)
            registerDialog(InputDialog.abort(this, getString(R.string.you_can_not_change_eyepieces_parameters_now))).show();
        } else {
            //Set List view
            mAdapter = new ELadapter();
            setListAdapter(mAdapter);
            updateListArray();
            initAlexMenu();
            initAlexContextMenu();

            //add new Eyepiece button
            bN = (Button) findViewById(R.id.ep_new);
            bN.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    InputDialog d1 = new InputDialog(EyepiecesListActivity.this);
                    d1.setValue("-1");
                    d1.setTitle(getString(R.string.select_eyepiece_ccd));
                    d1.setListItems(new String[]{getString(R.string.eyepiece2), getString(R.string.ccd)}, new InputDialog.OnButtonListener() {
                        public void onClick(String value) {
                            int which = AstroTools.getInteger(value, 0, -1, 10000);
                            TYPE type;
                            switch (which) {
                                case 0:
                                    type = TYPE.EYEPIECE;
                                    break;
                                case 1:
                                    type = TYPE.CCD;
                                    break;
                                default:
                                    return;
                            }
                            d = new EpDialog(EyepiecesListActivity.this, type);
                            d.buildNew();
                            registerDialog(d).show();

                        }
                    });
                    registerDialog(d1).show();
                }
            });
        }
    }

    /**
     * Type of the Eyepiece
     */
    enum TYPE {
        EYEPIECE, CCD
    }

    private class EpDialog extends InputDialog {
        TYPE type;

        public EpDialog(Context context, TYPE type) {
            super(context);
            this.type = type;
        }

        public void setName(String name) {
            EditText e = (EditText) findViewById(R.id.ep_name);
            e.setText(name);
        }

        public void setFocalLength(String length) {
            EditText e = (EditText) findViewById(R.id.ep_f);
            e.setText(length);
        }

        public void setFOV(String fov) {
            EditText e = (EditText) findViewById(R.id.ep_afov);
            e.setText(fov);
        }

        public void buildNew() {
            setTitle(getString(R.string.eyepiece_parameters));
            insertLayout(R.layout.eyepiece_dialog);

            if (type == TYPE.CCD) {
                redrawForCcd();
            }

            setupCrosshairWidget(EyepiecesRecord.NO_CROSS_DATA);

            //Buttons
            setPositiveButton(getString(R.string.save), new InputDialog.OnButtonListener() {
                public void onClick(String value) {
                    String name = ((EditText) findViewById(R.id.ep_name)).getText().toString();
                    String note = ((EditText) findViewById(R.id.ep_note)).getText().toString();
                    String crossData = ((EditText) findViewById(R.id.ep_cross)).getText().toString();

                    double[] focus_afov = inputFocusAndAfov(type);

                    String epid = ""; //temporary

                    saveItemList();
                    EyepiecesDatabase db = new EyepiecesDatabase();
                    db.open();
                    db.add(name, focus_afov[0], focus_afov[1], epid, EyepiecesRecord.encodeNote(note, crossData));
                    db.close();
                    updateListArray();
                }
            });
            setNegativeButton(getString(R.string.cancel));
            if (type == TYPE.EYEPIECE)
                setMiddleButton(getString(R.string.catalog), new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        Intent intent = new Intent(getApplicationContext(), SelectorActivity.class);
                        intent.putExtra(Constants.SELECTOR_DBNAME, Constants.EP_DB);
                        intent.putExtra(Constants.SELECTOR_FIELDS, new String[]{MAN, LENGTH, FOV});
                        intent.putExtra(Constants.SELECTOR_FNAME, "name");
                        intent.putExtra(Constants.SELECTOR_FNAMES, new String[]{getString(R.string.brand), getString(R.string.f_length), getString(R.string.fov)});
                        intent.putExtra(Constants.SELECTOR_TABLE, "list");
                        intent.putExtra(Constants.SELECTOR_ACTIVITY_NAME, getString(R.string.select_eyepiece));
                        startActivityForResult(intent, SELECTOR_ACTIVITY);
                    }
                });

        }

        /**
         * changes type depending on focus value between eyepiece and CCD
         *
         * @param rec
         */
        public void buildEdit(final EyepiecesRecord rec) {
            if (SettingsActivity.isCCD(rec.getFocus())) type = TYPE.CCD;
            else type = TYPE.EYEPIECE;

            final EyepiecesRequest request = new EyepiecesRequest(rec);
            setTitle(getString(R.string.eyepiece_parameters));
            insertLayout(R.layout.eyepiece_dialog);
            //init fields
            ((EditText) findViewById(R.id.ep_name)).setText(String.valueOf(request.getRecord().getName()));
            ((EditText) findViewById(R.id.ep_f)).setText(type == TYPE.EYEPIECE ? String.format(Locale.US, "%.2f", request.getRecord().getFocus()) : String.format(Locale.US, "%.2f", request.getRecord().getFocus() - OFFSET));
            ((EditText) findViewById(R.id.ep_afov)).setText(type == TYPE.EYEPIECE ? String.format(Locale.US, "%.2f", request.getRecord().getAfov()) : String.format(Locale.US, "%.2f", request.getRecord().getAfov() - OFFSET));
            ((EditText) findViewById(R.id.ep_note)).setText(String.valueOf(request.getRecord().getNote()));

            setupCrosshairWidget(String.valueOf(request.getRecord().getAngleData()));

            if (type == TYPE.CCD) {
                redrawForCcd();
            }

            setPositiveButton(getString(R.string.save), new InputDialog.OnButtonListener() {
                public void onClick(String value) {
                    String name = ((EditText) findViewById(R.id.ep_name)).getText().toString();
                    String note = ((EditText) findViewById(R.id.ep_note)).getText().toString();
                    String crossData = ((EditText) findViewById(R.id.ep_cross)).getText().toString();
                    if (EyepiecesRecord.hasAngleData(crossData) && !crossData.matches("\\d+(?:\\.\\d+)?")) {
                        crossData = "0"; //bad data detected (not a number), set 0
                    }

                    double[] focus_afov = inputFocusAndAfov(type);

                    request.record.setEp_id(rec.getEp_id());
                    request.record.setName(name);
                    request.record.setFocus(focus_afov[0]);
                    request.record.setAfov(focus_afov[1]);
                    request.record.setNote(note);
                    request.record.setCrossData(crossData);

                    saveItemList();
                    EyepiecesDatabase db = new EyepiecesDatabase();
                    db.open();
                    db.edit(request.record);
                    db.close();
                    SettingsActivity.updateEyepieces();
                    updateListArray();
                }
            });
        }

        /**
         * Change the EP input dialog to represe4nt the CCD
         */
        private void redrawForCcd() {
            setTitle(getString(R.string.ccd_parameters));
            TextView tv = findViewById(R.id.tv_focal_length);
            tv.setText(getString(R.string.horiz_size_mm_));
            tv = findViewById(R.id.tv_afov);
            tv.setText(getString(R.string.vert_size_mm_));
            ((CheckBox) findViewById(R.id.cb_cross)).setText("Rotate. Angle:");
        }

        /**
         * Read and validate focus and afov user input (Horiz/Vert size for CCD)
         *
         * @return array with two doubles with adjusted values above.
         */
        double[] inputFocusAndAfov(TYPE type) {
            double[] result = {0, 0};
            double offset = 0, foMax = 100, afovMax = 200; //Normal eyepiece
            if (type == TYPE.CCD) {
                offset = OFFSET;
                foMax = 10000;
                afovMax = 10000;
            }
            result[0] = AstroTools.getDouble(((EditText) findViewById(R.id.ep_f)).getText().toString(), 0, 0, foMax) + offset;
            result[1] = AstroTools.getDouble(((EditText) findViewById(R.id.ep_afov)).getText().toString(), 0, 0, afovMax) + offset;
            return result;
        }

        /**
         * Provides initial value and checkbox behaviour
         * <br>
         * Initially not checked, NO, and disabled
         */
        private void setupCrosshairWidget(String value) {
            final EditText cross = findViewById(R.id.ep_cross);
            final CheckBox cbCross = findViewById(R.id.cb_cross);
            boolean ifCross = EyepiecesRecord.hasAngleData(value);

            cbCross.setChecked(ifCross);
            cross.setEnabled(ifCross);
            cross.setText(value);
            cbCross.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cross.setEnabled(isChecked);
                    cross.setText(isChecked ? "0" : EyepiecesRecord.NO_CROSS_DATA);
                }
            });
        }

    }

    /**
     * update text with the number of eyepieces
     */
    private void updateNumText() {
        ((TextView) findViewById(R.id.elHead)).setText("" + itemlist.size() + getString(R.string._eyepieces_in_your_collection));
    }

    public boolean parseMenu(int id) {
        switch (id) {
            case MENU_ADD_CCD:
                d = new EpDialog(EyepiecesListActivity.this, TYPE.CCD);
                d.buildNew();
                registerDialog(d).show();
                break;
            case MENU_EXPORT:
                final ExportData.Predicate<OutputStream> r = out -> {

                    InfoListSaver saver = new InfoListStringSaverImp(out);

                    class Filler implements InfoListFiller {
                        List<EyepiecesRecord> list = new ArrayList<EyepiecesRecord>();

                        public Filler() {
                            for (Item item : itemlist) {
                                list.add(item.rec);
                            }
                        }

                        @Override
                        public Iterator getIterator() {
                            return list.iterator();
                        }
                    }

                    InfoList iL = new InfoListImpl("", EyepiecesRecord.class);
                    try {
                        iL.fill(new Filler());
                        boolean noError = iL.save(saver);
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
            case MENU_IMPORT: //import more records to the current list
                IPickFileCallback listener = new IPickFileCallback() {
                    public void callbackCall(Uri uri) {
                        try {
                            readStream(getContentResolver().openInputStream(uri));
                        } catch (IOException e) {
                            Log.d(TAG, "e=" + e);
                        }
                    }

                    public void readStream(InputStream inputStream) {
                        saveItemList();
                        InfoList iL = new InfoListImpl("", EyepiecesRecord.class);
                        InfoListLoader loader = new InfoListStringLoaderImp(inputStream);

                        //eyepieces are added in object inflator
                        ErrorHandler eh = iL.load(loader);//ListHolder.getListHolder().get(InfoList.EYEPIECES_LIST).load(loader);
                        if (eh.hasError()) {
                            eh.showError(EyepiecesListActivity.this);
                        }

                        EyepiecesDatabase db = new EyepiecesDatabase();
                        db.open();

                        for (Object o : iL) {
                            Log.d(TAG, "import, obj=" + o);
                            db.add((EyepiecesRecord) o);
                        }
                        db.close();
                        updateListArray();
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

    private boolean parseContextMenu(int itemId, int id) {
        final EyepiecesRecord rec = ((Item) itemlist.get(id)).rec;


        switch (itemId) {
            case MENU_EDIT:
                EpDialog de = new EpDialog(this, TYPE.EYEPIECE);
                de.buildEdit(rec);
                registerDialog(de).show();

                return true;
            case MENU_REMOVE:
                final int listID = id;

                InputDialog dl = new InputDialog(EyepiecesListActivity.this);
                dl.setTitle(getString(R.string.warning));
                dl.setMessage(getString(R.string.do_you_want_to_remove_this_eyepiece_));
                dl.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        saveItemList();
                        EyepiecesDatabase db = new EyepiecesDatabase();
                        db.open();
                        db.remove(rec);
                        db.close();
                        SettingsActivity.updateEyepieces();
                        updateListArray();
                    }
                });
                dl.setNegativeButton(getString(R.string.cancel));
                registerDialog(dl).show();

                return true;
            case MENU_REMOVEALL:
                InputDialog dl2 = new InputDialog(EyepiecesListActivity.this);
                dl2.setTitle(getString(R.string.warning));
                dl2.setMessage(getString(R.string.do_you_want_to_remove_all_of_your_eyepieces_));
                dl2.setPositiveButton(getString(R.string.ok), new OnButtonListener() {
                    public void onClick(String v) {
                        EyepiecesDatabase db = new EyepiecesDatabase();
                        db.open();
                        db.removeAll();
                        db.close();
                        SettingsActivity.updateEyepieces();
                        updateListArray();
                    }
                });
                dl2.setNegativeButton(getString(R.string.cancel));
                registerDialog(dl2).show();

                return true;

            default:
                return false;
        }

    }

    private void updateListArray() {
        clearItemList();
        fillItemList();
        updateNumText();
        mAdapter.notifyDataSetChanged();
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        Log.d(TAG, "item clicked");
        Item item = itemlist.get(position);
        if (action_selected) {
            Intent intent = new Intent();
            intent.putExtra(Constants.SELECTED_EYEPIECE, item.rec.getSummary());
            setResult(RESULT_OK, intent);
            finish();
            return;
        }
        CheckBox ch = (CheckBox) v.findViewById(R.id.ep_selected);
        boolean isChecked = ch.isChecked();
        isChecked = !isChecked;
        ch.setChecked(isChecked);

        item.marked = isChecked;
        dirty = true;
    }

    //overriding menu button
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (aMenu != null && keyCode == KeyEvent.KEYCODE_MENU) {
            aMenu.show(bN);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this.curScopeEPid >= 0 && dirty) { //on explicit exit after changes
                saveUserSelection();
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                return true;
            } else finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void saveUserSelection() {
        boolean scope_epid_changed = false;

        EyepiecesDatabase db = new EyepiecesDatabase();
        db.open();

        //some checks were pressed for sure because of dirty flag
        for (int i = 0; i < itemlist.size(); i++) { //for each eyepiece checkbox
            if (curScopeEPid == 0) { //new EPid for telescope without it
                curScopeEPid = maxEPid + 1;
                scope_epid_changed = true;//here we mark that the telescope has a zero id among eyepieces, later we assign it a special eyep id
            }
            EyepiecesRecord rec = itemlist.get(i).rec;
            String new_ep_id = "";
            String[] ep_ids = rec.getEp_id().split("[,]");
            Log.d(TAG, "before, i=" + i + " ep_ids=" + rec.getEp_id());
            boolean checked = itemlist.get(i).marked;

            Set<Integer> set = new TreeSet<Integer>();
            for (String s : ep_ids) {
                int tid = AstroTools.getInteger(s, -1, -1, 1000);
                if (tid != -1) set.add(tid);
            }
            if (checked) set.add(curScopeEPid);
            else set.remove(curScopeEPid);
            for (int tid : set) {
                new_ep_id = new_ep_id + "," + tid;
            }
            if (new_ep_id.length() > 0) {
                new_ep_id = new_ep_id.substring(1, new_ep_id.length());//removing comma
            }

            Log.d(TAG, "after, i=" + i + " ep_ids=" + new_ep_id);
            //save to the database
            if (!rec.getEp_id().contentEquals(new_ep_id)) { //changed
                rec.setEp_id(new_ep_id);
                EyepiecesRequest request = new EyepiecesRequest(rec);
                db.edit(request.record);
            }
        }
        db.close();

        //Update selected Telescope data
        if (scope_epid_changed && (callerId == 0 || callerId == 2)) { //from graph or telescope
            //locate current telescope record by id
            InfoList list = ListHolder.getListHolder().get(InfoList.TELESCOPE_LIST);
            TelescopeRecord re = null;
            if (list.getCount() > 0) { //list of telescopes is not empty, search for scope id
                Iterator<TelescopeRecord> it = list.iterator();
                for (re = it.next(); re.id != curTelescopeId && it.hasNext(); re = it.next()) ;
            }
            if (re != null && re.id == curTelescopeId) { //found modify and save it
                re.ep_id = curScopeEPid;
                TelescopeRequest req = new TelescopeRequest(re);

                //update scope database
                TelescopeDatabase dbt = new TelescopeDatabase();
                dbt.open();
                dbt.edit(req.record);
                dbt.close();

                //update local curScope copy
                SettingsActivity.setCurrentTelescope(req.record); //will update eyepieces list too
            }
        } else SettingsActivity.updateEyepieces();
    }

    private void initAlexMenu() {

        boolean dayMode = !nightMode;

        aMenu = new alexMenu(this, new OnMenuItemSelectedListener() {
            public void MenuItemSelectedEvent(alexMenuItem selection) {
                parseMenu(selection.getId());
            }
        }, getLayoutInflater());
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

        menuItems.add(new alexMenuItem(MENU_EXPORT, getString(R.string.export), dayMode ? R.drawable.am_load_v : R.drawable.ram_load_v, true));
        menuItems.add(new alexMenuItem(MENU_IMPORT, getString(R.string.import2), dayMode ? R.drawable.am_save_v : R.drawable.ram_save_v, true));

        if (aMenu.isNotShowing()) {
            try {
                aMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                registerDialog(InputDialog.message(EyepiecesListActivity.this, getString(R.string.menu_error_) + e.getMessage(), 0)).show();
            }
        }
    }

    private void initAlexContextMenu() {
        contextMenu = new alexMenu(this, new OnMenuItemSelectedListener() {
            public void MenuItemSelectedEvent(alexMenuItem selection) {
                parseContextMenu(selection.getId(), contextMenu.getMenuItemId());
            }
        }, getLayoutInflater());
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

        menuItems.add(new alexMenuItem(MENU_EDIT, getString(R.string.edit), 0, true));
        menuItems.add(new alexMenuItem(MENU_REMOVE, getString(R.string.remove), 0, true));
        menuItems.add(new alexMenuItem(MENU_REMOVEALL, getString(R.string.remove_all), 0, true));

        if (contextMenu.isNotShowing()) {
            try {
                contextMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(EyepiecesListActivity.this, getString(R.string.contextmenu_error_) + e.getMessage(), 0).show();
            }
        }
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v, int index, long arg3) {
                contextMenu.setMenuItemId(index);
                contextMenu.setHeader(((TextView) v.findViewById(R.id.ep_name)).getText());
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
}