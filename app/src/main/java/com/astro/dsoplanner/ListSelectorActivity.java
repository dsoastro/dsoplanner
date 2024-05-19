package com.astro.dsoplanner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListSelectorActivity extends ParentListActivity {
    private LSadapter mAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        hideMenuBtn();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_selector);
        String[] names = getIntent().getStringArrayExtra(Constants.LIST_SELECTOR_NAMES);
        boolean[] checks = getIntent().getBooleanArrayExtra(Constants.LIST_SELECTOR_CHECKS);
        if (checks == null) {
            checks = new boolean[names.length];
        } else if (checks.length != names.length) {
            checks = new boolean[names.length];
        }
        String name = getIntent().getStringExtra(Constants.LIST_SELECTOR_NAME);
        setTitle(name);
        mAdapter = new LSadapter(names, checks);
        setListAdapter(mAdapter);
        getListView().setVerticalFadingEdgeEnabled(!nightMode);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent();
            i.putExtra(Constants.LIST_SELECTOR_CHECKS, mAdapter.getChecks());
            setResult(RESULT_OK, i);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
        if (position == 0) {
            mAdapter.clearAll();
            mAdapter.notifyDataSetChanged();
            return;
        }

        ChB chb = (ChB) v.findViewById(R.id.list_selector_item_ch);
        boolean status = mAdapter.getChecks()[position - 1];
        status = !status;
        mAdapter.setCheck(position - 1, status);
        chb.setChecked(status);
    }

    private class LSadapter extends BaseAdapter {
        private LayoutInflater mInflater;
        String[] names;
        boolean[] checks;

        public LSadapter(String[] names, boolean[] checks) {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.names = names;
            this.checks = checks;
        }

        public boolean[] getChecks() {
            return checks;
        }

        public void setCheck(int position, boolean status) {
            checks[position] = status;
        }

        public void clearAll() {
            for (int i = 0; i < checks.length; i++) {
                checks[i] = false;
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_selector_item, null);
            }
            if (position == 0) {
                ((TextView) convertView.findViewById(R.id.list_selector_text)).setText(getString(R.string.deselect_all));
                ChB chb = (ChB) convertView.findViewById(R.id.list_selector_item_ch);
                chb.setVisibility(View.GONE);
            } else {
                ((TextView) convertView.findViewById(R.id.list_selector_text)).setText(names[position - 1]);


                ChB chb = (ChB) convertView.findViewById(R.id.list_selector_item_ch);
                chb.setChecked(checks[position - 1]);
                chb.setVisibility(View.VISIBLE);
                chb.setClickable(false);
                chb.position = position - 1;
            }


            if (SettingsActivity.getDarkSkin() || SettingsActivity.getNightMode())
                convertView.setBackgroundColor(0xff000000);

            return convertView;
        }

        public int getCount() {
            return names.length + 1;

        }

        public Object getItem(int position) {
            return names[position - 1];

        }

        public long getItemId(int position) {
            return position;
        }
    }
}
