package com.astro.dsoplanner;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.astro.dsoplanner.InputDialog.OnButtonListener;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.database.Db;
import com.astro.dsoplanner.database.NoteDatabase;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.infolist.ObsInfoListImpl;

//observation note
public class NoteActivity extends ParentActivity implements OnGestureListener, ActivityCompat.OnRequestPermissionsResultCallback {


    private static final String DIRTY2 = "dirty";
    private static final String NOTE2 = "note";
    private static final String DATE = "date";
    private static final String PATH = "path";
    private static final String _3GP = ".3gp";
    private static final String STRING = "string";
    private static final String BUNDLE = "bundle";


    private static final String TAG = NoteActivity.class.getSimpleName();
    protected static final int SET_TIME = 1;
    protected static final int SET_NOTE = 2;
    private static final int CALL_EYEPIECES_REQUEST = 3;
    private static final int CALL_TELESCOPE_REQUEST = 4;
    private static final int CALL_WORLD_CITIES_REQUEST = 5;
    /**
     * user location
     */
    private static final int CALL_LOCATION_LIST_REQUEST = 6;

    private static final int requestCode = 20;

    EditText note;
    private MyDateDialog dd;//used for running set date and set time requests
    private Calendar c = Calendar.getInstance();
    TextView tv;
    AstroObject obj;
    NoteRequest request;
    Player mPlayer;
    String mPath = "";
    boolean isRecording = false;//whether recording is on
    boolean isPlaying = false;//whether playing is on

    boolean dirty = false;
    String initialNote = "";

    boolean permissions_requested = false;

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRecording) {
            Log.d(TAG, "onStop");
            stopRecoring();
            releasePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideMenuBtn();

        if (!permissions_requested) {
            //ask just once
            permissions_requested = true;

            Runnable r = new Runnable() {
                @Override
                public void run() {

                }
            };
            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
            };

            String explanation = getString(R.string.microphone_permission);
            AstroTools.askForPermissionsOrRun(this, r, permissions,
                    explanation, requestCode, true);
        }

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState");
        savedInstanceState.putString(PATH, mPath);
        savedInstanceState.putBoolean(DIRTY2, dirty);
        savedInstanceState.putLong(DATE, c.getTimeInMillis());
        String noteText = note.getText().toString();
        if (isRecording)//onStop is called after onSaveInsState, thus audio record is not added to text just there
            savedInstanceState.putString(NOTE2, getNoteTextWithAudioString(noteText));
        else
            savedInstanceState.putString(NOTE2, noteText);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.notecomment);

        boolean restored = false;
        String rpath = null;
        long rdate = -1;
        String rnote = null;
        if (savedInstanceState != null) {
            rpath = savedInstanceState.getString(PATH);
            rdate = savedInstanceState.getLong(DATE, -1);
            rnote = savedInstanceState.getString(NOTE2);
            dirty = savedInstanceState.getBoolean(DIRTY2, false);
        }


        Bundle b = getIntent().getBundleExtra(BUNDLE);
        if (b == null) {
            finish();
            return;
        }

        String text = "";
        request = new NoteRequest(b);
        Log.d(TAG, "" + request);
        if (request.record != null) {
            mPath = (rpath == null ? request.record.path : rpath);
            text = (rnote == null ? request.getRecord().note : rnote);
        } else {
            mPath = (rpath == null ? "" : rpath);
            text = (rnote == null ? "" : rnote);
        }

        c.setTimeInMillis(rdate == -1 ? request.getRecord().date : rdate);

        dd = new MyDateDialog(this, c, new MyDateDialog.Updater() {//after setting date or time need to update the screen
            public void update() {
                NoteActivity.this.update();
            }
        });

        note = (EditText) findViewById(R.id.object_note);
        note.setText(text);
        initialNote = text;

        note.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Log.d(TAG,"edit text clicked");
                dirty = true;
            }
        });
        if (SettingsActivity.getNightMode()) {//so custom keyboard is used
            note.setFocusable(false); //to prevent stock keyboard popup
            note.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    final InputDialog id = new InputDialog(NoteActivity.this);
                    id.setTitle(getString(R.string.edit_observation_note));
                    id.setMessage("");
                    id.setValue(note.getText().toString());
                    id.setType(InputDialog.DType.INPUT_TEXT);
                    id.setTextLinesNumber(16);
                    //	id.setNegativeButton(getString(R.string.cancel));
                    id.setPositiveButton(getString(R.string.done), new InputDialog.OnButtonListener() {
                        public void onClick(String value) {
                            note.setText(value);
                            id.dismiss();
                        }
                    });
                    id.toggleKeyboard();
                    registerDialog(id);
                    id.showAtTop();
                }
            });

        }
        // OK
        Button btn = (Button) findViewById(R.id.comment_btn);//ok button in fact
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (isRecording && mPlayer != null) mPlayer.stopRecording();
                if (request.action == NoteRequest.NEW_NOTE_ACTION) {
                    if (request.obj == null) {
                        finish();
                        return;
                    }

                    String noteText = note.getText().toString();
                    //replaceAudioNoteIfNeeded();
                    NoteDatabase db = new NoteDatabase();
                    ErrorHandler eh = new ErrorHandler();
                    db.open(eh);
                    if (eh.hasError()) {
                        eh.showError(NoteActivity.this);
                        return;
                    }
                    Log.d(TAG, "request=" + request + " catalog=" + request.obj.getCatalog() + " id=" + request.obj.getId());
                    db.add(request.obj, noteText, c.getTimeInMillis(), mPath, "");
                    db.close();
                    releasePlayer();
                    for (int obslist = InfoList.PrimaryObsList; obslist <= InfoList.PrimaryObsList + 3; obslist++) {
                        Iterator it = ListHolder.getListHolder().get(obslist).iterator();
                        for (; it.hasNext(); ) {
                            Object o = it.next();
                            if (o instanceof ObsInfoListImpl.Item) {
                                ObsInfoListImpl.Item item = (ObsInfoListImpl.Item) o;
                                if (item.x.equals(request.obj))
                                    item.y = true;
                            }
                        }
                    }

                }
                if (request.action == NoteRequest.EDIT_NOTE_ACTION) {
                    if (request.record == null) {
                        finish();
                        return;
                    }
                    //replaceAudioNoteIfNeeded();
                    request.record.path = mPath;
                    request.record.note = note.getText().toString();
                    request.record.date = c.getTimeInMillis();
                    NoteDatabase db = new NoteDatabase();
                    ErrorHandler eh = new ErrorHandler();
                    db.open(eh);
                    if (eh.hasError()) {
                        eh.showError(NoteActivity.this);
                        return;
                    }

                    db.edit(request.record);
                    db.close();
                    releasePlayer();
                    Log.d(TAG, "" + request);
                }

                finish();
            }
        });

        tv = (TextView) findViewById(R.id.comment_time_text);
        TextView tvDso = (TextView) findViewById(R.id.comment_dso_text);
        if (request.obj != null)
            tvDso.setText(request.obj.getCanonicName());
        else
            tvDso.setText(request.getRecord().name);

        //Change time button
        Button time_btn = (Button) findViewById(R.id.comment_time_btn);
        final Activity noteactivity = this;
        time_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                registerDialog(getActionDialog()).show();
            }
        });

        setTimeLabel();

        final Button rec_btn = (Button) findViewById(R.id.record_btn);
        final Button play_btn = (Button) findViewById(R.id.play_btn);

        boolean mic = AstroTools.doesMicExist(getApplicationContext());
        if (!mic) {
            rec_btn.setVisibility(View.GONE);
            play_btn.setVisibility(View.GONE);
            findViewById(R.id.nreectext).setVisibility(View.GONE);
        }

        String rec_text = (mPath.isEmpty() ? getString(R.string.start_recording) : getString(R.string.overwrite));
        rec_btn.setText(rec_text);

        //this changes the text of play btn back to Play after playing is over
        final MediaPlayer.OnCompletionListener oncl = new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                isPlaying = false;
                play_btn.setText(R.string._play_);
                rec_btn.setEnabled(true);
            }
        };
        //Audionote record start
        rec_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ErrorHandler eh = new ErrorHandler();
                int perm = ActivityCompat.checkSelfPermission(NoteActivity.this, Manifest.permission.RECORD_AUDIO);
                if (perm != PackageManager.PERMISSION_GRANTED) {
                    eh.addError(ErrorHandler.WARNING, getString(R.string.microphone_permission_not_available));
                }
                if (eh.hasError()) {
                    eh.showError(NoteActivity.this);
                    return;
                }


                dirty = true;
                if (!isRecording) { //user want to record
                    if (!mPath.isEmpty()) {//there is some recording path already

                        final File f = new File(Global.notesPath, mPath);
                        if (f.exists()) {
                            InputDialog dimp = new InputDialog(NoteActivity.this);
                            dimp.setMessage(getString(R.string.an_old_record_exists_overwrite_it_));
                            dimp.setPositiveButton(getString(R.string.ok), new OnButtonListener() {
                                public void onClick(String v) {

                                    mPlayer = new Player(new File(Global.notesPath, mPath).getAbsolutePath(), oncl);
                                    boolean result = mPlayer.startRecording();
                                    if (!result) {
                                        InputDialog.message(NoteActivity.this, R.string.error_starting_an_audio_recorder, 0).show();
                                        return;
                                    }
                                    isRecording = true;
                                    rec_btn.setText(R.string.stop_recording);
                                    play_btn.setEnabled(false);

                                }
                            });
                            dimp.setNegativeButton(getString(R.string.cancel));
                            registerDialog(dimp).show();
                        } else { //there is no recording with a given path

                            mPlayer = new Player(f.getAbsolutePath(), oncl);
                            boolean result = mPlayer.startRecording();
                            if (!result) {
                                registerDialog(InputDialog.message(NoteActivity.this, R.string.error_starting_an_audio_recorder, 0)).show();
                                return;
                            }
                            isRecording = true;
                            rec_btn.setText(R.string.stop_recording);
                            play_btn.setEnabled(true);
                        }
                    } else {//there is no recording path, making new one
                        long time = Calendar.getInstance().getTimeInMillis() / 1000;
                        String name = "";
                        if (request.obj != null)
                            name = request.obj.getShortName();
                        mPath = name + " " + String.valueOf(time) + _3GP;
                        File f1 = new File(Global.notesPath, mPath);
                        mPlayer = new Player(f1.getAbsolutePath(), oncl);
                        mPlayer.startRecording();
                        isRecording = true;
                        rec_btn.setText(R.string.stop_recording);
                        play_btn.setEnabled(false);
                        Log.d(TAG, "mPath=" + mPath);
                    }
                } else {//stop recording


                    stopRecoring();


                }
            }
        });

        //Audionote record play
        play_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (isPlaying) {
                    if (mPlayer != null)
                        mPlayer.stopPlaying();
                    isPlaying = false;
                    play_btn.setText(R.string._play_);
                    rec_btn.setEnabled(true);
                    return;
                }
                if (!mPath.isEmpty() && !isPlaying) {
                    File f = new File(Global.notesPath, mPath);
                    mPlayer = new Player(f.getAbsolutePath(), oncl);
                    mPlayer.startPlaying();
                    isPlaying = true;
                    play_btn.setText(R.string.stop);
                    rec_btn.setEnabled(false);
                }

            }
        });
        if (mPath.isEmpty()) {
            play_btn.setEnabled(false);//there is no voice note
        }

    }


    private void setTime() {
        dirty = true;
        SettingsActivity.putSharedPreferences(Constants.DTP_DISPLAY_MODE, DateTimePickerActivity.BOTH, NoteActivity.this);
        SettingsActivity.putSharedPreferences(Constants.DTP_TIME, dd.getDateTime().getTimeInMillis(), NoteActivity.this);
        Intent i = new Intent(getApplicationContext(), DateTimePickerActivity.class);

        i.putExtra(Constants.DTP_RT, false);//no real time button
        startActivityForResult(i, SET_TIME);
    }

    private void stopRecoring() {
        if (mPlayer != null)
            mPlayer.stopRecording();
        isRecording = false;
        String rec_text = (mPath.length() == 0 ? getString(R.string.start_recording) : getString(R.string.overwrite));
        Button rec_btn = (Button) findViewById(R.id.record_btn);
        Button play_btn = (Button) findViewById(R.id.play_btn);


        rec_btn.setText(rec_text);
        play_btn.setEnabled(true);


        String noteText = note.getText().toString();

        note.setText(getNoteTextWithAudioString(noteText));
        note.invalidate();
    }

    /**
     * @param noteText incoming text
     * @return outgoing text. If text contains audio record, it is replaced
     */
    private String getNoteTextWithAudioString(String noteText) {
        if ("".equals(mPath))
            return noteText;
        String aunote = getString(R.string._audio_record_) + mPath + "']";
        int i0 = noteText.indexOf(getString(R.string._audio_record_2));
        //Log.d(TAG,"i0="+i0+" text="+noteText+" a2="+);
        if (i0 < 0) //not found
            noteText += "\n" + aunote;
        else {
            int i1 = noteText.indexOf("]", i0);
            String search = (String) noteText.subSequence(i0, i1 + 1);
            noteText = noteText.replace(search, aunote);
        }
        return noteText;
    }

    private void releaseResources() {
        Log.d(TAG, "mPlayer=" + mPlayer);
        if (isRecording && mPlayer != null) mPlayer.stopRecording();
        releasePlayer();


    }

    @Override
    protected void onDestroy() {
        releaseResources();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case SET_TIME: //from DateTimePicker
                if (dd == null) return;
                try {
                    long time = data.getLongExtra(Constants.DATE_TIME_PICKER_MILLIS, Calendar.getInstance().getTimeInMillis());
                    dd.setMillis(time);
                    update();
                } catch (Exception e) {
                }
                break;
            case SET_NOTE:
                if (note == null) return;
                String res = "";
                if (data != null)
                    res = (String) data.getCharSequenceExtra(STRING);
                if (res != null)
                    note.setText(res);
                break;
            case CALL_EYEPIECES_REQUEST:
                String ep = data.getStringExtra(Constants.SELECTED_EYEPIECE);
                if (ep != null)
                    insertContent(getString(R.string.eyepiece), ep, false);
                break;
            case CALL_TELESCOPE_REQUEST:
                String scope = data.getStringExtra(Constants.SELECTED_TELESCOPE);
                if (scope != null)
                    insertContent(getString(R.string.telescope2), scope, false);
                break;
            case CALL_WORLD_CITIES_REQUEST:
                insLocation(data);
                break;
            case CALL_LOCATION_LIST_REQUEST:
                InfoList loclist = ListHolder.getListHolder().get(InfoList.LOCATION_LIST);
                int pos = data.getIntExtra(LocationListActivity.POS, -1);
                if (pos != -1) {
                    LocationItem item = (LocationItem) loclist.get(pos);
                    insertLatLonIntoNote(item.lat, item.lon);
                }
                break;
        }
    }

    /**
     * take location from world cities db and insert it into note
     *
     * @param data
     */
    private void insLocation(Intent data) {
        int rowid = data.getIntExtra(Constants.SELECTOR_RESULT, -1);
        if (rowid != -1) {
            try {
                Db db = new Db(getApplicationContext(), Constants.LOCATIONS_DB);
                db.open();
                Cursor cursor = db.rawQuery(SettingsGeoActivity.SELECT_LAT_LON_FROM_LIST2_WHERE_ROWID + rowid);
                if (cursor.moveToNext()) {

                    double lat = cursor.getFloat(0);
                    double lon = cursor.getFloat(1);
                    insertLatLonIntoNote(lat, lon);

                }
                db.close();
            } catch (Exception e) {
            }
        }
    }

    private void setTimeLabel() {
        tv.setText(DetailsActivity.makeShortDateTimeString(c));
    }

    private void update() { //needed for MyDateDialog
        c = dd.getDateTime();
        setTimeLabel();
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    //overriding buttons
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "onBackPressed");
            Runnable r = new Runnable() {
                public void run() {
                    //deleting record file for new note when exiting on back press
                    if (request.action == NoteRequest.NEW_NOTE_ACTION) {
                        releasePlayer();
                        if (!"".equals(mPath)) {
                            File f = new File(Global.notesPath, mPath);
                            f.delete();
                        }
                    }
                }
            };

            String currentNote = note.getText().toString();
            if (!initialNote.equals(currentNote))
                dirty = true;
            if (dirty)
                registerDialog(InputDialog.abort(NoteActivity.this, getString(R.string.this_note_is_not_saved_yet_exit_anyway_), r)).show();
            else {
                r.run();
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void insertObservatory() {
        String[] items = new String[]{getString(R.string.current_location), getString(R.string.select_from_world_s_cities), getString(R.string.select_from_user_locations)};
        InputDialog d = new InputDialog(NoteActivity.this);
        d.setTitle(getString(R.string.select_location));
        d.setPositiveButton(""); //disable
        d.setValue("-1"); //remove checks
        d.setNegativeButton(getString(R.string.cancel));
        d.setListItems(items, new InputDialog.OnButtonListener() {
            public void onClick(final String value) {
                final int i = AstroTools.getInteger(value, -1, -2, 1000);
                switch (i) {
                    case -1:
                        return;
                    case 0:
                        double lat = SettingsActivity.getLattitude();
                        double lon = SettingsActivity.getLongitude();
                        insertLatLonIntoNote(lat, lon);
                        return;
                    case 1:
                        Intent intent = new Intent(NoteActivity.this, SelectorActivity.class);
                        SettingsGeoActivity.setWorldCitiesParam(intent, getApplicationContext());
                        startActivityForResult(intent, CALL_WORLD_CITIES_REQUEST);
                        break;
                    case 2:
                        intent = new Intent(NoteActivity.this, LocationListActivity.class);
                        startActivityForResult(intent, CALL_LOCATION_LIST_REQUEST);
                        break;
                }
            }
        });
        registerDialog(d).show();
    }

    private void insertLatLonIntoNote(double lat, double lon) {

        String latStr = AstroTools.getLatString(lat);
        String lonStr = AstroTools.getLonString(lon);
        insertContent(getString(R.string.location2), latStr + " " + lonStr, true);
    }


    private void insertEyepiece() {
        Intent intent = new Intent(NoteActivity.this, EyepiecesListActivity.class);
        intent.setAction(Constants.ACTION_EP_SELECT);//COM_ASTRO_DSOPLANNER_EYEPIECES_LIST_VIEW);
        startActivityForResult(intent, CALL_EYEPIECES_REQUEST);


    }

    private void insertTelescope() {
        String[] items = new String[]{getString(R.string.current_scope), getString(R.string.select_from_the_list)};
        InputDialog d = new InputDialog(NoteActivity.this);
        d.setTitle(getString(R.string.select_telesope));
        d.setPositiveButton(""); //disable
        d.setValue("-1"); //remove checks
        d.setNegativeButton(getString(R.string.cancel));
        d.setListItems(items, new InputDialog.OnButtonListener() {
            public void onClick(final String value) {
                final int i = AstroTools.getInteger(value, -1, -2, 1000);
                switch (i) {
                    case 0:
                        String scope = SettingsActivity.getCurrentTelescope().getSummary(getApplicationContext());
                        insertContent(getString(R.string.telescope2), scope, false);
                        break;
                    case 1:
                        Intent intent = new Intent(NoteActivity.this, TelescopeListActivity.class);
                        intent.setAction(Constants.ACTION_TELESCOPE_SELECT);
                        startActivityForResult(intent, CALL_TELESCOPE_REQUEST);
                        break;
                }
            }
        });
        registerDialog(d).show();

    }

    private void insertAll() {
        double lat = SettingsActivity.getLattitude();
        double lon = SettingsActivity.getLongitude();
        insertLatLonIntoNote(lat, lon);

        String scope = SettingsActivity.getCurrentTelescope().getSummary(getApplicationContext());
        insertContent(getString(R.string.telescope2), scope, false);


        int num = SettingsActivity.getEPsNumber();
        if (num > 3) {
            InputDialog.message(NoteActivity.this, R.string.only_3_eyepieces_were_inserted).show();
        }
        for (int i = 0; i < Math.min(num, 3); i++) {
            EyepiecesRecord rec = SettingsActivity.getEpRecord(i);
            String ep_summary = rec.getSummary();
            insertContent(getString(R.string.eyepiece), ep_summary, false);
        }

    }

    /**
     * @param tag     - tag to find in note
     * @param content - new content to be inserted
     * @param replace whether to replace content (true) or just add it
     */
    private void insertContent(String tag, String content, boolean replace) {
        if (note == null)
            return;
        String noteStr = note.getText().toString();
        if (noteStr.contains("[" + tag + " " + content + "]"))
            return;
        if (replace) {
            Pattern p = Pattern.compile("\\[" + tag + "[ ]+" + ".*\\]");
            Matcher m = p.matcher(noteStr);
            if (m.find()) {
                int start = m.start();
                int end = m.end();
                String s1 = noteStr.substring(0, start);
                String s2 = noteStr.substring(end);
                noteStr = s1 + "[" + tag + " " + content + "]" + s2;
                note.setText(noteStr);
                return;
            }
        }

        noteStr = noteStr + "\n" + "[" + tag + " " + content + "]";
        note.setText(noteStr);

    }


    private InputDialog getActionDialog() {
        String[] items = new String[]{getString(R.string.set_new_date_time), getString(R.string.insert_observatory_data),
                getString(R.string.insert_telescope), getString(R.string.insert_eyepiece), getString(R.string.insert_all_of_the_above)};
        InputDialog d = new InputDialog(NoteActivity.this);
        d.setTitle(getString(R.string.select_action));
        d.setPositiveButton(""); //disable
        d.setNegativeButton(getString(R.string.cancel));
        d.setValue("-1"); //remove checks
        d.setListItems(items, new InputDialog.OnButtonListener() {
            public void onClick(final String value) {

                final int i = AstroTools.getInteger(value, -1, -2, 1000);
                switch (i) {
                    case -1:
                        return;
                    case 0:
                        setTime();
                        break;
                    case 1:
                        insertObservatory();
                        break;
                    case 2:
                        insertTelescope();
                        break;
                    case 3:
                        insertEyepiece();
                        break;
                    case 4:
                        insertAll();
                        break;
                }

            }
        });
        return d;
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
        return false;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    //-----------

}
