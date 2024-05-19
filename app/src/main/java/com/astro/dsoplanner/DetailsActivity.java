package com.astro.dsoplanner;

import static android.provider.BaseColumns._ID;
import static com.astro.dsoplanner.Constants.A;
import static com.astro.dsoplanner.Constants.B;
import static com.astro.dsoplanner.Constants.CALDWELL;
import static com.astro.dsoplanner.Constants.CONSTEL;
import static com.astro.dsoplanner.Constants.DEC;
import static com.astro.dsoplanner.Constants.HERSHELL;
import static com.astro.dsoplanner.Constants.MAG;
import static com.astro.dsoplanner.Constants.MESSIER;
import static com.astro.dsoplanner.Constants.NAME;
import static com.astro.dsoplanner.Constants.PA;
import static com.astro.dsoplanner.Constants.RA;
import static com.astro.dsoplanner.Constants.TYPE;
import static java.lang.Math.abs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astro.dsoplanner.SearchRules.HolderA;
import com.astro.dsoplanner.SearchRules.MySet;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.CMO;
import com.astro.dsoplanner.base.CustomObject;
import com.astro.dsoplanner.base.CustomObjectLarge;
import com.astro.dsoplanner.base.DoubleStarObject;
import com.astro.dsoplanner.base.Exportable;
import com.astro.dsoplanner.base.ExtendedObject;
import com.astro.dsoplanner.base.HrStar;
import com.astro.dsoplanner.base.NgcicObject;
import com.astro.dsoplanner.base.Planet;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.base.TychoStar;
import com.astro.dsoplanner.database.CrossDb;
import com.astro.dsoplanner.database.CustomDatabaseLarge;
import com.astro.dsoplanner.database.Db;
import com.astro.dsoplanner.database.DbListItem.FieldTypes;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.database.NgcicDatabase;
import com.astro.dsoplanner.graph.GraphActivity;
import com.astro.dsoplanner.graph.GraphRec;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.infolist.ObsInfoListImpl;
import com.astro.dsoplanner.infolist.ObsListFiller;
import com.astro.dsoplanner.scopedrivers.ComInterface;
import com.astro.dsoplanner.scopedrivers.CommunicationManager;
import com.astro.dsoplanner.startools.BitTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

//info screen
public class DetailsActivity extends ParentActivity implements OnGestureListener {
    private static final String SELECT_NAME_FROM_NGCIC_WHERE_REF = "select name from ngcic where ref=";
    private static final String NGC2 = "NGC";
    private static final String COMMENT3 = "comment";
    private static final String WDS = "WDS";
    private static final String SELECT_NAME1_FROM_NGCIC_WHERE_ID = "select name1 from ngcic where _id=";
    private static final String STRING = "--";
    private static final String CLASS = "class";
    private static final String WDS_0_9_0_9 = "WDS[0-9]+[\\+\\-][0-9]+";
    private static final String SELECT_COMP_FROM_COMPONENTS_WHERE_NAME = "select comp from components where name='";
    private static final String HR_0_9 = "HR[0-9]+";
    private static final String COMPONENTS = "components";
    private static final String INVIS = "invis";
    private static final String DR = "dr";
    private static final String IC = "ic";
    private static final String NGC = "ngc";
    private static final String HR2 = "HR ";
    private static final String HR = " HR ";
    private static final String TYC = "TYC ";
    private static final String JPG = ".jpg";
    private static final String HTTP = "http://";
    private static final String COMMENT2 = COMMENT3;
    private static final String N_R = "n/r";
    private static final String N_S = "n/s";
    private static final String NOMORE2 = "nomore";
    private static final String DOUBLE_DATA_COMPS = "DoubleData [comps=";
    private static final String SELECT_COMPONENTS_SEPARATION_MAG_MAG2_PA_YEAR_ID_FROM_CUSTOMDBB_WHERE_NAME1 = "select components,separation,mag,mag2,pa,year,_id from customdbb where name1='";

    private static final String SELECT_PERIOD_AXIS_INCL_OMEGA1_PERIT_E_OMEGA2_FROM_ELEMENTS_WHERE_WDSID = "select period,axis,incl,omega1,perit,e,omega2 from elements where wdsid=";
    private static final String SELECT_COMPONENTS_FROM_CUSTOMDBB_WHERE_ID = "select components from customdbb where _id=";
    private static final String SELECT_COMP_FROM_COMPONENTS_WHERE_NAME_HR = "select comp from components where name='HR";
    private static final String HR_0_92 = "HR[0-9]+";

    private boolean callerGraph; //used to notify the activity that it'as called from SkyView (for locking the object marker)
    private AstroObject obj = null;
    private AstroObject objclone = null;
    private boolean nomore = false;//no more button flag
    private List<AstroObject> moreObjects = new ArrayList<AstroObject>();//new AODBsorter()
    private static final String TAG = DetailsActivity.class.getSimpleName();
    boolean dirtyObs = false;//adding to obs list, for saving in onPause
    private static final Map<Integer, Integer> mapFields = new HashMap<Integer, Integer>();//map of text fields id to rel layout ids in details.xml of the fields that could be removed if necessary
    boolean crossTaskStarted = false;

    static {
        mapFields.put(R.id.dim_text, R.id.dim_rel_layout);
        mapFields.put(R.id.vis_text, R.id.vis_rel_layout);
        mapFields.put(R.id.comment_text, R.id.comment_rel_layout);
        mapFields.put(R.id.mag_text, R.id.mag_rel_layout);
        mapFields.put(R.id.pa_text, R.id.pa_rel_layout);
    }

    /**
     * class for sorting the moreObjects
     */
    class AODBsorter implements Comparator<AstroObject> {
        @Override
        public int compare(AstroObject o1, AstroObject o2) {
            return o1.getCatalogName().compareTo(o2.getCatalogName());
        }
    }

    Handler geoHandler = new Handler() {
        @Override
        public void handleMessage(Message m) {
            //geoChanged=true;
            //	if(initPerformed)
            updateFields();
            SettingsActivity.putSharedPreferences(Constants.GEO_DETAILS_UPDATE, false, DetailsActivity.this);

        }
    };


    BroadcastReceiver geoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateFields();
            SettingsActivity.putSharedPreferences(Constants.GEO_DETAILS_UPDATE, false, DetailsActivity.this);

        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(Constants.GEO_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(geoReceiver, filter);
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(geoReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dirtyObs) {
            int obsList = SettingsActivity.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
            new Prefs(this).saveList(obsList);
            dirtyObs = false;
        }
        unregisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        if (SettingsActivity.getSharedPreferences(this).getBoolean(Constants.GEO_DETAILS_UPDATE, true)) {
            SettingsActivity.putSharedPreferences(Constants.GEO_DETAILS_UPDATE, false, this);
            updateFields();
        }
        hideMenuBtn();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

    }

    public static int getFcFillingColor(boolean nightmode) {

        int fc;
        if (nightmode) {

            fc = Color.argb(0xff, 0xaf, 00, 00);//ffdf0000
        } else if (SettingsActivity.getDarkSkin()) {

            fc = Color.argb(0xFF, 0xff, 0xff, 0xff);
        } else {

            fc = Color.argb(0xFF, 0xff, 0xff, 0xff);
        }
        return fc;
    }

    public static int getBcFillingColor(boolean nightmode) {
        int bc;

        if (nightmode) {
            bc = Color.argb(0xff, 0x4f, 0, 0);

        } else if (SettingsActivity.getDarkSkin()) {
            bc = Color.argb(0xFF, 49, 49, 49);

        } else {
            bc = Color.argb(0xFF, 0x9e, 0x9e, 0x9e);

        }
        return bc;
    }

    private void setCommonName(String name) {
        TextView tv = (TextView) findViewById(R.id.common_name);
        tv.setText(name);
        tv.setVisibility(View.VISIBLE);
    }

    private void updateFields() {
        Calendar defc = AstroTools.getDefaultTime(DetailsActivity.this);
        obj.recalculateRaDec(defc);//recalculate comet and minor planet coords

        int bc;
        int fc;

        if (obj instanceof NgcicObject) {
            NgcicObject o = (NgcicObject) obj;
            if (o.clas == null && o.comment == null) {
                NgcicDatabase db = new NgcicDatabase(this);
                ErrorHandler eh = new ErrorHandler();
                db.open(eh);
                if (eh.hasError()) eh.showError(this);
                else obj = db.getObject(obj.getId());
            }

            int ngc = o.ngc;
            String name = AstroTools.getCommonName(ngc);
            if (name != null) {
                setCommonName(name);
            } else {
                name = AstroTools.getCommonNameFromRef(obj);//for possible equivalents in ngcic
                if (name != null) setCommonName(name);
            }

        } else {
            if (obj.ref != 0) {
                String name = AstroTools.getCommonNameFromRef(obj);
                if (name != null) setCommonName(name);
            }
        }
        if (obj.getCatalog() == AstroCatalog.UGC) {
            FieldTypes types = new FieldTypes();
            types.put(CLASS, FieldTypes.TYPE.STRING);
            CustomDatabaseLarge db = new CustomDatabaseLarge(this, DbManager.getDbFileName(AstroCatalog.UGC), AstroCatalog.UGC, types);
            ErrorHandler eh = new ErrorHandler();
            db.open(eh);
            if (eh.hasError()) eh.showError(this);
            else obj = db.getObject(obj.getId());
        }

        bc = getBcFillingColor(nightMode);
        fc = getFcFillingColor(nightMode);

        Log.d(TAG, "obj=" + obj);
        TextView ngc = (TextView) findViewById(R.id.description);
        ngc.setText(getTitleName(obj));

        TextView tl = (TextView) findViewById(R.id.type_div);
        tl.setBackgroundColor(bc);
        tl.setTextColor(fc);

        tl = (TextView) findViewById(R.id.double_div);
        tl.setBackgroundColor(bc);
        tl.setTextColor(fc);

        tl = (TextView) findViewById(R.id.cross_div);
        tl.setBackgroundColor(bc);
        tl.setTextColor(fc);

        tl = (TextView) findViewById(R.id.riseset_div);
        tl.setBackgroundColor(bc);
        tl.setTextColor(fc);

        tl = (TextView) findViewById(R.id.loctime_div);
        tl.setBackgroundColor(bc);
        tl.setTextColor(fc);

        tl = (TextView) findViewById(R.id.pos_div);
        tl.setBackgroundColor(bc);
        tl.setTextColor(fc);

        tl = (TextView) findViewById(R.id.objp_div);
        tl.setBackgroundColor(bc);
        tl.setTextColor(fc);

        tl = (TextView) findViewById(R.id.other_div);
        tl.setBackgroundColor(bc);
        tl.setTextColor(fc);

        TextView type = (TextView) findViewById(R.id.typed);
        Log.d(TAG, "type=" + type);
        type.setText(obj.getLongTypeString());

        TextView tm = (TextView) findViewById(R.id.mag_text);
        if (obj.equals(Global.moon)) {
            if (tm != null) {
                tm.setText(String.format(Locale.US, "%.1f", 100 * AstroTools.MoonFraction(defc)) + "%");
                TextView label = (TextView) findViewById(R.id.mag_label);
                label.setText(R.string.moon_phase);

            }
        } else if (obj.getCatalog() != AstroCatalog.MARK_CATALOG && !obj.equals(Global.sun) && !obj.equals(Global.moon)) {
            if (tm != null) {
                String magstr = Double.isNaN(obj.getMag()) ? STRING : String.format(Locale.US, "%.1f", obj.getMag());
                tm.setText(magstr);
            }
        }

        TextView tp = (TextView) findViewById(R.id.pa_text);
        if (obj instanceof ExtendedObject && !(obj instanceof CMO)) {
            ExtendedObject o = (ExtendedObject) obj;

            if (tp != null) {
                String pastr = Double.isNaN(o.pa) ? STRING : String.format(Locale.US, "%.1f", o.pa);
                tp.setText(pastr);
            }

        }
        if (obj.hasDimension()) {
            double a = obj.getA();
            double b = obj.getB();

            boolean anan = Double.isNaN(a);
            boolean bnan = Double.isNaN(b);
            TextView dim = (TextView) findViewById(R.id.dim_text);
            String s1 = anan ? STRING : String.format(Locale.US, "%.1f", obj.getA());
            String s2 = bnan ? STRING : String.format(Locale.US, "%.1f", obj.getB());
            String dimstr = s1 + "x" + s2 + (char) 39;
            if (anan && bnan) dimstr = STRING;

            if (dim != null) dim.setText(dimstr);

        }

        TextView tc = (TextView) findViewById(R.id.con);
        String conName = Constants.constellationLong[AstroTools.getConstellation(AstroTools.getNormalisedRa(obj.getRa()), obj.getDec())];
        tc.setText(conName);


        TextView td = (TextView) findViewById(R.id.date_text);
        td.setText(makeDateString(defc, true));
        TextView tt = (TextView) findViewById(R.id.time_text);
        tt.setText(makeTimeString(defc, true));

        TextView lattext = (TextView) findViewById(R.id.lat_text);
        TextView lontext = (TextView) findViewById(R.id.lon_text);

        double lat = SettingsActivity.getLattitude();
        double lon = SettingsActivity.getLongitude();


        String latStr = AstroTools.getLatString(lat);
        lattext.setText(latStr);
        String lonStr = AstroTools.getLonString(lon);
        lontext.setText(lonStr);

        new LocationTask().execute(lat, lon);


        String comment = obj.getComment();
        Log.d(TAG, "comment=" + comment);

        TextView tvcomment = ((TextView) findViewById(R.id.comment_text));
        if (tvcomment != null)
            tvcomment.setText(comment);//could have been removed, see mapFields!!!

        TextView rt = (TextView) findViewById(R.id.ra_text);
        Point p = obj.getCurrentRaDec(defc);
        double ra = p.getRa();
        double dec = p.getDec();
        ra = AstroTools.getNormalisedRa(ra);
        rt.setText(doubleToGrad(ra, 'h', 'm'));

        TextView dt = (TextView) findViewById(R.id.dec_text);
        dt.setText(doubleToGrad(dec, '\u00B0', (char) 39));

        rt = (TextView) findViewById(R.id.ra2000_text);

        ra = obj.getRa();
        dec = obj.getDec();
        ra = AstroTools.getNormalisedRa(ra);
        rt.setText(doubleToGrad(ra, 'h', 'm'));

        dt = (TextView) findViewById(R.id.dec2000_text);
        dt.setText(doubleToGrad(dec, '\u00B0', (char) 39));


        double lst = AstroTools.sdTime(defc);
        Log.d(TAG, "lst=" + lst);
        lat = SettingsActivity.getLattitude();
        TextView azt = (TextView) findViewById(R.id.az_text);
        double az = AstroTools.Azimuth(lst, lat, p.ra, p.dec);
        azt.setText(doubleToGrad(az, '\u00B0', (char) 39));

        TextView altt = (TextView) findViewById(R.id.alt_text);
        double alt = AstroTools.Altitude(lst, lat, p.ra, p.dec);
        altt.setText(doubleToGrad(alt, '\u00B0', (char) 39));

        TextView tr = (TextView) findViewById(R.id.transittime_text);
        double minh = AstroTools.hStars;
        if (obj instanceof Planet) {
            Planet pl = (Planet) obj;
            if (pl.getPlanetType() == Planet.PlanetType.Sun) minh = AstroTools.hSun;
            else {
                if (pl.getPlanetType() == Planet.PlanetType.Moon) minh = AstroTools.hSun;
            }
        }
        if (obj instanceof Point) {
            p = (Point) obj;
            AstroTools.TransitRec time = AstroTools.getRiseSetting(p, defc, minh);
            TextView ttransit = (TextView) findViewById(R.id.transittime_text);
            ttransit.setText(makeTimeString(time.tTransit, false));

            TextView dtransit = (TextView) findViewById(R.id.transitdate_text);
            dtransit.setText(makeDateString(time.tTransit, false));

            TextView ttralt = (TextView) findViewById(R.id.transitalt_text);
            double alttr = AstroTools.Altitude(AstroTools.sdTime(time.tTransit), SettingsActivity.getLattitude(), p.ra, p.dec);
            ttralt.setText(doubleToGrad(alttr, '\u00B0', (char) 39));

            if (time.tRise != null) {

                TextView trisetime = (TextView) findViewById(R.id.risetime_text);
                trisetime.setText(makeTimeString(time.tRise, false));

                TextView trisedate = (TextView) findViewById(R.id.risedate_text);
                trisedate.setText(makeDateString(time.tRise, false));
            } else {
                TextView trisetime = (TextView) findViewById(R.id.risetime_text);
                trisetime.setText("");

                TextView trisedate = (TextView) findViewById(R.id.risedate_text);

                if (alttr > 0) trisedate.setText(N_S);
                else trisedate.setText(N_R);
            }
            if (time.tSetting != null) {
                TextView tsettime = (TextView) findViewById(R.id.settime_text);
                tsettime.setText(makeTimeString(time.tSetting, false));

                TextView tsetdate = (TextView) findViewById(R.id.setdate_text);
                tsetdate.setText(makeDateString(time.tSetting, false));
            } else {

                TextView tsettime = (TextView) findViewById(R.id.settime_text);
                tsettime.setText("");
                TextView tsetdate = (TextView) findViewById(R.id.setdate_text);

                if (alttr > 0) tsetdate.setText(N_S);
                else tsetdate.setText(N_R);
            }

        }

        if (obj.hasVisibility() && obj.getMag() != 0) {
            TextView vis_txt = (TextView) findViewById(R.id.vis_text);
            if (vis_txt != null) {
                vis_txt.setText(""); //reset
                double magnification;
                double vis;
                String res = "";
                Log.d(TAG, "tfocus=" + SettingsActivity.getTFocus());
                boolean nonzerovis = false;
                for (int e = 0; e < SettingsActivity.getEPsNumber(); e++) {
                    double epfocus = SettingsActivity.getEpFocus(e);
                    if (SettingsActivity.isCCD(epfocus)) //CCD
                        continue;
                    magnification = SettingsActivity.getTFocus() / epfocus;
                    Log.d(TAG, "ep focus=" + SettingsActivity.getEpFocus(e));
                    Log.d(TAG, "magnification=" + magnification);

                    vis = AstroTools.LogC(obj.getA(), obj.getB(), obj.getMag(), magnification, obj.getType());
                    String visStr = Double.isNaN(vis) ? STRING : (vis == AstroTools.NOT_VISIBLE ? INVIS : String.format(Locale.US, "%.1f", vis));

                    res += visStr + "/";
                }
                if (res.length() > 0) //remove last slash
                    vis_txt.setText(res.substring(0, res.length() - 1));
            }
        }
    }

    Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setContentView(R.layout.details);
            Log.d(TAG, "Details");
            Intent i = getIntent();
            Bundle bun = i.getBundleExtra(Constants.ASTRO_OBJECT_INTENT);
            Log.d(TAG, "bun=" + bun);
            callerGraph = i.getBooleanExtra(Constants.CALLER_GRAPH, false);

            nomore = i.getBooleanExtra(Constants.NO_MORE_BUTTON, false);
            if (bun != null) {
                Exportable e = AstroTools.getExportableFromBundle(bun);
                Log.d(TAG, "e=" + e);
                Exportable e1 = AstroTools.getExportableFromBundle(bun);//clone of obj
                if (e instanceof AstroObject) {
                    obj = (AstroObject) e;
                    objclone = (AstroObject) e1;
                    Log.d(TAG, "obj=" + obj);
                }
            }
            if (obj == null) {
                finish();
                return;
            }

            updateFields();
            Log.d(TAG, "obj=" + obj);

            boolean other = false;
            if (obj instanceof CustomObjectLarge) {
                CustomObjectLarge custom = (CustomObjectLarge) obj;
                other = showCustomFields(custom, null, false);
            } else other = !"".equals(obj.getComment());
            if (!other) {
                //hiding OTHER divider
                View v = findViewById(R.id.other_div);
                if (v != null) v.setVisibility(View.GONE);
            }

            //CROSS REFS
            String name = null;
            String comps = "";
            if (SearchRules.isInternalDeepSky(obj.getCatalog()) || SearchRules.isStarChartLayer(obj.getCatalog()) || obj.getCatalog() == AstroCatalog.YALE_CATALOG || obj.getCatalog() == AstroCatalog.TYCHO_CATALOG || obj.getCatalog() == AstroCatalog.WDS || obj.getCatalog() == AstroCatalog.HAAS) {

                if (obj instanceof NgcicObject) {
                    NgcicObject o = (NgcicObject) obj;
                    name = o.getNgcIcName();
                } else if (obj instanceof HrStar) {
                    name = ((HrStar) obj).getCrossdbSearchName();
                    if ("".equals(name)) name = null;
                    Log.d(TAG, "name=" + name);
                } else if (obj instanceof TychoStar) {
                    name = ((TychoStar) obj).getCrossdbSearchName();
                    if ("".equals(name)) name = null;
                    Log.d(TAG, "name=" + name);
                } else {
                    if (obj.getCatalog() == AstroCatalog.CALDWELL || obj.getCatalog() == AstroCatalog.MESSIER) {
                        name = obj.getLongName();
                    } else if (obj instanceof DoubleStarObject) {
                        DoubleStarObject o = (DoubleStarObject) obj;
                        name = o.getCrossdbSearchName();
                        comps = o.fields.get(COMPONENTS);
                        if (comps == null) comps = "";
                    } else name = obj.getShortName();
                }
            }
            if (name != null) {
                crossTaskStarted = true;
                if (obj.getCatalog() == AstroCatalog.WDS || obj.getCatalog() == AstroCatalog.HAAS)
                    new CrossTask(name, comps).execute();
                else if (obj.getCatalog() == AstroCatalog.NGCIC_CATALOG) {//for objects like ic4329-2
                    String name2 = name;
                    Db db = new Db(getApplicationContext(), Constants.NGCIC_DATABASE_NAME);
                    try {
                        db.open();
                        Cursor cursor = db.rawQuery(SELECT_NAME1_FROM_NGCIC_WHERE_ID + obj.getId());
                        if (cursor.moveToNext()) {
                            name2 = cursor.getString(0);
                            cursor.close();
                        }
                    } catch (Exception e) {
                    } finally {
                        try {
                            db.close();
                        } catch (Exception e) {
                        }
                    }
                    new CrossTask(name2).execute();

                } else new CrossTask(name).execute();

            }

            //double for wds
            if (obj.getCatalog() == AstroCatalog.WDS) {
                new DoubleStarTask(null).execute(WDS + obj.getShortName());
            }


            //MORE button
            boolean nomore = getIntent().getBooleanExtra(Constants.NO_MORE_BUTTON, false);//if true, a call from details, no more button to avoid recursion
            if (!nomore && !crossTaskStarted && isMoreNeeded(obj)) {
                new MoreTask(objclone).execute();

            }


            //Buttons
            //------------------------
            //GoTo
            Button bGo = (Button) findViewById(R.id.detGoTo);
            if (CommunicationManager.getComModule().isConnected()) {
                bGo.setVisibility(View.VISIBLE);
                bGo.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ComInterface bt = CommunicationManager.getComModule();
                        if (bt.isConnected()) {
                            bt.send(obj.getRa(), obj.getDec());
                        }
                    }
                });
            }

            final boolean returntograph = getIntent().getBooleanExtra(Constants.RETURN_TO_GRAPH, false);
            //Sky View/Lock Button
            final Button bL = (Button) findViewById(R.id.bBack);
            if (callerGraph) {//change button to Lock object
                bL.setText(Global.lockCursor ? R.string.unlock : R.string.lock);
                bL.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (Global.lockCursor) {
                            Global.lockCursor = false;
                            bL.setText(R.string.lock);
                        } else {
                            Global.lockCursor = true;
                            bL.setText(R.string.unlock);
                        }
                        finish();
                    }
                });
            } else  //for calls from other activities it will open SkyView
                bL.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (returntograph) {
                            setResult(RESULT_OK);
                            finish();
                            return;
                        }
                        Calendar c = AstroTools.getDefaultTime(DetailsActivity.this);
                        Point.setLST(AstroTools.sdTime(c));//need to calculate Alt and Az
                        SettingsActivity.putSharedPreferences(Constants.GRAPH_OBJECT, obj, DetailsActivity.this);
                        int zoom = SettingsActivity.getSharedPreferences(DetailsActivity.this).getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);

                        new GraphRec(zoom, ((Point) obj).getAz(), ((Point) obj).getAlt(), c, (Point) obj).save(DetailsActivity.this);
                        Intent i = new Intent(DetailsActivity.this, GraphActivity.class);
                        startActivity(i);

                    }
                });

            //Image button if available
            Button bR = (Button) findViewById(R.id.bImage);
            bR.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (obj instanceof AstroObject)
                        new PictureCommand(DetailsActivity.this, (AstroObject) obj).execute();
                }
            });
            if (isPictureAvailable(getApplicationContext(), obj))
                bR.setVisibility(View.VISIBLE); //visible
            else bR.setVisibility(View.INVISIBLE); //invisible*/

            //Notes button
            Button bN = (Button) findViewById(R.id.bNotes);
            if (obj.getCatalog() == AstroCatalog.MARK_CATALOG) {
                bN.setVisibility(View.INVISIBLE);
            }
            bN.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (obj instanceof AstroObject) {
                        Command command = new GetObjectNotesCommand(DetailsActivity.this, (AstroObject) obj);
                        command.execute();
                    }
                }
            });

            //Add button
            final Button bK = (Button) findViewById(R.id.bLock);
            if (obj.catalog == AstroCatalog.SNOTES) bK.setVisibility(View.INVISIBLE);
            bK.setText(R.string.add2);
            bK.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (obj instanceof AstroObject) {
                        int obsList = SettingsActivity.getSharedPreferences(DetailsActivity.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
                        InfoList list = ListHolder.getListHolder().get(obsList);
                        InfoListFiller filler = new ObsListFiller(Arrays.asList(new AstroObject[]{(AstroObject) obj}));
                        list.fill(filler);
                        bK.setVisibility(View.INVISIBLE);
                        dirtyObs = true;
                    }
                }
            });
            //make the button invisible if the object is already in the observation list
            int obsList = SettingsActivity.getSharedPreferences(DetailsActivity.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
            InfoList list = ListHolder.getListHolder().get(obsList);
            if (obj instanceof AstroObject) {
                AstroObject o = (AstroObject) obj;
                boolean inlist = false;
                Iterator it = list.iterator();
                for (; it.hasNext(); ) {
                    ObsInfoListImpl.Item item = (ObsInfoListImpl.Item) it.next();
                    if (item.x.equals(o)) {
                        inlist = true;
                        break;
                    }
                }
                //do not add center mark objects as well
                if (obj.getCatalog() == AstroCatalog.MARK_CATALOG) {
                    inlist = true;
                }
                if (inlist) bK.setVisibility(View.INVISIBLE);

            }

            callerGraph = false; //reset the caller id
            removeEmptyFields();

            if (SettingsActivity.getSharedPreferences(DetailsActivity.this).getBoolean(Constants.GEO_DETAILS_UPDATE, true)) {
                SettingsActivity.putSharedPreferences(Constants.GEO_DETAILS_UPDATE, false, DetailsActivity.this);
            }
        }
    };

    /**
     * @param obj
     * @return true if more search for the object is needed, false otherwise
     */
    private boolean isMoreNeeded(AstroObject obj) {
        boolean catalogok = !(obj.getCatalog() == AstroCatalog.PGC_CATALOG || obj.getCatalog() == AstroCatalog.TYCHO_CATALOG || obj.getCatalog() == AstroCatalog.UCAC4_CATALOG || obj.getCatalog() == AstroCatalog.PLANET_CATALOG || obj.getCatalog() == AstroCatalog.MARK_CATALOG);
        return catalogok;
    }


    List<String> crossnames;

    /**
     * showing other object designations from cross db
     *
     * @author leonid
     */
    class CrossTask extends AsyncTask<Void, Void, Void> {

        int type;
        String name;
        String comps;
        final int DOUBLE_TYPE = 1;
        final int NON_DOUBLE_TYPE = 2;

        List<List<String>> list2;//double

        /**
         * @param name
         */
        public CrossTask(String name) {
            this.type = NON_DOUBLE_TYPE;
            this.name = name;
        }

        public CrossTask(String name, String comps) {
            this.name = name;
            this.comps = comps;
            type = DOUBLE_TYPE;
        }

        private String getSaoNumber(String name) {
            String upname = name.toUpperCase();
            if (upname.matches("TYC[0-9]+[\\-][0-9]+[\\-][0-9]+")) {
                String s = upname.substring(3);
                String[] a = s.split("\\-");
                int tyc1 = -1;
                int tyc2 = -1;
                int tyc3 = -1;
                try {
                    tyc1 = Integer.parseInt(a[0]);
                    tyc2 = Integer.parseInt(a[1]);
                    tyc3 = Integer.parseInt(a[2]);
                } catch (Exception e) {
                }
                if (tyc1 != -1 && tyc2 != -1 && tyc3 != -1) {
                    int index = BitTools.convertTycToInt(tyc1, tyc2, tyc3);
                    return SaoTyc.getSaoNumberFromTycIndex(index, getApplicationContext());
                }
            }
            return null;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            registerTask(this);
            if (type == NON_DOUBLE_TYPE) {
                crossnames = CrossDb.searchName(name, DetailsActivity.this);
                list2 = new ArrayList<List<String>>();

                String sao = getSaoNumber(name);
                if (sao != null && !crossnames.contains(sao)) crossnames.add(sao);
                list2.add(crossnames);

            } else {
                list2 = CrossDb.searchNameForDouble(name, getApplicationContext());
                processDoubleList();


                Set<String> set = new HashSet<String>();
                for (List<String> l : list2) {
                    for (String s : l) {
                        set.add(s);
                    }
                }
                crossnames = new ArrayList<String>();
                crossnames.addAll(set);
            }
            return null;

        }

        /**
         * HR... , component
         */
        Map<String, String> hrcomps = new HashMap<String, String>();

        private void processDoubleList() {
            List<List<String>> list = new ArrayList<List<String>>();
            Set<String> set = new HashSet<String>();//keeping track of the names to avoid doubing them
            for (List<String> l : list2) {

                List<String> l2 = new ArrayList<String>();//list of names without doubling
                for (String s : l) {
                    if (set.contains(s)) continue;
                    else {
                        l2.add(s);
                        set.add(s);
                    }

                }

                String hr = "";
                for (String s : l2) {
                    if (s.matches(HR_0_9)) {
                        hr = s;
                        break;
                    }
                }
                if (!"".equals(hr)) {
                    Db db = new Db(getApplicationContext(), Constants.SQL_DATABASE_COMP_DB);
                    String comp = "";

                    try {
                        db.open();
                        Cursor cursor = db.rawQuery(SELECT_COMP_FROM_COMPONENTS_WHERE_NAME + hr + "'");
                        if (cursor.moveToNext()) {
                            comp = cursor.getString(0);
                            hrcomps.put(hr, comp);
                            cursor.close();
                            if (!"".equals(comps) && !comps.contains(comp)) continue;
                        }
                    } catch (Exception e) {
                    } finally {
                        try {
                            db.close();
                        } catch (Exception e) {
                        }
                    }

                }
                list.add(l2);
            }
            list2 = list;
        }

        @Override
        protected void onPostExecute(Void v) {
            try {
                Log.d(TAG, "hrcomps=" + hrcomps);
                String r = "";
                Set<String> set = new HashSet<String>();
                List<String> list = new ArrayList<String>();
                int j = 0;
                for (List<String> l : list2) {
                    j++;

                    if (l.size() == 0) continue;
                    String line = "";
                    String hr = "";
                    for (String s : l) {
                        set.add(s);
                        Log.d(TAG, "s=" + s);
                        if (s.matches(HR_0_9)) {
                            hr = s;
                            HrStar star = AstroTools.getHrStar(s);
                            if (star != null && (star.getBayer() != 0 || star.getFlamsteed() != 0)) {
                                int index = star.getLongName().indexOf("HR");
                                String name = star.getLongName();
                                if (index != -1) {
                                    name = name.substring(0, index).trim();
                                    s = s + ";" + name + ";";
                                }


                            }
                        }
                        line += " " + s + ";";


                    }
                    line = line.substring(1, line.length() - 1).replace(";;", ";");

                    Log.d(TAG, "hr=" + hr);
                    if (!"".equals(hr)) {
                        String comp = hrcomps.get(hr);
                        if (comp != null) {
                            line = comp + ": " + line;
                        }
                    }

                    if (!"".equals(line)) list.add(line);
                }

                for (int i = 0; i < list.size(); i++) {
                    r = r + list.get(i);
                    if (i != list.size() - 1) {
                        r = r + "\n\n";
                    }
                }
                if (!"".equals(r)) {
                    TextView tv = (TextView) findViewById(R.id.cross_ref);
                    tv.setText(r);
                    tv.setVisibility(View.VISIBLE);

                    tv = (TextView) findViewById(R.id.cross_div);
                    tv.setVisibility(View.VISIBLE);
                }

                if (!nomore && isMoreNeeded(obj)) new MoreTask(objclone).execute();

                for (String name : set) {
                    Log.d(TAG, "refname=" + name);
                    //not starting for WDS as the same name is excluded from cross search
                    if (name.matches(WDS_0_9_0_9)) {
                        Log.d(TAG, "ds task name=" + name);
                        new DoubleStarTask(set).execute(name);
                        break;
                    }
                }
            } finally {
                unregisterTask(this);
            }

        }
    }

    class DoubleData {

        String comps;
        String sep;
        String mag;
        String mag2;
        String pa;

        public DoubleData(String comps, String sep, String mag, String mag2, String pa) {
            super();
            this.comps = comps;
            this.sep = sep;
            this.mag = mag;
            this.mag2 = mag2;
            this.pa = pa;
        }

        @Override
        public String toString() {
            return DOUBLE_DATA_COMPS + comps + ", sep=" + sep + ", mag=" + mag + ", mag2=" + mag2 + ", pa=" + pa + "]";
        }

    }

    class Rec {
        double pa;
        double sep;

        public Rec(double pa, double sep) {
            super();
            this.pa = pa;
            this.sep = sep;
        }

    }

    class DoubleStarTask extends AsyncTask<String, Void, List<DoubleData>> {

        Collection<String> refnames = null;

        public DoubleStarTask(Collection<String> refnames) {
            this.refnames = refnames;
        }

        @Override
        protected List<DoubleData> doInBackground(String... strings) {
            registerTask(this);
            List<DoubleData> list = new ArrayList<DoubleData>();
            String wds = strings[0].replace(WDS, "");
            String dbname = DbManager.getDbFileName(AstroCatalog.WDS);
            Db db = new Db(getApplicationContext(), dbname);
            Calendar c = AstroTools.getDefaultTime(getApplicationContext());
            int yearnow = c.get(Calendar.YEAR);
            double yearcalc = c.get(Calendar.DAY_OF_YEAR) / 365f + yearnow;
            try {
                db.open();
                Cursor cursor = db.rawQuery(SELECT_COMPONENTS_SEPARATION_MAG_MAG2_PA_YEAR_ID_FROM_CUSTOMDBB_WHERE_NAME1 + wds + "';");

                while (cursor.moveToNext()) {
                    String comps = cursor.getString(0);
                    String sep = cursor.getString(1);
                    String mag = cursor.getString(2);
                    String mag2 = cursor.getString(3);
                    String pa = cursor.getString(4);
                    String year = cursor.getString(5);
                    int id = cursor.getInt(6);
                    Rec rec = calc(id, yearcalc);

                    DoubleData data = new DoubleData(comps.replace(" ", ""), String.format(Locale.US, "%.1f", Double.parseDouble(sep)) + "\"", String.format(Locale.US, "%.1f", Double.parseDouble(mag)) + "m", String.format(Locale.US, "%.1f", Double.parseDouble(mag2)) + "m", String.format(Locale.US, "%.1f", Double.parseDouble(pa)) + '\u00B0' + " (" + year + ")");
                    list.add(data);
                    if (rec != null) {
                        DoubleData data2 = new DoubleData(comps.replace(" ", ""), String.format(Locale.US, "%.1f", rec.sep) + "\"", String.format(Locale.US, "%.1f", Double.parseDouble(mag)) + "m", String.format(Locale.US, "%.1f", Double.parseDouble(mag2)) + "m", String.format(Locale.US, "%.1f", rec.pa) + '\u00B0' + " (" + yearnow + ")");
                        list.add(data2);

                    }
                }
                cursor.close();
                db.close();

            } catch (Exception e) {
                Log.d(TAG, "e=" + e);
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<DoubleData> data) {
            try {
                if (data.size() > 0) {
                    LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = findViewById(R.id.double_div);
                    v.setVisibility(View.VISIBLE);
                    String comp = "";
                    int yale = 0;
                    if (objclone.getCatalog() == AstroCatalog.YALE_CATALOG || objclone.getCatalog() == AstroCatalog.TYCHO_CATALOG) {
                        if (objclone.getCatalog() == AstroCatalog.YALE_CATALOG) {
                            if (objclone instanceof HrStar) {
                                yale = ((HrStar) objclone).getHr();
                            }
                        } else {
                            if (refnames != null) {
                                for (String s : refnames) {
                                    if (s.matches(HR_0_92)) {
                                        yale = AstroTools.getInteger(s.replace("HR", ""), 0, 0, 1000000);
                                    }
                                }
                            }
                        }

                        Db db = null;
                        try {

                            db = new Db(getApplicationContext(), Constants.SQL_DATABASE_COMP_DB);
                            db.open();
                            Cursor cursor = db.rawQuery(SELECT_COMP_FROM_COMPONENTS_WHERE_NAME_HR + yale + "';");
                            while (cursor.moveToNext()) {
                                comp = cursor.getString(0);
                            }
                            cursor.close();
                        } catch (Exception e) {

                        } finally {
                            try {
                                db.close();
                            } catch (Exception e) {
                            }
                        }
                    } else if (objclone.getCatalog() == AstroCatalog.HAAS || objclone.getCatalog() == AstroCatalog.WDS) {
                        String dbname = DbManager.getDbFileName(objclone.getCatalog());
                        Db db = null;
                        try {
                            db = new Db(getApplicationContext(), dbname);
                            db.open();
                            Cursor cursor = db.rawQuery(SELECT_COMPONENTS_FROM_CUSTOMDBB_WHERE_ID + objclone.getId());
                            while (cursor.moveToNext()) {
                                comp = cursor.getString(0);
                            }
                            cursor.close();
                        } catch (Exception e) {

                            e.printStackTrace();
                        } finally {
                            try {
                                db.close();
                            } catch (Exception e) {
                            }
                        }

                    }


                    if (!"".equals(comp)) {
                        ((TextView) v).setText(getString(R.string.double_star_info_) + comp.replace(" ", "") + ")");
                    }


                    LinearLayout ll = (LinearLayout) findViewById(R.id.double_ll);
                    ll.setVisibility(View.VISIBLE);
                    for (DoubleData d : data) {
                        View view = mInflater.inflate(R.layout.details_double_star, null);

                        TextView v2 = (TextView) view.findViewById(R.id.ds_1);
                        v2.setText(d.comps);

                        v2 = (TextView) view.findViewById(R.id.ds_2);
                        v2.setText(d.mag);

                        v2 = (TextView) view.findViewById(R.id.ds_3);
                        v2.setText(d.mag2);


                        v2 = (TextView) view.findViewById(R.id.ds_4);
                        v2.setText(d.sep + " at " + d.pa);

                        ll.addView(view);
                    }
                }
            } finally {
                unregisterTask(this);
            }
        }

        public Rec calc(int wdsid, double date) throws Exception {
            Db db = new Db(getApplicationContext(), Constants.SQL_DATABASE_OREL_DB);
            try {
                Log.d(TAG, "calc, wdsid=" + wdsid);
                db.open();
                Cursor cursor = db.rawQuery(SELECT_PERIOD_AXIS_INCL_OMEGA1_PERIT_E_OMEGA2_FROM_ELEMENTS_WHERE_WDSID + wdsid);
                Log.d(TAG, "calc, size=" + cursor.getCount());
                if (cursor.moveToNext()) {
                    double period = Double.parseDouble(cursor.getString(0));
                    double axis = Double.parseDouble(cursor.getString(1));
                    double incl = Double.parseDouble(cursor.getString(2));
                    double omega1 = Double.parseDouble(cursor.getString(3));
                    double periT = Double.parseDouble(cursor.getString(4));
                    double e = Double.parseDouble(cursor.getString(5));
                    double omega2 = Double.parseDouble(cursor.getString(6));

                    double Y = date - periT;
                    double M = Math.PI / 180 * 360 * (Y / period - (int) (Y / period));

                    int i = 0;
                    double E = M;
                    while (i++ < 100) {
                        E = M + e * Math.sin(E);
                    }
                    double v = 180 / Math.PI * (2 * Math.atan(Math.sqrt((1 + e) / (1 - e)) * Math.tan(E / 2)));
                    double r = axis * (1 - e * Math.cos(E));
                    double y = Math.sin(Math.PI / 180 * (v + omega2)) * Math.cos(Math.PI / 180 * incl);
                    double x = Math.cos(Math.PI / 180 * (v + omega2));
                    double at2 = 180 / Math.PI * Math.atan2(y, x);
                    double pa = at2 + omega1;

                    pa = AstroTools.normalise(pa);
                    double sep = r * Math.cos(Math.PI / 180 * (v + omega2)) / Math.cos(Math.PI / 180 * (pa - omega1));
                    cursor.close();
                    return new Rec(pa, sep);
                }
                cursor.close();
                return null;


            } catch (Exception e) {
                Log.d(TAG, "e=" + e);
                return null;
            } finally {
                try {
                    db.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * updating location
     *
     * @author leonid
     */
    class LocationTask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... ds) {
            registerTask(this);
            double lat = ds[0];
            double lon = ds[1];
            String name = AstroTools.getLocationName(lat, lon, getApplicationContext());
            return name;
        }

        @Override
        protected void onPostExecute(String locname) {

            if (locname != null) {
                TextView lattext = (TextView) findViewById(R.id.lat_text);
                TextView lontext = (TextView) findViewById(R.id.lon_text);

                lontext.setVisibility(View.GONE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) lattext.getLayoutParams();
                lp.weight = 11;
                lattext.setLayoutParams(lp);
                lattext.setText(locname);
            }
            unregisterTask(this);
        }
    }

    /**
     * looking for more objects with the same name
     *
     * @author leonid
     */
    class MoreTask extends AsyncTask<Void, Void, List<AstroObject>> {
        AstroObject obj;

        public MoreTask(AstroObject obj) {
            this.obj = obj;
        }

        @Override
        protected List<AstroObject> doInBackground(Void... voids) {
            registerTask(this);
            List<AstroObject> list = new ArrayList<AstroObject>();
            list.addAll(getMoreObjects(obj));
            if (crossnames != null) {
                List<AstroObject> list2 = getCrossObjects(crossnames);
                for (AstroObject o : list2) {
                    if (o.getCatalog() == obj.getCatalog() && o.getId() == obj.getId()) continue;
                    boolean contains = false;
                    for (AstroObject obj2 : list) {
                        if (o.getCatalog() == obj2.getCatalog() && o.getId() == obj2.getId()) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) list.add(o);
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<AstroObject> result) {
            try {
                moreObjects.addAll(result);
                Collections.sort(moreObjects, new AODBsorter());

                for (Object o : moreObjects) {
                    Log.d(TAG, "moreObjects element" + o);
                }

                if (moreObjects.size() > 0) {
                    Button btn = new Button(DetailsActivity.this);
                    btn.setText(R.string._more_);
                    float text_size = DetailsActivity.this.getResources().getDimension(R.dimen.button_text_size);
                    float density = getResources().getDisplayMetrics().density;
                    text_size = text_size / density;
                    btn.setTextSize(text_size);
                    LinearLayout ll = (LinearLayout) findViewById(R.id.details_ll);
                    ll.addView(btn);
                    btn.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            LinearLayout ll = (LinearLayout) findViewById(R.id.details_ll);
                            ll.removeView(v);//removing button on pressing
                            showMoreInfo();

                        }
                    });
                }
            } finally {
                unregisterTask(this);
            }
        }
    }

    private Set<Object> taskset = new HashSet<Object>();

    private synchronized void registerTask(Object task) {
        boolean empty = (taskset.size() == 0);
        taskset.add(task);
        if (empty) {
            runOnUiThread(new Runnable() {
                public void run() {
                    setProgressBarIndeterminateVisibility(true);
                }
            });

        }

    }

    private synchronized void unregisterTask(Object task) {
        taskset.remove(task);
        boolean empty = (taskset.size() == 0);
        if (empty) {
            setProgressBarIndeterminateVisibility(false);
        }
    }


    boolean initRequired = false;//global init

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        initHandler.handleMessage(null);
    }

    /**
     * remove some of the fields that are empty (comment, dimension, visibility,pa)
     */
    private void removeEmptyFields() {
        for (Map.Entry<Integer, Integer> e : mapFields.entrySet()) {
            TextView tv = ((TextView) findViewById(e.getKey()));
            CharSequence text = tv.getText();
            if (text == null || "".equals(text.toString())) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.details_ll);
                ll.removeView(findViewById(e.getValue()));
            }
        }

    }

    private String getTitleName(AstroObject obj) {
        if (obj.getCatalog() == AstroCatalog.MESSIER || obj.getCatalog() == AstroCatalog.CALDWELL)
            return obj.getCatalogName() + ": " + obj.getLongName() + " " + obj.getShortName();//+" ("+obj.getTypeString()+")";
        else return obj.getCatalogName() + ": " + obj.getLongName();//+" ("+obj.getTypeString()+")";
    }

    /**
     * showing info on More pressed
     */
    private void showMoreInfo() {
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout ll = (LinearLayout) findViewById(R.id.details_ll);
        for (AstroObject obj : moreObjects) {
            //if (obj instanceof CustomObject){
            class OnClick extends Command {
                AstroObject obj;

                public OnClick(AstroObject obj) {
                    this.obj = obj;
                }

                public void execute() {
                    Runnable r = new Runnable() {
                        public void run() {
                            Log.d(DetailsActivity.TAG, "obj=" + obj);
                            new DetailsCommand(obj, DetailsActivity.this, true).execute();
                        }
                    };
                    registerDialog(AstroTools.getDialog(DetailsActivity.this, DetailsActivity.this.getString(R.string.this_will_take_you_to_another_details_screen), r)).show();

                }
            }

            View v = mInflater.inflate(R.layout.details_name_text, null);
            TextView tv = (TextView) v.findViewById(R.id.f_label);
            tv.setText(getTitleName(obj));
            final AstroObject ob = obj;
            tv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    new OnClick(ob).execute();

                }
            });
            ll.addView(v);


            if (obj instanceof CustomObject)
                showCustomFields((CustomObject) obj, null, true);//new OnClick(obj));
        }
    }

    /**
     * @param custom
     * @param command
     * @param comment
     * @return true if non empty
     */
    private boolean showCustomFields(CustomObject custom, final Command command, boolean comment) {
        boolean nonempty = false;

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Map<String, String> map = new TreeMap<String, String>();
        if (comment) {
            String com = custom.getComment();
            map.put(COMMENT2, com);
            if (!"".equals(com)) nonempty = true;
        }
        Map<String, String> mapUrl = null;
        Map<String, String> mapPhoto = null;
        if (custom instanceof CustomObjectLarge) {
            CustomObjectLarge col = (CustomObjectLarge) custom;
            map.putAll(col.getFields().getFieldMap());
            mapUrl = col.getFields().getUrlMap();
            mapPhoto = col.getFields().getPhotoMap();
        }

        List<View> list = new ArrayList<View>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            if ("".equals(e.getValue()))//omitting empty fields
                continue;
            nonempty = true;
            View v = mInflater.inflate(R.layout.details_relative_layout, null);
            if (command != null) v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    command.execute();
                }
            });
            TextView tvField = (TextView) v.findViewById(R.id.f_label);
            TextView tvText = (TextView) v.findViewById(R.id.f_text);
            if (e.getKey().equals(COMMENT3)) {
                tvField.setVisibility(View.GONE);
            }
            tvField.setText(e.getKey());
            tvText.setText(e.getValue());
            if (mapUrl != null && mapUrl.containsKey(e.getKey())) {//url type, needs linkifying
                Pattern pattern = Pattern.compile(e.getValue());
                Linkify.addLinks(tvText, pattern, HTTP);
            }

            if (mapPhoto != null && mapPhoto.containsKey(e.getKey())) {//photo field, need action
                final String dataLocation = e.getValue();
                if (dataLocation != null) {
                    tvText.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if ("".equals(dataLocation)) {
                                return;
                            }
                            List<String> list = new ArrayList<String>();
                            list.add(dataLocation);
                            Intent w = new Intent(DetailsActivity.this, PictureActivity.class);
                            w.putExtra(PictureActivity.NAME, obj.getLongName());
                            String[] arr = list.toArray(new String[list.size()]);
                            w.putExtra(PictureActivity.ARRAY, arr);
                            DetailsActivity.this.startActivity(w);

                        }
                    });
                }
            }
            list.add(v);
        }
        LinearLayout ll = (LinearLayout) findViewById(R.id.details_ll);
        if (custom.getCatalog() == AstroCatalog.COMET_CATALOG || custom.getCatalog() == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) {
            list = changeFieldsOrderForCMO(list);
        }
        for (View v : list) {
            ll.addView(v);
        }
        return nonempty;
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
    private List<View> changeFieldsOrderForCMO(List<View> list) {
        String[] first_fields = new String[]{CMO.YEAR, CMO.MONTH, CMO.DAY};
        Map<String, Integer> map = new HashMap<String, Integer>();
        View[] arr = new View[list.size()];

        for (int i = 0; i < first_fields.length; i++) {
            map.put(first_fields[i], i);
        }
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }


        for (int i = 0; i < arr.length; i++) {
            View view = arr[i];
            TextView tvField = (TextView) view.findViewById(R.id.f_label);
            Integer pos = map.get(tvField.getText().toString());
            if (pos != null && i != pos) {
                swap(arr, i, pos);
            }

        }

        return Arrays.asList(arr);
    }

    private List<AstroObject> getCrossObjects(List<String> crossnames) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        if (crossnames == null) {
            return list;
        }
        for (String s : crossnames) {
            Set<Integer> set = new HashSet<Integer>();
            int db = SearchRules.getAssociatedDatabase(s);
            if (db == -1) {
                set.add(AstroCatalog.SAC);
                set.add(AstroCatalog.HAAS);

            } else if (db == AstroCatalog.YALE_CATALOG) {
                HrStar star = AstroTools.getHrStar(s);
                if (star != null) list.add(star);
                continue;
            } else set.add(db);

            for (int dbb : set) {
                AstroCatalog cdb;


                cdb = SearchRules.getCatalog(dbb, getApplicationContext());
                if (cdb == null) continue;
                ErrorHandler eh = new ErrorHandler();
                cdb.open(eh);
                if (!eh.hasError()) {

                    if (dbb == AstroCatalog.WDS) list.addAll(cdb.searchName(s.replace(WDS, "")));
                    else list.addAll(cdb.searchName(s));
                    cdb.close();
                }
            }

        }
        return list;
    }

    private Collection<AstroObject> getMoreObjects(AstroObject obj) {
        MySet set = new MySet();
        if (obj instanceof NgcicObject) {
            NgcicObject o = (NgcicObject) obj;


            set.addAll2(SearchResultActivity.makeSearchByName(o.getNgcIcName(), this));

            String messier = o.getMessierName();
            if (!"".equals(messier)) {
                set.addAll2(SearchResultActivity.makeSearchByName(o.getMessierName(), this));

            }
            String caldwell = o.getCaldwellName();
            if (!"".equals(caldwell)) {

                set.addAll2(SearchResultActivity.makeSearchByName(o.getCaldwellName(), this));
            }
            set.remove(new HolderA(obj));
            return set.get();

        }

        set.addAll2(SearchResultActivity.makeSearchByName(obj.getShortName(), this));
        set.addAll2(SearchResultActivity.makeSearchByName(obj.getLongName(), this));
        set.remove(new HolderA(obj));

        Log.d(TAG, "set=" + set);
        return set.get();
    }

    /**
     * Sorting AstroObject Set by db name
     *
     * @param s
     * @return
     */
    private Set<AstroObject> sortSet(Set s) {
        class AODBsorter implements Comparator<AstroObject> {
            @Override
            public int compare(AstroObject o1, AstroObject o2) {
                return o1.getCatalogName().compareTo(o2.getCatalogName());
            }
        }
        Set<AstroObject> treeSet = new TreeSet<AstroObject>(new AODBsorter());
        treeSet.addAll(s);
        return s;
    }

    public static boolean isPictureAvailable(Context context, Object obj) {
        if (obj != null && obj instanceof NgcicObject) {
            int ngc = ((NgcicObject) obj).ngc;

            String path = AstroTools.getNgcPic(ngc);
            if (path != null) return true;


        }
        if (obj instanceof AstroObject) {
            AstroObject o = (AstroObject) obj;
            int ngc = 0;
            if (o.getCatalog() == AstroCatalog.MESSIER || o.getCatalog() == AstroCatalog.CALDWELL || o.getCatalog() == AstroCatalog.SAC) {
                ngc = AstroTools.getInteger(o.getLongName().replace(NGC2, ""), 0, 0, 100000);
                Log.d(TAG, "ngc=" + ngc + " long name=" + o.getLongName());
            }
            if (ngc != 0) {
                String path = AstroTools.getNgcPic(ngc);
                if (path != null) {
                    return true;
                }
            }

            List<String> list2 = getPicByRef(context, o);
            if (list2.size() > 0) return true;

        }
        if (obj != null && obj instanceof CustomObjectLarge) {
            return ((CustomObjectLarge) obj).arePhotosAvailable(context);
        }
        return false;
    }

    public static List<String> getPicturePaths(Context context, Object obj) {
        List<String> list = new ArrayList<String>();
        //Log.d(TAG,"getPicPaths "+obj);
        if (obj == null) return list;
        if (obj instanceof NgcicObject) {
            int ngc = ((NgcicObject) obj).ngc;

            String path = AstroTools.getNgcPic(ngc);
            if (path != null) {
                path = Uri.fromFile(new File(path)).toString();
                list.add(path);
                return list;
            }

        }
        if (obj instanceof AstroObject) {
            AstroObject o = (AstroObject) obj;
            int ngc = 0;
            if (o.getCatalog() == AstroCatalog.MESSIER || o.getCatalog() == AstroCatalog.CALDWELL || o.getCatalog() == AstroCatalog.SAC) {
                ngc = AstroTools.getInteger(o.getLongName().replace(NGC2, ""), 0, 0, 100000);
                Log.d(TAG, "ngc=" + ngc + " long name=" + o.getLongName());
            }
            if (ngc != 0) {
                String path = AstroTools.getNgcPic(ngc);
                if (path != null) {
                    path = Uri.fromFile(new File(path)).toString();
                    list.add(path);
                    return list;
                }
            }
            List<String> list2 = getPicByRef(context, o);
            if (list2.size() > 0) return list2;
        }

        if (obj instanceof CustomObjectLarge) {
            list = ((CustomObjectLarge) obj).getAvailablePhotos(context);
            return list;
        }
        return list;
    }


    private static List<String> getPicByRef(Context context, AstroObject obj) {
        List<String> list = new ArrayList<String>();
        if (obj.ref == 0) return list;
        Db db = new Db(context, Constants.NGCIC_DATABASE_NAME);
        try {
            db.open();
            Cursor cursor = db.rawQuery(SELECT_NAME_FROM_NGCIC_WHERE_REF + obj.ref);
            while (cursor.moveToNext()) {
                int ngc = cursor.getInt(0);
                if (ngc < NgcicObject.IC_THRESHOLD) {
                    String path = AstroTools.getNgcPic(ngc);
                    if (path != null) {
                        path = Uri.fromFile(new File(path)).toString();
                        list.add(path);
                        return list;
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        } finally {
            try {
                db.close();
            } catch (Exception e) {
            }
        }
        return list;
    }

    public static String makeDateString(Calendar c, boolean addYear) {
        if (c != null) {
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1; //january equals 0
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            String dateStr = makeNumber(month) + "." + makeNumber(day);
            if (addYear) dateStr = year + "." + dateStr;
            return dateStr;
        } else return STRING;
    }

    /**
     * @param c           - date/time of the event	 *
     * @param ref         - reference date. > is put before the time if next day, < if prev day
     * @param showSeconds
     * @return
     */
    public static String makeTimeString(Calendar c, Calendar ref, boolean showSeconds) {

        if (c == null) return "---";

        Calendar refb = Calendar.getInstance();
        refb.setTimeInMillis(ref.getTimeInMillis());
        refb.set(Calendar.HOUR_OF_DAY, 0);
        refb.set(Calendar.MINUTE, 0);
        refb.set(Calendar.SECOND, 0);
        refb.set(Calendar.MILLISECOND, 0);

        Calendar refe = Calendar.getInstance();
        refe.setTimeInMillis(ref.getTimeInMillis());
        refe.set(Calendar.HOUR_OF_DAY, 23);
        refe.set(Calendar.MINUTE, 59);
        refe.set(Calendar.SECOND, 59);
        refe.set(Calendar.MILLISECOND, 999);
        boolean nextday = false;
        boolean prevday = false;
        if (c.getTimeInMillis() < refb.getTimeInMillis()) prevday = true;
        else if (c.getTimeInMillis() > refe.getTimeInMillis()) nextday = true;


        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        String timeStr;
        if (showSeconds)
            timeStr = makeNumber(hour) + ":" + makeNumber(minute) + ":" + makeNumber(sec);
        else timeStr = makeNumber(hour) + ":" + makeNumber(minute);


        if (prevday) timeStr = "<" + timeStr;
        else if (nextday) timeStr = ">" + timeStr;

        return timeStr;
    }


    public static String makeTimeString(Calendar c, boolean showSeconds) {
        if (c != null) {
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1; //january equals 0
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);
            String timeStr;
            if (showSeconds)
                timeStr = makeNumber(hour) + ":" + makeNumber(minute) + ":" + makeNumber(sec);
            else timeStr = makeNumber(hour) + ":" + makeNumber(minute);

            return timeStr;
        } else return "---";
    }

    //Short international time stamp notation
    public static String makeShortDateTimeString(Calendar c) {
        if (c != null) {
            return String.format(Locale.US, "%04d.%02d.%02d %02d:%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        } else return STRING;
    }

    private static String makeNumber(int i) {
        String s = String.valueOf(i);
        if (i < 10) s = "0" + s;
        return s;
    }

    public static String doubleToGrad(double d, char ch1, char ch2) {
        //Log.d(TAG,"d="+d);
        int sign;
        if (d < 0) sign = -1;
        else sign = 1;
        d = abs(d);
        int grad = (int) d;
        //Log.d(TAG,"grad="+grad);
        double min = (d - grad) * 60;
        String s = makeNumber(grad) + ch1;
        String min_str = String.format(Locale.US, "%.1f", min);
        if (min < 10 && !min_str.matches("10.*"))//to avoid 9.999
            s = s + "0" + min_str + ch2;
        else s = s + String.format(Locale.US, "%.1f", min) + ch2;
        if (sign < 0) s = "-" + s;
        return s;

    }

    /**
     * @param s - string like ngc231, ic2
     * @return number to search for in ngcic db, or -1 if noresult
     */
    private static int getNumAfterPrefix(String prefix, String s) {
        //String prefix="NGC";
        Log.d(TAG, "prefix=" + prefix + " s=" + s);
        String up = s.toUpperCase();
        if (up.length() <= prefix.length()) return -1;
        if (up.startsWith(prefix.toUpperCase())) {
            String s1 = up.substring(prefix.length());
            Log.d(TAG, "s1=" + s1);
            int num = SettingsActivity.getInt(s1, -1, -2, 1000000);
            return num;
        }
        return -1;
    }

    /**
     * @param queryString
     * @param context     - need real context, eg for showing errors
     * @return
     */
    static Point Search(String queryString, Context context) { //process search request
        //Log.d(TAG,"queryString="+queryString);
        String[] from = {_ID, NAME, RA, DEC, MESSIER, CALDWELL, HERSHELL, CONSTEL, TYPE, MAG, A, B, PA};
        if ((queryString == null) || (queryString.compareTo("") == 0)) return null;

        String s = queryString.toUpperCase();
        if (s.compareTo(context.getString(R.string.venus)) == 0) {
            return Global.venus;
        }
        if (s.compareTo(context.getString(R.string.mars)) == 0) {
            return Global.mars;
        }
        if (s.compareTo(context.getString(R.string.moon)) == 0) return Global.moon;
        if (s.compareTo(context.getString(R.string.jupiter)) == 0) return Global.jupiter;
        if (s.compareTo(context.getString(R.string.sun)) == 0) return Global.sun;
        if (s.compareTo(context.getString(R.string.saturn)) == 0) return Global.saturn;

        if (s.compareTo(context.getString(R.string.uranus)) == 0) return Global.uranus;
        if (s.compareTo(context.getString(R.string.neptune)) == 0) return Global.neptune;
        if (s.compareTo(context.getString(R.string.mercury)) == 0) return Global.mercury;

        int m = 0;
        int c = 0;
        int num = 0;
        int i = 0;//IC catalog
        int h = 0;//HR catalog
        char ch = Character.toUpperCase(queryString.charAt(0));
        try {
            switch (ch) {
                case 'M':
                    String s2 = queryString.substring(1);
                    Log.d(TAG, "s2=" + s2);
                    m = Integer.parseInt(s2);
                    Log.d(TAG, "m=" + m);
                    break;
                case 'C':
                    c = Integer.parseInt(queryString.substring(1));
                    break;

                case 'H':
                    h = Integer.parseInt(queryString.substring(1));
                    break;
                default:
                    num = getNumAfterPrefix(NGC, queryString);
                    if (num != -1) break;
                    num = getNumAfterPrefix(IC, queryString);
                    if (num != -1) {
                        num = 10000 + num;
                        break;
                    }
                    num = getNumAfterPrefix("i", queryString);
                    if (num != -1) {
                        num = 10000 + num;
                        break;
                    }
                    num = Integer.parseInt(queryString);
            }
        } catch (NumberFormatException e) {
            Log.d(TAG, "NumberFormatException" + e);
            return null;
        }

        Log.d(TAG, "num=" + num);
        String squery = null;
        if (m > 0) squery = MESSIER + "=" + String.valueOf(m);
        else {
            if (c > 0) squery = CALDWELL + "=" + String.valueOf(c);
            else if (i > 0) squery = NAME + "=" + String.valueOf(10000 + i);
            else if (h > 0) {
                if (h > 9095) return null;
                HrStar st = new HrStar(Global.databaseHr[StarData.ConvHrToRow(h)]);
                if (st.getHr() != h) return null;
                return st;
            } else squery = NAME + "=" + String.valueOf(num);
        }
        AstroCatalog catalog;
        catalog = new NgcicDatabase(context);
        ErrorHandler eh = new ErrorHandler();
        catalog.open(eh);
        if (eh.hasError()) {
            Log.d(TAG, "error=" + eh.getErrorString());
            return null;
        }
        Log.d(TAG, "start search " + squery);
        List<AstroObject> list = catalog.search(squery);
        catalog.close();
        Log.d(TAG, "end search " + squery);
        AstroObject obj = null;
        Log.d(TAG, "Search method, list size=" + list.size());
        if (list.size() > 0) {
            obj = list.get(0);
            Log.d(TAG, "" + obj);
        }

        return obj;
    }


    //overriding buttons
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            //aMenu.show(bN);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
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
