package com.astro.dsoplanner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.astro.dsoplanner.f.IFolderItemListener;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FL2 extends LinearLayout implements AdapterView.OnItemClickListener {

    Context context;
    IFolderItemListener folderListener;
    private List<String> item = null;
    private List<String> path = null;
    private String ROOT = "/";
    private String curPath = ROOT;
    private TextView mFolderPath;
    private Button mSortButton;
    private ListView lstView;
    private static final java.lang.String DELIMITER = "%";

    //Sorting
    private int mSortMode = SB_NAME;
    private static final int SB_NAME = 0;
    private static final int SB_TIME = 1;
    private static final int SB_EXT = 2;
    private static final int SB_MAX = 2;
    private String[] mSortModes;// = {"NAME","TIME","TYPE"};

    public FL2(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mSortModes = new String[]{context.getString(R.string.name), context.getString(R.string.time2), context.getString(R.string.type)};
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.folderview2, this);

        mFolderPath = (TextView) findViewById(R.id.path);
        mSortButton = (Button) findViewById(R.id.bnSort);
        lstView = (ListView) findViewById(R.id.list);
        setNightMode(SettingsActivity.getNightMode());
        getDir(ROOT, lstView);
        mSortButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSortMode++;
                if (mSortMode > SB_MAX) mSortMode = 0;
                mSortButton.setText(mSortModes[mSortMode]);
                getDir(curPath, lstView);
            }
        });
    }

    public void setIFolderItemListener(IFolderItemListener folderItemListener) {
        this.folderListener = folderItemListener;
    }

    /**
     * Set directory to show
     *
     * @param dirPath
     */
    public void setDir(String dirPath) {
        getDir(dirPath, lstView);
    }

    private boolean nightmode;

    private void setNightMode(boolean nightmode) {
        this.nightmode = nightmode;
        if (nightmode) lstView.setFastScrollEnabled(false);
    }

    /**
     * Reads, sorts, and formats the directory content into a list
     *
     * @param dirPath
     * @param v
     */
    private void getDir(String dirPath, ListView v) {
        curPath = dirPath;
        mFolderPath.setText(getContext().getString(R.string.location_) + dirPath);
        item = new ArrayList<String>();
        path = new ArrayList<String>();
        File f = new File(dirPath);
        File[] files = f.listFiles();

        String goback = getContext().getString(R.string.go_back);
        String dirStr = getContext().getString(R.string.directory);

        //Sort Folders first, sort by name case insensitive.
        if (files != null && files.length > 1) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    boolean isFirstDirectory = o1.isDirectory();
                    boolean isSecondDirectory = o2.isDirectory();
                    if (!isFirstDirectory && !isSecondDirectory) { //files
                        return compareFiles(o1, o2);
                    } else if (isFirstDirectory && !isSecondDirectory) {
                        return -1;
                    } else if (isSecondDirectory && !isFirstDirectory) {
                        return 1;
                    } else {
                        return compareFiles(o1, o2);
                    }
                }
            });
        }

        if (files != null) {
            if (!dirPath.contentEquals(ROOT)) {
                String parent = f.getParent();
                if (parent != null) {
                    item.add("../" + DELIMITER + goback);
                    path.add(parent);
                }
            }

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                path.add(file.getPath());
                if (file.isDirectory()) {
                    item.add(file.getName() + DELIMITER + dirStr + "\t" + getDate(file.lastModified(), "yyyy/MM/dd hh:mm:ss"));
                } else { //must keep \t to draw folder icon properly! Or check for "Directory" word!
                    item.add(file.getName() + DELIMITER + "\t" + getDate(file.lastModified(), "yyyy/MM/dd hh:mm:ss") + "\t" + getSize(file.length()));
                }
            }

        }
        setItemList(item);
    }

    private int compareFiles(File f1, File f2) {
        switch (mSortMode) {
            case SB_NAME:
                return f1.getAbsolutePath().toLowerCase().compareTo(f2.getAbsolutePath().toLowerCase());
            case SB_TIME:
                return compareLongs(f1.lastModified(), f2.lastModified());
            case SB_EXT:
                return fileExt(f1).compareTo(fileExt(f2));
        }
        return 0;
    }

    private int compareLongs(long l1, long l2) {
        if (l1 < l2) return 1;
        if (l1 == l2) return 0;
        return -1;
    }

    /**
     * Setup the list with data
     *
     * @param items
     */
    public void setItemList(List<String> items) {
        ArrayAdapter<String> fileList = new FileListArrayAdapter(context, items);
        lstView.setAdapter(fileList);
        lstView.setOnItemClickListener(this);
    }

    /**
     * Pass the file path back to the listener method registered
     *
     * @param l
     * @param v
     * @param position
     * @param id
     */
    public void onListItemClick(ListView l, View v, int position, long id) {
        File file = new File(path.get(position));
        if (file.isDirectory()) {
            if (file.canRead()) getDir(path.get(position), l);
            else {
                //folder is unreadable
                if (folderListener != null) {
                    folderListener.onCannotFileRead(file);
                }
            }
        } else {
            //file is clicked
            if (folderListener != null) {
                folderListener.onFileClicked(file);
            }

        }
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        onListItemClick((ListView) arg0, arg0, arg2, arg3);
    }

    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @param dateFormat   Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    /**
     * Returns filename extension
     *
     * @param file
     * @return
     */
    private String fileExt(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Convert file size to M KB
     *
     * @param size
     * @return
     */
    public String getSize(long size) {
        String hrSize;
        double m = size / 1024.0;

        if (m > 1024) { //Mb
            m /= 1024;
            DecimalFormat dec = new DecimalFormat("0.00");
            hrSize = dec.format(m).concat("M");
        } else if (m > 1) { //kB
            DecimalFormat dec = new DecimalFormat("0.0");
            hrSize = dec.format(m).concat("k");
        } else { //bytes
            DecimalFormat dec = new DecimalFormat("0");
            hrSize = dec.format(size).concat("b");
        }
        return hrSize;
    }

    /**
     * ListView Adapter class
     */
    public class FileListArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final List<String> values;

        public FileListArrayAdapter(Context context, List v) {
            super(context, -1, v);
            this.context = context;
            this.values = v;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.folderitem, parent, false);
                if (nightmode) {
                    Drawable d = getResources().getDrawable(R.drawable.rfolder);
                    ImageView iv = (ImageView) rowView.findViewById(R.id.bFolderIcon);
                    iv.setImageDrawable(d);
                }
            } else rowView = convertView;


            TextView tvName = (TextView) rowView.findViewById(R.id.tvFileName);
            TextView tvInfo = (TextView) rowView.findViewById(R.id.tvFileInfo);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.bFolderIcon);
            //Decode date and size
            String[] arr = values.get(position).split(DELIMITER);
            char c = arr[1].charAt(0);
            if (c == '\t') imageView.setVisibility(GONE); //file
            else imageView.setVisibility(VISIBLE); //folder
            tvName.setText(arr[0]); //name
            tvInfo.setText(arr[1]); //date, size

            return rowView;
        }
    }
}
