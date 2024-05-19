package com.astro.dsoplanner.infolist;

import static com.astro.dsoplanner.Constants.SELECTED;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.AstroTools.TransitRec;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.base.Exportable;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.util.Holder2;


import com.astro.dsoplanner.ObjectInflater;
import com.astro.dsoplanner.SettingsActivity;


public class ObsInfoListImpl implements InfoList {

    private static final String MAXIMUM_NUMBER_OF_OBJECTS_IN_THE_LIST_REACHED = "Maximum number of objects in the list reached";
    private static final String CLASS_CAST_ERROR = "class cast error";
    private static final String TAG = ObsInfoListImpl.class.getSimpleName();
    public static final int SORT_CONSTELLATION = 1;
    public static final int SORT_MAGNITUDE = 2;
    public static final int SORT_DIMENSION = 3;
    public static final int SORT_NUMBER = 4;
    public static final int SORT_SETTIME = 5;
    public static final int SORT_RA = 6;
    public static final int SORT_DEC = 7;
    public static final int SORT_ALT = 8;
    public static final int SORT_VIS = 9;
    public static final int SORT_TYPE = 10;

    public static class Item extends Holder2<AstroObject, Boolean> implements Exportable {
        public Item(AstroObject obj, Boolean b) {
            super(obj, b);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Item) {
                AstroObject obj = ((Item) o).x;
                return (this.x.equals(obj));
            }
            return false;
        }

        public Item(DataInputStream stream) throws IOException {
            super(null, false);
            y = stream.readBoolean();
            int classId = stream.readInt();
            x = (AstroObject) ObjectInflater.getInflater().inflate(classId, stream);
        }

        public int getClassTypeId() {
            return Exportable.OBS_LIST_ITEM;
        }

        public byte[] getByteRepresentation() {

            byte[] astroObj = x.getByteRepresentation();
            if (astroObj == null)
                return null;
            ByteArrayOutputStream buff = new ByteArrayOutputStream(4 + 4 + astroObj.length);
            DataOutputStream stream = new DataOutputStream(buff);
            try {
                stream.writeBoolean(y);
                stream.writeInt(x.getClassTypeId());
                stream.write(astroObj);
            } catch (IOException e) {
                return null;
            }

            return buff.toByteArray();
        }

        public HashMap<String, String> getStringRepresentation() {
            HashMap<String, String> map = x.getStringRepresentation();
            map.put(SELECTED, "" + y);
            return map;
        }

        public String toString() {
            return "" + x + " " + y;
        }
    }

    protected List<Item> list = new ArrayList<Item>();
    private String name;

    public ObsInfoListImpl(String name) {
        this.name = name;
    }

    public int getCount() {
        return list.size();
    }

    /**
     * this method implementation checks for equal objects and does not allow adding equal objects
     */
    public void fill(InfoListFiller filler) {
        Iterator it = filler.getIterator();
        List<AstroObject> tmpList = new ArrayList<AstroObject>();
        Map<AstroObject, Boolean> map = new HashMap<AstroObject, Boolean>();
        int k = 0;
        while (it.hasNext()) {
            Object o = it.next();
            Log.d(TAG, "fill o=" + o);
            if (k > 5 * Global.OBS_LIST_NUM_OBJECTS_LIMIT)
                break;

            if (o != null && o instanceof AstroObject) {
                tmpList.add((AstroObject) o);
                k++;
            } else if (o != null && o instanceof ObsInfoListImpl.Item) {
                ObsInfoListImpl.Item item = (ObsInfoListImpl.Item) o;
                map.put(item.x, item.y);
                k++;
            }
        }
        for (Item h : list) {
            if (tmpList.contains(h.x))
                tmpList.remove(h.x);
            if (map.containsKey(h.x))
                map.remove(h.x);
        }

        int i = list.size();
        for (AstroObject obj : tmpList) {
            if (i > Global.OBS_LIST_NUM_OBJECTS_LIMIT) break;
            list.add(new Item(obj, false));
            i++;
        }
        for (Map.Entry<AstroObject, Boolean> e : map.entrySet()) {
            if (i > Global.OBS_LIST_NUM_OBJECTS_LIMIT) break;
            list.add(new Item(e.getKey(), e.getValue()));
            i++;
        }


    }

    public Object get(int position) {
        return list.get(position);
    }

    public void remove(int position) {
        list.remove(position);
    }

    public void removeAll() {
        list = new ArrayList();
    }

    public Iterator iterator() {
        return list.iterator();
    }

    public String getListName() {
        return name;
    }

    public void setListName(String name) {
        this.name = name;
    }

    public boolean save(InfoListSaver listSaver) {
        //boolean noError=true;
        try {
            listSaver.addName(name);
            for (Item i : list) {
                listSaver.addObject(i);
            }
            listSaver.close();
        } catch (IOException e) {
            Log.d(TAG, "" + e);
            return false;
        }
        return true;
    }

    private boolean sortByName = false;

    /**
     * sort loaded list by load method by name
     */
    public void sortLoadedList() {
        sortByName = true;
    }

    public ErrorHandler load(InfoListLoader listLoader) {//adding only items not in the list already
        boolean noError = true;
        List<Item> tmpList = new ArrayList<Item>();
        try {
            name = listLoader.getName();
            listLoader.open();
        } catch (Exception e) {

            return new ErrorHandler(ErrorHandler.IO_ERROR, "");
        }
        Object obj = null;
        ErrorHandler eh = new ErrorHandler();
        int line = 1;
        boolean limit = false;
        while (true) {
            ErrorHandler.ErrorRec erec = new ErrorHandler.ErrorRec();
            erec.lineNum = line;
            try {
                obj = listLoader.next(erec);
                if (line > Global.OBS_LIST_NUM_OBJECTS_LIMIT)
                    throw new EOFException();
            } catch (IOException e) {
                Log.d(TAG, "e=" + e);
                if (!(e instanceof EOFException))
                    eh.addError(ErrorHandler.IO_ERROR, "", "", line);

                try {
                    listLoader.close();
                } catch (IOException e1) {
                }

                if (sortByName) {
                    Comparator<Item> comp = new ComparatorImpl(SORT_NUMBER, 1);
                    Collections.sort(tmpList, comp);
                }
                tmpList.removeAll(list);
                int i = list.size();
                for (Item item : tmpList) {
                    if (i > Global.OBS_LIST_NUM_OBJECTS_LIMIT) {
                        ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.WARNING, MAXIMUM_NUMBER_OF_OBJECTS_IN_THE_LIST_REACHED);
                        eh.addError(rec);
                        break;
                    }
                    list.add(item);
                    i++;
                }
                return eh;
            }

            if (obj != null && obj instanceof Item) {
                Item item = (Item) obj;
                if (item.x != null)
                    tmpList.add((Item) obj);
            } else if (obj != null)
                eh.addError(ErrorHandler.WRONG_TYPE, CLASS_CAST_ERROR, "", line);
            else {
                eh.addError(erec);
                Log.d(TAG, "erec=" + erec);
            }

            line++;
        }
    }

    int line = 1;
    boolean limit = false;

    /**
     * @param listLoader
     * @param eh
     * @return holder with object and flag that work is over (true for over)
     */
    public Holder2<Item, Boolean> next(InfoListLoader listLoader, ErrorHandler eh) {//adding only items not in the list already
        boolean noError = true;
        List<Item> tmpList = new ArrayList<Item>();
        try {
            name = listLoader.getName();
            listLoader.open();
        } catch (Exception e) {
            eh.addError(new ErrorHandler(ErrorHandler.IO_ERROR, ""));
            return new Holder2<Item, Boolean>(null, true);
        }
        Object obj = null;

        ErrorHandler.ErrorRec erec = new ErrorHandler.ErrorRec();
        erec.lineNum = line;
        try {
            obj = listLoader.next(erec);
            if (line > Global.OBS_LIST_NUM_OBJECTS_LIMIT)
                return new Holder2<Item, Boolean>(null, true);
            ;
        } catch (IOException e) {
            Log.d(TAG, "e=" + e);
            if (!(e instanceof EOFException))
                eh.addError(ErrorHandler.IO_ERROR, "", "", line);

            try {
                listLoader.close();
            } catch (IOException e1) {
            }

            return new Holder2<Item, Boolean>(null, true);
        }

        if (obj != null && obj instanceof Item) {
            Item item = (Item) obj;
            if (item.x != null) {
                return new Holder2<Item, Boolean>(item, false);

            } else
                eh.addError(erec);

        } else if (obj != null)
            eh.addError(ErrorHandler.WRONG_TYPE, CLASS_CAST_ERROR, "", line);
        else {
            eh.addError(erec);
            Log.d(TAG, "erec=" + erec);
        }
        line++;
        return new Holder2<Item, Boolean>(null, false);
    }

    /**
     * move item1 before item2
     *
     * @param item1
     * @param item2
     */
    public void move(Item item1, Item item2) {
        int pos1 = -1;
        int pos2 = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(item1))
                pos1 = i;
            if (list.get(i).equals(item2))
                pos2 = i;
        }
        if (pos1 == -1 || pos2 == -1) return;
        if (pos1 == pos2) return;

        list.add(pos2, item1);
        if (pos1 > pos2)
            pos1++;

        list.remove(pos1);

    }

    public void sort(int sortType, int order) {
        Comparator<Item> comp = null;
        if (sortType != SORT_SETTIME) {
            comp = new ComparatorImpl(sortType, order);
        } else {
            Map<AstroObject, TransitRec> map = new HashMap<AstroObject, TransitRec>();
            Calendar c = AstroTools.getDefaultTime(Global.getAppContext());
            for (Item h : list) {
                TransitRec rec = AstroTools.getRiseSetting(h.x, c, AstroTools.hStars);
                if (rec.tSetting != null) {
                    if (rec.tSetting.getTimeInMillis() < c.getTimeInMillis()) {
                        Log.d(TAG, "tsetting=" + rec.tSetting.getTimeInMillis() + " time=" + c.getTimeInMillis());
                        Calendar c1 = Calendar.getInstance();
                        c1.setTimeInMillis(c.getTimeInMillis() + 24 * 3600 * 1000);
                        rec = AstroTools.getRiseSetting(h.x, c1, AstroTools.hStars);
                    }
                }
                map.put(h.x, rec);
            }
            comp = new ComparatorImpl(sortType, order, map);
        }
        Collections.sort(list, comp);
    }

    public void sort(Comparator comp) {

    }

    private class ComparatorImpl implements Comparator<Item> {
        private int cmp;
        private int order;
        private double lst;
        private double lat;
        private Map<AstroObject, TransitRec> map;//transit rec for dso to be compared on the basis of rise/setting

        public ComparatorImpl(int cmpType, int order) { //-1 from lower to high, 1 from high to lower
            this.cmp = cmpType;
            this.order = order;
            if (cmp == SORT_ALT) {
                lat = SettingsActivity.getLattitude();
                lst = AstroTools.sdTime(AstroTools.getDefaultTime(Global.getAppContext()));
            }

        }

        public ComparatorImpl(int cmpType, int order, Map<AstroObject, TransitRec> map) {
            this(cmpType, order);
            this.map = map;
        }

        public int compare(Item lhs, Item rhs) {
            return compare(lhs.x, rhs.x);
        }

        private int compare(AstroObject lhs, AstroObject rhs) {
            switch (cmp) {
                case SORT_RA:
                    double lhs_ra = AstroTools.normalise24(lhs.getRa());
                    double rhs_ra = AstroTools.normalise24(rhs.getRa());
                    if (lhs_ra < rhs_ra)
                        return -order;
                    if (lhs_ra == rhs_ra)
                        return new ComparatorImpl(SORT_NUMBER, 1).compare(lhs, rhs);
                    return order;
                case SORT_DEC:
                    double lhs_dec = lhs.getDec();
                    double rhs_dec = rhs.getDec();
                    if (lhs_dec < rhs_dec)
                        return -order;
                    if (lhs_dec == rhs_dec)
                        return new ComparatorImpl(SORT_NUMBER, 1).compare(lhs, rhs);
                    return order;
                case SORT_TYPE:
                    int lhs_type = lhs.getType();
                    int rhs_type = rhs.getType();
                    if (lhs_type < rhs_type)
                        return -order;
                    if (lhs_type == rhs_type)
                        return new ComparatorImpl(SORT_NUMBER, 1).compare(lhs, rhs);
                    return order;
                case SORT_ALT:
                    double lhs_alt = AstroTools.Altitude(lst, lat, lhs.getRa(), lhs.getDec());
                    double rhs_alt = AstroTools.Altitude(lst, lat, rhs.getRa(), rhs.getDec());
                    ;
                    if (lhs_alt < rhs_alt)
                        return -order;
                    if (lhs_alt == rhs_alt)
                        return new ComparatorImpl(SORT_NUMBER, 1).compare(lhs, rhs);
                    return order;
                case SORT_NUMBER:
                    return lhs.getLongName().compareTo(rhs.getLongName()) * order;

                case SORT_CONSTELLATION:
                    if (lhs.getCon() < rhs.getCon()) return -1 * order;
                    if (lhs.getCon() == rhs.getCon())
                        return new ComparatorImpl(SORT_NUMBER, order).compare(lhs, rhs);
                    return 1 * order;
                case SORT_MAGNITUDE:
                    boolean lnan = Double.isNaN(lhs.getMag());
                    boolean rnan = Double.isNaN(rhs.getMag());
                    if (lnan && !rnan)
                        return -order;
                    if (lnan && rnan)
                        return new ComparatorImpl(SORT_NUMBER, 1).compare(lhs, rhs);
                    if (!lnan && rnan) {
                        return order;
                    }

                    if (lhs.getMag() < rhs.getMag()) return -1 * order;
                    if (lhs.getMag() == rhs.getMag())
                        return new ComparatorImpl(SORT_NUMBER, 1).compare(lhs, rhs);
                    return 1 * order;
                case SORT_DIMENSION:
                    double lhsdim = Math.max(lhs.getA(), lhs.getB());
                    double rhsdim = Math.max(rhs.getA(), rhs.getB());

                    lnan = Double.isNaN(lhsdim);
                    rnan = Double.isNaN(rhsdim);
                    if (lnan && !rnan)
                        return -order;
                    if (lnan && rnan)
                        return new ComparatorImpl(SORT_NUMBER, 1).compare(lhs, rhs);
                    if (!lnan && rnan) {
                        return order;
                    }


                    if (lhsdim < rhsdim) return -1 * order;
                    if (lhsdim == rhsdim)
                        return new ComparatorImpl(SORT_NUMBER, 1).compare(lhs, rhs);
                    return 1 * order;

                case SORT_SETTIME:
                    TransitRec trL = map.get(lhs);
                    TransitRec trR = map.get(rhs);
                    double altL = 0;
                    double altR = 0;

                    if (trL.tSetting == null) {
                        altL = AstroTools.Altitude(AstroTools.sdTime(trL.tTransit), SettingsActivity.getLattitude(), lhs.ra, lhs.dec);
                    }
                    if (trR.tSetting == null) {
                        altR = AstroTools.Altitude(AstroTools.sdTime(trR.tTransit), SettingsActivity.getLattitude(), rhs.ra, rhs.dec);
                    }

                    if (trL.tSetting == null) {
                        if (trR.tSetting == null) {
                            if ((altL > 0 && altR > 0) || (altL < 0 && altR < 0))
                                return new ComparatorImpl(SORT_NUMBER, order).compare(lhs, rhs);
                            if (altL > 0 && altR < 0) return 1 * order;
                            if (altL < 0 && altR > 0) return -1 * order;
                            return new ComparatorImpl(SORT_NUMBER, order).compare(lhs, rhs);
                        } else {
                            if (altL > 0) return 1 * order;
                            return -1 * order;
                        }
                    }
                    if (trR.tSetting == null) {
                        if (trL.tSetting == null) {
                            if ((altL > 0 && altR > 0) || (altL < 0 && altR < 0))
                                return new ComparatorImpl(SORT_NUMBER, order).compare(lhs, rhs);
                            if (altL > 0 && altR < 0) return 1 * order;
                            if (altL < 0 && altR > 0) return -1 * order;
                            return new ComparatorImpl(SORT_NUMBER, order).compare(lhs, rhs);
                        } else {
                            if (altR > 0) return -1 * order;
                            return 1 * order;
                        }
                    }

                    long lset = trL.tSetting.getTimeInMillis();
                    long rset = trR.tSetting.getTimeInMillis();
                    if (lset < rset) return -1 * order;
                    if (lset == rset)
                        return new ComparatorImpl(SORT_NUMBER, order).compare(lhs, rhs);
                    return 1 * order;
            }
            return -1;
        }

    }
}

