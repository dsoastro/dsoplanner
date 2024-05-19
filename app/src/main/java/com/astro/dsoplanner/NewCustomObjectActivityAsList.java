package com.astro.dsoplanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astro.dsoplanner.AstroTools.RaDecRec;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.CMO;
import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.base.CustomObject;
import com.astro.dsoplanner.base.CustomObjectLarge;
import com.astro.dsoplanner.base.Fields;
import com.astro.dsoplanner.base.MinorPlanet;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.CustomDatabaseLarge;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.infolist.ObsInfoListImpl;
import com.astro.dsoplanner.infolist.ObsListFiller;
import com.astro.dsoplanner.util.Holder2;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class NewCustomObjectActivityAsList extends ParentActivity {


    private static final String OBJ_POS = "objPos";
    private static final String SOME_NAME = "Some name...";
    private static final String EQUINOX = "equinox";
    private static final String COMMENT2 = "comment";
    private static final String NAME22 = "name2";
    private static final String NAME12 = "name1";
    private static final String PA2 = "pa";
    private static final String MAG2 = "mag";
    private static final String B2 = "b";
    private static final String A2 = "a";
    private static final String TYPE2 = "type";
    private static final String DEC2 = "dec";
    private static final String RA2 = "ra";
    private static final String CHANGED = "changed";
    private static final String CATALOG_POSITION = "catalogPosition";
    private static final String EDIT = "edit";
    private static final String WRONG_DATA = "wrong data";
    private static final String _0_00000 = "0.00000";
    private static final String _0_00_00 = "0 00 00";
    private static final String _0_000000 = "0.000000";


    private static final String TAG = NewCustomObjectActivityAsList.class.getSimpleName();
    List<DepictableView> list;
    DbListItem item;
    boolean editMode;
    CustomObject globj;


    private static final int RA = 0;
    private static final int DEC = 1;
    private static final int TYPE = 2;
    private static final int A = 3;
    private static final int B = 4;
    private static final int MAG = 5;
    private static final int PA = 6;
    private static final int NAME1 = 7;
    private static final int NAME2 = 8;
    private static final int EQ = 9;
    private static final int COMMENT = 10;

    Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setContentView(R.layout.new_custom_obj_act2);

            editMode = getIntent().getBooleanExtra(EDIT, false);

            AstroObject aobj = SettingsActivity.getObjectFromSharedPreferencesNew(Constants.NCOA_OBJECT, NewCustomObjectActivityAsList.this);
            if (aobj instanceof CustomObject) {
                globj = (CustomObject) aobj;
            }
            Log.d(TAG, "" + getIntent().getExtras());
            //database we are working with
            int pos = getIntent().getIntExtra(CATALOG_POSITION, -1);
            InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
            if (pos != -1)
                item = (DbListItem) iL.get(pos);
            else
                //custom catalog
                item = new DbListItem(0, AstroCatalog.CUSTOM_CATALOG, "", "", new DbListItem.FieldTypes());

            DoubleField.initNumSigns();
            if (item.cat == AstroCatalog.COMET_CATALOG || item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) {
                Log.d(TAG, "6");
                DoubleField.setNumSigns(6);
            }
            //setting view
            if (editMode) {
                if (globj == null) {
                    finish();
                    return;
                }
                list = convertObjectToFields(globj, item);
                makeView(list);
            } else {//zero object
                list = convertObjectToFields(null, item);
                makeView(list);
                NewCustomObjectActivityAsList.this.setTitle(R.string.create_new_object);
            }


            Button btnCancel = (Button) findViewById(R.id.co_cancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (isDataChanged(list)) {//data was changed
                        InputDialog d0 = new InputDialog(NewCustomObjectActivityAsList.this);
                        d0.setMessage(getString(R.string.data_was_changed_do_you_want_to_exit_));
                        d0.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                            public void onClick(String value) {
                                NewCustomObjectActivityAsList.this.finish();
                            }
                        });
                        d0.setNegativeButton(getString(R.string.cancel), new InputDialog.OnButtonListener() {
                            public void onClick(String value) {

                            }
                        });
                        registerDialog(d0).show();
                    } else
                        finish();

                }
            });
            Button btnOK = (Button) findViewById(R.id.co_ok);
            btnOK.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    ErrorHandler eh = new ErrorHandler();
                    Holder2<CustomObject, Boolean> h = convertFieldsToObject(list, item);//objects in the list were updated

                    if (h == null) return;
                    CustomObject obj = h.x;
                    Log.d(TAG, "obj=" + obj + " changed=" + h.y);
                    if (!h.y && !editMode) {//new zero object
                        registerDialog(InputDialog.message(NewCustomObjectActivityAsList.this, R.string.zero_object_will_not_be_added_to_the_database_)).show();
                        return;
                    }
                    if (!h.y) {//there were no changes
                        Intent i = new Intent();
                        i.putExtra(CHANGED, false);
                        NewCustomObjectActivityAsList.this.setResult(Activity.RESULT_OK, i);
                        finish();
                        return;
                    }
                    Log.d(TAG, "error=" + eh);

                    CustomDatabase catalog;
                    if (item.ftypes.isEmpty())
                        catalog = new CustomDatabase(NewCustomObjectActivityAsList.this, item.dbFileName, item.cat);
                    else
                        catalog = new CustomDatabaseLarge(NewCustomObjectActivityAsList.this, item.dbFileName, item.cat, item.ftypes);
                    Log.d(TAG, "db item=" + item);

                    Intent i = new Intent();
                    i.putExtra(CHANGED, true);//we say there were changes to the calling activity so that it updates itself
                    NewCustomObjectActivityAsList.this.setResult(Activity.RESULT_OK, i);

                    if (editMode) {//existing object
                        obj.catalog = (short) item.cat;//in here the edited object of the deleted database will change its catalog to Custom_Catalog which is a right thing to do
                        obj.id = globj.id;

                        //check if the database was deleted. It was deleted if there were no item passed to the activity in "catalogPosition"
                        if (obj.catalog != globj.catalog) {
                            obj.id = CustomObject.idNum++;//putting correct id to the custom object without a database
                        }

                        if (obj.catalog != AstroCatalog.CUSTOM_CATALOG) {//updating obj right in the database
                            ErrorHandler eh1 = new ErrorHandler();
                            catalog.open(eh1);
                            if (eh1.hasError())
                                eh1.showError(NewCustomObjectActivityAsList.this);
                            else {
                                long result = catalog.edit(obj);
                                Log.d(TAG, "updated obj=" + obj);
                                //Log.d(TAG,"passed obj="+Global.obs_pass);
                                Log.d(TAG, "result=" + result);
                                catalog.close();
                                ObservationListActivity.updateObsLists(obj, NewCustomObjectActivityAsList.this);//update observation lists if they contain the object
                            }
                        } else {//updating temp object right in the obs list
                            int obsList = SettingsActivity.getSharedPreferences(NewCustomObjectActivityAsList.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
                            //pos in global obs list
                            int pos = NewCustomObjectActivityAsList.this.getIntent().getIntExtra(OBJ_POS, -1);
                            if (pos == -1) return;
                            ObsInfoListImpl.Item item = (ObsInfoListImpl.Item) ListHolder.getListHolder().get(obsList).get(pos);
                            if (!(item.x instanceof CustomObject)) return;
                            ObservationListActivity.copyObjectFields((CustomObject) item.x, obj);//item.x keeps a link to the object, so we need to change its fields
                            new Prefs(NewCustomObjectActivityAsList.this).saveList(obsList);
                        }
                        finish();
                    } else {//new object
                        obj.catalog = (short) item.cat;
                        //we implicitly assume that all calls from ObsLists come with catalog==AstroCatalog.Custom_Catalog
                        if (obj.catalog != AstroCatalog.CUSTOM_CATALOG) {
                            ErrorHandler eh2 = new ErrorHandler();
                            catalog.open(eh2);
                            if (eh2.hasError())
                                eh2.showError(NewCustomObjectActivityAsList.this);
                            else {
                                catalog.add(obj, eh2);
                                catalog.close();
                            }
                        } else {
                            addObj(obj);
                        }
                        finish();
                    }

                }
            });
        }
    };


    private void addObj(AstroObject obj) {
        InfoListFiller filler = new ObsListFiller(Arrays.asList(new AstroObject[]{obj}));
        int obsList = SettingsActivity.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
        ListHolder.getListHolder().get(obsList).fill(filler);
        new Prefs(this).saveList(obsList);
    }

    boolean initRequired = false;//global init

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideMenuBtn();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "init required=" + initRequired);
        initHandler.handleMessage(null);


    }

    private void addObjToIntent(CustomObject obj, Intent i) {
        Bundle bn = new Bundle();
        bn.putInt("objType", obj.getClassTypeId());
        bn.putByteArray("byteArray", obj.getByteRepresentation());
        i.putExtras(bn);
    }

    private void makeView(List<DepictableView> list) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.co_ll3);

        for (DepictableView v : list) {
            ll.addView(v.getView());
        }
    }

    /**
     * @return object without catalog and id, flag indicating if the data was changed (true - for changed data)
     */
    private Holder2<CustomObject, Boolean> convertFieldsToObject(List<DepictableView> list, DbListItem item) {
        boolean changed = false;
        for (DepictableView v : list) {
            if (v.dataChanged()) {
                changed = true;
                break;
            }
        }

        double ra = 0;
        double dec = 0;

        double a = 0;
        double b = 0;
        double mag = 0;
        double pa = 0;
        String name1 = "";
        String name2 = "";
        String comment = "";
        int type = 1;
        if (item.cat == AstroCatalog.COMET_CATALOG)
            type = AstroObject.Comet;
        else if (item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) {
            type = AstroObject.MINOR_PLANET;
        }
        String typeStr = "";

        Fields fields = new Fields();
        List<DepictableField> dlist = new ArrayList<DepictableField>();
        for (DepictableView v : list) {
            dlist.add(v.getField());
        }
        int eq = EqField.EQ_2000;
        for (DepictableField f : dlist) {
            switch (f.getId()) {
                case RA:
                    ra = (Double) f.getInternalRep();
                    break;
                case DEC:
                    dec = (Double) f.getInternalRep();
                    break;
                case TYPE:
                    Holder2<Integer, String> h = (Holder2<Integer, String>) f.getInternalRep();
                    type = h.x;
                    typeStr = h.y;
                    break;
                case A:
                    a = (Double) f.getInternalRep();
                    break;
                case B:
                    b = (Double) f.getInternalRep();
                    break;
                case MAG:
                    mag = (Double) f.getInternalRep();
                    break;
                case PA:
                    pa = (Double) f.getInternalRep();
                    break;
                case NAME1:
                    name1 = (String) f.getInternalRep();
                    break;
                case NAME2:
                    name2 = (String) f.getInternalRep();
                    if ("".equals(name2)) {//name1 is not allowed to be empty. Fill name2 if it is empty
                        name2 = name1;
                    }
                    break;
                case COMMENT:
                    comment = (String) f.getInternalRep();
                    break;
                case EQ:
                    eq = (Integer) f.getInternalRep();
                    break;
                default:
                    Object o = f.getInternalRep();
                    Log.d(TAG, "inside f to obj, o=" + o);
                    if (o instanceof String) fields.put(f.getName(), (String) o);
                    if (o instanceof Integer) fields.put(f.getName(), (Integer) o);
                    if (o instanceof Double) fields.put(f.getName(), (Double) o);
                    if (o instanceof Fields.Photo) fields.put(f.getName(), (Fields.Photo) o);
                    if (o instanceof Fields.Url) fields.put(f.getName(), (Fields.Url) o);
            }

        }

        if (eq == EqField.EQ_CURRENT) {//if ra/dec in current equinox need to precess backwards
            Log.d(TAG, "precessed");
            RaDecRec rec = AstroTools.get2000RaDec(ra, dec, Calendar.getInstance());
            ra = rec.ra;
            dec = rec.dec;
        }

        CustomObject obj;
        if (fields.isEmpty())
            obj = new CustomObject(AstroCatalog.CUSTOM_CATALOG, 0, ra, dec, AstroTools.getConstellation(ra, dec), type, typeStr, a, b, mag, pa, name1, name2, comment);
        else
            obj = new CustomObjectLarge(AstroCatalog.CUSTOM_CATALOG, 0, ra, dec, AstroTools.getConstellation(ra, dec), type, typeStr, a, b, mag, pa, name1, name2, comment, fields);
        return new Holder2<CustomObject, Boolean>(obj, changed);
    }

    private List<DepictableView> convertObjectToFields(CustomObject obj, DbListItem item) {
        List<DepictableView> list = new ArrayList<DepictableView>();
        int i = 0;
        if (obj != null) {
            if (item.cat != AstroCatalog.COMET_CATALOG && item.cat != AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) {
                list.add(new EquinoxView(new EqField(EqField.EQ_2000, EQ, EQUINOX)));

                DepictableField f1 = new GradRaField((double) obj.ra, RA2, RA);
                list.add(new DepictableTextView(f1));

                DepictableField f2 = new GradDecField((double) obj.dec, DEC2, DEC);
                list.add(new DepictableTextView(f2));

                boolean type = SearchRules.isInternalCatalog(item.cat);//catalogType(item.dbName);
                TypeField f3 = new TypeField((int) obj.type, obj.typeStr, TYPE2, TYPE);
                DepictableTypeView v = new DepictableTypeView(f3, obj.type, obj.typeStr);
                if (type) {
                    v.blockOnClickListener();//do not allow to change type for standard databases
                }
                list.add(v);

                DepictableField f4 = new DoubleNanField(obj.a, A2, A);
                list.add(new DepictableTextView(f4));

                DepictableField f5 = new DoubleNanField(obj.b, B2, B);
                list.add(new DepictableTextView(f5));

                DepictableField f6 = new DoubleNanField(obj.mag, MAG2, MAG);
                list.add(new DepictableTextView(f6));

                DepictableField f7 = new DoubleNanField(obj.pa, PA2, PA);
                list.add(new DepictableTextView(f7));
            }
            DepictableField f8 = new NonEmptyStringField(obj.name1, NAME12, NAME1);
            list.add(new DepictableTextView(f8));

            DepictableField f9 = new StringField(obj.name2, NAME22, NAME2);
            list.add(new DepictableTextView(f9));

            DepictableField f10 = new StringField(obj.comment, COMMENT2, COMMENT);
            list.add(new DepictableTextView(f10));

            i = COMMENT + 1;
            if (obj instanceof CustomObjectLarge) {
                Fields fields = ((CustomObjectLarge) obj).getFields();
                for (Map.Entry<String, String> e : fields.getStringMap().entrySet()) {
                    StringField f = new StringField(e.getValue(), e.getKey(), i++);
                    list.add(new DepictableTextView(f));
                }
                for (Map.Entry<String, Integer> e : fields.getIntMap().entrySet()) {
                    IntField f = new IntField(e.getValue(), e.getKey(), i++);
                    list.add(new DepictableTextView(f));
                }
                for (Map.Entry<String, Double> e : fields.getDoubleMap().entrySet()) {
                    DoubleField f = new DoubleField(e.getValue(), e.getKey(), i++);
                    list.add(new DepictableTextView(f));
                }
                for (Map.Entry<String, String> e : fields.getPhotoMap().entrySet()) {
                    PhotoField f = new PhotoField(e.getValue(), e.getKey(), i++);
                    list.add(new DepictableFileView(f));
                }
                for (Map.Entry<String, String> e : fields.getUrlMap().entrySet()) {
                    UrlField f = new UrlField(e.getValue(), e.getKey(), i++);
                    list.add(new DepictableTextView(f));
                }
            }
        } else {//zero object
            if (item.cat != AstroCatalog.COMET_CATALOG && item.cat != AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) {
                list.add(new EquinoxView(new EqField(EqField.EQ_2000, EQ, EQUINOX)));

                DepictableField f1 = new GradRaField(0., RA2, RA);
                list.add(new DepictableTextView(f1));

                DepictableField f2 = new GradDecField(0., DEC2, DEC);
                list.add(new DepictableTextView(f2));

                boolean type = SearchRules.isInternalCatalog(item.cat);//catalogType(item.dbName);
                int itype = 1;

                TypeField f3 = new TypeField(AstroObject.Gxy, "", TYPE2, TYPE);
                DepictableTypeView v = new DepictableTypeView(f3, itype, "");
                if (type)
                    v.blockOnClickListener();
                list.add(v);

                list.add(new DepictableTextView(new DoubleNanField(0., A2, A)));
                list.add(new DepictableTextView(new DoubleNanField(0., B2, B)));
                list.add(new DepictableTextView(new DoubleNanField(0., MAG2, MAG)));
                list.add(new DepictableTextView(new DoubleNanField(0., PA2, PA)));
            }
            DepictableField f4 = new NonEmptyStringField(SOME_NAME, NAME12, NAME1);
            list.add(new DepictableTextView(f4));//the field should not be empty!

            list.add(new DepictableTextView(new StringField("", NAME22, NAME2)));
            list.add(new DepictableTextView(new StringField("", COMMENT2, COMMENT)));
            i = COMMENT + 1;
            if (!item.ftypes.isEmpty()) {//additional fields
                Map<String, DbListItem.FieldTypes.TYPE> map //map of names and object types
                        = item.ftypes.getNameTypeMap();
                for (Map.Entry<String, DbListItem.FieldTypes.TYPE> e : map.entrySet()) {
                    switch (e.getValue()) {
                        case STRING:
                            StringField fs = new StringField("", e.getKey(), i++);
                            list.add(new DepictableTextView(fs));
                            break;
                        case INT:
                            IntField fi = new IntField(0, e.getKey(), i++);
                            list.add(new DepictableTextView(fi));
                            break;
                        case DOUBLE:
                            DoubleField fd = new DoubleField(0., e.getKey(), i++);
                            list.add(new DepictableTextView(fd));
                            break;
                        case PHOTO:
                            PhotoField fp = new PhotoField("", e.getKey(), i++);
                            list.add(new DepictableFileView(fp));
                            break;
                        case URL:
                            UrlField fu = new UrlField("", e.getKey(), i++);
                            list.add(new DepictableTextView(fu));
                    }
                }
            }
        }
        setFieldComments(list);
        if (item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG || item.cat == AstroCatalog.COMET_CATALOG) {

            return changeFieldsOrderForCMO(list);
        } else
            return list;
    }

    /**
     * replaces setting value dialog title to special comment
     *
     * @param list
     */
    private void setFieldComments(List<DepictableView> list) {
        String[] fields = new String[]{NAME12, NAME22, COMMENT2, A2, B2, CMO.YEAR, CMO.MONTH, CMO.DAY, Comet.ABSMAG,
                CMO.E, CMO.I, CMO.NODE, Comet.Q, Comet.SLOPE, CMO.W, MinorPlanet.G, MinorPlanet.H, MinorPlanet.MA,
                MinorPlanet.A, MAG2, PA2, RA2, DEC2};
        int[] comments_id =
                new int[]{R.string.short_name_of_the_object, R.string.long_name_of_the_object, R.string.text_comment_on_an_object,
                        R.string.object_dimensions_in_minutes_longer_axis, R.string.object_dimensions_in_minutes_shorter_axis,
                        R.string.orbit_elements_epoch_year, R.string.orbit_elements_epoch_month, R.string.orbit_elements_epoch_day,
                        R.string.absolute_star_magnitude, R.string.orbital_excentricity, R.string.inclination_to_the_ecliptic_j2000_0_degrees_,
                        R.string.longitude_of_the_ascending_node_j2000_0_degrees_, R.string.perihelion_distance_au_,
                        R.string.star_magnitude_slope_parameter, R.string.argument_of_perihelion_j2000_0_degrees_,
                        R.string.star_magnitude_slope_parameter, R.string.absolute_star_magnitude, R.string.mean_anomaly_at_the_epoch_degrees_,
                        R.string.semimajor_axis_of_the_orbit_au_, R.string.magnitude_of_an_object, R.string.position_angle_in_degrees,
                        R.string.right_ascension, R.string.declination};
        String[] comments = new String[comments_id.length];
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < fields.length; i++) {
            map.put(fields[i], i);
        }
        for (int i = 0; i < comments_id.length; i++) {
            comments[i] = getString(comments_id[i]);
        }
        for (DepictableView view : list) {
            String name = view.getField().getName();
            Integer pos = map.get(name);
            if (pos != null) {
                view.getField().setFieldComment(comments[pos]);
            }

        }
    }

    private <T> void swap(T[] arr, int pos1, int pos2) {
        T e1 = arr[pos1];
        T e2 = arr[pos2];

        arr[pos1] = e2;
        arr[pos2] = e1;
    }

    /**
     * @param list
     * @return
     */
    private List<DepictableView> changeFieldsOrderForCMO(List<DepictableView> list) {
        String[] first_fields = new String[]{NAME12, NAME22, COMMENT2, CMO.YEAR, CMO.MONTH, CMO.DAY};
        Map<String, Integer> map = new HashMap<String, Integer>();
        DepictableView[] arr = new DepictableView[list.size()];

        for (int i = 0; i < first_fields.length; i++) {
            map.put(first_fields[i], i);
        }
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }


        for (int i = 0; i < arr.length; i++) {
            DepictableView view = arr[i];
            Integer pos = map.get(view.getField().getName());
            if (pos != null && i != pos) {
                swap(arr, i, pos);
            }

        }

        return Arrays.asList(arr);

    }


    private boolean isDataChanged(List<DepictableView> list) {
        for (DepictableView v : list) {
            if (v.dataChanged())
                return true;
        }
        return false;
    }

    interface DepictableView {
        View getView();

        DepictableField getField();

        /**
         * @return true if the field was changed
         */
        boolean dataChanged();
    }


    class DepictableTextView implements DepictableView {


        DepictableField f;
        String initialValue;

        /**
         * @param f - field in the view
         */
        public DepictableTextView(DepictableField f) {
            this.f = f;
            this.initialValue = f.getValue();
        }


        public View getView() {
            LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = mInflater.inflate(R.layout.new_custom_obj_act_aslist_item, null);
            TextView tv1 = (TextView) v.findViewById(R.id.f1_text);//name
            //tv1.setPaintFlags(tv1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tv1.setText(f.getName());

            final TextView tv2 = (TextView) v.findViewById(R.id.f2_text);//value
            tv2.setPaintFlags(tv2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tv2.setText(f.getValue());

            tv2.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final String in_value;
                    String hint = null;
                    if (f.getValue().equals(_0_00_00)) {
                        hint = _0_00_00;
                        in_value = "";
                    } else if (f.getValue().equals(_0_00000) || f.getValue().equals(_0_000000)) {
                        in_value = "";
                    } else
                        in_value = f.getValue();

                    AstroTools.NameDialogRunnable r = new AstroTools.NameDialogRunnable() {
                        String s;

                        public void setName(String s) {
                            this.s = s;
                        }

                        public void run() {
                            String initValue = in_value;//f.getValue();
                            if (initValue.equals(s))
                                return;//no change
                            f.setValue(s);
                            if (f.convert()) {//successfull conversion
                                tv2.setText(f.getValue());
                            } else {
                                f.setValue(initValue);//restoring initial value
                                ErrorHandler eh = new ErrorHandler(ErrorHandler.DATA_CORRUPTED, WRONG_DATA);
                                eh.showError(NewCustomObjectActivityAsList.this);

                            }
                        }
                    };

                    boolean phone_keyboard = (f instanceof DoubleField || f instanceof GradRaField || f instanceof GradDecField);
                    String title;
                    if (f.getFieldComment() == null) {
                        title = getString(R.string.set_field_value_) + f.getType() + ")";
                    } else {
                        title = f.getFieldComment();
                    }

                    String majorTitle = DbManager.getDbName(item.cat);
                    if (majorTitle != null) {
                        majorTitle = getString(R.string.edit_) + majorTitle;
                    }
                    InputDialog d = AstroTools.getNameDialog
                            (NewCustomObjectActivityAsList.this, title, in_value, hint, phone_keyboard, r, majorTitle);
                    registerDialog(d).show();
                }
            });

            return v;
        }

        public DepictableField getField() {
            return f;
        }

        public boolean dataChanged() {
            return !initialValue.equals(f.getValue());
        }
    }

    class DepictableTypeView extends DepictableTextView {
        int initialType;
        String typeStr;

        public DepictableTypeView(TypeField f, int initialType, String typeStr) {
            super(f);
            this.initialType = initialType;
            this.typeStr = typeStr;
        }

        @Override
        public boolean dataChanged() {
            Holder2<Integer, String> h = (Holder2<Integer, String>) f.getInternalRep();
            int type = h.x;
            if (type != initialType)
                return true;
            if (type == AstroObject.Custom) {
                if (!typeStr.equals(h.y))
                    return true;
            }
            return false;

        }

        boolean block_listener = false;

        public void blockOnClickListener() {
            block_listener = true;
        }

        /**
         * this method is needed as positions in the list are different from standard type numbers
         *
         * @return AstroObject type
         * @pos - position in a list
         */
        private int getAstroObjType(int pos) {
            if (pos + 1 < AstroObject.Custom)
                return pos + 1;
            switch (pos + 1) {
                case AstroObject.Custom:
                    return AstroObject.MINOR_PLANET;
                case AstroObject.Custom + 1:
                    return AstroObject.Star;
                case AstroObject.Custom + 2:
                    return AstroObject.DoubleStar;
                case AstroObject.Custom + 3:
                    return AstroObject.Comet;
                case AstroObject.Custom + 4:
                    return AstroObject.CG;
                case AstroObject.Custom + 5:
                    return AstroObject.DN;
                case AstroObject.Custom + 6:
                    return AstroObject.AST;
                default:
                    return AstroObject.Custom;

            }
        }

        private int getPosFromAstroObjType(int type) {
            for (int i = 1; i < AstroObject.AST; i++) {
                if (getAstroObjType(i) == type)
                    return i;
            }
            return 0;
        }

        @Override
        public View getView() {
            LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = mInflater.inflate(R.layout.new_custom_obj_act_aslist_item, null);
            TextView tv1 = (TextView) v.findViewById(R.id.f1_text);//name
            tv1.setText(f.getName());
            //tv1.setPaintFlags(tv1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            final TextView tv2 = (TextView) v.findViewById(R.id.f2_text);//value
            tv2.setText(f.getValue());
            tv2.setPaintFlags(tv2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            if (!block_listener) {
                tv2.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        final InputDialog d1 = new InputDialog(NewCustomObjectActivityAsList.this);
                        d1.setTitle(Global.getAppContext().getString(R.string.select_type_of_the_object));
                        d1.setMessage("");
                        d1.disableBackButton(false);

                        Holder2<Integer, String> h = (Holder2<Integer, String>) f.getInternalRep();

                        final int oldSelection = getPosFromAstroObjType(h.x);
                        d1.setValue(String.valueOf(oldSelection)); //default value
                        d1.setListItems(R.array.object_types, new InputDialog.OnButtonListener() {

                            public void onClick(String value) {
                                int row = AstroTools.getInteger(value, 0, 0, 10000);

                                if (oldSelection == row)
                                    d1.dismiss(); //no selection made, just close it

                                int astro_type = getAstroObjType(row);
                                f.setValue(AstroObject.getTypeString(astro_type));
                                f.convert();

                                if (astro_type == AstroObject.Custom) {
                                    AstroTools.NameDialogRunnable rPos = new AstroTools.NameDialogRunnable() {
                                        String s;

                                        public void setName(String s) {
                                            this.s = s;
                                        }

                                        public void run() {
                                            f.setValue(s);
                                            f.convert();
                                            tv2.setText(f.getValue());
                                        }
                                    };
                                    AstroTools.NameDialogRunnable rNeg = new AstroTools.NameDialogRunnable() {

                                        public void setName(String s) {

                                        }

                                        public void run() {
                                            tv2.setText(f.getValue());
                                        }
                                    };
                                    String majorTitle = DbManager.getDbName(item.cat);
                                    if (majorTitle != null) {
                                        majorTitle = getString(R.string.edit_) + majorTitle;
                                    }
                                    InputDialog d = AstroTools.getNameDialog(NewCustomObjectActivityAsList.this,
                                            Global.getAppContext().getString(R.string.define_name_for_custom_object_type), f.getValue(), null, false, rPos, rNeg, majorTitle);
                                    d.show();
                                } else
                                    tv2.setText(f.getValue());

                            }
                        });
                        registerDialog(d1).show();

                    }
                });
            } else {
                tv2.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        registerDialog(InputDialog.message(NewCustomObjectActivityAsList.this,
                                R.string.object_type_could_not_be_changed_for_this_catalog_, 0)).show();
                    }
                });


            }
            return v;
        }
    }

    class EquinoxView extends DepictableTextView {
        public EquinoxView(EqField f) {
            super(f);
        }

        @Override
        public View getView() {
            LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = mInflater.inflate(R.layout.new_custom_obj_act_aslist_item, null);
            TextView tv1 = (TextView) v.findViewById(R.id.f1_text);//name
            tv1.setText(f.getName());
            final TextView tv2 = (TextView) v.findViewById(R.id.f2_text);//value
            tv2.setText(f.getValue());
            tv2.setPaintFlags(tv2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tv2.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final InputDialog d1 = new InputDialog(NewCustomObjectActivityAsList.this);
                    d1.setTitle(Global.getAppContext().getString(R.string.select_equinox));
                    d1.setMessage("");
                    d1.disableBackButton(false);
                    final String[] arr = new String[]{Global.getAppContext().getString(R.string._2000), Global.getAppContext().getString(R.string.current)};
                    d1.setValue((String.valueOf((Integer) f.getInternalRep())));
                    d1.setListItems(arr, new InputDialog.OnButtonListener() {

                        public void onClick(String value) {
                            int res = AstroTools.getInteger(value, 0, 0, 1000);
                            f.setValue(arr[res]);
                            f.convert();
                            tv2.setText(f.getValue());
                        }
                    });
                    registerDialog(d1).show();
                }
            });
            return v;
        }

    }

    class DepictableFileView extends DepictableTextView {
        public DepictableFileView(PhotoField f) {
            super(f);
        }

        @Override
        public View getView() {
            LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = mInflater.inflate(R.layout.new_custom_obj_act_aslist_item, null);
            TextView tv1 = (TextView) v.findViewById(R.id.f1_text);//name
            tv1.setText(f.getName());
            //tv1.setPaintFlags(tv1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            final TextView tv2 = (TextView) v.findViewById(R.id.f2_text);//value
            tv2.setText(f.getValue());
            tv2.setPaintFlags(tv2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tv2.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    class Callback implements IPickFileCallback {
                        DepictableField field;
                        TextView view;

                        public Callback(DepictableField field, TextView view) {
                            this.field = field;
                            this.view = view;
                        }

                        public void callbackCall(Uri uri) {
                            SettingsActivity.persistUriIfNeeded(uri, getApplicationContext());
                            if (field != null) {

                                field.setValue(uri.toString());
                                field.convert();
                                view.setText(field.getValue());
                            }
                        }
                    }

                    SelectFileActivity.setPath(SettingsActivity.getFileDialogPath(getApplicationContext()));
                    SelectFileActivity.setListener(new Callback(f, tv2));
                    Intent fileDialog = new Intent(NewCustomObjectActivityAsList.this, SelectFileActivity.class);
                    startActivity(fileDialog);
                }
            });

            return v;
        }
    }

    /**
     * encapsulating different field behaviour (eg double, int, string, etc)
     */
    interface DepictableField {
        /**
         * @return String rep of underlying type
         */
        String getValue();

        /**
         * @return field name
         */
        String getName();

        /**
         * @return field type
         */
        String getType();

        /**
         * @param s - the String value of the field to be further converted to underlying type
         *          make conversion before calling getValue as getValue returns underlying value, not the one
         *          set by this method.
         *          It would have been better to combine setValue and convert in one method!!!
         */
        void setValue(String s);

        /**
         * @return result of the conversion of the stringValue set by method setValue to internal type
         */
        boolean convert();

        /**
         * @return underlying field (e.g. Double/Integer/String etc
         */
        Object getInternalRep();

        /**
         * @return id in the list of edit texts
         */
        int getId();

        /**
         * use for difficult fields (e.g. for comet / minor planet). Null if comment is not set
         * the comment is show instead of "set field (double)"
         */
        void setFieldComment(String comment);

        String getFieldComment();
    }

    class StringField implements DepictableField {
        protected String value;
        private String name;
        private int id;
        private String comment = null;

        public StringField(String value, String name, int id) {
            this.value = value;
            this.name = name;
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return DbListItem.FieldTypes.TYPE.STRING.name();
        }

        public void setValue(String s) {
            value = s;
        }

        public boolean convert() {
            return true;
        }

        public Object getInternalRep() {
            return value;
        }

        public int getId() {
            return id;
        }

        public String toString() {
            return value;
        }

        public void setFieldComment(String comment) {
            this.comment = comment;
        }

        public String getFieldComment() {
            return comment;
        }
    }

    /**
     * This class checks that the field is not empty (e.g. for name field)
     */
    class NonEmptyStringField extends StringField {
        private String stringValue;

        public NonEmptyStringField(String value, String name, int id) {
            super(value, name, id);
        }

        @Override
        public void setValue(String s) {
            stringValue = s;
        }

        @Override
        public boolean convert() {
            if ("".equals(stringValue))
                return false;
            else {
                value = stringValue;
                return true;
            }
        }

    }

    class PhotoField extends StringField {


        public PhotoField(String value, String name, int id) {
            super(value, name, id);
        }


        @Override
        public String getType() {
            return DbListItem.FieldTypes.TYPE.PHOTO.name();
        }

        @Override
        public Object getInternalRep() {
            return new Fields.Photo(value);
        }
    }

    class UrlField extends StringField {
        private String stringValue;

        public UrlField(String value, String name, int id) {
            super(value, name, id);
        }

        @Override
        public void setValue(String s) {
            stringValue = s.trim();
        }

        public boolean convert() {
            if ("".equals(stringValue)) {
                value = "";
                return true;
            }
            try {
                URL url = new URL(stringValue);
            } catch (Exception e) {
                try {
                    URL url = new URL("http://" + stringValue);
                } catch (Exception e1) {
                    return false;
                }
            }
            value = stringValue;
            return true;
        }

        @Override
        public String getType() {
            return DbListItem.FieldTypes.TYPE.URL.name();
        }

        @Override
        public Object getInternalRep() {
            return new Fields.Url(value);
        }
    }

    class IntField implements DepictableField {
        private Integer value;
        private String name;
        private String stringValue;
        private int id;
        private String comment = null;

        public IntField(Integer value, String name, int id) {
            this.value = value;
            this.name = name;
            this.id = id;
        }

        public String getValue() {
            return value.toString();
        }

        public void setValue(String s) {
            stringValue = s;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return DbListItem.FieldTypes.TYPE.INT.name();
        }

        public boolean convert() {
            try {
                value = Integer.parseInt(stringValue);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        public Object getInternalRep() {
            return value;
        }

        public int getId() {
            return id;
        }

        public void setFieldComment(String comment) {
            this.comment = comment;
        }

        public String getFieldComment() {
            return comment;
        }
    }

    static class DoubleField implements DepictableField {
        private static int num_signs = 5;

        /**
         * sets the number of signs
         *
         * @param num
         */
        public static void setNumSigns(int num) {
            num_signs = num;
        }

        public static void initNumSigns() {
            num_signs = 5;
        }

        protected Double value;
        private String name;
        protected String stringValue;
        private int id;
        private String comment = null;

        public DoubleField(Double value, String name, int id) {
            this.value = value;
            this.name = name;
            this.id = id;
        }

        public String getValue() {

            String f = "%.5f";
            if (num_signs != 5)
                f = f.replace("5", "" + num_signs);


            return String.format(Locale.US, f, value);
        }

        public void setValue(String s) {
            stringValue = s;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return DbListItem.FieldTypes.TYPE.DOUBLE.name();
        }

        public boolean convert() {
            Double v;
            try {
                v = Double.parseDouble(stringValue);
            } catch (Exception e) {
                return false;
            }
            if (Double.isNaN(v))
                return false;
            value = v;
            return true;
        }

        public Object getInternalRep() {
            return value;
        }

        public int getId() {
            return id;
        }

        public void setFieldComment(String comment) {
            this.comment = comment;
        }

        public String getFieldComment() {
            return comment;
        }
    }

    class DoubleNanField extends DoubleField {
        public DoubleNanField(Double value, String name, int id) {
            super(value, name, id);
        }

        @Override
        public boolean convert() {
            Double v;
            try {
                v = Double.parseDouble(stringValue);
            } catch (Exception e) {
                return false;
            }

            value = v;
            return true;
        }
    }

    class EqField implements DepictableField {
        public static final int EQ_2000 = 0;
        public static final int EQ_CURRENT = 1;
        private int value;
        private int id;
        private String name;
        private String comment = null;

        public EqField(int value, int id, String name) {
            super();
            this.value = value;
            this.id = id;
            this.name = name;
        }

        public String getValue() {
            if (value == EQ_2000)
                return Global.getAppContext().getString(R.string._2000);
            return getString(R.string.current);
        }

        String stringValue;

        public void setValue(String s) {
            stringValue = s;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return "";
        }

        public boolean convert() {
            if (Global.getAppContext().getString(R.string._2000).equals(stringValue))
                value = EQ_2000;
            else
                value = EQ_CURRENT;
            return true;
        }

        public Object getInternalRep() {
            return value;
        }

        public int getId() {
            return id;
        }

        public void setFieldComment(String comment) {
            this.comment = comment;
        }

        public String getFieldComment() {
            return comment;
        }
    }

    class TypeField implements DepictableField {
        private Integer value;
        private String typeStr;
        private String name;
        private String stringValue;
        private int id;
        private String comment = null;

        public TypeField(Integer value, String typeStr, String name, int id) {
            this.value = value;
            this.typeStr = typeStr;
            this.name = name;
            this.id = id;
        }

        public String getValue() {
            if (value == AstroObject.Custom)
                return typeStr;
            else
                return AstroObject.getTypeString(value);
        }

        public void setValue(String s) {
            stringValue = s;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return DbListItem.FieldTypes.TYPE.STRING.name();
        }

        public boolean convert() {
            String typeUp = stringValue.toUpperCase();
            Integer i = AstroObject.typeMap.get(typeUp);
            if (i != null) {
                value = i;
            } else {
                value = AstroObject.Custom;
                typeStr = stringValue;
            }
            return true;
        }

        public Object getInternalRep() {
            return new Holder2<Integer, String>(value, typeStr);
        }

        public int getId() {
            return id;
        }

        public void setFieldComment(String comment) {
            this.comment = comment;
        }

        public String getFieldComment() {
            return comment;
        }
    }

    class GradRaField implements DepictableField {
        private Double value;
        private String name;
        private String stringValue;
        private int id;
        private String comment = null;

        public GradRaField(Double value, String name, int id) {
            this.value = value;
            this.name = name;
            this.id = id;
        }

        public String getValue() {

            return AstroTools.getGradString(value);
        }

        public void setValue(String s) {
            stringValue = s;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return getString(R.string.hour_min_sec);
        }

        public boolean convert() {
            Double d = AstroTools.getRaDecValue(stringValue);
            if (d != null) {
                value = d;
                return true;
            }
            return false;
        }

        public Object getInternalRep() {
            return value;
        }

        public int getId() {
            return id;
        }

        public void setFieldComment(String comment) {
            this.comment = comment;
        }

        public String getFieldComment() {
            return comment;
        }
    }

    class GradDecField extends GradRaField {

        public GradDecField(Double value, String name, int id) {
            super(value, name, id);
        }

        @Override
        public String getType() {
            return getString(R.string.deg_min_sec);
        }
    }
}

