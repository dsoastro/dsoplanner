package com.astro.dsoplanner.infolist;

import com.astro.dsoplanner.ErrorHandler;

import java.util.Comparator;
import java.util.Iterator;

public interface InfoList extends Iterable { //one needs to check for correct algorithm in class cast as there is no limitations here
    final int NGCIC_SELECTION_LIST = 1;//id of list holding objects (dso selection list, observation list etc)
    final int PrimaryObsList = 20;
    final int DB_LIST = 2;
    final int SREQUEST_LIST = 3;
    final int TELESCOPE_LIST = 4;
    final int LOCATION_LIST = 7;
    final int NEBULA_CONTOUR_LIST = 8;

    final int PREFERENCE_LIST = 10;
    final int NGC_PIC_LIST = 11;

    Iterator iterator();

    int getCount();

    void fill(InfoListFiller filler);//adds objects from the filler

    Object get(int position);

    void remove(int position);

    void removeAll();

    String getListName();

    void setListName(String name);

    boolean save(InfoListSaver listSaver);//true if all OK, false otherwise

    ErrorHandler load(InfoListLoader listLoader);

    void sort(int sortType, int order);

    void sort(Comparator com);

}
