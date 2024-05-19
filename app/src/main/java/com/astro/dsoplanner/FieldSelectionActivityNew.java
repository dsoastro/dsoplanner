package com.astro.dsoplanner;

import static com.astro.dsoplanner.Constants.A;
import static com.astro.dsoplanner.Constants.B;
import static com.astro.dsoplanner.Constants.COMMENT;
import static com.astro.dsoplanner.Constants.CONSTEL;
import static com.astro.dsoplanner.Constants.DEC;
import static com.astro.dsoplanner.Constants.MAG;
import static com.astro.dsoplanner.Constants.PA;
import static com.astro.dsoplanner.Constants.RA;
import static com.astro.dsoplanner.Constants.TYPE;
import static com.astro.dsoplanner.Constants.constellations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.DbManager;

public class FieldSelectionActivityNew extends ParentActivity implements OnGestureListener {
    private static final String A_Z_A_Z_A_Z_A_Z0_9 = "[a-zA-Z][a-zA-Z0-9]+";
    private static final String DELETE_IT = "<Delete it>";
    private DbListItem.FieldTypes fields = new DbListItem.FieldTypes();
    private TextView message;

    private Map<Button, Button> fieldsMap = new LinkedHashMap<>();
    private Map<Button, View> mapLayout = new HashMap<Button, View>();//association of button with a layout that holds the button

    /**
     * Menu for the selector dialog.
     */
    private static final String[] btnTypes = new String[DbListItem.FieldTypes.TYPE.values().length];
    static {
        int i = 0;
        for (DbListItem.FieldTypes.TYPE type : DbListItem.FieldTypes.TYPE.values()) {
            btnTypes[i++] = type.name;
        }
        //Move `Delete` to be first on the list
        // as the user may not read to the end of it to realize the delete command is there too.
        btnTypes[0] = DELETE_IT;
    }

    boolean dirty = false;
    private String dbName = "";//database name
    LinearLayout layoutMain;

    @Override
    protected void onResume() {
        super.onResume();
        hideMenuBtn();
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);

    }

    private Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setContentView(R.layout.add_fields);

            String name = SettingsActivity.getStringFromSharedPreferences(FieldSelectionActivityNew.this, Constants.FSA_DBNAME, "");

            dbName = name;
            if ("".equals(dbName)) {
                finish();
                return;
            }
            //Add new field button
            View btnAdd = findViewById(R.id.fsa_btn);
            btnAdd.setOnClickListener(new View.OnClickListener() {

                public void onClick(View arg0) {
                    //construct the new EditText input field
                    layoutMain = (LinearLayout) findViewById(R.id.fsa_ll);
                    LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View v = mInflater.inflate(R.layout.fsa_item, null);

                    //DB field name input
                    final Button bnName = v.findViewById(R.id.fsa_name);
                    bnName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AstroTools.NameDialogRunnable r = new AstroTools.NameDialogRunnable() {
                                String s;

                                public void setName(String s) {
                                    this.s = s;
                                }

                                public void run() {
                                    final Pattern p = Pattern.compile(A_Z_A_Z_A_Z_A_Z0_9);
                                    Matcher m = p.matcher(s);
                                    if (!m.matches()) {
                                        ErrorHandler eh = new ErrorHandler(ErrorHandler.DATA_CORRUPTED, getString(R.string.wrong_field_format));
                                        eh.showError(FieldSelectionActivityNew.this);
                                    } else bnName.setText(s);
                                }
                            };
                            //Pre-set the name of the field to edit (make empty if never set)
                            String textToSet = bnName.getText().toString();
                            if (textToSet.contentEquals(bnName.getResources().getString(R.string.name2))) {
                                textToSet = "";
                            }
                            InputDialog d = AstroTools.getNameDialog(FieldSelectionActivityNew.this, getString(R.string.set_custom_field_name), textToSet, r);
                            registerDialog(d).show();
                        }
                    });

                    //DB field type button
                    final Button bnType = v.findViewById(R.id.fsa_type);
                    bnType.setText(btnTypes[1]); //string is default pre-selected type from the list

                    mapLayout.put(bnType, v);
                    layoutMain.addView(v);

                    fieldsMap.put(bnType, bnName);
                    dirty = true;
                    bnType.setOnClickListener(new View.OnClickListener() {
                        public void onClick(final View arg0) {
                            final InputDialog d = new InputDialog(FieldSelectionActivityNew.this);
                            d.setTitle(getString(R.string.select_field_type));
                            d.setMessage("");
                            int value = 0;
                            String btnText = bnType.getText().toString();
                            value = getType(btnText);
                            d.setValue(String.valueOf(value));
                            d.setListItems(btnTypes, new InputDialog.OnButtonListener() {
                                public void onClick(final String selection) {
                                    int id = AstroTools.getInteger(selection, 0, -1, 1000);
                                    DbListItem.FieldTypes.TYPE type = DbListItem.FieldTypes.TYPE.valueOf(id);
                                    if (type == DbListItem.FieldTypes.TYPE.NONE) {//delete (also if selection is unknown)
                                        final Runnable deleteAction = new Runnable() {
                                            public void run() {
                                                layoutMain.removeView(mapLayout.get(arg0));
                                                fieldsMap.remove(arg0);
                                            }
                                        };

                                        AstroTools.getDialog(FieldSelectionActivityNew.this, getString(R.string.do_you_want_to_delete_the_field_), deleteAction).show();
                                    } else ((Button) arg0).setText(btnTypes[id]);
                                }
                            });
                            registerDialog(d).show();
                        }
                    });

                }
            });

            //Save new fields Button
            View btnOK = findViewById(R.id.fsa_ok_btn);
            btnOK.setOnClickListener(new View.OnClickListener() {

                public void onClick(View arg0) {
                    for (Map.Entry<Button, Button> e : fieldsMap.entrySet()) {
                        String fieldName = e.getValue().getText().toString();
                        if (!checkIfStandardField(fieldName)) {
                            registerDialog(InputDialog.message(FieldSelectionActivityNew.this, getString(R.string.the_field_name_) + fieldName + getString(R.string._is_reserved_please_choose_a_different_one), 0)).show();
                            return;
                        }
                        if (!checkIfFieldNamesAreDifferent()) {
                            registerDialog(InputDialog.message(FieldSelectionActivityNew.this, R.string.some_field_names_coincide_please_choose_different_names, 0)).show();
                            return;
                        }
                        if (!"".equals(fieldName)) {
                            String typeName = e.getKey().getText().toString();
                            fields.put(fieldName, DbListItem.FieldTypes.TYPE.fromString(typeName));
                        }

                    }

                    if (!fields.isEmpty()) {//adding database info
                        DbManager.addNewDatabase(fields, FieldSelectionActivityNew.this, dbName, true, null);
                        finish();
                    } else {
                        DbManager.addNewDatabase(new DbListItem.FieldTypes(), FieldSelectionActivityNew.this, dbName, true, null);
                        registerDialog(InputDialog.abort(FieldSelectionActivityNew.this, getString(R.string.database_without_custom_fields_has_been_created_))).show();
                    }
                }
            });

            View btnHelp = findViewById(R.id.fsa_help_btn);
            btnHelp.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    InputDialog dh = new InputDialog(FieldSelectionActivityNew.this);
                    dh.setMessage(getString(R.string.default_existing_database_fields_are_));
                    dh.setValue(getString(R.string._name1_short_name_name2_long_name_etc));
                    dh.setType(InputDialog.DType.INPUT_TEXT);
                    dh.setFreeze();
                    registerDialog(dh).show();
                }
            });
        }
    };
    boolean initRequired = false;//global init

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHandler.handleMessage(null);
    }

    /**
     * @param field
     * @return checks that the field is not a standard field used in databases throughout the program. true - no coincidence, false - coincidence
     */
    private boolean checkIfStandardField(String field) {
        String[] sf = new String[]{CustomDatabase.NAME1, CustomDatabase.NAME2, CustomDatabase.TYPESTR, COMMENT, MAG, RA, DEC, A, B, CONSTEL, TYPE, PA, "m", "c", "h", "GC", "GX", "GXYCLD", "HIIRGN", "NEB", "OC", "OCN", "PN", "SNR", "con", "id", "_id", "ref"};
        List<String> standardFields = new ArrayList<String>();
        for (int i = 1; i < constellations.length; i++) {
            standardFields.add(constellations[i]);
        }
        for (String s : sf) {
            standardFields.add(s);
        }
        for (String s : standardFields) {
            String f = field.replace(" ", "");
            f = f.toUpperCase();
            s = s.toUpperCase();
            if (s.equals(f)) return false;
        }
        return true;
    }

    /**
     * @return true if field names are different, false otherwise
     */
    private boolean checkIfFieldNamesAreDifferent() {
        Set<String> set = new HashSet<String>();
        for (Map.Entry<Button, Button> e : fieldsMap.entrySet()) {
            String fieldName = e.getValue().getText().toString();
            fieldName = fieldName.replace(" ", "").toUpperCase();
            if (set.contains(fieldName)) return false;
            else set.add(fieldName);
        }
        return true;
    }

    //overriding buttons
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dirty) {//fields were added
                registerDialog(InputDialog.abort(FieldSelectionActivityNew.this, getString(R.string.you_decided_to_cancel_adding_new_database_))).show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private int getType(String s) {
        for (int i = 0; i < btnTypes.length; i++) {
            if (btnTypes[i].equals(s)) {
                return i;
            }
        }
        return 0;
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