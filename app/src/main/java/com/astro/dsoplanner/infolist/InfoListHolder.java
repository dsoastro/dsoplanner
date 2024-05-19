package com.astro.dsoplanner.infolist;

import com.astro.dsoplanner.infolist.InfoList;

import java.util.Set;

public interface InfoListHolder {  //holds info lists
    void addNewList(int id, InfoList list);//to have non-default implementations

    int getCount();

    InfoList get(int id);

    void remove(int id);

    Set<Integer> getIdSet();

    void removeAll();//remove all lists
}
