package com.astro.dsoplanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.ScrollingMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import static android.view.ViewGroup.LayoutParams.FILL_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class InputDialog extends Dialog implements android.view.View.OnClickListener, OnTouchListener, OnGestureListener {
    private static final String LOWER = "lower";
    private static final String UPPER = "upper";
    private static final String EDIT_FIELD = "editField";
    private static final String DSO_PLANNER = "DSO PLANNER";
    private static final String EDIT_OUT = "EDIT_OUT";
    private float mScrDensity; //screen density holder

    public interface OnButtonListener {
        void onClick(String value);
    }

    public interface OnMidButtonListener extends OnButtonListener {

    }

    //Constants
    public enum DType {
        TOAST_WAIT, MESSAGE_ONLY, INPUT_STRING, INPUT_NUMBER, INPUT_LIST, INPUT_TEXT, //multiline text
        INPUT_LAYOUT, //for addView functionality
        INPUT_DROPDOWN, SHOW_SPINNER, INPUT_CHECKBOXES
    }

    private static final String TAG = InputDialog.class.getSimpleName();

    private int TEXT_LINES = 8; //min number of lines in the multiline mode

    //Vars
    private static String ret_value = "";

    private int mTimeout = -1; //timeout for message Toast like
    private final String mOutputVarName = EDIT_OUT;
    private String mTitle = DSO_PLANNER;
    private String mMessage = "";
    private String mValue = ""; //initialValue, for the list it's 0 based index of selected item
    private DType mMode = DType.MESSAGE_ONLY; //message only
    private EditText mEt; // Edit Text box
    private String editFieldTag = EDIT_FIELD;
    private Button mBSpace, mBack, mBCaps, mNum, mBClose, mBCloseN; //mBdone
    private LinearLayout mLayout;
    private String mUpper = UPPER, mLower = LOWER;
    private Button mB[] = new Button[46]; //alphanumeric keyboard first then all of the numeric keys
    private OnButtonListener mOnPositiveClick;
    private OnButtonListener mOnNegativeClick;
    private OnButtonListener mOnMiddleClick;
    private String textOK = "";
    private String textCancel = "";
    private String textMiddle = "";
    private CharSequence[] mStringsList;
    private OnButtonListener mRadioListener;
    private boolean capsFlag = true;
    private EditText mEditField = null;
    private View mInsertLayout;
    private boolean backDisabled = false;
    private boolean isNightMode = false;
    private boolean isOnyx = false;
    private boolean useCustomKeyboard = false;
    private Object mTextLines = TEXT_LINES;
    private LinearLayout mDialogFrame;
    private boolean mNotEditable = false;
    private boolean mShowAtTop = false;
    private boolean keyboardShown = false;
    private boolean mToggleKeyboard = false;
    private String mHelpText = null;
    private View mWindowFrame;
    private Button mBEnter;
    private Button btnOK;
    private Button btnMiddle;
    private String hint;//for input_string
    Animation spinit;

    //Keyboard tags/values
    private String sL[] = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "~", "?", "-", ",", "."};
    private String cL[] = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "`", "/", "|", ",", "."};
    private String nS[] = {"!", ")", "'", "#", "3", "$", "%", "&", "8", "*", "?", "/", "+", "-", "9", "0", "1", "4", "@", "5", "7", "(", "2", "\"", "6", "_", "=", "[", "]", "<", ">"};

    private OnEditorActionListener mKbdActionListener = new OnEditorActionListener() {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            Log.d(TAG, "Kbd IN");
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                findViewById(R.id.bnd_ok).performClick();
                Log.d(TAG, "Kbd DONE");
            }
            return false;
        }
    };

    Context context;
    /**
     * On radio button click common listener.
     * Replacing the radio group onChange() to react on already selected radio click.
     */
    View.OnClickListener radioClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            ((RadioButton) v).setChecked(true);
            mValue = String.valueOf(v.getId());
            mEt.setText(mValue);
            saveOKValue();
            final boolean closeOnSelect = (textOK.length() == 0);
            if (closeOnSelect) dismiss();
            if (mRadioListener != null) mRadioListener.onClick(mValue);
        }
    };

    public InputDialog(Context context) {
        super(context, SettingsActivity.getNightMode() ? R.style.SkinDlg_Night : (SettingsActivity.getDarkSkin() ? R.style.SkinDlg_Onyx : R.style.SkinDlg_Day));
        isNightMode = SettingsActivity.getNightMode();
        isOnyx = SettingsActivity.getDarkSkin();
        this.context = context;
    }

    public InputDialog(Context context, String title, String message) {
        this(context);
        setTitle(title);
        setMessage(message);
    }

    public static String getResult() {
        return ret_value;
    }

    protected void onCreate(Bundle savedInstanceState) {
        mScrDensity = getContext().getResources().getDisplayMetrics().density;

        //Disable title bars (if compound will trigger exception)
        if (mMode != DType.INPUT_LAYOUT && mMode != DType.SHOW_SPINNER) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (SettingsActivity.isFullScreen(getContext()) || SettingsActivity.isHideNavBar(getContext())) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            setContentView(R.layout.inputdlg);
        }
        //Need custom night mode keyboard?
        useCustomKeyboard = isNightMode && !SettingsActivity.getNoRedKeyboard();
        mWindowFrame = findViewById(R.id.xMLayout);
        //mine
        mDialogFrame = findViewById(R.id.xdialogFrame);
        mDialogFrame.setBackgroundResource(getDrawable(R.drawable.dlg_wht, R.drawable.dlg_blk, R.drawable.dlg_red));
        if (mShowAtTop) {
            fromTopOfTheScreen(true);
        }

        //------------------------------------------
        //Set dialog title
        TextView hd = findViewById(R.id.Header);
        if (mTitle.length() == 0) { //the title was set empty on purpouse!
            hd.setVisibility(View.GONE);
            findViewById(R.id.xTopline).setVisibility(View.GONE);
        } else {
            hd.setText(mTitle);
        }
        //------------------------------------------
        //Set dialog message (instructions)
        TextView message = findViewById(R.id.dialog_message);
        if (mMessage.length() == 0) { //remove that line then
            message.setVisibility(View.GONE);
        } else {
            message.setText(mMessage);
        }
        //------------------------------------------
        //Main input/output field
        mEt = findViewById(R.id.xEt);

        //------------------------------------------
        //Process various dispaly modes
        if (mMode == DType.INPUT_LAYOUT) { //hide all controls except header
            mEt.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
        } else if (mMode == DType.TOAST_WAIT) {
            mEt.setText("");
            mEt.setVisibility(View.GONE);
            mWindowFrame.setBackgroundColor(Color.TRANSPARENT); //revert to fully transparent
        } else if (mMode == DType.SHOW_SPINNER) {
            mEt.setText("");
            mEt.setVisibility(View.GONE);
            message.setVisibility(View.GONE); //no message
            findViewById(R.id.xTopline).setVisibility(View.GONE);
            findViewById(R.id.inpDheader).setVisibility(View.GONE);
            mWindowFrame.setBackgroundColor(Color.TRANSPARENT); //revert to fully transparent

            //Set message
            ((TextView) findViewById(R.id.inpd_waitmessage)).setText(mMessage);
            //Set image
            ImageView spinner = findViewById(R.id.inpd_spinimage);
            spinner.setImageResource(getDrawable(R.drawable.spin_w, R.drawable.spin_bk, R.drawable.spin_r));
            spinit = AnimationUtils.loadAnimation(context, R.anim.spin);
            spinit.setInterpolator(new LinearInterpolator());
            spinner.startAnimation(spinit);
        } else if (mMode == DType.MESSAGE_ONLY) { //hide text input field
            mEt.setText("");
            mEt.setVisibility(View.GONE);
        } else if (mMode == DType.INPUT_LIST) {
            //hide text input field
            mEt.setText("");
            mEt.setVisibility(View.GONE);
            Context cont = getContext();
            //Populate the list
            LinearLayout sv = findViewById(R.id.inputlayout);
            RadioGroup rg = new RadioGroup(cont);

            LinearLayout.LayoutParams bnParams = new LinearLayout.LayoutParams(FILL_PARENT, WRAP_CONTENT);
            bnParams.weight = 1;
            float textsize = message.getTextSize();
            int id = 0; //For the radiogroup the id does not need to be unique
            for (CharSequence s : mStringsList) {
                RadioButton rb = new RadioButton(cont);
                rb.setTextSize(textsize / mScrDensity);//mine
                rb.setText(s);
                rb.setId(id++);
                rb.setGravity(Gravity.CENTER_VERTICAL);
                rb.setLayoutParams(bnParams);
                rb.setCompoundDrawablePadding(20);
                rb.setPadding(80, 0, 0, 0);
                rb.setOnClickListener(radioClickListener);
                rg.addView(rb);
            }

            LinearLayout.LayoutParams rgParams = new LinearLayout.LayoutParams(FILL_PARENT, WRAP_CONTENT);
            rg.setLayoutParams(rgParams);
            rg.setWeightSum(mStringsList.length);
            rg.setScrollContainer(true);
            rg.check(AstroTools.getInteger(mValue, 0, -10000, 10000));

            sv.setClipChildren(false);
            sv.addView(rg);
            findViewById(R.id.inputlayout).setVisibility(View.VISIBLE);
        } else if (mMode == DType.INPUT_DROPDOWN) {
            int DWIDTH = (int) (150 * mScrDensity);

            //hide text input fields
            mEt.setText("");
            mEt.setVisibility(View.GONE);

            float bottom_margin = getContext().getResources().getDimension(R.dimen.graph_spinner_size);//mine
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            p.setMargins(0, 0, 0, (int) (bottom_margin / mScrDensity));

            p.width = DWIDTH;
            p.addRule(RelativeLayout.CENTER_HORIZONTAL);
            p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            mDialogFrame.setLayoutParams(p);

            Context cont = getContext();

            //Populate the list
            LinearLayout sv = findViewById(R.id.inputlayout);

            LayoutParams params = new LayoutParams(FILL_PARENT, WRAP_CONTENT);
            float textsize = message.getTextSize(); //mine

            Log.d(TAG, "textsize=" + textsize + "bottom_margin=" + bottom_margin);

            for (CharSequence s : mStringsList) {
                Button rb = new Button(cont);
                rb.setTextSize(textsize / mScrDensity);//mine
                rb.setText(s);
                rb.setTag(s);
                rb.setMinimumWidth(DWIDTH);
                rb.setLayoutParams(params);
                rb.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mValue = v.getTag().toString();
                        mEt.setText(mValue);
                        saveOKValue();
                        dismiss();
                        if (mRadioListener != null) mRadioListener.onClick(mValue);
                    }
                });
                sv.addView(rb);
            }

            final boolean closeOnSelect = true; //not used though
            findViewById(R.id.inputlayout).setVisibility(View.VISIBLE);
            mWindowFrame.setBackgroundColor(Color.TRANSPARENT); //disable dimming sky
        } else if (mMode == DType.INPUT_CHECKBOXES) {
            //hide text input fields
            mEt.setText("");
            mEt.setVisibility(View.GONE);

            float bottom_margin = getContext().getResources().getDimension(R.dimen.graph_spinner_size);//mine
            Context cont = getContext();

            //Populate the list
            LinearLayout sv = findViewById(R.id.inputlayout);
            Log.d(TAG, "textsize=" + message.getTextSize() + "bottom_margin=" + bottom_margin);

            int id = 0; //For the radio group the id does not need to be unique
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (CharSequence s : mStringsList) {
                View v = mInflater.inflate(R.layout.input_dialog_ch_item, null);
                sv.addView(v);
            }

            final boolean closeOnSelect = true; //not used though
            sv.setClipChildren(false);
            findViewById(R.id.inputlayout).setVisibility(View.VISIBLE);

        } else { //INPUT_STRING,INPUT_NUMBER, INPUT_TEXT
            if (mMode == DType.INPUT_TEXT) {
                mEt.setScroller(new Scroller(context));
                mEt.setMaxLines((Integer) mTextLines);
                mEt.setVerticalScrollBarEnabled(true);
                mEt.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                mEt.setMovementMethod(new ScrollingMovementMethod());
                mEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                mDialogFrame.setMinimumWidth(Global.screenW);
            } else {
                mEt.setSingleLine();
                if (mMode == DType.INPUT_NUMBER) mEt.setInputType(InputType.TYPE_CLASS_PHONE);
                else mEt.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            if (hint != null) {
                mEt.setHint(hint);
            }
            mEt.setSelectAllOnFocus(true);
            mEt.setVisibility(View.VISIBLE);
            mEt.setText(mValue);
            mEt.setSelection(mValue.length());//to the end of the string
            if (!useCustomKeyboard) mEt.requestFocus(); //pop system keyboard
        }
        setChildrenListeners(mDialogFrame);

        if (mNotEditable) {
            mEt.setKeyListener(null);
            mEt.setSelection(0);
        }

        //------------------------------------------
        //OK/Cancel
        btnOK = findViewById(R.id.bnd_ok);
        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveOKValue();
                dismiss();
                if (mOnPositiveClick != null) mOnPositiveClick.onClick(mValue);
            }
        });

        //At least one button must be set except for the LIST mode
        if (textOK.length() != 0) {
            btnOK.setText(textOK);
            btnOK.setVisibility(View.VISIBLE);
        } else { //OK set to ""
            if (mMode != DType.INPUT_LIST && mMode != DType.INPUT_DROPDOWN && mMode != DType.TOAST_WAIT && mMode != DType.SHOW_SPINNER) { //then at least one button must be present even though user did not ask for that
                textOK = "OK";
                btnOK.setVisibility(View.VISIBLE);
            } else //In the List and Toast view there could be no buttons at all
                btnOK.setVisibility(View.GONE);
        }
        btnOK.setText(textOK);

        Button btnCancel = findViewById(R.id.bnd_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String s = setCancelValue();
                dismiss();
                if (mOnNegativeClick != null) mOnNegativeClick.onClick(s);
            }
        });

        //Second button is optional. If not defined - not shown
        if (textCancel.length() != 0) {
            btnCancel.setText(textCancel);
            btnCancel.setVisibility(View.VISIBLE);
        } else {
            btnCancel.setVisibility(View.GONE);
        }


        Button btnMiddle = findViewById(R.id.bnd_middle);
        btnMiddle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mOnMiddleClick != null && mOnMiddleClick instanceof OnMidButtonListener) {
                    String name = mEt.getText().toString();
                    mOnMiddleClick.onClick(name);
                } else {
                    if (mOnMiddleClick != null) mOnMiddleClick.onClick("");
                }
            }
        });

        if (textMiddle.length() != 0) {
            btnMiddle.setText(textMiddle);
            btnMiddle.setVisibility(View.VISIBLE);
        } else {
            btnMiddle.setVisibility(View.GONE);
        }

        //HelpScreen
        View v = findViewById(R.id.inpDhelpBn);
        if (mHelpText != null) {
            v.setVisibility(View.VISIBLE);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputDialog.message(context, mHelpText, 0).show();
                }
            });
        } else v.setVisibility(View.GONE);


        //------------------------------------------
        //Keyboard sub layouts (invisible)
        keyboardShown = false;
        if (useCustomKeyboard) {
            setKeys();
            mLayout = findViewById(mMode == DType.INPUT_NUMBER ? R.id.xK2 : R.id.xK1);
            if (mToggleKeyboard) {
                mEditField = mEt;
                enableKeyboard();
            }
        } else {
            mLayout = null;
            if (mToggleKeyboard) {
                mEt.postDelayed(new Runnable() {
                    public void run() {
                        InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.showSoftInput(mEt, 0);
                    }
                }, 10);
            }
        }
        //Old dev semi-trans background fix
        if (SettingsActivity.getTrans()) { //disable
            mWindowFrame.setBackgroundColor(Color.TRANSPARENT);
        }
        if (Global.TEST_MODE) registerTestReceiver();
    }

    private void fromTopOfTheScreen(boolean isTop) {
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(FILL_PARENT, WRAP_CONTENT);
        if (isTop) {
            p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            p.addRule(RelativeLayout.CENTER_IN_PARENT);
        }
        mDialogFrame.setLayoutParams(p);
    }

    private int getDrawable(@DrawableRes int white, @DrawableRes int black, @DrawableRes int red) {
        return isNightMode ? red : (SettingsActivity.getDarkSkin() ? black : white);
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    /**
     * For InsertLayout mode set all tagged controls onClick's to this. Recursive
     *
     * @param vg
     */
    private void setChildrenListeners(ViewGroup vg) {
        View v = null;
        for (int i = 0; i < vg.getChildCount(); i++) {
            try {
                v = vg.getChildAt(i);
                if (v.getTag().toString().contentEquals(editFieldTag)) {
                    v.setOnClickListener(this);
                    v.setOnTouchListener(this);
                    ((EditText) v).setCursorVisible(true);
                    ((EditText) v).setOnEditorActionListener(mKbdActionListener);
                } else throw new Exception();
            } catch (Exception e1) { //if null tag, null View
                try {
                    setChildrenListeners((ViewGroup) v);
                } catch (Exception ignored) {
                } //v is not a view group
            }
        }
    }

    public void setMessage(String s) {
        mMessage = s;
    }

    public void setTitle(String s) {
        mTitle = s;
    }

    public void setValue(String s) {
        mValue = s;
    }

    public void setType(DType i) {
        mMode = i;
    }

    public void setPositiveButton(String string) {
        textOK = string;
    }

    public void setNegativeButton(String string) {
        textCancel = string;
    }

    public void setMiddleButton(String text, final InputDialog.OnButtonListener m) {
        textMiddle = text;
        mOnMiddleClick = m;
    }

    public void setPositiveButton(String text, final InputDialog.OnButtonListener m) {
        setPositiveButton(text);
        mOnPositiveClick = m;

    }

    public void setNegativeButton(String text, final InputDialog.OnButtonListener m) {
        setNegativeButton(text);
        mOnNegativeClick = m;
    }

    public void setTextLinesNumber(int n) {
        if (n > 1) {
            mTextLines = n;
        } else mTextLines = TEXT_LINES;
    }

    public void setListItems(CharSequence[] sa, final InputDialog.OnButtonListener m) {
        mStringsList = sa;
        mRadioListener = m;
        if (!(mMode == DType.INPUT_DROPDOWN || mMode == DType.INPUT_CHECKBOXES))
            mMode = DType.INPUT_LIST; //so not to override it
    }

    public void setListItems(CharSequence[] sa) {
        setListItems(sa, null);
    }

    public void setListItems(int resId, final InputDialog.OnButtonListener m) {
        setListItems(getContext().getResources().getStringArray(resId), m);
    }

    public void setListItems(int resid) {
        setListItems(getContext().getResources().getStringArray(resid), null);
    }

    public boolean onTouch(View v, MotionEvent event) {
        //if that's our special EditText view
        if (v.getTag().toString().contentEquals(editFieldTag)) {
            boolean sameField = false;
            if (mEditField != null && mEditField.equals(v)) {
                sameField = true;
            } else {
                mEditField = (EditText) v;
            }

            if (!useCustomKeyboard) { //normal keyboard
                mEditField.onTouchEvent(event); // call native handler
                return true;
            }
            //else Red keyboard
            if (!mNotEditable) { //then show the keyboard
                if (sameField && keyboardShown) { //second click on same field move cursor
                    mEditField.requestFocus();
                    int i = getCharIndexFromCoordinate(mEditField, event.getX(), event.getY());
                    if (i > -1) mEditField.setSelection(i);
                } else { //new field
                    if (keyboardShown) {
                        disableKeyboard(); //hide it first
                    }
                    enableKeyboard(); //show
                }
            }
            return true; // consume touch even

        }
        return v.onTouchEvent(event);
    }

    //overriding BACK button
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (keyboardShown) { //on first back close keyboard
                disableKeyboard();
                keyboardShown = false;
                return true;
            } else if (backDisabled) //back not allowed
                return true;
            else if (mMode == DType.INPUT_LIST) {
                mEt.setText(mValue);
                saveOKValue();
                if (mRadioListener != null) mRadioListener.onClick(mValue);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Insert custom layout above buttons.
     * The dialog will automatically became INPUT_LAYOUT type (regular content will be hided)
     *
     * @param id resource id of the target layout
     */
    public void insertLayout(int id) {
        mMode = DType.INPUT_LAYOUT;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (SettingsActivity.isFullScreen(getContext())) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.inputdlg);

        mInsertLayout = getLayoutInflater().inflate(id, null);
        LinearLayout la = findViewById(R.id.insertlayout);
        la.addView(mInsertLayout, 0);
        la.setVisibility(View.VISIBLE);
    }

    //Main keyboard events dispatch
    public void onClick(View v) {
        if (useCustomKeyboard) {
            if (v == mBCaps) {
                if (mBCaps.getTag().equals(mUpper)) {
                    changeSmallLetters();
                    changeSmallTags();
                    capsFlag = false;
                } else if (mBCaps.getTag().equals(mLower)) {
                    changeCapitalLetters();
                    changeCapitalTags();
                }

            } else if (v == mBClose || v == mBCloseN) {
                disableKeyboard();
                keyboardShown = false;
            } else if (v.getTag().toString().contentEquals("back")) { //BACKSPACE BUTTON
                isBack(v);
            } else if (v == mNum) {
                String nTag = (String) mNum.getTag();
                if (nTag.contentEquals("num")) { // #123 mode of text keyboard
                    changeSyNuLetters();
                    changeSyNuTags();
                    mBCaps.setVisibility(Button.INVISIBLE);
                    mBCaps.setTag(mLower);
                }
                if (nTag.contentEquals("ABC")) { //switch back to letters
                    if (mBCaps.getTag().equals(mUpper) || capsFlag) {
                        //See what was before or if caps flag raised on dot.
                        changeCapitalLetters();
                        changeCapitalTags();
                    } else if (mBCaps.getTag().equals(mLower)) {
                        changeSmallLetters();
                        changeSmallTags();
                    }
                }

            } else if (v == mBEnter) {
                if (mMode == DType.INPUT_TEXT) { //multiline, insert CR
                    v.setTag("\n");
                    addText(v);
                } else { //single - click positive button
                    if (btnOK.getVisibility() == View.VISIBLE) btnOK.performClick();
                }
            } else { //if (v != mBdone && v != mBack && v != mBCaps && v != mNum) {
                String c = addText(v);
                if (c == null) return;

                //Switch back to low caps after first letter in caps entered
                //If in the numeric mode, don't switch right away, wait
                if (capsFlag && !c.matches("[ \n]")) {
                    if (mNum.getTag().toString().contentEquals("num")) {
                        changeSmallLetters();
                        changeSmallTags();
                    }
                    capsFlag = false;
                }
                //First letter is in caps on sentence start.
                //If not in the numeric mode yet, don't switch right away, wait
                if (c.matches("[!.?]")) {
                    capsFlag = true;
                    if (mNum.getTag().toString().contentEquals("num")) {
                        changeCapitalLetters();
                        changeCapitalTags();
                    }
                }
            }
        }
    }

    //Save the field value to the preferences and output as string
    private String saveOKValue() {
        if (mEt == null) return "OK";
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).edit();
        mValue = mEt.getText().toString();

        //Shared prefs assignment
        editor.putString(mOutputVarName, mValue);
        editor.commit();
        ret_value = mValue; //static assignment
        if (SettingsActivity.getNoRedKeyboard()) disableKeyboard();

        return mValue;
    }

    protected String setCancelValue() {
        return "Canceled";
    }

    // == KEYBOARD ===============================================
    //returns tag processed or null if not keyboard tag
    private String addText(View v) {
        String tag = (String) v.getTag();
        if (tag != null && !tag.contentEquals(editFieldTag)) {
            String t0 = mEditField.getText().toString();
            int i0 = mEditField.getSelectionStart();
            int i1 = mEditField.getSelectionEnd();
            if (i0 < 0 || i1 < 0) return tag;
            int end = t0.length();
            //insert new symbol and advance cursor
            mEditField.setText(t0.substring(0, i0) + tag + t0.substring(i1, end));
            mEditField.setSelection(i0 + 1);

            return tag;
        }
        return null;
    }

    //Backspace
    private void isBack(View v) {
        CharSequence cc = mEditField.getText();
        if (cc != null && cc.length() > 0) {
            int i0 = mEditField.getSelectionStart();
            if (i0 <= 0) return;
            String t0 = cc.toString();
            int end = t0.length();
            //delete char
            mEditField.setText(t0.substring(0, i0 - 1) + t0.substring(i0, end));
            mEditField.setSelection(i0 - 1);
        }
    }

    private void changeSmallLetters() {
        mBCaps.setVisibility(Button.VISIBLE);
        for (int i = 0; i < sL.length; i++)
            mB[i].setText(sL[i]);
        mNum.setText("12#");
    }

    private void changeSmallTags() {
        for (int i = 0; i < sL.length; i++)
            mB[i].setTag(sL[i]);
        mBCaps.setTag(LOWER);
        mNum.setTag("num");
    }

    private void changeCapitalLetters() {
        mBCaps.setVisibility(Button.VISIBLE);
        for (int i = 0; i < cL.length; i++)
            mB[i].setText(cL[i]);
        mNum.setText("12#");
    }

    private void changeCapitalTags() {
        for (int i = 0; i < cL.length; i++)
            mB[i].setTag(cL[i]);
        mBCaps.setTag(UPPER);
        mNum.setTag("num");
    }

    private void changeSyNuLetters() {
        for (int i = 0; i < nS.length; i++)
            mB[i].setText(nS[i]);
        mNum.setText("ABC");
    }

    private void changeSyNuTags() {
        for (int i = 0; i < nS.length; i++)
            mB[i].setTag(nS[i]);
        mNum.setTag("ABC");
    }

    //Enable customized keyboard
    // change the keyboard type to numeric if phone is set for the EditText type
    // Could be defined globally by setting mMode to INPUT_NUMBER.
    private void enableKeyboard() {
        if (mEditField == null || mLayout == null) return;
        mLayout = findViewById(isNumericKeyboard() ? R.id.xK2 : R.id.xK1);
        fromTopOfTheScreen(true);
        mLayout.setVisibility(LinearLayout.VISIBLE);
        keyboardShown = true;
        Log.d(TAG, "showkbd");
    }

    private boolean isNumericKeyboard() {
        return mEditField.getInputType() == InputType.TYPE_CLASS_PHONE || mMode == DType.INPUT_NUMBER;
    }

    // Disable customized keyboard
    private void disableKeyboard() {
        if (mLayout != null) mLayout.setVisibility(LinearLayout.GONE);
        keyboardShown = false;
        fromTopOfTheScreen(false);
        Log.d(TAG, "hidekbd");
    }

    private void setKeys() {
        // mWindowWidth = mMainView.getMeasuredWidth(); // getting
        mB[0] = findViewById(R.id.xA);
        mB[1] = findViewById(R.id.xB);
        mB[2] = findViewById(R.id.xC);
        mB[3] = findViewById(R.id.xD);
        mB[4] = findViewById(R.id.xE);
        mB[5] = findViewById(R.id.xF);
        mB[6] = findViewById(R.id.xG);
        mB[7] = findViewById(R.id.xH);
        mB[8] = findViewById(R.id.xI);
        mB[9] = findViewById(R.id.xJ);
        mB[10] = findViewById(R.id.xK);
        mB[11] = findViewById(R.id.xL);
        mB[12] = findViewById(R.id.xM);
        mB[13] = findViewById(R.id.xN);
        mB[14] = findViewById(R.id.xO);
        mB[15] = findViewById(R.id.xP);
        mB[16] = findViewById(R.id.xQ);
        mB[17] = findViewById(R.id.xR);
        mB[18] = findViewById(R.id.xS);
        mB[19] = findViewById(R.id.xT);
        mB[20] = findViewById(R.id.xU);
        mB[21] = findViewById(R.id.xV);
        mB[22] = findViewById(R.id.xW);
        mB[23] = findViewById(R.id.xX);
        mB[24] = findViewById(R.id.xY);
        mB[25] = findViewById(R.id.xZ);

        mB[26] = findViewById(R.id.xS2);
        mB[27] = findViewById(R.id.xS3);
        mB[28] = findViewById(R.id.xS4);
        mB[29] = findViewById(R.id.xS5);
        mB[30] = findViewById(R.id.xS6);
        //Numeric keys
        mB[31] = findViewById(R.id.x0);
        mB[32] = findViewById(R.id.x1);
        mB[33] = findViewById(R.id.x2);
        mB[34] = findViewById(R.id.x3);
        mB[35] = findViewById(R.id.x4);
        mB[36] = findViewById(R.id.x5);
        mB[37] = findViewById(R.id.x6);
        mB[38] = findViewById(R.id.x7);
        mB[39] = findViewById(R.id.x8);
        mB[40] = findViewById(R.id.x9);
        mB[41] = findViewById(R.id.xComma);
        mB[42] = findViewById(R.id.xPeriod);
        mB[43] = findViewById(R.id.xSpaceNum);
        mB[44] = findViewById(R.id.xNeg);
        mB[45] = findViewById(R.id.xBackNum);

        mBEnter = findViewById(R.id.xEnter);
        mBSpace = findViewById(R.id.xSpace);
        mBCaps = findViewById(R.id.xChange);
        mBack = findViewById(R.id.xBack);
        mNum = findViewById(R.id.xNum);
        mBClose = findViewById(R.id.xClose);
        mBCloseN = findViewById(R.id.xCloseN);

        for (Button aMB : mB)
            aMB.setOnClickListener(this);
        mBEnter.setOnClickListener(this);
        mBSpace.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mBCaps.setOnClickListener(this);
        mNum.setOnClickListener(this);
        mBClose.setOnClickListener(this);
        mBCloseN.setOnClickListener(this);
    }

    /**
     * Positioning cursor inside the edit field
     */
    public int getCharIndexFromCoordinate(EditText e, float x, float y) {
        if (e == null || e.getLayout() == null) return 0;
        int height = e.getPaddingTop() - e.getScrollY(); // Offset the top padding

        for (int i = 0; i < e.getLayout().getLineCount(); i++) {
            Rect bounds = new Rect();
            e.getLayout().getLineBounds(i, bounds);
            height += bounds.height();

            if (height >= y) {
                int lineStart = e.getLayout().getLineStart(i);
                int lineEnd = e.getLayout().getLineEnd(i);

                Spanned span = e.getText();
                RelativeSizeSpan[] sizeSpans = span.getSpans(lineStart, lineEnd, RelativeSizeSpan.class);
                float scaleFactor = 1;
                if (sizeSpans != null) {
                    for (RelativeSizeSpan sizeSpan : sizeSpans) {
                        scaleFactor = sizeSpan.getSizeChange();
                    }
                }

                String lineSpan = e.getText().subSequence(lineStart, lineEnd).toString();
                float[] widths = new float[lineSpan.length()];
                TextPaint paint = e.getPaint();
                paint.getTextWidths(lineSpan, widths);

                float width = 0;
                int j;
                for (j = 0; j < lineSpan.length(); j++) {

                    width += widths[j] * scaleFactor;

                    if (width >= x) { // || j == lineSpan.length() - 1 ) {

                        return lineStart + j;
                    }
                }
                return lineStart + j;
            }
        }
        return -1;
    }

    public InputDialog pop() {
        show();
        return this;
    }

    @Override
    public void show() {
        boolean full_screen = SettingsActivity.isHideNavBar(getContext());
        int t = SettingsActivity.DismissWarningsDelay();
        if (mMode == DType.TOAST_WAIT && t > 0 && mTimeout != 0) {
            if (mTimeout > 0) t = mTimeout; //override the settings value
            if (full_screen) {
                SettingsActivity.hideNavBarForView(getWindow().getDecorView());
            }
            super.show();

            final InputDialog cd = this;
            Runnable r = new Runnable() {
                public void run() {
                    cd.dismiss();
                }
            };
            mEt.postDelayed(r, t);
        } else {

            //http://vardhan-justlikethat.blogspot.com/2014/06/android-immersive-mode-for-dialog.html
            if (full_screen) {
                //Log.d(TAG, "decorview=" + getWindow().getDecorView());
                if (SettingsActivity.isHideNavBarSupported()) {
                    SettingsActivity.hideNavBarForView(getWindow().getDecorView());
                }
            } else if (isNightMode || isOnyx) {
                SettingsActivity.changeNavBarBackground(getWindow(), getContext());
            }
            super.show();
        }
    }

    /**
     * Simple screen note with one button
     */
    public void show(String title, String message) {
        setTitle(title);
        setMessage(message);
        setPositiveButton("OK");
        show();
    }

    /**
     * Quick constructor to finish current activity on exit and run something on OK
     *
     * @param a          - target activity to close on OK
     * @param m          - message (if ends with '?' Cancel button is added to skip runnable)
     * @param okrunnable - runnable to be implemented when finishing the activity
     * @return
     */
    public static InputDialog abort(final Activity a, String m, final Runnable okrunnable) {
        InputDialog d = new InputDialog(a);
        d.setType(DType.TOAST_WAIT);
        d.setMessage(m);
        d.mTimeout = 0;
        d.setPositiveButton("OK", new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                if (okrunnable != null) okrunnable.run();
                a.finish();
            }
        });
        if (m.endsWith("?")) d.setNegativeButton("Cancel");

        return d;
    }

    /**
     * Quick constructor to close activity without any on abort runnable
     *
     * @param a - target activity to close on OK
     * @param m - message (if ends with '?' Cancel button is added to skip runnable)
     */
    public static InputDialog abort(final Activity a, String m) {
        return abort(a, m, null);
    }

    /**
     * Constructs dialog in Toast replacement mode
     *
     * @param context
     * @param message
     * @param timeout set to 0 for manual dismiss
     * @return
     */
    public static InputDialog message(Context context, String message, int timeout) {
        InputDialog d = new InputDialog(context);
        d.setType(DType.TOAST_WAIT);
        d.setMessage(message);
        d.mTimeout = timeout;
        if (d.mTimeout == 0) { //manual dismiss
            d.setPositiveButton("OK");
        }
        return d;
    }


    /**
     * Quick constructor of message dialog auto dismissed by Settings time delay
     */
    public static InputDialog message(Context context, String message) {
        if (!Global.ALEX_DIALOG_MESSAGE) {
            return new InputDialogReplacement(context, message);
        }
        return message(context, message, SettingsActivity.DismissWarningsDelay());
    }

    public static InputDialog message(Context context, int resid) {
        return message(context, context.getResources().getString(resid), SettingsActivity.DismissWarningsDelay());
    }

    public static InputDialog message(Context context, int resid, int timeout) {
        return message(context, context.getResources().getString(resid), timeout);
    }

    /**
     * Showing please wait message, back disabled
     */
    public static InputDialog pleaseWait(Context context, String message) {
        InputDialog d = new InputDialog(context);
        d.insertLayout(R.layout.inpd_spinner);
        d.setType(DType.SHOW_SPINNER);
        d.setMessage(message);
        d.setPositiveButton("");
        d.setNegativeButton("");
        d.backDisabled = true;

        return d;
    }

    /**
     * Showing please wait message, back enabled
     */
    public static InputDialog pleaseWaitBackEnabled(Context context, String message) {
        InputDialog d = new InputDialog(context);
        d.insertLayout(R.layout.inpd_spinner);
        d.setType(DType.SHOW_SPINNER);
        d.setMessage(message);
        d.setPositiveButton("");
        d.setNegativeButton("");
        d.backDisabled = false;

        return d;
    }

    /**
     * Set text string to show on ? icon click in the dialog's header
     */
    public void setHelp(String s) {
        if (s.length() == 0) s = null;
        mHelpText = s;
    }

    /**
     * Set text resource to show on ? icon click in the dialog's header
     */
    public void setHelp(int resid) {
        setHelp(Global.getAppContext().getResources().getString(resid));
    }

    public void disableBackButton(boolean b) {
        backDisabled = b;
    }

    /**
     * disable editing of the eT field
     */

    public void setFreeze() {
        mNotEditable = true;
    }

    public void showAtTop() {
        mShowAtTop = true;
        show();
    }


    public static Toast toast(String message, Context context) {
        InputDialog d = new InputDialog(context);
        LayoutInflater inflater = d.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_r, (ViewGroup) d.findViewById(R.id.toast_root));
        LinearLayout frame = layout.findViewById(R.id.toast_frame);
        TextView text = layout.findViewById(R.id.toast_tv);
        text.setText(message);

        if (SettingsActivity.getNightMode()) {
            text.setTextColor(0xFFEF0000);
            text.setBackgroundColor(0);
            frame.setBackgroundResource(R.drawable.dlg_red);
        } else if (SettingsActivity.getDarkSkin()) {
            text.setTextColor(0xffffffff);
            text.setBackgroundColor(0);
            frame.setBackgroundResource(R.drawable.dlg_blk);
        } else {
            text.setTextColor(0xff000000);
            text.setBackgroundColor(0xffffffff);
            frame.setBackgroundResource(R.drawable.dlg_wht);
        }

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);//.LENGTH_LONG);
        toast.setView(layout);
        return toast;
    }


    //Gesture Detector (just implement OnGestureListener in the Activity)
    GestureDetector gDetector = new GestureDetector(this);

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return gDetector.onTouchEvent(me);
    }

    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
        if (start == null || finish == null) return false;
        float dx = start.getRawX() - finish.getRawX();
        if (dx > Global.flickLength) { //left
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            Log.d(TAG, "dispatch bak");
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

    public void toggleKeyboard() {
        mToggleKeyboard = true;
    }

    protected void startTest(int action, int param) {
        switch (action) {
            case TestIntentService.DIALOG_OK:
                btnOK.performClick();
                unregisterTestReceiver();
                break;
            case TestIntentService.DIALOG_CANCEL:
                Button btnCancel = findViewById(R.id.bnd_cancel);
                btnCancel.performClick();
                unregisterTestReceiver();
            case TestIntentService.DIALOG_TEXT_TESTTXT:
                mEt.setText("test.txt");
                break;
            case TestIntentService.DIALOG_TEXT_TEST2:
                mEt.setText("test2");
                break;
        }
    }

    protected int getTestActivityNumber() {
        return TestIntentService.DIALOG;
    }

    BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int activity = intent.getIntExtra(Constants.TEST_ACTIVITY, 0);
            if (activity != getTestActivityNumber()) return;
            int action = intent.getIntExtra(Constants.TEST_ACTIVITY_ACTION, 0);
            int param = intent.getIntExtra(Constants.TEST_ACTIVITY_PARAM, 0);
            startTest(action, param);
        }
    };

    private void registerTestReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.TEST_BROADCAST);
        LocalBroadcastManager.getInstance(context).registerReceiver(testReceiver, filter);
    }

    private void unregisterTestReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(testReceiver);
    }
}



