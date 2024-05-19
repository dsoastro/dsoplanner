package com.astro.dsoplanner;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.astro.dsoplanner.database.Db;

public class SelectorActivity extends ParentListActivity {

    private static final String LIKE = " like '";
    private static final String COUNTRY_LAT_LON = ",country,lat,lon";
    private static final String ROWID = ",rowid";
    private static final String ORDER_BY = " order by ";
    private static final String GROUP_BY = " group by ";
    private static final String AND = " and ";
    private static final String WHERE = " where ";
    private static final String ALL = "ALL";
    private static final String FROM = " from ";
    private static final String SELECT = "select ";
    private static final String TAG = SelectorActivity.class.getSimpleName();


    class data {
        String name;
        int rowid;

        public data(String name, int rowid) {
            super();
            this.name = name;
            this.rowid = rowid;
        }

    }

    Cursor cursor;
    SAAdapter mAdapter;

    String[] fnames;
    String[] fields;
    String fname;
    String table;
    String dbname;
    Db db;
    String search_title;

    public static final int GEO = 1;
    int calling_id = 0;
    String search_string = "";


    Handler handler = new Handler();

    String[] positions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mselector);
        mAdapter = new SAAdapter();
        setListAdapter(mAdapter);
        init();
        updateList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideMenuBtn();
    }

    private void init() {
        fnames = getIntent().getStringArrayExtra(Constants.SELECTOR_FNAMES);
        fields = getIntent().getStringArrayExtra(Constants.SELECTOR_FIELDS);
        fname = getIntent().getStringExtra(Constants.SELECTOR_FNAME);
        table = getIntent().getStringExtra(Constants.SELECTOR_TABLE);
        dbname = getIntent().getStringExtra(Constants.SELECTOR_DBNAME);
        String actname = getIntent().getStringExtra(Constants.SELECTOR_ACTIVITY_NAME);
        calling_id = getIntent().getIntExtra(Constants.SELECTOR_CALLING_ACTIVITY, 0);
        search_title = getIntent().getStringExtra(Constants.SELECTOR_SEARCH_TITLE);
        if (search_title == null) search_title = "";
        setTitle(actname);


        boolean search_btn = getIntent().getBooleanExtra(Constants.SELECTOR_SEARCH_BUTTON, false);
        db = new Db(getApplicationContext(), dbname);

        positions = new String[fnames.length];
        for (int i = 0; i < fields.length; i++) {

            positions[i] = ALL;
        }


        LinearLayout ll = (LinearLayout) findViewById(R.id.selector_ll);
        int[] abtn = new int[]{R.id.selector_btn1, R.id.selector_btn2, R.id.selector_btn3, R.id.selector_btn4};
        if (fields.length > 4)
            throw new UnsupportedOperationException("4 fields max");

        //removing excessive buttons
        int search = 0;
        if (search_btn) search = 1;
        for (int i = 3; i > fields.length - 1 + search; i--) {
            View v = findViewById(abtn[i]);
            ll.removeView(v);
        }

        if (search_btn) {
            Button btn = (Button) findViewById(abtn[fields.length]);
            btn.setText(R.string.search);
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    InputDialog d5 = new InputDialog(SelectorActivity.this);
                    d5.setValue(search_string);
                    d5.setType(InputDialog.DType.INPUT_STRING);
                    d5.setTitle(getString(R.string.search));
                    d5.setMessage(search_title);
                    d5.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                        public void onClick(String value) {

                            search_string = value;
                            handler.post(new Runnable() {
                                public void run() {
                                    updateList();
                                }
                            });
                        }
                    });
                    d5.setNegativeButton(getString(R.string.cancel), new InputDialog.OnButtonListener() {
                        public void onClick(String value) {

                        }
                    });
                    registerDialog(d5).show();

                }
            });
        }


        for (int i = 0; i < fields.length; i++) {
            Button btn = (Button) findViewById(abtn[i]);

            btn.setText(fnames[i]);
            final String fn = fnames[i];
            final int j = i;
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Db db = new Db(getApplicationContext(), dbname);
                    String[] a = null;
                    try {
                        db.open();
                        String sql = SELECT + fields[j] + FROM + table;
                        Set<String> set = new HashSet<String>();
                        for (int i = 0; i < positions.length; i++) {
                            if (!ALL.equals(positions[i])) {
                                set.add(fields[i] + "='" + positions[i] + "'");
                            }
                        }


                        if (set.size() != 0) {
                            sql = sql + WHERE;
                            for (String s : set) {
                                sql = sql + s + AND;
                            }
                            sql = sql.substring(0, sql.length() - 5);
                        }

                        sql = sql + GROUP_BY + fields[j] + ORDER_BY + fields[j] + " asc";


                        Log.d(TAG, "init sql=" + sql);
                        Cursor cursor = db.rawQuery(sql);

                        List<String> list = new ArrayList<String>();
                        while (cursor.moveToNext()) {
                            String s = cursor.getString(0);
                            list.add(s);
                        }

                        a = new String[list.size() + 1];
                        for (int j = 0; j < list.size(); j++) {
                            a[j + 1] = list.get(j);
                        }
                        a[0] = ALL;
                        db.close();
                        Log.d(TAG, "sql over");

                    } catch (Exception e) {
                        Log.d(TAG, "exception=" + e);

                    }

                    final String[] items = a;
                    if (items != null && items.length != 0) {
                        int pos = 0;
                        for (int k = 0; k < items.length; k++) {
                            if (items[k].equals(positions[j])) {
                                pos = k;
                                break;
                            }
                        }


                        InputDialog d2 = new InputDialog(SelectorActivity.this);
                        d2.setTitle(fn);
                        d2.setValue("" + pos);
                        d2.setListItems(a, new InputDialog.OnButtonListener() {
                            public void onClick(String value) {
                                Log.d(TAG, "clicked");
                                int pos = AstroTools.getInteger(value, -1, -1, Integer.MAX_VALUE);
                                positions[j] = items[pos];
                                handler.post(new Runnable() {
                                    public void run() {
                                        updateList();
                                    }
                                });


                            }
                        });
                        registerDialog(d2).show();
                    }
                }
            });
        }
    }


    private void updateList() {
        try {
            cursor.close();
            db.close();

        } catch (Exception e) {
        }
        try {

            db.open();
            String sql = SELECT + fname + ROWID;
            if (calling_id == GEO) {
                sql = sql + COUNTRY_LAT_LON;
            }
            sql = sql + FROM + table;
            Set<String> set = new HashSet<String>();
            for (int i = 0; i < positions.length; i++) {
                if (!positions[i].equals(ALL)) {
                    set.add(fields[i] + "='" + positions[i] + "'");
                }
            }


            if (set.size() != 0) {
                sql = sql + WHERE;
                for (String s : set) {
                    sql = sql + s + AND;
                }
                sql = sql.substring(0, sql.length() - 5);
                if (!"".equals(search_string)) {
                    sql = sql + AND + fname + LIKE + search_string + "%'";
                }
            } else {
                if (!"".equals(search_string)) {
                    sql = sql + WHERE + fname + LIKE + search_string + "%'";
                }
            }

            sql = sql + ORDER_BY + fname + " asc";
            Log.d(TAG, "update sql=" + sql);
            cursor = db.rawQuery(sql);
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        }
        mAdapter.notifyDataSetChanged();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            cursor.close();
        } catch (Exception e) {
        }

        try {
            db.close();
        } catch (Exception e) {
        }
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        if (cursor == null) return;
        Intent intent = new Intent();
        cursor.moveToPosition(position);
        intent.putExtra(Constants.SELECTOR_RESULT, cursor.getInt(1));
        setResult(RESULT_OK, intent);
        finish();
    }

    private class SAAdapter extends BaseAdapter {

        private LayoutInflater mInflater;


        public SAAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.selector_item, null);
            }
            cursor.moveToPosition(position);
            if (calling_id == GEO) {
                String city = cursor.getString(0);
                String country = cursor.getString(2);
                double lat = cursor.getDouble(3);
                double lon = cursor.getDouble(4);
                ((TextView) convertView.findViewById(R.id.selector_name)).setText(city + ", " + country + ", " + AstroTools.getLatString(lat) + " " + AstroTools.getLonString(lon));
            } else
                ((TextView) convertView.findViewById(R.id.selector_name)).setText(cursor.getString(0));


            return convertView;
        }

        @Override
        public int getCount() {
            if (cursor == null)
                return 0;
            else
                return cursor.getCount();

        }

        @Override
        public Object getItem(int position) {
            cursor.moveToPosition(position);
            return cursor.getString(0);

        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

}
