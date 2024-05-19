package com.astro.dsoplanner.infolist;

import android.util.Log;

import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.base.Exportable;


import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author leonid
 * fill method impl - does not check for equals
 * load method impl - checks for equals
 */
public class InfoListImpl implements InfoList {

    private static final String CLASS_CAST_ERROR = "class cast error";


    private static final String TAG = InfoListImpl.class.getSimpleName();
    protected List list = new ArrayList();

    protected String name;
    protected Class objClass;
    private boolean extractFromOLobjects = false;//if true - allow object extraction from ObservationList objects


    public InfoListImpl(String name, Class objClass) {
        this.name = name;
        this.objClass = objClass;
    }

    public int getCount() {
        return list.size();
    }

    /**
     * allow object extraction from Observation List Object
     */
    public void allowObjExtraction() {
        extractFromOLobjects = true;
    }

    @Override
    public String toString() {
        String s = "";
        for (Object o : list) {
            s = s + o + "\n";
        }
        return s;
    }
    //Populate the list from scratch

    /**
     * This method implementation could add equal objects
     */
    public synchronized void fill(InfoListFiller filler) {
        Iterator it = filler.getIterator();
        while (it.hasNext()) {

            if (objClass != null) {
                try {
                    Object o = castObject(it.next());
                    list.add(o);
                } catch (ClassCastException e) {
                }
            } else
                list.add(it.next());
        }
    }

    public synchronized Object get(int position) {
        if (position >= list.size())
            return null;
        return list.get(position);
    }

    @SuppressWarnings("unchecked")
    public synchronized void set(int position, Object object) {
        list.set(position, object);
    }

    public synchronized void remove(int position) {
        list.remove(position);
    }

    public synchronized void removeAll() {
        list = new ArrayList();
    }

    public synchronized Iterator iterator() {
        return list.iterator();
    }

    public synchronized String getListName() {
        return name;
    }

    public synchronized void setListName(String name) {
        this.name = name;
    }

    public synchronized boolean save(InfoListSaver listSaver) {
        try {
            listSaver.open();
            listSaver.addName(name);
            for (Object i : list) {
                if (i instanceof Exportable)
                    listSaver.addObject((Exportable) i);
            }


            listSaver.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void sort(int sortType, int order) {

    }

    public void sort(Comparator cmp) {
        try {//ClassCastException expected
            Collections.sort(list, cmp);
        } catch (Exception e) {
        }
    }
    //Load more objects to the list

    /**
     * this method implementation checks for equal objects and does not allow to add equal objects
     * it implicitly assumes that all objects coming from the loader are different and compares coming objects with the
     * objects in the existing list only. This may need to be fixed!!!
     */
    public synchronized ErrorHandler load(InfoListLoader listLoader) {
        List tmpList = new ArrayList();
        int line = 1;
        try {
            name = listLoader.getName();
        } catch (IOException e) {

            return new ErrorHandler(ErrorHandler.IO_ERROR, "");
        }
        Object obj = null;
        ErrorHandler eh = new ErrorHandler();
        while (true) {
            try {
                ErrorHandler.ErrorRec erec = new ErrorHandler.ErrorRec();
                erec.lineNum = line;
                obj = listLoader.next(erec);
                if (obj == null) {
                    eh.addError(erec);//error from listLoader processing
                    continue;
                }
                if (objClass != null) {
                    try {
                        Object o = castObject(obj);
                        if (o != null) {
                            tmpList.add(o);
                        }
                    } catch (ClassCastException e) {
                        Log.d(TAG, "Incompatible import format. " + e);
                        eh.addError(ErrorHandler.WRONG_TYPE, CLASS_CAST_ERROR, "", line);
                    }
                } else
                    tmpList.add(obj);
            } catch (IOException e) {
                Log.d(TAG, "IOException=" + e);
                if (!(e instanceof EOFException)) {
                    eh.addError(ErrorHandler.IO_ERROR, "", "", line);
                }
                try {
                    listLoader.close();
                } catch (IOException e1) {
                }
                tmpList.removeAll(list);
                list.addAll(tmpList);
                return eh;
            }
            line++;
        }
    }

    /**
     * @param object - object to be tried to be casted to objClass
     * @throws ClassCastException if Object could not be cast
     *                            takes extractFromOLobjects into account
     */
    protected Object castObject(Object obj) throws ClassCastException {
        Object object = obj;
        if (extractFromOLobjects) {
            if (obj instanceof ObsInfoListImpl.Item) {
                object = ((ObsInfoListImpl.Item) obj).x;
            }
        }
        Object o = objClass.cast(object);
        return o;

    }

}
